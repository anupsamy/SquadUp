import WebSocket from 'ws';
import { Server, IncomingMessage } from 'http';
import logger from '../utils/logger.util';

export interface WebSocketMessage {
  type: 'group_join' | 'group_leave' | 'group_update' | 'error';
  groupId: string;
  joinCode: string;
  userId: string;
  userName: string;
  message: string;
  timestamp: string;
  data?: any;
}

export class WebSocketService {
  private wss: WebSocket.Server;
  private clients: Map<string, WebSocket> = new Map(); // userId -> WebSocket
  private groupSubscriptions: Map<string, Set<string>> = new Map(); // joinCode -> Set<userId>

  constructor(server: Server) {
    console.log('🔧 Creating WebSocket server...');
    try {
      this.wss = new WebSocket.Server({ 
        server,
        path: '/ws'
      });

      this.setupWebSocketServer();
      console.log('✅ WebSocket server initialized successfully');
      logger.info('WebSocket server initialized');
    } catch (error: unknown) {
      console.error('❌ Error creating WebSocket server:', error);
      throw error;
    }
  }

  private setupWebSocketServer() {
    this.wss.on('connection', (ws: WebSocket, req: IncomingMessage) => {
      logger.info('New WebSocket connection established');

      ws.on('message', (data: WebSocket.Data) => {
        try {
          const message = JSON.parse(data.toString());
          this.handleMessage(ws, message);
        } catch (error: unknown) {
          logger.error('Error parsing WebSocket message:', error);
          this.sendError(ws, 'Invalid message format');
        }
      });

      ws.on('close', () => {
        this.handleDisconnection(ws);
      });

      ws.on('error', (error: Error) => {
        logger.error('WebSocket error:', error);
        this.handleDisconnection(ws);
      });

      // Send welcome message
      this.sendMessage(ws, {
        type: 'group_update',
        groupId: '',
        joinCode: '',
        userId: '',
        userName: '',
        message: 'Connected to SquadUp WebSocket server',
        timestamp: new Date().toISOString()
      });
    });
  }

  private handleMessage(ws: WebSocket, message: any) {
    const { type, userId, joinCode } = message;

    switch (type) {
      case 'subscribe':
        if (userId && joinCode) {
          this.subscribeToGroup(ws, userId, joinCode);
        } else {
          this.sendError(ws, 'Missing userId or joinCode for subscription');
        }
        break;
      
      case 'unsubscribe':
        if (userId && joinCode) {
          this.unsubscribeFromGroup(ws, userId, joinCode);
        } else {
          this.sendError(ws, 'Missing userId or joinCode for unsubscription');
        }
        break;
      
      case 'ping':
        this.sendMessage(ws, { type: 'pong', timestamp: new Date().toISOString() });
        break;
      
      default:
        this.sendError(ws, `Unknown message type: ${type}`);
    }
  }

  private subscribeToGroup(ws: WebSocket, userId: string, joinCode: string) {
    // Store client connection
    this.clients.set(userId, ws);
    
    // Add to group subscription
    if (!this.groupSubscriptions.has(joinCode)) {
      this.groupSubscriptions.set(joinCode, new Set());
    }
    this.groupSubscriptions.get(joinCode)!.add(userId);

    logger.info(`User ${userId} subscribed to group ${joinCode}`);
    
    this.sendMessage(ws, {
      type: 'group_update',
      groupId: '',
      joinCode,
      userId,
      userName: '',
      message: `Subscribed to group ${joinCode}`,
      timestamp: new Date().toISOString()
    });
  }

  private unsubscribeFromGroup(ws: WebSocket, userId: string, joinCode: string) {
    // Remove from group subscription
    const subscribers = this.groupSubscriptions.get(joinCode);
    if (subscribers) {
      subscribers.delete(userId);
      if (subscribers.size === 0) {
        this.groupSubscriptions.delete(joinCode);
      }
    }

    logger.info(`User ${userId} unsubscribed from group ${joinCode}`);
  }

  private handleDisconnection(ws: WebSocket) {
    // Find and remove client from all subscriptions
    for (const [userId, clientWs] of this.clients.entries()) {
      if (clientWs === ws) {
        this.clients.delete(userId);
        
        // Remove from all group subscriptions
        for (const [joinCode, subscribers] of this.groupSubscriptions.entries()) {
          subscribers.delete(userId);
          if (subscribers.size === 0) {
            this.groupSubscriptions.delete(joinCode);
          }
        }
        
        logger.info(`User ${userId} disconnected`);
        break;
      }
    }
  }

  // Public methods for sending notifications
  public notifyGroupJoin(joinCode: string, userId: string, userName: string, groupName: string) {
    const message: WebSocketMessage = {
      type: 'group_join',
      groupId: '',
      joinCode,
      userId,
      userName,
      message: `${userName} joined the group "${groupName}"`,
      timestamp: new Date().toISOString()
    };

    this.broadcastToGroup(joinCode, message);
    logger.info(`Notified group ${joinCode} about user ${userName} joining`);
  }

  public notifyGroupLeave(joinCode: string, userId: string, userName: string, groupName: string) {
    const message: WebSocketMessage = {
      type: 'group_leave',
      groupId: '',
      joinCode,
      userId,
      userName,
      message: `${userName} left the group "${groupName}"`,
      timestamp: new Date().toISOString()
    };

    this.broadcastToGroup(joinCode, message);
    logger.info(`Notified group ${joinCode} about user ${userName} leaving`);
  }

  public notifyGroupUpdate(joinCode: string, message: string, data?: any) {
    const wsMessage: WebSocketMessage = {
      type: 'group_update',
      groupId: '',
      joinCode,
      userId: '',
      userName: '',
      message,
      timestamp: new Date().toISOString(),
      data
    };

    this.broadcastToGroup(joinCode, wsMessage);
    logger.info(`Notified group ${joinCode} about update: ${message}`);
  }

  private broadcastToGroup(joinCode: string, message: WebSocketMessage) {
    const subscribers = this.groupSubscriptions.get(joinCode);
    if (!subscribers) {
      logger.warn(`No subscribers found for group ${joinCode}`);
      return;
    }

    const messageStr = JSON.stringify(message);
    let successCount = 0;
    let failureCount = 0;

    subscribers.forEach(userId => {
      const client = this.clients.get(userId);
      if (client && client.readyState === WebSocket.OPEN) {
        try {
          client.send(messageStr);
          successCount++;
        } catch (error) {
          logger.error(`Failed to send message to user ${userId}:`, error);
          failureCount++;
          // Remove failed client
          this.clients.delete(userId);
          subscribers.delete(userId);
        }
      } else {
        failureCount++;
        // Clean up disconnected clients
        this.clients.delete(userId);
        subscribers.delete(userId);
      }
    });

    logger.info(`Broadcast to group ${joinCode}: ${successCount} success, ${failureCount} failures`);
  }

  private sendMessage(ws: WebSocket, message: any) {
    if (ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(message));
    }
  }

  private sendError(ws: WebSocket, errorMessage: string) {
    this.sendMessage(ws, {
      type: 'error',
      groupId: '',
      joinCode: '',
      userId: '',
      userName: '',
      message: errorMessage,
      timestamp: new Date().toISOString()
    });
  }

  public getStats() {
    return {
      totalClients: this.clients.size,
      totalGroups: this.groupSubscriptions.size,
      groupSubscriptions: Object.fromEntries(
        Array.from(this.groupSubscriptions.entries()).map(([joinCode, subscribers]) => [
          joinCode,
          subscribers.size
        ])
      )
    };
  }
}

// Singleton instance
let wsService: WebSocketService | null = null;

export const initializeWebSocketService = (server: Server): WebSocketService => {
  if (!wsService) {
    wsService = new WebSocketService(server);
  }
  return wsService;
};

export const getWebSocketService = (): WebSocketService | null => {
  return wsService;
};

# WebSocket Integration for Group Notifications

This document explains how to use the WebSocket functionality for real-time group notifications in SquadUp.

## Overview

The backend now includes a WebSocket server that provides real-time notifications when users join or leave groups. The WebSocket server runs on the same port as the HTTP server with the path `/ws`.

## WebSocket Server Features

- **Real-time notifications** for group join/leave events
- **Group subscription system** - clients can subscribe to specific groups
- **Automatic reconnection handling** in the frontend
- **Message broadcasting** to all subscribers of a group
- **Connection management** with automatic cleanup

## Backend Integration

### WebSocket Service

The `WebSocketService` class handles all WebSocket functionality:

```typescript
// Located in: backend/src/services/websocket.service.ts
export class WebSocketService {
  // Notify when a user joins a group
  notifyGroupJoin(joinCode: string, userId: string, userName: string, groupName: string)
  
  // Notify when a user leaves a group  
  notifyGroupLeave(joinCode: string, userId: string, userName: string, groupName: string)
  
  // Send general group updates
  notifyGroupUpdate(joinCode: string, message: string, data?: any)
}
```

### Automatic Notifications

The following API endpoints now automatically send WebSocket notifications:

1. **POST /api/groups/update** - When users join groups
2. **POST /api/groups/leave/:joinCode** - When users leave groups

### Test Endpoint

A test endpoint is available for testing WebSocket functionality:

```
POST /api/groups/test-notification/:joinCode
```

Example:
```bash
curl -X POST http://localhost:3000/api/groups/test-notification/test123 \
  -H "Content-Type: application/json" \
  -d '{"message": "Test notification", "type": "test"}'
```

## Frontend Integration

### WebSocketManager Usage

The existing `WebSocketManager` class can be used to connect to the backend WebSocket server:

```kotlin
// Connect to the WebSocket server
val wsManager = WebSocketManager("ws://your-backend-url:3000/ws")

// Set up message handling
wsManager.setListener(object : WebSocketManager.WebSocketListenerCallback {
    override fun onMessageReceived(message: String) {
        try {
            val notification = JSONObject(message)
            when (notification.getString("type")) {
                "group_join" -> {
                    val userName = notification.getString("userName")
                    val groupName = notification.getString("message")
                    // Show notification: "$userName joined $groupName"
                }
                "group_leave" -> {
                    val userName = notification.getString("userName")
                    val groupName = notification.getString("message")
                    // Show notification: "$userName left $groupName"
                }
                "group_update" -> {
                    val message = notification.getString("message")
                    // Show general group update
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocket", "Error parsing message: $e")
        }
    }
    
    override fun onConnectionStateChanged(isConnected: Boolean) {
        // Handle connection state changes
    }
})

// Start the connection
wsManager.start()
```

### Subscribing to Group Notifications

To receive notifications for a specific group, send a subscription message:

```kotlin
// Subscribe to group notifications
val subscribeMessage = JSONObject().apply {
    put("type", "subscribe")
    put("userId", currentUserId)
    put("joinCode", groupJoinCode)
}
wsManager.sendMessage(subscribeMessage.toString())
```

### Message Types

The WebSocket server sends the following message types:

#### 1. Group Join Notification
```json
{
  "type": "group_join",
  "groupId": "",
  "joinCode": "abc123",
  "userId": "user123",
  "userName": "John Doe",
  "message": "John Doe joined the group \"Study Group\"",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

#### 2. Group Leave Notification
```json
{
  "type": "group_leave",
  "groupId": "",
  "joinCode": "abc123", 
  "userId": "user123",
  "userName": "John Doe",
  "message": "John Doe left the group \"Study Group\"",
  "timestamp": "2024-01-15T10:30:00.000Z"
}
```

#### 3. Group Update Notification
```json
{
  "type": "group_update",
  "groupId": "",
  "joinCode": "abc123",
  "userId": "",
  "userName": "",
  "message": "Group information updated",
  "timestamp": "2024-01-15T10:30:00.000Z",
  "data": {
    "newLeader": {
      "id": "user456",
      "name": "Jane Smith",
      "email": "jane@example.com"
    }
  }
}
```

## Testing

### 1. Start the Backend Server
```bash
cd backend
npm run dev
```

### 2. Test WebSocket Connection
```bash
cd backend
node websocket-client-test.js
```

### 3. Test Notifications
Use the test endpoint or trigger actual group join/leave events through the API.

## Production Deployment

### AWS Considerations

For AWS deployment, ensure:

1. **Load Balancer Configuration**: WebSocket connections need sticky sessions or use AWS Application Load Balancer with WebSocket support
2. **Security Groups**: Allow WebSocket traffic on your server port
3. **Health Checks**: WebSocket connections should be handled in health check endpoints
4. **Scaling**: Consider using AWS API Gateway WebSocket API for better scaling

### Environment Variables

No additional environment variables are required. The WebSocket server uses the same port as the HTTP server.

## Troubleshooting

### Common Issues

1. **Connection Refused**: Ensure the backend server is running and accessible
2. **No Notifications**: Check that clients are subscribed to the correct group
3. **Message Parsing Errors**: Verify JSON format of WebSocket messages

### Debugging

Enable debug logging by checking the server console for WebSocket connection logs and message broadcasts.

## Security Considerations

- WebSocket connections are not authenticated by default
- Consider adding authentication tokens to WebSocket connections
- Validate all incoming WebSocket messages
- Implement rate limiting for WebSocket connections

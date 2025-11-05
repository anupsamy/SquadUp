import dotenv from 'dotenv';
import express from 'express';
import { createServer } from 'http';
import path from 'path';

import { connectDB } from './database';
import { errorHandler, notFoundHandler } from './middleware/errorHandler.middleware';
import router from './routes';
import { initializeWebSocketService } from './services/websocket.service';
import logger from './utils/logger.util';
dotenv.config();

const app = express();
const server = createServer(app);
const PORT = process.env.PORT ?? 3000;

app.use(express.json());

app.use('/api', router);
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));
app.use('*', notFoundHandler);
app.use(errorHandler);

try {
  initializeWebSocketService(server);
  logger.info('✅ WebSocket service initialization attempted');
} catch (error: unknown) {
  logger.error('❌ Failed to initialize WebSocket service:', error);
}

connectDB().catch((error: unknown) => {
  logger.error('Failed to connect to database:', error);
  process.exitCode = 1;
});
server.listen(PORT, () => {
  const portString = String(PORT);
  logger.info(`🔌 WebSocket server available at ws://localhost:${portString}/ws`);
});

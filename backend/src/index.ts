import dotenv from 'dotenv';
dotenv.config();
import express from 'express';
import { createServer } from 'http';

import { connectDB } from './database';
import { errorHandler, notFoundHandler } from './middleware/errorHandler.middleware';
import router from './routes';
import path from 'path';
//import { initializeWebSocketService } from './services/websocket.service';

const app = express();
const server = createServer(app);
const PORT = process.env.PORT ?? 3000;

app.use(express.json());

app.use('/api', router);
app.use('/uploads', express.static(path.join(__dirname, '../uploads')));
app.use('*', notFoundHandler);
app.use(errorHandler);

// Initialize WebSocket service
/*try {
  initializeWebSocketService(server);
  console.log('✅ WebSocket service initialization attempted');
} catch (error: unknown) {
  console.error('❌ Failed to initialize WebSocket service:', error);
}*/

connectDB();
server.listen(PORT, () => {
  console.log(`🚀 Server running on port ${PORT}`);
  console.log(`🔌 WebSocket server available at ws://localhost:${PORT}/ws`);
});

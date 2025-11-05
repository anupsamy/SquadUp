import mongoose from 'mongoose';
import dotenv from 'dotenv';

dotenv.config();

beforeAll(async () => {
  const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/squadup';
  
  try {
    await mongoose.connect(mongoUri);
    console.log('✓ MongoDB connected for tests');
  } catch (error) {
    console.error('✗ MongoDB connection failed:', error);
    throw error;
  }
});

afterAll(async () => {
  try {
    await mongoose.disconnect();
    console.log('✓ MongoDB disconnected');
  } catch (error) {
    console.error('✗ MongoDB disconnection failed:', error);
  }
});
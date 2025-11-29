import mongoose from 'mongoose';
import dotenv from 'dotenv';

dotenv.config();

// Create a unique database name for this test run
const DB_NAME = `test_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
const MONGO_URI = process.env.MONGO_URI || 'mongodb://localhost:27017';
const TEST_DB_URI = `${MONGO_URI}/${DB_NAME}`;

console.log('Mongo URI: ', TEST_DB_URI);

beforeAll(async () => {
  await mongoose.connect(TEST_DB_URI);
  console.log('✓ MongoDB connected for tests');
});

afterAll(async () => {
  // Close all connections and drop database
  try {
    if (mongoose.connection.readyState !== 0) {
      await mongoose.connection.dropDatabase();
      await mongoose.connection.close();
      console.log('✓ MongoDB disconnected and test database dropped');
    }
  } catch (error) {
    console.error('Error during cleanup:', error);
  }
});

// Add a global teardown to ensure everything closes
afterAll(async () => {
  // Force close all connections
  await new Promise(resolve => setTimeout(resolve, 500));
  await mongoose.disconnect();
});

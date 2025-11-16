"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const mongoose_1 = __importDefault(require("mongoose"));
const dotenv_1 = __importDefault(require("dotenv"));
dotenv_1.default.config();
beforeAll(async () => {
    const mongoUri = process.env.MONGODB_URI || 'mongodb://localhost:27017/squadup';
    try {
        await mongoose_1.default.connect(mongoUri);
        console.log('✓ MongoDB connected for tests');
    }
    catch (error) {
        console.error('✗ MongoDB connection failed:', error);
        throw error;
    }
});
afterAll(async () => {
    try {
        await mongoose_1.default.disconnect();
        console.log('✓ MongoDB disconnected');
    }
    catch (error) {
        console.error('✗ MongoDB disconnection failed:', error);
    }
});

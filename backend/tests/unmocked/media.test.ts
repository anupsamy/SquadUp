import request from 'supertest';
import express, { Express } from 'express';
import path from 'path';
import fs from 'fs';
import mongoose from 'mongoose';
import { MediaController } from '../../src/controllers/media.controller';
import { MediaService } from '../../src/services/media.service';
import { IMAGES_DIR } from '../../src/storage';

jest.mock('../../src/utils/logger.util');

describe('Unmocked: Media Controller - uploadImage', () => {
  let app: Express;
  let mediaController: MediaController;
  let testImagePath: string;

  beforeAll(() => {
    app = express();
    app.use(express.json());
    mediaController = new MediaController();

    // Middleware to attach user to requests
    app.use((req: any, res: any, next: any) => {
      if (!req.user) {
        req.user = {
          _id: new mongoose.Types.ObjectId(),
          email: 'test@example.com',
          name: 'Test User',
        };
      }
      next();
    });

    // Create a test image file
    if (!fs.existsSync(IMAGES_DIR)) {
      fs.mkdirSync(IMAGES_DIR, { recursive: true });
    }

    app.post('/media/upload', (req: any, res: any) => {
      // Simulate multer file upload
      const tempDir = path.join(process.cwd(), 'temp');
      if (!fs.existsSync(tempDir)) {
        fs.mkdirSync(tempDir, { recursive: true });
      }

      const tempFile = path.join(tempDir, `test-${Date.now()}.jpg`);
      fs.writeFileSync(tempFile, Buffer.from('fake image data'));

      req.file = {
        path: tempFile,
        filename: 'test.jpg',
        mimetype: 'image/jpeg',
      };

      mediaController.uploadImage(req, res, () => {});
    });
  });

  afterAll(async () => {
    // Cleanup test files
    if (fs.existsSync(IMAGES_DIR)) {
      const files = fs.readdirSync(IMAGES_DIR);
      files.forEach(file => {
        fs.unlinkSync(path.join(IMAGES_DIR, file));
      });
    }

    const tempDir = path.join(process.cwd(), 'temp');
    if (fs.existsSync(tempDir)) {
      const files = fs.readdirSync(tempDir);
      files.forEach(file => {
        fs.unlinkSync(path.join(tempDir, file));
      });
      fs.rmdirSync(tempDir);
    }

    await mongoose.connection.close();
  });

  describe('POST /media/upload', () => {
    it('should return 400 when no file is uploaded', async () => {
      app.post('/media/upload/no-file', (req: any, res: any) => {
        req.file = undefined;
        mediaController.uploadImage(req, res, () => {});
      });

      const res = await request(app).post('/media/upload/no-file');

      expect(res.status).toBe(400);
      expect(res.body).toHaveProperty('message', 'No file uploaded');
    });

    it('should upload image successfully', async () => {
      const res = await request(app).post('/media/upload');

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'Image uploaded successfully');
      expect(res.body.data).toHaveProperty('image');
      expect(res.body.data.image).toMatch(/\.(jpg|jpeg|png)$/);
    });

    it('should return 500 when saveImage throws an error', async () => {
      jest.spyOn(MediaService, 'saveImage').mockRejectedValueOnce(
        new Error('Disk write failed')
      );

      const res = await request(app).post('/media/upload');

      expect(res.status).toBe(500);
      expect(res.body).toHaveProperty('message', 'Disk write failed');
    });

    it('should handle non-Error exceptions', async () => {
      jest.spyOn(MediaService, 'saveImage').mockRejectedValueOnce('String error');

      const res = await request(app).post('/media/upload');

      expect(res.status).toBe(500);
      expect(res.body).toHaveProperty('message', 'String error');
    });
  });

// Input: request without user authentication
// Expected status code: 401
// Expected behavior: returns unauthorized error
// Expected output: "User not authenticated" message
it('should return 401 when user is not authenticated', async () => {
  const unauthApp = express();
  unauthApp.use(express.json());
  
  app.post('/media/upload/unauthenticated', (req: any, res: any) => {
    const tempDir = path.join(process.cwd(), 'temp');
    if (!fs.existsSync(tempDir)) {
      fs.mkdirSync(tempDir, { recursive: true });
    }

    const tempFile = path.join(tempDir, `test-${Date.now()}.jpg`);
    fs.writeFileSync(tempFile, Buffer.from('fake image data'));

    req.file = {
      path: tempFile,
      filename: 'test.jpg',
      mimetype: 'image/jpeg',
    };
    
    // Explicitly set user to undefined to simulate unauthenticated request
    req.user = undefined;
    
    mediaController.uploadImage(req, res, () => {});
  });

  const res = await request(app).post('/media/upload/unauthenticated');

  expect(res.status).toBe(401);
  expect(res.body).toHaveProperty('message', 'User not authenticated');
});
});
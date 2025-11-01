import request from 'supertest';
import express from 'express';
import { UserController } from '../controllers/user.controller';

// Mock dependencies (none for GET /profile right now)
jest.mock('../../src/utils/logger.util');

const app = express();
const userController = new UserController();

// Setup route under test
app.get('/profile', (req, res) => {
  // Simulate authentication middleware that attaches user
  req.user = { _id: '123', name: 'Test User', email: 'test@example.com' };
  userController.getProfile(req, res);
});

describe('GET /profile', () => {
  describe('existing behavior', () => {
    it('returns 200 and user data when req.user exists', async () => {
      const res = await request(app).get('/profile');

      expect(res.status).toBe(200);
      expect(res.body).toEqual({
        message: 'Profile fetched successfully',
        data: {
          user: {
            _id: '123',
            name: 'Test User',
            email: 'test@example.com',
          },
        },
      });
    });

    it('throws if req.user is missing (no handling implemented)', async () => {
      // Rebuild app route without setting req.user
      const noUserApp = express();
      noUserApp.get('/profile', (req, res) =>
        userController.getProfile(req, res)
      );

      await expect(request(noUserApp).get('/profile')).rejects.toThrow();
    });
  });

  describe('expected behavior (to implement)', () => {
    it('should return 401 if user is not authenticated', async () => {
      // Future expected spec: instead of throwing, respond gracefully
      const appFuture = express();
      appFuture.get('/profile', (req, res) =>
        userController.getProfile(req, res)
      );

      const res = await request(appFuture).get('/profile');

      expect(res.status).toBe(401);
      expect(res.body).toEqual({
        message: 'Unauthorized: user not authenticated',
      });
    });
  });
});

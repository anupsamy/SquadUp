import request from 'supertest';
import express, { Express, Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { UserController } from '../controllers/user.controller';
import { userModel } from '../user.model';
import { GoogleUserInfo } from '../types/user.types';
import { TRANSIT_TYPES } from '@/types/transit.types';

jest.mock('../utils/logger.util');
jest.mock('../services/media.service');

describe('Unmocked: User Endpoints (No Mocks)', () => {
  let app: Express;
  let userController: UserController;
  let testUserId: mongoose.Types.ObjectId;
  let testUserEmail: string;
  let testUser: GoogleUserInfo;

  beforeAll(async () => {
    app = express();
    app.use(express.json());
    userController = new UserController();

    // Middleware to attach test user to requests
    app.use((req: Request, res: Response, next: NextFunction) => {
      if (!req.user) {
        req.user = {
          _id: testUserId,
          googleId: testUser.googleId,
          email: testUserEmail,
          name: testUser.name,
          bio: '',
          createdAt: new Date(),
          updatedAt: new Date(),
        } as any;
      }
      next();
    });

    // Setup routes
    app.get('/profile', (req, res) => userController.getProfile(req, res));
    app.post('/profile', (req, res, next) => userController.updateProfile(req, res, next));
    app.delete('/profile', (req, res, next) => userController.deleteProfile(req, res, next));

    // Create a test user in the database
    testUserEmail = `test-${Date.now()}@example.com`;
    const googleId = `google-${Date.now()}`;
    testUser = {
      googleId,
      email: testUserEmail,
      name: 'Test User',
    };

    const createdUser = await userModel.create(testUser);
    testUserId = createdUser._id;
  });

  afterAll(async () => {
    // Cleanup: delete test user if it still exists
    try {
      await userModel.delete(testUserId);
    } catch (e) {
      // User may already be deleted
    }
  });

  describe('GET /profile', () => {
    // Input: valid authenticated user
    // Expected status code: 200
    // Expected behavior: user profile is returned
    // Expected output: user data with all fields
    it('should return 200 and user profile when authenticated', async () => {
      const res = await request(app).get('/profile');

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'Profile fetched successfully');
      expect(res.body.data).toHaveProperty('user');
      expect(res.body.data.user).toHaveProperty('_id');
      expect(res.body.data.user).toHaveProperty('email', testUserEmail);
      expect(res.body.data.user).toHaveProperty('name', 'Test User');
      expect(res.body.data.user).toHaveProperty('googleId');
    });
  });

  describe('POST /profile', () => {
    // Input: valid user update with name change
    // Expected status code: 200
    // Expected behavior: user is updated in database
    // Expected output: updated user data
    it('should update user name and return 200', async () => {
      const updateData = {
        name: 'Updated Name',
      };

      const res = await request(app)
        .post('/profile')
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'User info updated successfully');
      expect(res.body.data.user.name).toBe('Updated Name');

      // Verify update persisted in database
      const dbUser = await userModel.findById(testUserId);
      expect(dbUser).toBeDefined();
      if (dbUser) {
        expect(dbUser.name).toBe('Updated Name');
      }
    });

    // Input: partial user update with address
    // Expected status code: 200
    // Expected behavior: only specified fields are updated, others remain unchanged
    // Expected output: user data with updated address
    it('should update only specified fields (address)', async () => {
      const updateData = {
        address: {
          formatted: '123 Main St, Vancouver, BC',
          placeId: 'place123',
          lat: 49.2827,
          lng: -123.1207,
          components: {
            streetNumber: '123',
            route: 'Main St',
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
            postalCode: 'V6B 1A1',
          },
        },
      };

      const res = await request(app)
        .post('/profile')
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.data.user.address).toBeDefined();
      expect(res.body.data.user.address.formatted).toBe('123 Main St, Vancouver, BC');

      // Verify name hasn't changed
      expect(res.body.data.user.name).toBe('Updated Name');
    });

    // Input: partial user update with transitType
    // Expected status code: 200
    // Expected behavior: only specified fields are updated
    // Expected output: user data with updated transitType
    it('should update transitType', async () => {
      const updateData = {
        transitType: 'transit',
      };

      const res = await request(app)
        .post('/profile')
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.data.user.transitType).toBe('transit');
    });

    // Input: empty update object
    // Expected status code: 200
    // Expected behavior: user is not modified
    // Expected output: unchanged user data
    it('should handle empty update gracefully', async () => {
      const currentUser = await userModel.findById(testUserId);
      const updateData = {};

      const res = await request(app)
        .post('/profile')
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message');

      // Verify no changes
      const afterUpdate = await userModel.findById(testUserId);
      expect(afterUpdate?.name).toBe(currentUser?.name);
    });

    // Input: update with multiple fields
    // Expected status code: 200
    // Expected behavior: all specified fields are updated
    // Expected output: user data with all updated fields
    it('should update multiple fields simultaneously', async () => {
      const updateData = {
        name: 'Multi Field User',
        transitType: 'bicycling',
      };

      const res = await request(app)
        .post('/profile')
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.data.user.name).toBe('Multi Field User');
      expect(res.body.data.user.transitType).toBe('bicycling');
    });
  });

  describe('DELETE /profile', () => {
    // Input: valid authenticated user requesting deletion
    // Expected status code: 200
    // Expected behavior: user and all associated images are deleted
    // Expected output: success message
    it('should delete user profile and return 200', async () => {
      // Create a separate user for deletion
      const deleteTestEmail = `delete-test-${Date.now()}@example.com`;
      const deleteGoogleId = `google-delete-${Date.now()}`;

      const userToDelete = await userModel.create({
        googleId: deleteGoogleId,
        email: deleteTestEmail,
        name: 'User To Delete',
      });

      // Create a separate app instance with this user
      const deleteApp = express();
      deleteApp.use(express.json());
      deleteApp.use((req: Request, res: Response, next: NextFunction) => {
        req.user = {
          _id: userToDelete._id,
          googleId: deleteGoogleId,
          email: deleteTestEmail,
          name: 'User To Delete',
          bio: '',
          createdAt: userToDelete.createdAt,
          updatedAt: userToDelete.updatedAt,
        } as any;
        next();
      });
      deleteApp.delete('/profile', (req, res, next) =>
        userController.deleteProfile(req, res, next)
      );

      const res = await request(deleteApp).delete('/profile');

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'User deleted successfully');

      // Verify user is deleted from database
      const deletedUser = await userModel.findById(userToDelete._id);
      expect(deletedUser).toBeNull();
    });
  });
});
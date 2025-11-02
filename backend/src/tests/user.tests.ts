import request from 'supertest';
import express, { Express } from 'express';
import { ObjectId } from 'mongodb';
import { UserController } from '../controllers/user.controller';
import { userModel } from '../user.model';

jest.mock('../utils/logger.util');
jest.mock('../services/media.service');

describe('Unmocked: User Endpoints (No Mocks)', () => {
  let app: Express;
  let userController: UserController;
  let testUserId: ObjectId;
  let testUserEmail: string;

  beforeAll(async () => {
    app = express();
    app.use(express.json());
    userController = new UserController();

    // Setup routes
    app.get('/profile', (req, res) => userController.getProfile(req, res));
    app.patch('/profile', (req, res, next) => userController.updateProfile(req, res, next));
    app.delete('/profile', (req, res, next) => userController.deleteProfile(req, res, next));

    // Create a test user in the database
    testUserEmail = `test-${Date.now()}@example.com`;
    const newUser = await userModel.create({
      email: testUserEmail,
      name: 'Test User',
      transitType: 'transit',
      address: '123 Main St',
    });
    testUserId = newUser._id;
  });

  afterAll(async () => {
    // Cleanup: delete test user if it still exists
    try {
      await userModel.delete(testUserId);
    } catch (e) {
      // User may already be deleted
    }
  });

  // Helper middleware to attach user to request
  const attachUser = (req: Request, res: Response, next: Function) => {
    req.user = {
      _id: testUserId,
      email: testUserEmail,
      name: 'Test User',
    };
    next();
  };

  describe('GET /profile', () => {
    // Input: valid authenticated user
    // Expected status code: 200
    // Expected behavior: user profile is returned
    // Expected output: user data
    it('should return 200 and user profile when authenticated', async () => {
      const res = await request(app)
        .get('/profile')
        .use(attachUser);

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'Profile fetched successfully');
      expect(res.body.data).toHaveProperty('user');
      expect(res.body.data.user._id).toEqual(testUserId.toString());
      expect(res.body.data.user.email).toBe(testUserEmail);
    });
  });

  describe('PATCH /profile', () => {
    // Input: valid user update with name change
    // Expected status code: 200
    // Expected behavior: user is updated in database
    // Expected output: updated user data
    it('should update user profile and return 200', async () => {
      const updateData = {
        name: 'Updated Name',
        transitType: 'car',
        address: '456 Oak Ave',
      };

      const res = await request(app)
        .patch('/profile')
        .use(attachUser)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'User info updated successfully');
      expect(res.body.data.user.name).toBe('Updated Name');
      expect(res.body.data.user.transitType).toBe('car');
      expect(res.body.data.user.address).toBe('456 Oak Ave');

      // Verify update persisted in database
      const dbUser = await userModel.findById(testUserId);
      expect(dbUser.name).toBe('Updated Name');
    });

    // Input: partial user update with only name
    // Expected status code: 200
    // Expected behavior: only specified fields are updated
    // Expected output: user data with updated name
    it('should update only specified fields', async () => {
      const updateData = {
        name: 'Another Name',
      };

      const res = await request(app)
        .patch('/profile')
        .use(attachUser)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body.data.user.name).toBe('Another Name');
    });

    // Input: empty update object
    // Expected status code: 200
    // Expected behavior: user is not modified (or gracefully handled)
    // Expected output: unchanged user data
    it('should handle empty update gracefully', async () => {
      const updateData = {};

      const res = await request(app)
        .patch('/profile')
        .use(attachUser)
        .send(updateData);

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message');
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
      const userToDelete = await userModel.create({
        email: deleteTestEmail,
        name: 'User To Delete',
        transitType: 'bus',
        address: '789 Delete St',
      });

      const res = await request(app)
        .delete('/profile')
        .use((req: any, res: any, next: any) => {
          req.user = {
            _id: userToDelete._id,
            email: deleteTestEmail,
            name: 'User To Delete',
          };
          next();
        });

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'User deleted successfully');

      // Verify user is deleted from database
      const deletedUser = await userModel.findById(userToDelete._id);
      expect(deletedUser).toBeNull();
    });
  });
});
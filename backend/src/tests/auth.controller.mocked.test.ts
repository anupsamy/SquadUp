import request from 'supertest';
import express, { Express } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authService } from '../services/auth.service';

jest.mock('../utils/logger.util');
jest.mock('../services/auth.service');

describe('Mocked: Auth Endpoints', () => {
  let app: Express;
  let authController: AuthController;

  beforeAll(() => {
    app = express();
    app.use(express.json());
    authController = new AuthController();

    // Setup routes
    app.post('/signup', (req, res, next) =>
      authController.signUp(req, res, next)
    );
    app.post('/signin', (req, res, next) =>
      authController.signIn(req, res, next)
    );
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('POST /signup', () => {
    // Mocked behavior: authService.signUpWithGoogle returns valid auth result
    // Input: valid idToken
    // Expected status code: 201
    // Expected behavior: user is created and token is returned
    // Expected output: auth result with token and user
    it('should return 201 when signup succeeds', async () => {
      const mockUser = {
        _id: 'user123',
        googleId: 'google123',
        email: 'newuser@example.com',
        name: 'New User',
        bio: '',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      jest.spyOn(authService, 'signUpWithGoogle').mockResolvedValueOnce({
        token: 'jwt-token-123',
        user: mockUser as any,
      });

      const res = await request(app)
        .post('/signup')
        .send({ idToken: 'valid-google-token' });

      expect(res.status).toBe(201);
      expect(res.body).toHaveProperty('message', 'User signed up successfully');
      expect(res.body.data).toHaveProperty('token', 'jwt-token-123');
      expect(res.body.data.user.email).toBe('newuser@example.com');
    });

    // Mocked behavior: authService.signUpWithGoogle throws "Invalid Google token"
    // Input: invalid idToken
    // Expected status code: 401
    // Expected behavior: returns unauthorized error
    // Expected output: error message about invalid token
    it('should return 401 when Google token is invalid', async () => {
      jest
        .spyOn(authService, 'signUpWithGoogle')
        .mockRejectedValueOnce(new Error('Invalid Google token'));

      const res = await request(app)
        .post('/signup')
        .send({ idToken: 'invalid-token' });

      expect(res.status).toBe(401);
      expect(res.body).toHaveProperty('message', 'Invalid Google token');
    });

    // Mocked behavior: authService.signUpWithGoogle throws "User already exists"
    // Input: idToken for user that already exists
    // Expected status code: 409
    // Expected behavior: returns conflict error
    // Expected output: error message about existing user
    it('should return 409 when user already exists', async () => {
      jest
        .spyOn(authService, 'signUpWithGoogle')
        .mockRejectedValueOnce(new Error('User already exists'));

      const res = await request(app)
        .post('/signup')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(409);
      expect(res.body).toHaveProperty(
        'message',
        'User already exists, please sign in instead.'
      );
    });

    // Mocked behavior: authService.signUpWithGoogle throws "Failed to process user"
    // Input: valid idToken but user creation fails
    // Expected status code: 500
    // Expected behavior: returns server error
    // Expected output: error message about processing failure
    it('should return 500 when user processing fails', async () => {
      jest
        .spyOn(authService, 'signUpWithGoogle')
        .mockRejectedValueOnce(new Error('Failed to process user'));

      const res = await request(app)
        .post('/signup')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(500);
      expect(res.body).toHaveProperty(
        'message',
        'Failed to process user information'
      );
    });
    // Mocked behavior: authService.signUpWithGoogle throws non-Error object
    // Input: idToken that causes non-Error exception
    // Expected behavior: error is passed to next()
    // Expected output: next() is called with error
    // For signIn - test calling controller directly
    it('should call next() when error is not an Error instance in signin', async () => {
      const mockNext = jest.fn();

      jest
        .spyOn(authService, 'signInWithGoogle')
        .mockRejectedValueOnce('Unknown error');

      const req = { body: { idToken: 'token' } } as any;
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      } as any;

      await authController.signIn(req, res, mockNext);

      expect(mockNext).toHaveBeenCalledWith('Unknown error');
    });
  });

  describe('POST /signin', () => {
    // Mocked behavior: authService.signInWithGoogle returns valid auth result
    // Input: valid idToken for existing user
    // Expected status code: 200
    // Expected behavior: user is authenticated and token is returned
    // Expected output: auth result with token and user
    it('should return 200 when signin succeeds', async () => {
      const mockUser = {
        _id: 'user123',
        googleId: 'google123',
        email: 'user@example.com',
        name: 'Existing User',
        bio: '',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      jest.spyOn(authService, 'signInWithGoogle').mockResolvedValueOnce({
        token: 'jwt-token-456',
        user: mockUser as any,
      });

      const res = await request(app)
        .post('/signin')
        .send({ idToken: 'valid-google-token' });

      expect(res.status).toBe(200);
      expect(res.body).toHaveProperty('message', 'User signed in successfully');
      expect(res.body.data).toHaveProperty('token', 'jwt-token-456');
      expect(res.body.data.user.email).toBe('user@example.com');
    });

    // Mocked behavior: authService.signInWithGoogle throws "Invalid Google token"
    // Input: invalid idToken
    // Expected status code: 401
    // Expected behavior: returns unauthorized error
    // Expected output: error message about invalid token
    it('should return 401 when Google token is invalid', async () => {
      jest
        .spyOn(authService, 'signInWithGoogle')
        .mockRejectedValueOnce(new Error('Invalid Google token'));

      const res = await request(app)
        .post('/signin')
        .send({ idToken: 'invalid-token' });

      expect(res.status).toBe(401);
      expect(res.body).toHaveProperty('message', 'Invalid Google token');
    });

    // Mocked behavior: authService.signInWithGoogle throws "User not found"
    // Input: valid idToken but for non-existent user
    // Expected status code: 404
    // Expected behavior: returns not found error
    // Expected output: error message about user not found
    it('should return 404 when user does not exist', async () => {
      jest
        .spyOn(authService, 'signInWithGoogle')
        .mockRejectedValueOnce(new Error('User not found'));

      const res = await request(app)
        .post('/signin')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(404);
      expect(res.body).toHaveProperty(
        'message',
        'User not found, please sign up first.'
      );
    });

    // Mocked behavior: authService.signInWithGoogle throws "Failed to process user"
    // Input: valid idToken but processing fails
    // Expected status code: 500
    // Expected behavior: returns server error
    // Expected output: error message about processing failure
    it('should return 500 when user processing fails', async () => {
      jest
        .spyOn(authService, 'signInWithGoogle')
        .mockRejectedValueOnce(new Error('Failed to process user'));

      const res = await request(app)
        .post('/signin')
        .send({ idToken: 'valid-token' });

      expect(res.status).toBe(500);
      expect(res.body).toHaveProperty(
        'message',
        'Failed to process user information'
      );
    });
    // Mocked behavior: authService.signInWithGoogle throws non-Error object
    // Input: idToken that causes non-Error exception
    // Expected behavior: error is passed to next()
    // Expected output: next() is called with error
    it('should call next() when error is not an Error instance in signup', async () => {
      const mockNext = jest.fn();

      jest
        .spyOn(authService, 'signUpWithGoogle')
        .mockRejectedValueOnce('Unknown error');

      const req = { body: { idToken: 'token' } } as any;
      const res = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      } as any;

      await authController.signUp(req, res, mockNext);

      expect(mockNext).toHaveBeenCalledWith('Unknown error');
    });

    // it('should fail to test pipeline', async () => {
    //   expect(0).toEqual(1);
    // });
  });
});

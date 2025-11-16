import { AuthService } from '../services/auth.service';
import { userModel } from '../user.model';
import * as jwt from 'jsonwebtoken';
import * as googleAuthLibrary from 'google-auth-library';
import mongoose from 'mongoose';

jest.mock('../utils/logger.util');
jest.mock('jsonwebtoken');
jest.mock('google-auth-library');

describe('Mocked: AuthService', () => {
  let authService: AuthService;

  beforeEach(() => {
    jest.clearAllMocks();
    authService = new AuthService();
  });

  describe('signUpWithGoogle', () => {
    it('should create user and return token on successful signup', async () => {
    const mockPayload = {
      sub: 'google-123',
      email: 'newuser@example.com',
      name: 'New User',
      picture: 'https://example.com/pic.jpg',
    };

    const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
    
    // Setup mock BEFORE creating AuthService instance
    (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
      verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
    }));

    // Create new instance after mock is set
    authService = new AuthService();

    const mockUser = {
      _id: new mongoose.Types.ObjectId(),
      googleId: 'google-123',
      email: 'newuser@example.com',
      name: 'New User',
      bio: '',
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(null);
    jest.spyOn(userModel, 'create').mockResolvedValueOnce(mockUser as any);
    jest.spyOn(jwt, 'sign').mockReturnValueOnce('jwt-token-123' as any);

    const result = await authService.signUpWithGoogle('valid-token');

    expect(result.token).toBe('jwt-token-123');
    expect(result.user.email).toBe('newuser@example.com');
  });

    it('should throw "Invalid Google token" when verification fails', async () => {
      (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
        verifyIdToken: jest.fn().mockRejectedValue(new Error('Invalid token')),
      }));
      authService = new AuthService();

      await expect(authService.signUpWithGoogle('invalid-token')).rejects.toThrow('Invalid Google token');
    });

    it('should throw "User already exists" when user already signed up', async () => {
      const mockPayload = {
        sub: 'google-123',
        email: 'existing@example.com',
        name: 'Existing User',
      };

      const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
      (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
        verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
      }));
      authService = new AuthService();

      const mockExistingUser = {
        _id: new mongoose.Types.ObjectId(),
        googleId: 'google-123',
        email: 'existing@example.com',
        name: 'Existing User',
        bio: '',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(mockExistingUser as any);

      await expect(authService.signUpWithGoogle('token')).rejects.toThrow('User already exists');
      expect(userModel.create).not.toHaveBeenCalled();
    });

    it('should throw error when user creation fails', async () => {
      const mockPayload = {
        sub: 'google-456',
        email: 'newuser@example.com',
        name: 'New User',
      };

      const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
      (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
        verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
      }));
      authService = new AuthService();

      jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(null);
      jest.spyOn(userModel, 'create').mockRejectedValueOnce(new Error('Database error'));

      await expect(authService.signUpWithGoogle('token')).rejects.toThrow('Database error');
    });
  });

  describe('signInWithGoogle', () => {
    it('should return token and user on successful signin', async () => {
      const mockPayload = {
        sub: 'google-123',
        email: 'user@example.com',
        name: 'Existing User',
      };

      const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
      (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
        verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
      }));
      authService = new AuthService();

      const mockUser = {
        _id: new mongoose.Types.ObjectId(),
        googleId: 'google-123',
        email: 'user@example.com',
        name: 'Existing User',
        bio: '',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(mockUser as any);
      jest.spyOn(jwt, 'sign').mockReturnValueOnce('jwt-token-456' as any);

      const result = await authService.signInWithGoogle('valid-token');

      expect(result.token).toBe('jwt-token-456');
      expect(result.user.email).toBe('user@example.com');
    });

    it('should throw "User not found" when user does not exist', async () => {
      const mockPayload = {
        sub: 'google-789',
        email: 'nonexistent@example.com',
        name: 'Non Existent User',
      };

      const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
      (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
        verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
      }));
      authService = new AuthService();

      jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(null);

      await expect(authService.signInWithGoogle('token')).rejects.toThrow('User not found');
    });


    it('should throw "Invalid Google token" when payload is null', async () => {
      const mockTicket = { getPayload: jest.fn().mockReturnValue(null) };
      (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
        verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
      }));
      authService = new AuthService();

      await expect(authService.signInWithGoogle('token')).rejects.toThrow('Invalid Google token');
    });

    it('should return token and user on successful signin', async () => {
    const mockPayload = {
      sub: 'google-123',
      email: 'user@example.com',
      name: 'Existing User',
    };

    const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
    (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
      verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
    }));
    authService = new AuthService();

    const mockUser = {
      _id: new mongoose.Types.ObjectId(),
      googleId: 'google-123',
      email: 'user@example.com',
      name: 'Existing User',
      bio: '',
      createdAt: new Date(),
      updatedAt: new Date(),
    };

    jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(mockUser as any);
    jest.spyOn(jwt, 'sign').mockReturnValueOnce('jwt-token-456' as any);

    const result = await authService.signInWithGoogle('valid-token');

    expect(result.token).toBe('jwt-token-456');
    expect(result.user.email).toBe('user@example.com');
  });

  it('should throw "Invalid Google token" when verification fails', async () => {
    // Mock verifyIdToken to reject
    (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
      verifyIdToken: jest.fn().mockRejectedValue(new Error('Invalid token')),
    }));
    authService = new AuthService();

    await expect(authService.signInWithGoogle('invalid-token')).rejects.toThrow('Invalid Google token');
  });

  it('should throw "User not found" when user does not exist', async () => {
    const mockPayload = {
      sub: 'google-789',
      email: 'nonexistent@example.com',
      name: 'Non Existent User',
    };

    const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
    (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
      verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
    }));
    authService = new AuthService();

    // Mock findByGoogleId to return null
    jest.spyOn(userModel, 'findByGoogleId').mockResolvedValueOnce(null);

    await expect(authService.signInWithGoogle('token')).rejects.toThrow('User not found');
  });


  it('should throw "Invalid Google token" when payload is null', async () => {
    const mockTicket = { getPayload: jest.fn().mockReturnValue(null) };
    (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
      verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
    }));
    authService = new AuthService();

    await expect(authService.signInWithGoogle('token')).rejects.toThrow('Invalid Google token');
  });
  it('should throw "Invalid Google token" when payload missing email', async () => {
  const mockPayload = {
    sub: 'google-123',
    name: 'User Name',
    // Missing email
  };

  const mockTicket = { getPayload: jest.fn().mockReturnValue(mockPayload) };
  (googleAuthLibrary.OAuth2Client as unknown as jest.Mock).mockImplementation(() => ({
    verifyIdToken: jest.fn().mockResolvedValue(mockTicket),
  }));
  authService = new AuthService();

  await expect(authService.signInWithGoogle('token')).rejects.toThrow('Invalid Google token');
});
  });
});
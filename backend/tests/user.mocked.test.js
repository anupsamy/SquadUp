"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const supertest_1 = __importDefault(require("supertest"));
const express_1 = __importDefault(require("express"));
const mongodb_1 = require("mongodb");
const user_controller_1 = require("../controllers/user.controller");
const user_model_1 = require("../user.model");
const media_service_1 = require("../services/media.service");
jest.mock('../utils/logger.util');
jest.mock('../services/media.service');
describe('Mocked: User Endpoints (With Mocks)', () => {
    let app;
    let userController;
    let testUserId;
    let testUserEmail;
    beforeAll(() => {
        app = (0, express_1.default)();
        app.use(express_1.default.json());
        userController = new user_controller_1.UserController();
        testUserId = new mongodb_1.ObjectId();
        testUserEmail = 'mocked-test@example.com';
        // Middleware to attach user to requests
        app.use((req, res, next) => {
            if (!req.user) {
                req.user = {
                    _id: testUserId,
                    googleId: 'google-id',
                    email: testUserEmail,
                    name: 'Mocked Test User',
                    bio: '',
                    createdAt: new Date(),
                    updatedAt: new Date(),
                };
            }
            next();
        });
        // Setup routes
        app.post('/profile', (req, res, next) => userController.updateProfile(req, res, next));
        app.delete('/profile', (req, res, next) => userController.deleteProfile(req, res, next));
    });
    beforeEach(() => {
        jest.clearAllMocks();
    });
    describe('POST /profile', () => {
        // Mocked behavior: userModel.update throws a database error
        // Input: valid user update request
        // Expected status code: 500
        // Expected behavior: error is caught and handled gracefully
        // Expected output: error message
        it('should return 500 when database throws error', async () => {
            jest.spyOn(user_model_1.userModel, 'update').mockRejectedValueOnce(new Error('Database connection failed'));
            const updateData = {
                name: 'Updated Name',
                transitType: 'car',
                address: '456 Oak Ave',
            };
            const res = await (0, supertest_1.default)(app)
                .post('/profile')
                .send(updateData);
            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message');
            expect(res.body.message).toContain('Database connection failed');
            expect(user_model_1.userModel.update).toHaveBeenCalledTimes(1);
        });
        // Mocked behavior: userModel.update returns null (user not found)
        // Input: valid user update request for non-existent user
        // Expected status code: 404
        // Expected behavior: 404 response is returned
        // Expected output: "User not found" message
        it('should return 404 when user does not exist', async () => {
            jest.spyOn(user_model_1.userModel, 'update').mockResolvedValueOnce(null);
            const updateData = {
                name: 'Updated Name',
                transitType: 'car',
                address: '456 Oak Ave',
            };
            const res = await (0, supertest_1.default)(app)
                .post('/profile')
                .send(updateData);
            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'User not found');
        });
        // Mocked behavior: userModel.update throws a validation error
        // Input: invalid user update request (e.g., invalid email format)
        // Expected status code: 500
        // Expected behavior: validation error is caught and handled
        // Expected output: error message
        it('should return 500 when validation error occurs', async () => {
            jest.spyOn(user_model_1.userModel, 'update').mockRejectedValueOnce(new Error('Invalid email format'));
            const updateData = {
                name: 'Valid Name',
                transitType: 'car',
                address: '456 Oak Ave',
            };
            const res = await (0, supertest_1.default)(app)
                .post('/profile')
                .send(updateData);
            expect(res.status).toBe(500);
            expect(res.body.message).toContain('Invalid email format');
        });
        it('calls next(error) if a non-Error is thrown', async () => {
            const mockNext = jest.fn();
            // Mock userModel.update or MediaService.deleteAllUserImages to throw a non-Error value
            jest.spyOn(user_model_1.userModel, 'update').mockRejectedValueOnce('some string error');
            const req = {
                user: { _id: '123' },
                body: { name: 'Test', transitType: 'driving', address: 'abc' },
            };
            const res = {
                status: jest.fn().mockReturnThis(),
                json: jest.fn(),
            };
            await userController.updateProfile(req, res, mockNext);
            expect(mockNext).toHaveBeenCalledWith('some string error');
        });
    });
    describe('DELETE /profile', () => {
        // Mocked behavior: MediaService.deleteAllUserImages throws an error
        // Input: valid user deletion request
        // Expected status code: 500
        // Expected behavior: error is caught and handled gracefully
        // Expected output: error message
        it('should return 500 when media deletion fails', async () => {
            jest.spyOn(media_service_1.MediaService, 'deleteAllUserImages').mockRejectedValueOnce(new Error('Failed to connect to storage service'));
            const res = await (0, supertest_1.default)(app)
                .delete('/profile');
            expect(res.status).toBe(500);
            expect(res.body.message).toContain('Failed to connect to storage service');
            expect(media_service_1.MediaService.deleteAllUserImages).toHaveBeenCalledWith(testUserId.toString());
        });
        // Mocked behavior: userModel.delete throws a database error
        // Input: valid user deletion request
        // Expected status code: 500
        // Expected behavior: error is caught and handled gracefully
        // Expected output: error message
        it('should return 500 when database deletion fails', async () => {
            jest.spyOn(media_service_1.MediaService, 'deleteAllUserImages').mockResolvedValueOnce(undefined);
            jest.spyOn(user_model_1.userModel, 'delete').mockRejectedValueOnce(new Error('Database write failed'));
            const res = await (0, supertest_1.default)(app)
                .delete('/profile');
            expect(res.status).toBe(500);
            expect(res.body.message).toContain('Database write failed');
            expect(user_model_1.userModel.delete).toHaveBeenCalledWith(testUserId);
        });
        // Mocked behavior: both media and user deletion succeed
        // Input: valid user deletion request
        // Expected status code: 200
        // Expected behavior: both services are called in
        // Expected output: success message
        it('should delete user and media in correct order', async () => {
            const mediaDeleteSpy = jest.spyOn(media_service_1.MediaService, 'deleteAllUserImages').mockResolvedValueOnce(undefined);
            const userDeleteSpy = jest.spyOn(user_model_1.userModel, 'delete').mockResolvedValueOnce(undefined);
            const res = await (0, supertest_1.default)(app)
                .delete('/profile');
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'User deleted successfully');
            expect(mediaDeleteSpy).toHaveBeenCalled();
            expect(userDeleteSpy).toHaveBeenCalled();
        });
        // Mocked behavior: MediaService succeeds but user deletion fails silently
        // Input: valid user deletion request
        // Expected status code: 500
        // Expected behavior: error is propagated
        // Expected output: error message
        it('should handle errors when media is deleted but user deletion fails', async () => {
            jest.spyOn(media_service_1.MediaService, 'deleteAllUserImages').mockResolvedValueOnce(undefined);
            jest.spyOn(user_model_1.userModel, 'delete').mockRejectedValueOnce(new Error('Concurrent deletion attempted'));
            const res = await (0, supertest_1.default)(app)
                .delete('/profile');
            expect(res.status).toBe(500);
            expect(res.body.message).toContain('Concurrent deletion attempted');
        });
        // Input: invalid googleId parameter
        // Expected behavior: throws error
        // Expected output: "Failed to find user" error message
        it('should throw error when findByGoogleId fails', async () => {
            // Pass an object instead of string to trigger MongoDB error
            const invalidGoogleId = {};
            await expect(user_model_1.userModel.findByGoogleId(invalidGoogleId)).rejects.toThrow('Failed to find user');
        });
        // Input: invalid user ID format
        // Expected behavior: throws error
        // Expected output: "Failed to find user" error message
        it('should throw error when findById fails', async () => {
            const invalidId = 'invalid-id';
            await expect(user_model_1.userModel.findById(invalidId)).rejects.toThrow('Failed to find user');
        });
        // Input: invalid user ID format
        // Expected behavior: throws error
        // Expected output: "Failed to delete user" error message
        it('should throw error when delete fails', async () => {
            const invalidId = 'invalid-id';
            await expect(user_model_1.userModel.delete(invalidId)).rejects.toThrow('Failed to delete user');
        });
        it('calls next(error) if deleteAllUserImages throws non-Error', async () => {
            const mockNext = jest.fn();
            jest.spyOn(media_service_1.MediaService, 'deleteAllUserImages').mockRejectedValueOnce('fail');
            const req = { user: { _id: '123' } };
            const res = {
                status: jest.fn().mockReturnThis(),
                json: jest.fn(),
            };
            await userController.deleteProfile(req, res, mockNext);
            expect(mockNext).toHaveBeenCalledWith('fail');
        });
        // // Mocked behavior: error thrown is not an Error instance
        // // Input: valid user deletion request
        // // Expected status code: 500
        // // Expected behavior: non-Error is caught, next() is called
        // // Expected output: default error message
        // it('should call next() when error is not an Error instance', async () => {
        //   const nextMock = jest.fn();
        //   jest.spyOn(MediaService, 'deleteAllUserImages').mockRejectedValueOnce(
        //     'Unknown error string'
        //   );
        //   app.delete('/profile', (req, res, next) => userController.deleteProfile(req, res, next));
        //   app.use(nextMock);
        //   await request(app).delete('/profile');
        //   expect(nextMock).toHaveBeenCalled();
        // });
    });
});

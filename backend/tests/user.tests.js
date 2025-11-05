"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const supertest_1 = __importDefault(require("supertest"));
const express_1 = __importDefault(require("express"));
const mongoose_1 = __importDefault(require("mongoose"));
const user_controller_1 = require("../src/controllers/user.controller");
const user_model_1 = require("../user.model");
jest.mock('../utils/logger.util');
jest.mock('../services/media.service');
describe('Unmocked: User Model', () => {
    describe('UserModel.create', () => {
        // Input: user data missing required googleId field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating user with missing googleId', async () => {
            const invalidUserData = {
                email: 'test@example.com',
                name: 'Test User',
            };
            await expect(user_model_1.userModel.create(invalidUserData)).rejects.toThrow('Invalid update data');
        });
        // Input: user data missing required email field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating user with missing email', async () => {
            const invalidUserData = {
                googleId: `google-${Date.now()}`,
                name: 'Test User',
            };
            await expect(user_model_1.userModel.create(invalidUserData)).rejects.toThrow('Invalid update data');
        });
        // Input: user data missing required name field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating user with missing name', async () => {
            const invalidUserData = {
                googleId: `google-${Date.now()}`,
                email: 'test@example.com',
            };
            await expect(user_model_1.userModel.create(invalidUserData)).rejects.toThrow('Invalid update data');
        });
        // Input: user data with duplicate googleId
        // Expected behavior: throws error (unique constraint violation)
        // Expected output: error about duplicate key
        it('should throw error when creating user with duplicate googleId', async () => {
            const googleId = `google-duplicate-${Date.now()}`;
            const firstUser = {
                googleId,
                email: `first-${Date.now()}@example.com`,
                name: 'First User',
            };
            await user_model_1.userModel.create(firstUser);
            const secondUser = {
                googleId,
                email: `second-${Date.now()}@example.com`,
                name: 'Second User',
            };
            await expect(user_model_1.userModel.create(secondUser)).rejects.toThrow();
        });
        // Input: user data with duplicate email
        // Expected behavior: throws error (unique constraint violation)
        // Expected output: error about duplicate key
        it('should throw error when creating user with duplicate email', async () => {
            const email = `duplicate-${Date.now()}@example.com`;
            const firstUser = {
                googleId: `google-${Date.now()}`,
                email,
                name: 'First User',
            };
            await user_model_1.userModel.create(firstUser);
            const secondUser = {
                googleId: `google-${Date.now()}`,
                email,
                name: 'Second User',
            };
            await expect(user_model_1.userModel.create(secondUser)).rejects.toThrow();
        });
    });
    describe('UserModel.update', () => {
        // Input: valid update data but for non-existent user ID
        // Expected behavior: returns null
        // Expected output: null instead of user object
        it('should return null when updating non-existent user', async () => {
            const fakeUserId = new mongoose_1.default.Types.ObjectId();
            const updateData = { name: 'Updated Name' };
            const result = await user_model_1.userModel.update(fakeUserId, updateData);
            expect(result).toBeNull();
        });
        // Input: invalid update data (empty strings, etc.)
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when updating with invalid name (empty string)', async () => {
            const testUser = await user_model_1.userModel.create({
                googleId: `google-${Date.now()}`,
                email: `test-${Date.now()}@example.com`,
                name: 'Test User',
            });
            const updateData = { name: '' };
            await expect(user_model_1.userModel.update(testUser._id, updateData)).rejects.toThrow();
            // Cleanup
            await user_model_1.userModel.delete(testUser._id);
        });
    });
    describe('UserModel.delete', () => {
        // Input: valid user ID of existing user
        // Expected behavior: user is deleted successfully
        // Expected output: no error thrown
        it('should delete user successfully', async () => {
            const testUser = await user_model_1.userModel.create({
                googleId: `google-${Date.now()}`,
                email: `delete-test-${Date.now()}@example.com`,
                name: 'User To Delete',
            });
            await expect(user_model_1.userModel.delete(testUser._id)).resolves.not.toThrow();
            const deletedUser = await user_model_1.userModel.findById(testUser._id);
            expect(deletedUser).toBeNull();
        });
        // Input: non-existent user ID
        // Expected behavior: no error thrown (MongoDB doesn't error on delete of non-existent)
        // Expected output: operation completes without error
        it('should handle deletion of non-existent user gracefully', async () => {
            const fakeUserId = new mongoose_1.default.Types.ObjectId();
            await expect(user_model_1.userModel.delete(fakeUserId)).resolves.not.toThrow();
        });
    });
    describe('UserModel.findById', () => {
        // Input: valid user ID of existing user
        // Expected behavior: user is found and returned
        // Expected output: user object
        it('should find user by ID', async () => {
            const testUser = await user_model_1.userModel.create({
                googleId: `google-${Date.now()}`,
                email: `find-${Date.now()}@example.com`,
                name: 'User To Find',
            });
            const foundUser = await user_model_1.userModel.findById(testUser._id);
            expect(foundUser).toBeDefined();
            expect(foundUser?.email).toBe(testUser.email);
            await user_model_1.userModel.delete(testUser._id);
        });
        // Input: non-existent user ID
        // Expected behavior: returns null
        // Expected output: null
        it('should return null when user ID does not exist', async () => {
            const fakeUserId = new mongoose_1.default.Types.ObjectId();
            const result = await user_model_1.userModel.findById(fakeUserId);
            expect(result).toBeNull();
        });
    });
    describe('UserModel.findByGoogleId', () => {
        // Input: valid googleId of existing user
        // Expected behavior: user is found and returned
        // Expected output: user object
        it('should find user by googleId', async () => {
            const googleId = `google-${Date.now()}`;
            const testUser = await user_model_1.userModel.create({
                googleId,
                email: `find-google-${Date.now()}@example.com`,
                name: 'User To Find by Google ID',
            });
            const foundUser = await user_model_1.userModel.findByGoogleId(googleId);
            expect(foundUser).toBeDefined();
            expect(foundUser?.googleId).toBe(googleId);
            await user_model_1.userModel.delete(testUser._id);
        });
        // Input: non-existent googleId
        // Expected behavior: returns null
        // Expected output: null
        it('should return null when googleId does not exist', async () => {
            const fakeGoogleId = `google-nonexistent-${Date.now()}`;
            const result = await user_model_1.userModel.findByGoogleId(fakeGoogleId);
            expect(result).toBeNull();
        });
    });
});
describe('Unmocked: User Controller', () => {
    let app;
    let userController;
    let testUserId;
    beforeAll(async () => {
        app = (0, express_1.default)();
        app.use(express_1.default.json());
        userController = new user_controller_1.UserController();
        // Middleware to attach user to requests
        app.use((req, res, next) => {
            if (!req.user) {
                req.user = {
                    _id: testUserId,
                    googleId: 'google-id',
                    email: 'test@example.com',
                    name: 'Test User',
                    bio: '',
                    createdAt: new Date(),
                    updatedAt: new Date(),
                };
            }
            next();
        });
        app.get('/profile', (req, res, next) => userController.getProfile(req, res));
        app.post('/profile', (req, res, next) => userController.updateProfile(req, res, next));
        app.delete('/profile', (req, res, next) => userController.deleteProfile(req, res, next));
        const testUser = await user_model_1.userModel.create({
            googleId: `google-${Date.now()}`,
            email: `controller-test-${Date.now()}@example.com`,
            name: 'Controller Test User',
        });
        testUserId = testUser._id;
    });
    afterAll(async () => {
        try {
            await user_model_1.userModel.delete(testUserId);
        }
        catch (e) {
            // Already deleted
        }
    });
    describe('GET /profile', () => {
        // Input: valid authenticated user
        // Expected status code: 200
        // Expected behavior: user profile is returned
        // Expected output: user data with all fields
        it('should return 200 and user profile when authenticated', async () => {
            const res = await (0, supertest_1.default)(app).get('/profile');
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Profile fetched successfully');
            expect(res.body.data).toHaveProperty('user');
            expect(res.body.data.user).toHaveProperty('_id');
            expect(res.body.data.user).toHaveProperty('email');
            expect(res.body.data.user).toHaveProperty('name');
            expect(res.body.data.user).toHaveProperty('googleId');
        });
    });
    describe('updateProfile', () => {
        // Input: valid user update with name change
        // Expected status code: 200
        // Expected behavior: user is updated in database
        // Expected output: updated user data
        it('should update user profile and return 200', async () => {
            const updateData = {
                name: 'Updated Name',
            };
            const res = await (0, supertest_1.default)(app)
                .post('/profile')
                .send(updateData);
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'User info updated successfully');
            expect(res.body.data.user.name).toBe('Updated Name');
            // Verify update persisted in database
            const dbUser = await user_model_1.userModel.findById(testUserId);
            expect(dbUser).toBeDefined();
            if (dbUser) {
                expect(dbUser.name).toBe('Updated Name');
            }
        });
        // Input: attempt to update non-existent user
        // Expected status code: 404
        // Expected behavior: returns not found error
        // Expected output: "User not found" message
        it('should return 404 when user does not exist', async () => {
            const fakeUserId = new mongoose_1.default.Types.ObjectId();
            const fakeApp = (0, express_1.default)();
            fakeApp.use(express_1.default.json());
            fakeApp.use((req, res, next) => {
                req.user = {
                    _id: fakeUserId,
                    googleId: 'fake-google-id',
                    email: 'fake@example.com',
                    name: 'Fake User',
                    bio: '',
                    createdAt: new Date(),
                    updatedAt: new Date(),
                };
                next();
            });
            fakeApp.post('/profile', (req, res, next) => userController.updateProfile(req, res, next));
            const res = await (0, supertest_1.default)(fakeApp)
                .post('/profile')
                .send({ name: 'Updated Name' });
            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'User not found');
        });
        // Input: update with invalid data (empty name)
        // Expected status code: 500
        // Expected behavior: validation error is caught and handled
        // Expected output: error message
        it('should return 500 when update validation fails', async () => {
            const res = await (0, supertest_1.default)(app)
                .post('/profile')
                .send({ name: '' });
            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message');
        });
    });
    describe('deleteProfile', () => {
        // Input: attempt to delete non-existent user
        // Expected status code: 200
        // Expected behavior: deletion completes without error (MongoDB gracefully handles)
        // Expected output: success message
        it('should return 200 even when deleting non-existent user', async () => {
            const fakeUserId = new mongoose_1.default.Types.ObjectId();
            const fakeApp = (0, express_1.default)();
            fakeApp.use(express_1.default.json());
            fakeApp.use((req, res, next) => {
                req.user = {
                    _id: fakeUserId,
                    googleId: 'fake-google-id',
                    email: 'fake@example.com',
                    name: 'Fake User',
                    bio: '',
                    createdAt: new Date(),
                    updatedAt: new Date(),
                };
                next();
            });
            fakeApp.delete('/profile', (req, res, next) => userController.deleteProfile(req, res, next));
            const res = await (0, supertest_1.default)(fakeApp).delete('/profile');
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'User deleted successfully');
        });
    });
});
/*
tests to add for user (non mocked)
user.model coverage
- create with incorrct model schema
- update with error (TODO add better error handling)
- delete user error (TODO add better error handling)
- error finding by google id (requires auth controller)
- finding by google id at all (requires auth controller)

user.controller coverate
-update profile user not found
-update profile failed to update profile (TODO add better specific handling)
-delete profile failed to delete profile

*/ 

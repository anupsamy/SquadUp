"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const supertest_1 = __importDefault(require("supertest"));
const express_1 = __importDefault(require("express"));
const mongoose_1 = __importDefault(require("mongoose"));
const group_controller_1 = require("../src/controllers/group.controller");
const group_model_1 = require("../group.model");
jest.mock('../utils/logger.util');
jest.mock('../services/media.service');
describe('Unmocked: Group Model', () => {
    describe('GroupModel.create', () => {
        // Input: group data missing required joinCode field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating group with missing join code', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleActivityType = "CAFE";
            const invalidGroupData = {
                groupName: "TestGroup1",
                groupLeaderId: exampleGroupLeader,
                expectedPeople: "1",
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime, // Default to current time for now,
                activityType: exampleActivityType
            };
            await expect(group_model_1.groupModel.create(invalidGroupData)).rejects.toThrow('Invalid update data');
        });
        // Input: group data missing required groupLeaderId field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating group with missing group leader', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleActivityType = "CAFE";
            const invalidGroupData = {
                joinCode: "eck6c7",
                groupName: "TestGroup",
                expectedPeople: "1",
                groupMemberIds: [],
                meetingTime: exampleMeetingTime, // Default to current time for now,
                activityType: exampleActivityType
            };
            await expect(group_model_1.groupModel.create(invalidGroupData)).rejects.toThrow('Invalid update data');
        });
        // Input: group data missing required expectedPeople field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating group with expected people missing', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleActivityType = "CAFE";
            const invalidGroupData = {
                joinCode: "eck6c7",
                groupName: "TestGroup1",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime, // Default to current time for now,
                activityType: exampleActivityType
            };
            await expect(group_model_1.groupModel.create(invalidGroupData)).rejects.toThrow('Invalid update data');
        });
        // Input: group data missing required groupMemberIds field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating group with group members array missing', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleActivityType = "CAFE";
            const invalidGroupData = {
                joinCode: "eck6c7",
                groupName: "TestGroup1",
                expectedPeople: "1",
                groupLeaderId: exampleGroupLeader,
                meetingTime: exampleMeetingTime, // Default to current time for now,
                activityType: exampleActivityType
            };
            await expect(group_model_1.groupModel.create(invalidGroupData)).rejects.toThrow('Invalid update data');
        });
        // Input: group data missing required meetingTime field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating group with meeting time missing', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleActivityType = "CAFE";
            const invalidGroupData = {
                joinCode: "eck6c7",
                groupName: "TestGroup1",
                expectedPeople: "1",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                activityType: exampleActivityType
            };
            await expect(group_model_1.groupModel.create(invalidGroupData)).rejects.toThrow('Invalid update data');
        });
        // Input: group data missing required activityType field
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating group with activity type missing', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const invalidGroupData = {
                joinCode: "eck6c7",
                groupName: "TestGroup1",
                expectedPeople: "1",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
            };
            await expect(group_model_1.groupModel.create(invalidGroupData)).rejects.toThrow('Invalid update data');
        });
        // Input: group data with duplicate joinCode
        // Expected behavior: throws validation error
        // Expected output: error message about invalid data
        it('should throw error when creating group with duplicate join code', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleActivityType_1 = "CAFE";
            const exampleActivityType_2 = "BAR";
            const exampleMeetingTime_1 = "2026-11-02T12:30:00Z";
            const exampleMeetingTime_2 = "2026-11-03T12:30:00Z";
            const exampleGroupData = {
                joinCode: "eck6c7",
                groupName: "TestGroup1",
                expectedPeople: "1",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime_1,
                activityType: exampleActivityType_1
            };
            await group_model_1.groupModel.create(exampleGroupData);
            const invalidGroupData = {
                joinCode: "eck6c7", //duplicate joinCode (shouldn't be allowed)
                groupName: "TestGroup2",
                expectedPeople: "2",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime_2,
                activityType: exampleActivityType_2
            };
            await expect(group_model_1.groupModel.create(invalidGroupData)).rejects.toThrow(/E11000/);
        });
    });
    describe('GroupModel.findAll', () => {
        it('should return both created groups', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleActivityType_1 = "CAFE";
            const exampleActivityType_2 = "BAR";
            const exampleMeetingTime_1 = "2026-11-02T12:30:00Z";
            const exampleMeetingTime_2 = "2026-11-03T12:30:00Z";
            const exampleGroupData_1 = {
                joinCode: "eck6c7",
                groupName: "TestGroup1",
                expectedPeople: "1",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime_1,
                activityType: exampleActivityType_1
            };
            const exampleGroupData_2 = {
                joinCode: "eck6c7", //duplicate joinCode (shouldn't be allowed)
                groupName: "TestGroup2",
                expectedPeople: "2",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime_2,
                activityType: exampleActivityType_2
            };
            await group_model_1.groupModel.create(exampleGroupData_1);
            await group_model_1.groupModel.create(exampleGroupData_2);
            const groups = await group_model_1.groupModel.findAll();
            expect(groups).toHaveLength(2);
            expect(groups[0]).toHaveProperty('joinCode');
            expect(groups[0]).toHaveProperty('groupName');
        });
    });
    describe('GroupModel.findByJoinCode', () => {
        it('should return the created group', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleActivityType = "CAFE";
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleJoinCode = "eck6c7";
            const exampleGroupData = {
                joinCode: exampleJoinCode,
                groupName: "TestGroup1",
                expectedPeople: "1",
                groupLeaderId: exampleGroupLeader,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
                activityType: exampleActivityType
            };
            await group_model_1.groupModel.create(exampleGroupData);
            await expect(group_model_1.groupModel.findByJoinCode(exampleJoinCode)).toHaveReturnedWith(exampleGroupData);
        });
        it('should return null when joinCode does not exist', async () => {
            const result = await group_model_1.groupModel.findByJoinCode('nonexistent123');
            expect(result).toBeNull();
        });
    });
    describe('GroupModel.updateGroupByJoinCode', () => {
        it('should fail to update non-existent group', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            };
            const exampleActivityType = "CAFE";
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleJoinCode = "eck6c7";
            const exampleGroupData = {
                joinCode: exampleJoinCode,
                expectedPeople: "5",
            };
            await expect(group_model_1.groupModel.updateGroupByJoinCode(exampleJoinCode, exampleGroupData)).rejects.toThrow('Invalid update data');
        });
        it('should update group successfully', async () => {
            const testGroup = await group_model_1.groupModel.create({
                joinCode: 'update123',
                groupName: 'Original Name',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: new Date(),
                activityType: 'CAFE',
            });
            const updatedGroup = await group_model_1.groupModel.updateGroupByJoinCode('update123', { groupName: 'Updated Name' });
            expect(updatedGroup).toBeDefined();
            expect(updatedGroup?.groupName).toBe('Updated Name');
        });
    });
    describe('GroupModel.delete', () => {
        it('should fail to delete non-existent group', async () => {
            const exampleJoinCode = "abc123";
            await expect(group_model_1.groupModel.delete(exampleJoinCode)).rejects.toThrow('Group with joinCode ${exampleJoinCode} not found');
        });
        it('should delete group successfully', async () => {
            const testGroup = await group_model_1.groupModel.create({
                joinCode: 'delete123',
                groupName: 'Group To Delete',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: new Date(),
                activityType: 'CAFE',
            });
            await expect(group_model_1.groupModel.delete('delete123')).resolves.not.toThrow();
            const deletedGroup = await group_model_1.groupModel.findByJoinCode('delete123');
            expect(deletedGroup).toBeNull();
        });
    });
    describe('GroupModel.leaveGroup', () => {
        it('should allow a user to leave a group successfully', async () => {
            const testGroup = await group_model_1.groupModel.create({
                joinCode: 'leave123',
                groupName: 'Group To Leave',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [
                    { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                    { id: 'user-id', name: 'User', email: 'user@example.com' },
                ],
                meetingTime: new Date(),
                activityType: 'CAFE',
            });
            await group_model_1.groupModel.leaveGroup('leave123', 'user-id');
            const updatedGroup = await group_model_1.groupModel.findByJoinCode('leave123');
            expect(updatedGroup?.groupMemberIds).toHaveLength(1);
            expect(updatedGroup?.groupMemberIds).not.toEqual(expect.arrayContaining([{ id: 'user-id', name: 'User', email: 'user@example.com' }]));
        });
        it('should throw an error when attempting to leave a non-existent group', async () => {
            await expect(group_model_1.groupModel.leaveGroup('nonexistent123', 'user-id')).rejects.toThrow('Group with joinCode nonexistent123 not found');
        });
    });
});
describe('Unmocked: Group Controller', () => {
    let app;
    let groupController;
    beforeAll(async () => {
        app = (0, express_1.default)();
        app.use(express_1.default.json());
        groupController = new group_controller_1.GroupController();
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        };
        // Middleware to attach user to requests
        app.use((req, res, next) => {
            if (!req.user) {
                req.user = {
                    _id: new mongoose_1.default.Types.ObjectId(),
                    googleId: 'google-id',
                    email: 'test@example.com',
                    name: 'Test User',
                };
            }
            next();
        });
        app.get('/group/info', (req, res, next) => groupController.getAllGroups(req, res, next));
        app.get('/group/:joinCode', (req, res, next) => groupController.getGroupByJoinCode(req, res));
        app.post('/group/join', (req, res, next) => groupController.joinGroupByJoinCode(req, res, next));
        app.post('/group/create', (req, res, next) => groupController.createGroup(req, res, next));
        app.delete('/group/delete/:joincode', (req, res, next) => groupController.deleteGroupByJoinCode(req, res, next));
    });
    afterAll(async () => {
        await mongoose_1.default.connection.close();
    });
    describe('GET /group/info', () => {
        // Input: valid authenticated user
        // Expected status code: 200
        // Expected behavior: user profile is returned
        // Expected output: user data with all fields
        it('should return 200 and user profile when authenticated', async () => {
            const res = await (0, supertest_1.default)(app).get('/group/info');
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Groups fetched successfully');
            expect(res.body.data).toHaveProperty('groups');
            expect(Array.isArray(res.body.data.groups)).toBe(true);
        });
    });
    describe('GET /group/:joinCode', () => {
        // Input: valid authenticated user
        // Expected status code: 200
        // Expected behavior: user profile is returned
        // Expected output: user data with all fields
        it('should return 200 and the group for a valid join code', async () => {
            const testGroup = await group_model_1.groupModel.create({
                joinCode: 'test123',
                groupName: 'Test Group',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: new Date(),
                activityType: 'CAFE',
            });
            const res = await (0, supertest_1.default)(app).get(`/group/${testGroup.joinCode}`);
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group fetched successfully');
            expect(res.body.data.group).toHaveProperty('joinCode', 'test123');
        });
        it('should return 404 for an invalid join code', async () => {
            const res = await (0, supertest_1.default)(app).get('/group/invalid123');
            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', "Group with joinCode 'invalid123' not found");
        });
    });
    describe('POST /group/create', () => {
        it('should create a new group and return 201', async () => {
            const groupData = {
                groupName: 'New Group',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                meetingTime: new Date(),
                activityType: 'CAFE',
            };
            const res = await (0, supertest_1.default)(app).post('/group/create').send(groupData);
            expect(res.status).toBe(201);
            expect(res.body).toHaveProperty('message', 'Group New Group created successfully');
            expect(res.body.data.group).toHaveProperty('groupName', 'New Group');
        });
        it('should return 400 for invalid group data', async () => {
            const invalidGroupData = {
                groupName: 'Invalid Group',
            };
            const res = await (0, supertest_1.default)(app).post('/group/create').send(invalidGroupData);
            expect(res.status).toBe(400);
            expect(res.body).toHaveProperty('message');
        });
    });
    describe('POST /group/join', () => {
        it('should join a group and return 200', async () => {
            const testGroup = await group_model_1.groupModel.create({
                joinCode: 'join123',
                groupName: 'Joinable Group',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: new Date(),
                activityType: 'CAFE',
            });
            const joinData = {
                joinCode: 'join123',
                groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
            };
            const res = await (0, supertest_1.default)(app).post('/group/join').send(joinData);
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group info updated successfully');
            expect(res.body.data.group.groupMemberIds).toEqual(expect.arrayContaining([{ id: 'user-id', name: 'User', email: 'user@example.com' }]));
        });
        it('should return 404 for an invalid join code', async () => {
            const joinData = {
                joinCode: 'invalid123',
                groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
            };
            const res = await (0, supertest_1.default)(app).post('/group/join').send(joinData);
            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');
        });
    });
    describe('DELETE /group/delete/:joinCode', () => {
        it('should delete a group and return 200', async () => {
            const testGroup = await group_model_1.groupModel.create({
                joinCode: 'delete123',
                groupName: 'Deletable Group',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: new Date(),
                activityType: 'CAFE',
            });
            const res = await (0, supertest_1.default)(app).delete(`/group/delete/${testGroup.joinCode}`);
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'group deleted successfully');
        });
        it('should return 404 for an invalid join code', async () => {
            const res = await (0, supertest_1.default)(app).delete('/group/delete/invalid123');
            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');
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

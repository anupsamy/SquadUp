import request from 'supertest';
import express, { Express } from 'express';
import { GroupController } from '../../src/controllers/group.controller';
import { groupModel } from '../../src/group.model';
import { WebSocketService } from '../../src/services/websocket.service';

jest.mock('../../src/utils/logger.util');
jest.mock('../../src/services/media.service');
jest.mock('../../src/services/websocket.service', () => ({
  getWebSocketService: jest.fn(() => ({
    notifyGroupUpdate: jest.fn(),
  })),
}));

describe('Mocked: Group Endpoints (With Mocks)', () => {
  let app: Express;
  let groupController: GroupController;

  beforeAll(() => {
    app = express();
    app.use(express.json());
    groupController = new GroupController();

    // Middleware to attach user to requests
    app.use((req: any, res: any, next: any) => {
      if (!req.user) {
        req.user = {
          _id: 'mocked-user-id',
          googleId: 'mocked-google-id',
          email: 'mocked@example.com',
          name: 'Mocked User',
        };
      }
      next();
    });

    // Setup routes
    app.get('/group/info',(req, res, next) => groupController.getAllGroups(req, res, next));
    app.get('/group/:joinCode',(req, res, next) => groupController.getGroupByJoinCode(req, res, next));
    app.post('/group/join', (req, res, next) => groupController.joinGroupByJoinCode(req, res, next));
    app.post('/group/create', (req, res, next) => groupController.createGroup(req, res, next));
    app.delete('/group/delete/:joinCode', (req, res, next) => groupController.deleteGroupByJoinCode(req, res, next));
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('GET /group/info', () => {
        it('should return 200 and a list of groups', async () => {
            const mockGroups = [
            { joinCode: 'group1', groupName: 'Group 1' },
            { joinCode: 'group2', groupName: 'Group 2' },
            ];

            jest.spyOn(groupModel, 'findAll').mockResolvedValueOnce(mockGroups as any);

            const res = await request(app).get('/group/info');

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Groups fetched successfully');
            expect(res.body.data.groups).toEqual(mockGroups);
            expect(groupModel.findAll).toHaveBeenCalledTimes(1);
        });

        it('should return 500 when an error occurs', async () => {
            jest.spyOn(groupModel, 'findAll').mockRejectedValueOnce(new Error('Database error'));

            const res = await request(app).get('/group/info');

            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message', 'Failed to fetch groups');
            expect(groupModel.findAll).toHaveBeenCalledTimes(1);
        });
    });

    describe('GET /group/:joinCode', () => {
        it('should return 200 and the group for a valid join code', async () => {
            const mockGroup = { joinCode: 'group1', groupName: 'Group 1' };

            jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(mockGroup as any);

            const res = await request(app).get('/group/group1');

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group fetched successfully');
            expect(res.body.data.group).toEqual(mockGroup);
            expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
        });

        it('should return 404 when the group does not exist', async () => {
            jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(null);

            const res = await request(app).get('/group/nonexistent');

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', "Group with joinCode 'nonexistent' not found");
            expect(groupModel.findByJoinCode).toHaveBeenCalledWith('nonexistent');
        });
    });

    describe('POST /group/join', () => {
        it('should return 200 when a user joins a group successfully', async () => {
            const mockGroup = { joinCode: 'group1', groupName: 'Group 1', groupMemberIds: [] };
            const updatedGroup = { ...mockGroup, groupMemberIds: [{ id: 'user-id', name: 'User' }] };

            jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(mockGroup as any);
            jest.spyOn(groupModel, 'updateGroupByJoinCode').mockResolvedValueOnce(updatedGroup as any);

            const joinData = { joinCode: 'group1', groupMemberIds: [{ id: 'user-id', name: 'User' }] };

            const res = await request(app).post('/group/join').send(joinData);

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group info updated successfully');
            expect(res.body.data.group).toEqual(updatedGroup);
            expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
            expect(groupModel.updateGroupByJoinCode).toHaveBeenCalledWith('group1', joinData);
        });

        it('should return 404 when the group does not exist', async () => {
            jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(null);

            const joinData = { joinCode: 'nonexistent', groupMemberIds: [{ id: 'user-id', name: 'User' }] };

            const res = await request(app).post('/group/join').send(joinData);

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');
            expect(groupModel.findByJoinCode).toHaveBeenCalledWith('nonexistent');
        });

        it('should return 500 if the group update fails', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const exampleGroupLeader = {
                id: "leader-id",
                name: "Leader",
                email: "leader@example.com",
            };

            // Create a group
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: "Joinable Group",
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 5,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
                activityType: "CAFE",
            });

            jest.spyOn(groupModel, 'updateGroupByJoinCode').mockRejectedValueOnce(new Error('Update failed'));

            const joinData = {
                joinCode: exampleJoinCode,
                groupMemberIds: [{ id: "user-id", name: "User", email: "user@example.com" }],
            };

            const res = await request(app).post('/group/join').send(joinData);

            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message', 'Failed to update group info');
            });


    });

    describe('POST /group/create', () => {
        it('should return 201 when a group is created successfully', async () => {
            const mockGroup = { joinCode: 'group1', groupName: 'Group 1' };

            jest.spyOn(groupModel, 'create').mockResolvedValueOnce(mockGroup as any);

            const groupData = { groupName: 'Group 1', groupLeaderId: { id: 'leader-id' }, expectedPeople: 5 };

            const res = await request(app).post('/group/create').send(groupData);

            expect(res.status).toBe(201);
            expect(res.body).toHaveProperty('message', 'Group Group 1 created successfully');
            expect(res.body.data.group).toEqual(mockGroup);
            expect(groupModel.create).toHaveBeenCalledWith(expect.objectContaining(groupData));
        });

        it('should return 500 when an error occurs', async () => {
            jest.spyOn(groupModel, 'create').mockRejectedValueOnce(new Error('Database error'));

            const groupData = { groupName: 'Group 1', groupLeaderId: { id: 'leader-id' }, expectedPeople: 5 };

            const res = await request(app).post('/group/create').send(groupData);

            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message', 'Failed to create group');
            expect(groupModel.create).toHaveBeenCalledWith(expect.objectContaining(groupData));
        });
    });

    describe('DELETE /group/delete/:joinCode', () => {
        it('should return 200 when a group is deleted successfully', async () => {
            jest.spyOn(groupModel, 'delete').mockResolvedValueOnce(undefined);

            const res = await request(app).delete('/group/delete/group1');

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'group deleted successfully');
            expect(groupModel.delete).toHaveBeenCalledWith('group1');
        });

        it('should return 404 when the group does not exist', async () => {
            jest.spyOn(groupModel, 'delete').mockRejectedValueOnce(new Error('Group not found'));

            const res = await request(app).delete('/group/delete/nonexistent');

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');
            expect(groupModel.delete).toHaveBeenCalledWith('nonexistent');
        });
    });

    describe('POST /group/test-websocket/:joinCode', () => {
        it('should send a test WebSocket notification', async () => {
            const joinCode = 'test123';
            const message = 'Test notification';
            const type = 'test';

            const res = await request(app)
            .post(`/group/test-websocket/${joinCode}`)
            .send({ message, type });

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Test notification sent successfully');
        });

        it('should return 500 if WebSocket service is unavailable', async () => {
            const joinCode = 'test123';
            const message = 'Test notification';

            const mockWebSocketService = getWebSocketService();
            jest.spyOn(mockWebSocketService, 'notifyGroupUpdate').mockImplementationOnce(() => {
                throw new Error('WebSocket service not available');
            });

            const res = await request(app)
                .post(`/group/test-websocket/${joinCode}`)
                .send({ message });

            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message', 'WebSocket service not available');
        });

        it('should update the group even if WebSocket service is unavailable', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const exampleGroupLeader = {
                id: "leader-id",
                name: "Leader",
                email: "leader@example.com",
            };

            // Create a group
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: "Joinable Group",
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 5,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
                activityType: "CAFE",
            });
            const mockWebSocketService = getWebSocketService();
            jest.spyOn(mockWebSocketService, 'notifyGroupJoin').mockImplementationOnce(() => {
                throw new Error('WebSocket service unavailable');
            });

            const joinData = {
                joinCode: exampleJoinCode,
                groupMemberIds: [{ id: "user-id", name: "User", email: "user@example.com" }],
            };

            const res = await request(app).post('/group/join').send(joinData);

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group info updated successfully');
            expect(res.body.data.group.groupMemberIds).toEqual(
                expect.arrayContaining([
                expect.objectContaining({
                    id: "user-id",
                    name: "User",
                    email: "user@example.com",
                }),
                ])
            );
            });
    });


});

import request from 'supertest';
import express, { Express } from 'express';
import { GroupController } from '../../src/controllers/group.controller';
import { groupModel } from '../../src/group.model';
import { WebSocketService } from '../../src/services/websocket.service';
import { locationService } from '@/services/location.service';
import { TRANSIT_TYPES } from '@/types/transit.types';

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
    app.get('/group/info',(req, res, next) => groupController.getAllGroups(req, res));
    app.get('/group/:joinCode',(req, res, next) => groupController.getGroupByJoinCode(req, res));
    app.post('/group/join', (req, res, next) => groupController.joinGroupByJoinCode(req, res));
    app.post('/group/create', (req, res, next) => groupController.createGroup(req, res, next));
    app.post('/group/update', (req, res, next) => groupController.updateGroupByJoinCode(req, res));
    app.post('/group/leave/:joinCode', (req, res, next) => groupController.leaveGroup(req, res));
    app.delete('/group/delete/:joinCode', (req, res, next) => groupController.deleteGroupByJoinCode(req, res));
    app.get('/group/:joinCode/midpoint',(req, res, next) => groupController.getMidpointByJoinCode(req, res, next));
    app.post('/group/:joinCode/midpoint/update', (req, res, next) => groupController.updateMidpointByJoinCode(req, res, next));
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('GET /group/info', () => {
        it('should return 200 and a list of groups', async () => {
            const mockGroups = [
            { toObject: () => ({ joinCode: 'group1', groupName: 'Group 1' }) },
            { toObject: () => ({ joinCode: 'group2', groupName: 'Group 2' }) },
            ];

            jest.spyOn(groupModel, 'findAll').mockResolvedValueOnce(mockGroups as any);

            const res = await request(app).get('/group/info');

            console.log('should return 200 and a list of groups', res.error);

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Groups fetched successfully');
            expect(res.body.data.groups).toEqual([
                { joinCode: 'group1', groupName: 'Group 1', groupMemberIds: [] },
                { joinCode: 'group2', groupName: 'Group 2', groupMemberIds: [] },
            ]);
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

            it('should return 404 when updateGroupByJoinCode returns null', async () => {
                const joinCode = 'test-code';
                const joinData = {
                    joinCode: joinCode,
                    expectedPeople: 5,
                    groupMemberIds: [{ id: 'user-1', name: 'User', email: 'user@example.com' }],
                };

                // Mock findByJoinCode to return a group (so first check passes)
                jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce({
                    joinCode,
                    groupName: 'Test Group',
                    groupMemberIds: []
                } as any);

                // Mock updateGroupByJoinCode to return null
                jest.spyOn(groupModel, 'updateGroupByJoinCode').mockResolvedValueOnce(null);

                const res = await request(app).post('/group/join').send(joinData);

                expect(res.status).toBe(404);
                expect(res.body).toHaveProperty('message', 'Group not found');
                expect(groupModel.updateGroupByJoinCode).toHaveBeenCalledTimes(1);
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

        // it('should return 500 if WebSocket service is unavailable', async () => {
        //     const joinCode = 'test123';
        //     const message = 'Test notification';

        //     const mockWebSocketService = getWebSocketService();
        //     jest.spyOn(mockWebSocketService, 'notifyGroupUpdate').mockImplementationOnce(() => {
        //         throw new Error('WebSocket service not available');
        //     });

        //     const res = await request(app)
        //         .post(`/group/test-websocket/${joinCode}`)
        //         .send({ message });

        //     expect(res.status).toBe(500);
        //     expect(res.body).toHaveProperty('message', 'WebSocket service not available');
        // });

        // it('should update the group even if WebSocket service is unavailable', async () => {
        //     const exampleMeetingTime = "2026-11-02T12:30:00Z";
        //     const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        //     const exampleGroupLeader = {
        //         id: "leader-id",
        //         name: "Leader",
        //         email: "leader@example.com",
        //     };

        //     // Create a group
        //     const testGroup = await groupModel.create({
        //         joinCode: exampleJoinCode,
        //         groupName: "Joinable Group",
        //         groupLeaderId: exampleGroupLeader,
        //         expectedPeople: 5,
        //         groupMemberIds: [exampleGroupLeader],
        //         meetingTime: exampleMeetingTime,
        //         activityType: "CAFE",
        //     });
        //     const mockWebSocketService = getWebSocketService();
        //     jest.spyOn(mockWebSocketService, 'notifyGroupJoin').mockImplementationOnce(() => {
        //         throw new Error('WebSocket service unavailable');
        //     });

        //     const joinData = {
        //         joinCode: exampleJoinCode,
        //         groupMemberIds: [{ id: "user-id", name: "User", email: "user@example.com" }],
        //     };

        //     const res = await request(app).post('/group/join').send(joinData);

        //     expect(res.status).toBe(200);
        //     expect(res.body).toHaveProperty('message', 'Group info updated successfully');
        //     expect(res.body.data.group.groupMemberIds).toEqual(
        //         expect.arrayContaining([
        //         expect.objectContaining({
        //             id: "user-id",
        //             name: "User",
        //             email: "user@example.com",
        //         }),
        //         ])
        //     );
        //     });
    });

   describe('GET /group/:joinCode/midpoint', () => {
  // Branch 4: Error in location service
  it('should handle location service errors', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: "leader-id",
      name: "Leader",
      email: "leader@example.com",
      address: { formatted: 'Address 1', lat: 49.28, lng: -123.12 },
      transitType: 'transit' as const,
    };

    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 1,
      groupMemberIds: [],
      meetingTime: "2026-11-02T12:30:00Z",
      activityType: 'CAFE',
    });

    jest.spyOn(locationService, 'findOptimalMeetingPoint').mockRejectedValueOnce(
      new Error('Location service error')
    );

    const res = await request(app).get(`/group/${exampleJoinCode}/midpoint`);

    expect(res.status).toBe(500);
  });
});
describe('POST /group/:joinCode/midpoint/update', () => {
// Branch 4: Error in location service
  it('should handle location service errors', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: "leader-id",
      name: "Leader",
      email: "leader@example.com",
      address: { formatted: 'Address 1', lat: 49.28, lng: -123.12 },
      transitType: 'transit' as const,
    };

    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 1,
      groupMemberIds: [],
      meetingTime: "2026-11-02T12:30:00Z",
      activityType: 'CAFE',
    });

    jest.spyOn(locationService, 'findOptimalMeetingPoint').mockRejectedValueOnce(
      new Error('Location service error')
    );

    const res = await request(app).post(`/group/${exampleJoinCode}/midpoint/update`);

    expect(res.status).toBe(500);
  });
});

describe('POST /group/update', () => {
  it('should return 500 when updateGroupByJoinCode throws an Error', async () => {
    const exampleJoinCode = 'test-join-code';
    const updateData = {
      joinCode: exampleJoinCode,
      expectedPeople: 7,
      groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
      meetingTime: "2026-11-02T12:30:00Z"
    };

    const errorMessage = 'Database connection failed';
    jest.spyOn(groupModel, 'updateGroupByJoinCode').mockRejectedValueOnce(new Error(errorMessage));

    const res = await request(app).post('/group/update').send(updateData);

    expect(res.status).toBe(500);
    expect(res.body).toHaveProperty('message', errorMessage);
  });
});

describe('POST /group/leave/:joinCode', () => {
  it('should return 500 when leaveGroup throws an Error', async () => {
    const exampleMeetingTime = "2026-11-02T12:30:00Z";
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: "leader-id",
      name: "Leader",
      email: "leader@example.com"
    };

    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 2,
      groupMemberIds: [exampleGroupLeader],
      meetingTime: exampleMeetingTime,
      activityType: 'CAFE',
    });

    const errorMessage = 'Database error occurred';
    jest.spyOn(groupModel, 'leaveGroup').mockRejectedValueOnce(new Error(errorMessage));

    const leaveData = {
      userId: exampleGroupLeader.id
    };

    const res = await request(app).post(`/group/leave/${exampleJoinCode}`).send(leaveData);

    expect(res.status).toBe(500);
    expect(res.body).toHaveProperty('message', errorMessage);
  });
});

describe('POST /group/:joinCode/midpoint/update (Mocked)', () => {
  // Mocked behavior: updateGroupByJoinCode returns null
  // Input: valid group with members that have address and transitType
  // Expected status code: 500
  // Expected behavior: error is returned when midpoint update fails
  // Expected output: "Failed to update group midpoint" message
  it('should return 500 when updateGroupByJoinCode returns null', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: 'leader-id',
      name: 'Leader',
      email: 'leader@example.com',
      address: { formatted: 'Address 1', lat: 49.28, lng: -123.12 },
      transitType: 'transit' as const,
    };

    // Create a real group
    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 1,
      groupMemberIds: [exampleGroupLeader],
      meetingTime: "2026-11-02T12:30:00Z",
      activityType: 'CAFE',
    });

    // Mock updateGroupByJoinCode to return null
    jest.spyOn(groupModel, 'updateGroupByJoinCode').mockResolvedValueOnce(null);

    const res = await request(app).post(`/group/${exampleJoinCode}/midpoint/update`);

    expect(res.status).toBe(500);
    expect(res.body).toHaveProperty('message', 'Failed to update group midpoint');
    expect(groupModel.updateGroupByJoinCode).toHaveBeenCalledTimes(1);
  });
});

// Add to mocked tests

describe('GET /group/:joinCode/midpoint (Mocked)', () => {
  // Mocked behavior: updateGroupByJoinCode returns null
  // Input: valid group with members that have address and transitType, no cached midpoint
  // Expected status code: 500
  // Expected behavior: error is returned when midpoint update fails
  // Expected output: "Failed to update group midpoint" message
  it('should return 500 when updateGroupByJoinCode returns null', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: 'leader-id',
      name: 'Leader',
      email: 'leader@example.com',
      address: { formatted: 'Address 1', lat: 49.28, lng: -123.12 },
      transitType: 'transit' as const,
    };

    // Create a real group without a cached midpoint
    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 1,
      groupMemberIds: [exampleGroupLeader],
      meetingTime: "2026-11-02T12:30:00Z",
      activityType: 'CAFE',
    });

    // Mock updateGroupByJoinCode to return null
    jest.spyOn(groupModel, 'updateGroupByJoinCode').mockResolvedValueOnce(null);

    const res = await request(app).get(`/group/${exampleJoinCode}/midpoint`);

    expect(res.status).toBe(500);
    expect(res.body).toHaveProperty('message', 'Failed to update group midpoint');
    expect(groupModel.updateGroupByJoinCode).toHaveBeenCalledTimes(1);
  });
});

});

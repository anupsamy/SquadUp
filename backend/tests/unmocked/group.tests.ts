import request from 'supertest';
import express, { Express, Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { GroupController } from '../../src/controllers/group.controller';
import { getWebSocketService } from '../../src/services/websocket.service';
import { groupModel } from '../../src/group.model';
import { locationService } from '@/services/location.service';


jest.mock('../../src/utils/logger.util');
jest.mock('../../src/services/media.service');

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
        }
        const exampleMeetingTime = "2026-11-02T12:30:00Z"
        const exampleActivityType = "CAFE"
        const invalidGroupData = {
            groupName: "TestGroup1",
            groupLeaderId: exampleGroupLeader,
            expectedPeople: 1,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime,  // Default to current time for now,
            activityType: exampleActivityType
        };

        await expect(groupModel.create(invalidGroupData as any)).rejects.toThrow(
        'Invalid update data'
        );
    });

    // Input: group data missing required groupLeaderId field
    // Expected behavior: throws validation error
    // Expected output: error message about invalid data
    it('should throw error when creating group with missing group leader', async () => {
        const exampleMeetingTime = "2026-11-02T12:30:00Z"
        const exampleActivityType = "CAFE"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const invalidGroupData = {
            joinCode: exampleJoinCode,
            groupName: "TestGroup",
            expectedPeople: 1,
            groupMemberIds: [],
            meetingTime: exampleMeetingTime,  // Default to current time for now,
            activityType: exampleActivityType
        };

        await expect(groupModel.create(invalidGroupData as any)).rejects.toThrow(
        'Invalid update data'
        );
    });

    // Input: group data missing required expectedPeople field
    // Expected behavior: throws validation error
    // Expected output: error message about invalid data
    it('should throw error when creating group with expected people missing', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleMeetingTime = "2026-11-02T12:30:00Z"
        const exampleActivityType = "CAFE"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const invalidGroupData = {
            joinCode: exampleJoinCode,
            groupName: "TestGroup1",
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime,  // Default to current time for now,
            activityType: exampleActivityType
        };

        await expect(groupModel.create(invalidGroupData as any)).rejects.toThrow(
        'Invalid update data'
        );
    });

    // Input: group data missing required meetingTime field
    // Expected behavior: throws validation error
    // Expected output: error message about invalid data
    it('should throw error when creating group with meeting time missing', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleActivityType = "CAFE"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const invalidGroupData = {
            joinCode: exampleJoinCode,
            groupName: "TestGroup1",
            expectedPeople: 1,
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            activityType: exampleActivityType
        };

        await expect(groupModel.create(invalidGroupData as any)).rejects.toThrow(
        'Invalid update data'
        );
    });

    // Input: group data missing required activityType field
    // Expected behavior: throws validation error
    // Expected output: error message about invalid data
    it('should throw error when creating group with activity type missing', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleMeetingTime = "2026-11-02T12:30:00Z"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const invalidGroupData = {
            joinCode: exampleJoinCode,
            groupName: "TestGroup1",
            expectedPeople: 1,
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime, 
        };

        await expect(groupModel.create(invalidGroupData as any)).rejects.toThrow(
        'Invalid update data'
        );
    });

    // Input: group data with duplicate joinCode
    // Expected behavior: throws validation error
    // Expected output: error message about invalid data
    it('should throw error when creating group with duplicate join code', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleActivityType_1 = "CAFE"
        const exampleActivityType_2 = "BAR"
        const exampleMeetingTime_1 = "2026-11-02T12:30:00Z"
        const exampleMeetingTime_2 = "2026-11-03T12:30:00Z"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const exampleGroupData = {
            joinCode: exampleJoinCode,
            groupName: "TestGroup1",
            expectedPeople: 1,
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime_1, 
            activityType: exampleActivityType_1
        };

        await groupModel.create(exampleGroupData as any);

        const invalidGroupData = {
            joinCode: exampleJoinCode, //duplicate joinCode (shouldn't be allowed)
            groupName: "TestGroup2",
            expectedPeople: 2,
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime_2, 
            activityType: exampleActivityType_2
        };

        await expect(groupModel.create(invalidGroupData as any)).rejects.toThrow(
            'Failed to update group'
        );
    });
});
    describe('GroupModel.findAll', () => {

        it('should return both created groups', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleActivityType_1 = "CAFE"
        const exampleActivityType_2 = "BAR"
        const exampleMeetingTime_1 = "2026-11-02T12:30:00Z"
        const exampleMeetingTime_2 = "2026-11-03T12:30:00Z"
        const exampleJoinCode_1 = Math.random().toString(36).slice(2, 8);
        const exampleJoinCode_2 = Math.random().toString(36).slice(2, 8);
        const exampleGroupData_1 = {
            joinCode: exampleJoinCode_1,
            groupName: "TestGroup1",
            expectedPeople: 1,
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime_1, 
            activityType: exampleActivityType_1
        };

        const exampleGroupData_2 = {
            joinCode: exampleJoinCode_2, //duplicate joinCode (shouldn't be allowed)
            groupName: "TestGroup2",
            expectedPeople: 2,
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime_2, 
            activityType: exampleActivityType_2
        };
        await groupModel.create(exampleGroupData_1 as any);
        await groupModel.create(exampleGroupData_2 as any);

        const groups = await groupModel.findAll();
        expect(groups).toEqual(
            expect.arrayContaining([
                expect.objectContaining({
                joinCode: exampleGroupData_1.joinCode,
                groupName: exampleGroupData_1.groupName,
                expectedPeople: exampleGroupData_1.expectedPeople,
                meetingTime: exampleGroupData_1.meetingTime,
                activityType: exampleGroupData_1.activityType,
                groupLeaderId: expect.objectContaining({
                    id: exampleGroupLeader.id,
                    name: exampleGroupLeader.name,
                    email: exampleGroupLeader.email
                }),
                groupMemberIds: expect.arrayContaining([
                    expect.objectContaining({
                    id: exampleGroupLeader.id,
                    name: exampleGroupLeader.name,
                    email: exampleGroupLeader.email
                    })
                ])
                }),
                expect.objectContaining({
                joinCode: exampleGroupData_2.joinCode,
                groupName: exampleGroupData_2.groupName,
                expectedPeople: exampleGroupData_2.expectedPeople,
                meetingTime: exampleGroupData_2.meetingTime,
                activityType: exampleGroupData_2.activityType,
                groupLeaderId: expect.objectContaining({
                    id: exampleGroupLeader.id,
                    name: exampleGroupLeader.name,
                    email: exampleGroupLeader.email
                }),
                groupMemberIds: expect.arrayContaining([
                    expect.objectContaining({
                    id: exampleGroupLeader.id,
                    name: exampleGroupLeader.name,
                    email: exampleGroupLeader.email
                    })
                ])
                })
            ])
            );
        });
    });

    describe('GroupModel.findByJoinCode', () => {
        it('should return the created group', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleActivityType = "CAFE"
        const exampleMeetingTime = "2026-11-02T12:30:00Z"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const exampleGroupData = {
            joinCode: exampleJoinCode,
            groupName: "TestGroup1",
            expectedPeople: 1,
            groupLeaderId: exampleGroupLeader,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime, 
            activityType: exampleActivityType
        };
        await groupModel.create(exampleGroupData as any);
        const returnGroup = await groupModel.findByJoinCode(exampleJoinCode);
        expect(returnGroup).toHaveProperty('joinCode', exampleJoinCode)
        });

        it('should return null when joinCode does not exist', async () => {
            const result = await groupModel.findByJoinCode('nonexistent123');
            expect(result).toBeNull();
        });
    });

    describe('GroupModel.updateGroupByJoinCode', () => {
        it('should fail to update non-existent group', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleActivityType = "CAFE"
        const exampleMeetingTime = "2026-11-02T12:30:00Z"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const exampleGroupData = {
            joinCode: exampleJoinCode,
            expectedPeople: 5,
        };
        await expect(groupModel.updateGroupByJoinCode(exampleJoinCode, exampleGroupData)).rejects.toThrow(
        'Invalid update data'
        );
        });
        it('should update group successfully', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: 'Original Name',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: exampleMeetingTime,
                activityType: 'CAFE',
            });

            const updatedGroup = await groupModel.updateGroupByJoinCode(exampleJoinCode, { expectedPeople: 7 });
            expect(updatedGroup).toBeDefined();
            expect(updatedGroup?.groupName).toBe('Updated Name');
        });
    });

    describe('GroupModel.delete', () => {
        it('should fail to delete non-existent group', async () => {
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);

            await expect(groupModel.delete(exampleJoinCode)).rejects.toThrow(
                'Failed to delete group'
            );
        });
        it('should delete group successfully', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: 'Group To Delete',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: exampleMeetingTime,
                activityType: 'CAFE',
            });

            await expect(groupModel.delete(exampleJoinCode)).resolves.not.toThrow();

            const deletedGroup = await groupModel.findByJoinCode('delete123');
            expect(deletedGroup).toBeNull();
        });
    });

    describe('GroupModel.leaveGroup', () => {
        it('should allow a user to leave a group successfully', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: 'Group To Leave',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [
                    { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                    { id: 'user-id', name: 'User', email: 'user@example.com' },
                ],
                meetingTime: exampleMeetingTime,
                activityType: 'CAFE',
            });

            await groupModel.leaveGroup(exampleJoinCode, 'user-id');

            const updatedGroup = await groupModel.findByJoinCode(exampleJoinCode);
            expect(updatedGroup?.groupMemberIds).toHaveLength(1);
            expect(updatedGroup?.groupMemberIds).not.toEqual(
                expect.arrayContaining([{ id: 'user-id', name: 'User', email: 'user@example.com' }])
            );
        });

        it('should throw an error when attempting to leave a non-existent group', async () => {
            await expect(groupModel.leaveGroup('nonexistent123', 'user-id')).rejects.toThrow(
                'Failed to leave group'
            );
        });
    });

});

describe('Unmocked: Group Controller', () => {
  let app: Express;
  let groupController: GroupController;

  beforeAll(async () => {
    app = express();
    app.use(express.json());
    groupController = new GroupController();
    const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
    }
    // Middleware to attach user to requests
    app.use((req: Request, res: Response, next: NextFunction) => {
      if (!req.user) {
        req.user = {
          _id: new mongoose.Types.ObjectId(),
          googleId: 'google-id',
          email: 'test@example.com',
          name: 'Test User',
        } as any;
      }
      next();
    });

    app.post('/group/create', (req, res, next) => groupController.createGroup(req, res, next));
    app.get('/group/info',(req, res, next) => groupController.getAllGroups(req, res, next));
    app.get('/group/:joinCode',(req, res, next) => groupController.getGroupByJoinCode(req, res, next));
    app.post('/group/update', (req, res, next) => groupController.updateGroupByJoinCode(req, res, next));
    app.delete('/group/delete/:joinCode', (req, res, next) => groupController.deleteGroupByJoinCode(req, res, next));
    app.post('/group/join', (req, res, next) => groupController.joinGroupByJoinCode(req, res, next));
    app.post('/group/leave/:joinCode', (req, res, next) => groupController.leaveGroup(req, res, next));
    app.get('/group/:joinCode/midpoint',(req, res, next) => groupController.getMidpointByJoinCode(req, res, next));
    app.post('/group/:joinCode/midpoint/update', (req, res, next) => groupController.updateMidpointByJoinCode(req, res, next));


  });

  afterAll(async () => {
    await mongoose.connection.close();
  });

  describe('GET /group/info', () => {
  // Input: valid authenticated user
  // Expected status code: 200
  // Expected behavior: user profile is returned
  // Expected output: user data with all fields
  it('should return 200 and user profile when authenticated', async () => {
    const res = await request(app).get('/group/info');

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
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const testGroup = await groupModel.create({
            joinCode: exampleJoinCode,
            groupName: 'Test Group',
            groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
            expectedPeople: 5,
            groupMemberIds: [],
            meetingTime: exampleMeetingTime,
            activityType: 'CAFE',
        });

            const res = await request(app).get(`/group/${testGroup.joinCode}`);

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group fetched successfully');
            expect(res.body.data.group).toHaveProperty('joinCode', exampleJoinCode);
        });

        it('should return 404 for an invalid join code', async () => {
            const res = await request(app).get('/group/invalid123');

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', "Group with joinCode 'invalid123' not found");
        });
    });

    describe('POST /group/create', () => {
        it('should create a new group and return 201', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const groupData = {
            groupName: 'New Group',
            groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
            expectedPeople: 5,
            meetingTime: exampleMeetingTime,
            activityType: 'CAFE',
            };

            const res = await request(app).post('/group/create').send(groupData);

            expect(res.status).toBe(201);
            expect(res.body).toHaveProperty('message', `Group ${groupData.groupName} created successfully`);
            expect(res.body.data.group).toHaveProperty('groupName', groupData.groupName);
        });

        it('should return 400 for invalid group data', async () => {
            const invalidGroupData = {
            groupName: 'Invalid Group',
            };

            const res = await request(app).post('/group/create').send(invalidGroupData);

            expect(res.status).toBe(400);
            expect(res.body).toHaveProperty('message');
        });
    });

    describe('POST /group/join', () => {
        it('should join a group and return 200', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: 'Joinable Group',
                groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
                expectedPeople: 5,
                groupMemberIds: [],
                meetingTime: exampleMeetingTime,
                activityType: 'CAFE',
            });

            const joinData = {
            joinCode: exampleJoinCode,
            groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
            };

            const res = await request(app).post('/group/join').send(joinData);

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group info updated successfully');
            expect(res.body.data.group).toHaveProperty('groupMemberIds');
            expect(res.body.data.group.groupMemberIds)
                .toEqual(
                    expect.arrayContaining([
                        expect.objectContaining({
                        id: 'user-id',
                        name: 'User',
                        email: 'user@example.com',
                        }),
                    ])
                );
        });

        it('should return 404 for an invalid join code', async () => {
            const joinData = {
            joinCode: 'invalid123',
            groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
            };

            const res = await request(app).post('/group/join').send(joinData);

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');
        });
    });

    describe('POST /group/update', () => {
        it('should return "404 not found" when trying to update with invalid join code', async () => {
            const updateData = {
                joinCode: 'join123',
                groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
            };
            const res = await request(app).post('/group/update/:').send(updateData);

            expect(res.status).toBe(404);
            //expect(res.body).toHaveProperty('message', 'Group not found');

        });

        it('should return 200 after successful update', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            }
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: 'Joinable Group',
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 5,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
                activityType: 'CAFE',
            });

            const updateData = {
                joinCode: exampleJoinCode,
                expectedPeople: 7,
            };

            const res = await request(app).post('/group/update').send(updateData);
            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group info updated successfully');
            expect(res.body.data.group.expectedPeople).toEqual(7);
        });
    });

    describe('DELETE /group/delete/:joinCode', () => {
        it('should delete a group and return 200', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const testGroup = await groupModel.create({
            joinCode: exampleJoinCode,
            groupName: 'Deletable Group',
            groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
            expectedPeople: 5,
            groupMemberIds: [],
            meetingTime: exampleMeetingTime,
            activityType: 'CAFE',
            });

            const res = await request(app).delete(`/group/delete/${testGroup.joinCode}`);

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'group deleted successfully');
        });

        it('should return 404 for an invalid join code', async () => {
            const res = await request(app).delete('/group/delete/invalid123');

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');
        });
    });

    describe('POST /group/leave/:joinCode', () => {
        it('should delete a group and return 200', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            }
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: 'Joinable Group',
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 5,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
                activityType: 'CAFE',
            });
            const exampleNewGroupMember = {
                id: "group_user_test",
                name: "Group Member",
                email: "group.member@example.com"
            }
            const joinedGroup = await groupModel.updateGroupByJoinCode(
                exampleJoinCode,
                {
                    joinCode: exampleJoinCode,
                    groupMemberIds: [exampleGroupLeader, exampleNewGroupMember]
                }
            )
            const leaveData = {
                joinCode: exampleJoinCode,
                userId: exampleNewGroupMember.id
            };

            const res = await request(app).post(`/group/leave/${exampleJoinCode}`).send(leaveData);

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Left group successfully');
        });

        it('should return 404 for an invalid join code', async () => {
            const joinCode = 'abc123'
            const leaveData = {
                joinCode: joinCode,
                userId: "group_user_test"
            };
            const res = await request(app).post(`/group/leave/${joinCode}`).send(leaveData);

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');
        });

        it('should transfer leadership when the leader leaves', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const exampleGroupLeader = {
                id: "leader-id",
                name: "Leader",
                email: "leader@example.com",
            };
            const exampleMember = {
                id: "member-id",
                name: "Member",
                email: "member@example.com",
            };

            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: "Group With Leader",
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 5,
                groupMemberIds: [exampleGroupLeader, exampleMember],
                meetingTime: exampleMeetingTime,
                activityType: "CAFE",
            });

            const res = await request(app).post(`/group/leave/${exampleJoinCode}`).send({
                userId: exampleGroupLeader.id,
            });

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Left group successfully');
            expect(res.body.data.newLeader).toMatchObject(exampleMember);
        });

        it('should delete the group when the last member leaves', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z";
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const exampleGroupLeader = {
                id: "leader-id",
                name: "Leader",
                email: "leader@example.com",
            };

            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: "Group To Delete",
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 1,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
                activityType: "CAFE",
            });

            const res = await request(app).post(`/group/leave/${exampleJoinCode}`).send({
                userId: exampleGroupLeader.id,
            });

            expect(res.status).toBe(200);
            expect(res.body).toHaveProperty('message', 'Group deleted successfully as no members remain');
        });

    });

    describe('GET /group/:joinCode/midpoint', () => {
  // Branch 1: Group does not exist
  it('should return 404 when group does not exist', async () => {
    const res = await request(app).get('/group/nonexistent/midpoint');

    expect(res.status).toBe(404);
    expect(res.body.message).toContain('not found');
  });

  // Branch 2: Group has cached midpoint
  it('should return cached midpoint without recalculating', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: "leader-id",
      name: "Leader",
      email: "leader@example.com",
    };

    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 2,
      groupMemberIds: [],
      meetingTime: "2026-11-02T12:30:00Z",
      activityType: 'CAFE',
    });

    await groupModel.updateGroupByJoinCode(exampleJoinCode, {
      joinCode: exampleJoinCode,
      midpoint: '49.28 -123.12',
    });

    const res = await request(app).get(`/group/${exampleJoinCode}/midpoint`);

    expect(res.status).toBe(200);
    expect(res.body.data.midpoint.location).toEqual({ lat: 49.28, lng: -123.12 });
  });

  // Branch 3: Normal midpoint calculation
  it('should calculate midpoint for group without cached value', async () => {
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

    const res = await request(app).get(`/group/${exampleJoinCode}/midpoint`);

    expect(res.status).toBe(200);
    expect(res.body.data.midpoint.location).toBeDefined();
    expect(res.body.data.midpoint.location.lat).toBeDefined();
    expect(res.body.data.midpoint.location.lng).toBeDefined();
  });
});

describe('POST /group/:joinCode/midpoint/update', () => {
  // Branch 1: Group does not exist
  it('should return 404 when group does not exist', async () => {
    const res = await request(app).post('/group/nonexistent/midpoint/update');

    expect(res.status).toBe(404);
    expect(res.body.message).toContain('not found');
  });

  // Branch 2: Successfully update midpoint
  it('should update midpoint and return 200', async () => {
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

    const res = await request(app).post(`/group/${exampleJoinCode}/midpoint/update`);

    expect(res.status).toBe(200);
    expect(res.body.data.midpoint.location).toBeDefined();
    expect(res.body.data.midpoint.location.lat).toBeDefined();
    expect(res.body.data.midpoint.location.lng).toBeDefined();
  });

  // Branch 3: Group with no members with location info
  it('should handle group with no valid members', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: "leader-id",
      name: "Leader",
      email: "leader@example.com",
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

    const res = await request(app).post(`/group/${exampleJoinCode}/midpoint/update`);

    expect(res.status).toBe(500);
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
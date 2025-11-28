import request from 'supertest';
import express, { Express, Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { GroupController } from '../../src/controllers/group.controller';
import { getWebSocketService } from '../../src/services/websocket.service';
import { groupModel } from '../../src/group.model';
import { locationService } from '../../src/services/location.service';


jest.mock('../../src/utils/logger.util');
jest.mock('../../src/services/media.service');

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

  describe('POST /group/create', () => {

    it('should create a new group with valid info and return 201', async () => {
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

        const res = await request(app).post('/group/create').send(invalidGroupData);

        expect(res.status).toBe(404);
        expect(res.body).toHaveProperty('message', `Group with joinCode '' not found`);
        expect(res.body.data.group).toHaveProperty('groupName', invalidGroupData.groupName);
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

        const res = await request(app).post('/group/create').send(invalidGroupData);

        expect(res.status).toBe(404);
        expect(res.body).toHaveProperty('message', `Invalid update data`);
        expect(res.body.data.group).toHaveProperty('groupName', invalidGroupData.groupName);
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

        const res = await request(app).post('/group/create').send(invalidGroupData);

        expect(res.status).toBe(404);
        expect(res.body).toHaveProperty('message', `Invalid update data`);
        expect(res.body.data.group).toHaveProperty('groupName', invalidGroupData.groupName);
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

        const res = await request(app).post('/group/create').send(invalidGroupData);

        expect(res.status).toBe(404);
        expect(res.body).toHaveProperty('message', `Invalid update data`);
        expect(res.body.data.group).toHaveProperty('groupName', invalidGroupData.groupName);
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

        const res = await request(app).post('/group/create').send(invalidGroupData);

        expect(res.status).toBe(404);
        expect(res.body).toHaveProperty('message', `Invalid update data`);
        expect(res.body.data.group).toHaveProperty('groupName', invalidGroupData.groupName);
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

        const res = await request(app).post('/group/create').send(invalidGroupData);

        expect(res.status).toBe(404);
        expect(res.body).toHaveProperty('message', `Failed to update group`);
        expect(res.body.data.group).toHaveProperty('groupName', invalidGroupData.groupName);
    });
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

// Input: group data for two different groups
// Expected behavior: adds new groups, returns list of all groups containing the two new groups
// Expected output: list of all groups containing the two new groups
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
        const res1 = await request(app).post('/group/create').send(exampleGroupData_1);
        expect(res1.status).toBe(201);
        expect(res1.body).toHaveProperty('message', `Group ${exampleGroupData_1.groupName} created successfully`);
        expect(res1.body.data.group).toHaveProperty('groupName', exampleGroupData_1.groupName);

        const res2 = await request(app).post('/group/create').send(exampleGroupData_2);
        expect(res2.status).toBe(201);
        expect(res2.body).toHaveProperty('message', `Group ${exampleGroupData_2.groupName} created successfully`);
        expect(res2.body.data.group).toHaveProperty('groupName', exampleGroupData_2.groupName);

        const res = await request(app).get('/group/info');

        expect(res.status).toBe(200);
        expect(res.body).toHaveProperty('message', 'Groups fetched successfully');
        expect(res.body.data).toHaveProperty('groups');
        expect(Array.isArray(res.body.data.groups)).toBe(true);

        //TODO: Groups created previously are also returned. Check why this fails
        expect(res.body.data.groups).toEqual(
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
        // Input: invalid group join code
        // Expected behavior: fails to return group info 
        // Expected output:  404 error + message explaining join code is invalid 
        it('should return 404 for an invalid join code', async () => {
            const res = await request(app).get('/group/invalid123');

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', "Group with joinCode 'invalid123' not found");
        });
    });

    describe('POST /group/join', () => {
        // Input: data containing join code and user information
        // Expected behavior: succesfully joins group
        // Expected output: 200 OK + group information including user
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

        // Input: invalid join code
        // Expected behavior: fails to join group
        // Expected output: 404 error + message explaining group couldn't be found
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
        // Input: invalid join code, user information of new group member
        // Expected behavior: fails to update group
        // Expected output: 404 error + message saying group not found
        it('should return "404 not found" when trying to update with invalid join code', async () => {
            const updateData = {
                joinCode: 'join123',
                groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
            };
            const res = await request(app).post('/group/update/:').send(updateData);

            expect(res.status).toBe(404);
            expect(res.body).toHaveProperty('message', 'Group not found');

        });
        // Input: join code and integer of expected people
        // Expected behavior: succesfully updates group
        // Expected output: 200 OK + success message
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
        // Input: invalid update information including new group leader
        // Expected behavior: fails to update group
        // Expected output: 500 error + message saying failed to update group
        it('should return 500 after attempting to update group leader', async () => {
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

            const newGroupLeader = {
                id: "2jk3h24j3j42kh3j42_test",
                name: "Group Leader 2",
                email: "group.leader2@example.com"
            }

            const updateData = {
                groupLeaderId: newGroupLeader,
                joinCode: exampleJoinCode,
            };

            const res = await request(app).post('/group/update').send(updateData);
            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message', 'Failed to update group info');
            expect(res.body.data.group.groupLeaderId).toEqual(exampleGroupLeader);
        });
        // Input: invalid update information including new group name
        // Expected behavior: fails to update group
        // Expected output: 500 error + message saying failed to update group
        it('should return 500 after attempting to update group name', async () => {
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            }
            const groupName = 'Joinable Group'
            const testGroup = await groupModel.create({
                joinCode: exampleJoinCode,
                groupName: groupName,
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 5,
                groupMemberIds: [exampleGroupLeader],
                meetingTime: exampleMeetingTime,
                activityType: 'CAFE',
            });

            const newGroupName = "New Group Name"
            const updateData = {
                groupName: newGroupName,
                joinCode: exampleJoinCode,
            };

            const res = await request(app).post('/group/update').send(updateData);
            expect(res.status).toBe(500);
            expect(res.body).toHaveProperty('message', 'Failed to update group info');
            expect(res.body.data.group.groupName).toEqual(groupName);
        });
    });

    describe('DELETE /group/delete/:joinCode', () => {
        // Input: join code of existing group
        // Expected behavior: successfully deletes group
        // Expected output: 200 OK + success message
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
        // Input: join code and user information of leaving member
        // Expected behavior: user is able to successfully leave group
        // Expected output: 200 OK + success message
        it('should let user leave a group and return 200', async () => {
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
            expect(res.body.data.group.groupMemberIds).toEqual([exampleGroupLeader]);
        });

        // Input: invalid join code, example user information
        // Expected behavior: user is unable to leaveleave group
        // Expected output: 404 error + fail message
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

        // Input: join code and user information of group leader
        // Expected behavior: user is able to successfully leave group, new leader is assigned
        // Expected output: 200 OK + success message + group information reflects new leader
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

        // Input: join code and user information of last group member
        // Expected behavior: user is able to successfully leave group, group is deleted
        // Expected output: 200 OK + success message for leave, 404 error for get group info with join code
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
            expect(res.body).toHaveProperty('message', 'Left group successfully');

            //Check that group was deleted
            const res1 = await request(app).post(`/group/${exampleJoinCode}`);
            expect(res1.status).toBe(404);
            expect(res.body).toHaveProperty('message', `Group with joinCode ${exampleJoinCode} not found`);
        });

    });

    describe('GET /group/:joinCode/midpoint', () => {
  // Branch 1: Group does not exist
        // Input: invalid join code
        // Expected behavior: fails to return midpoint
        // Expected output: 404 error + fail message
        it('should return 404 when group does not exist', async () => {
        const res = await request(app).get('/group/nonexistent/midpoint');

        expect(res.status).toBe(404);
        expect(res.body.message).toContain('not found');
    });

  // Branch 2: Group has cached midpoint
    // Input: join code and midpoint info to save
    // Expected behavior: saved midpoint info is returned without recalculation
    // Expected output: 200 OK + success message + group information reflecting midpoint
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
    // Input: join code
    // Expected behavior: new midpoint is calculated for new group
    // Expected output: 200 OK + success message + group information containing new midpoint
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

    // Input: new group information with addresses and transit types, join code
    // Expected behavior: midpoint is calculated using address and transit types
    // Expected output: 200 OK + success message + group info contains calculated values
  it('should calculate midpoint with members that have address and transitType', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: "leader-id",
      name: "Leader",
      email: "leader@example.com",
      address: { formatted: 'Address 1', lat: 49.28, lng: -123.12 },
      transitType: 'transit' as const,
    };
    const exampleMember = {
      id: "member-id",
      name: "Member",
      email: "member@example.com",
      address: { formatted: 'Address 2', lat: 49.29, lng: -123.13 },
      transitType: 'driving' as const,
    };

    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 2,
      groupMemberIds: [exampleGroupLeader, exampleMember],
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
    // Input: invalid join code (group doesn't exist)
    // Expected behavior: fails to recalculate midpoint
    // Expected output: 404 error + fail message
  it('should return 404 when group does not exist', async () => {
    const res = await request(app).post('/group/nonexistent/midpoint/update');

    expect(res.status).toBe(404);
    expect(res.body.message).toContain('not found');
  });

  // Branch 2: Successfully update midpoint
    // Input: join code of existing group
    // Expected behavior: midpoint is successfully recalculated
    // Expected output: 200 OK + success message + group info contains calculated values
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
    // Input: join code of existing group with no members
    // Expected behavior: fails to recalculate midpoint
    // Expected output: 500 error
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

    // Input: join code of existing group with valid member information (address and transit types)
    // Expected behavior: midpoint is successfully recalculated
    // Expected output: 200 OK + success message + group info contains calculated values
  it('should update midpoint with members that have address and transitType', async () => {
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleGroupLeader = {
      id: "leader-id",
      name: "Leader",
      email: "leader@example.com",
      address: { formatted: 'Address 1', lat: 49.28, lng: -123.12 },
      transitType: 'transit' as const,
    };
    const exampleMember = {
      id: "member-id",
      name: "Member",
      email: "member@example.com",
      address: { formatted: 'Address 2', lat: 49.29, lng: -123.13 },
      transitType: 'driving' as const,
    };

    await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: 'Test Group',
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 2,
      groupMemberIds: [exampleGroupLeader, exampleMember],
      meetingTime: "2026-11-02T12:30:00Z",
      activityType: 'CAFE',
    });

    const res = await request(app).post(`/group/${exampleJoinCode}/midpoint/update`);

    expect(res.status).toBe(200);
    expect(res.body.data.midpoint.location).toBeDefined();
    expect(res.body.data.midpoint.location.lat).toBeDefined();
    expect(res.body.data.midpoint.location.lng).toBeDefined();
  });

});

describe('POST /group/update', () => {
    // Input: invalid join code
    // Expected behavior: group is not found nor updated
    // Expected output: 404 error + fail message
  it('should return 404 when group does not exist', async () => {
    const nonexistentJoinCode = 'nonexistent123';
    const updateData = {
      joinCode: nonexistentJoinCode,
      expectedPeople: 5,
      groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
      meetingTime: "2026-11-02T12:30:00Z"
    };

    const res = await request(app).post('/group/update').send(updateData);

    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('message', 'Group not found');
  });
});

// Add to unmocked tests (in a new describe block or extend existing selectActivity tests)

describe('POST /group/select-activity', () => {
  // Input: joinCode is not a string (e.g., number, object)
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Join code must be a string" message
  it('should return 400 when joinCode is not a string', async () => {
    const activityData = {
      joinCode: 12345, // number instead of string
      activity: {
        placeId: 'place123',
        name: 'Test Activity',
      },
    };

    const res = await request(app).post('/group/select-activity').send(activityData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Join code must be a string');
    expect(res.body).toHaveProperty('error', 'ValidationError');
  });

  // Input: joinCode is an object
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Join code must be a string" message
  it('should return 400 when joinCode is an object', async () => {
    const activityData = {
      joinCode: { code: 'group123' }, // object instead of string
      activity: {
        placeId: 'place123',
        name: 'Test Activity',
      },
    };

    const res = await request(app).post('/group/select-activity').send(activityData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Join code must be a string');
    expect(res.body).toHaveProperty('error', 'ValidationError');
  });
});

describe('POST /group/:joinCode/midpoint/update', () => {
  // Input: joinCode route parameter is not a string (e.g., passed as object or undefined)
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Invalid joinCode" message with ValidationError
  it('should return 400 when joinCode is invalid', async () => {
    // Note: Express route params are always strings, so we test by passing empty string
    // or by testing the validation logic directly
    const res = await request(app).post('/group//midpoint/update');

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Invalid joinCode');
    expect(res.body).toHaveProperty('error', 'ValidationError');
  });
});

describe('POST /group/update', () => {
  // Input: joinCode in body is not a string (e.g., number, object)
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Invalid joinCode" message
  it('should return 400 when joinCode is not a string', async () => {
    const updateData = {
      joinCode: 12345, // number instead of string
      expectedPeople: 5,
      groupMemberIds: [],
      meetingTime: "2026-11-02T12:30:00Z",
    };

    const res = await request(app).post('/group/update').send(updateData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Invalid joinCode');
  });

  // Input: joinCode in body is an object
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Invalid joinCode" message
  it('should return 400 when joinCode is an object', async () => {
    const updateData = {
      joinCode: { code: 'group123' }, // object instead of string
      expectedPeople: 5,
      groupMemberIds: [],
      meetingTime: "2026-11-02T12:30:00Z",
    };

    const res = await request(app).post('/group/update').send(updateData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Invalid joinCode');
  });

  // Input: joinCode in body is an empty string
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Invalid joinCode" message
  it('should return 400 when joinCode is empty string', async () => {
    const updateData = {
      joinCode: '', // empty string
      expectedPeople: 5,
      groupMemberIds: [],
      meetingTime: "2026-11-02T12:30:00Z",
    };

    const res = await request(app).post('/group/update').send(updateData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Invalid joinCode');
  });
});

describe('POST /group/join', () => {
  // Input: joinCode is missing from request body
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Join code is required and must be a string" message
  it('should return 400 when joinCode is missing', async () => {
    const joinData = {
      expectedPeople: 5,
      groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
    };

    const res = await request(app).post('/group/join').send(joinData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Join code is required and must be a string');
  });

  // Input: joinCode is null
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Join code is required and must be a string" message
  it('should return 400 when joinCode is null', async () => {
    const joinData = {
      joinCode: null,
      expectedPeople: 5,
      groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
    };

    const res = await request(app).post('/group/join').send(joinData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Join code is required and must be a string');
  });

  // Input: joinCode is not a string (number)
  // Expected status code: 400
  // Expected behavior: validation error returned
  // Expected output: "Join code is required and must be a string" message
  it('should return 400 when joinCode is not a string', async () => {
    const joinData = {
      joinCode: 12345, // number instead of string
      expectedPeople: 5,
      groupMemberIds: [{ id: 'user-id', name: 'User', email: 'user@example.com' }],
    };

    const res = await request(app).post('/group/join').send(joinData);

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Join code is required and must be a string');
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
import request from 'supertest';
import express, { Express, Request, Response, NextFunction } from 'express';
import mongoose from 'mongoose';
import { GroupController } from '../../src/controllers/group.controller';
import { groupModel } from '../../src/group.model';


jest.mock('../../src/utils/logger.util');
jest.mock('../../src/services/media.service');

describe('Unmocked: Group Model', () => {
    describe('GroupModel.getActivities', () => {
        it('should throw error for invalid join code', async () => {
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            await expect(groupModel.getActivities(exampleJoinCode)).rejects.toThrow(
                `Failed to get activities`
            );
        });

        it('should return activities upon success', async () => {
            const exampleGroupLeader = {
                id: "68fbe599d84728c6da2_test",
                name: "Group Leader",
                email: "group.leader@example.com"
            }
            const exampleMeetingTime = "2026-11-02T12:30:00Z"
            const exampleActivityType = "CAFE"
            const exampleJoinCode = Math.random().toString(36).slice(2, 8);
            const newGroupData = {
                joinCode: exampleJoinCode,
                groupName: "TestGroup1",
                meetingTime: exampleMeetingTime,
                groupLeaderId: exampleGroupLeader,
                expectedPeople: 5,
                groupMemberIds: [exampleGroupLeader],
                activityType: exampleActivityType
            };
            const newGroup = await groupModel.create(newGroupData)
            await groupModel.updateGroupByJoinCode(exampleJoinCode, {joinCode: exampleJoinCode, midpoint: "some midpoint"})

            const activities = await groupModel.getActivities(exampleJoinCode);
            expect(activities).toHaveLength(3)
            expect(activities[0]).toHaveProperty('name', "Sushi Palace one")
            expect(activities[1]).toHaveProperty('name', "Pizza Garden two")
            expect(activities[2]).toHaveProperty('name', "Brew Bros Coffee three")
            
        });
    });


  describe('GroupModel.updateSelectedActivity', () => {
    it('should throw error for invalid join code', async () => {
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const exampleActivity = {
          name: "Killer Ice Cream",
          placeId: "KJDSfjuwir_dnsjdkafnJNKn",
          address: "3659 W 4th Ave, Vancouver",
          rating: 4.5,
          userRatingsTotal: 292,
          priceLevel: 2,
          type: "cafe",
          latitude: 49.2725,
          longitude: -123.1840,
          businessStatus: "OPERATIONAL",
          isOpenNow: true
        }
        await expect(groupModel.updateSelectedActivity(exampleJoinCode, exampleActivity)).rejects.toThrow(
            `Failed to update selected activity`
        );
    });
    it('should throw error for invalid activity (no place ID)', async () => {
        const exampleGroupLeader = {
            id: "68fbe599d84728c6da2_test",
            name: "Group Leader",
            email: "group.leader@example.com"
        }
        const exampleMeetingTime = "2026-11-02T12:30:00Z"
        const exampleActivityType = "CAFE"
        const exampleJoinCode = Math.random().toString(36).slice(2, 8);
        const newGroupData = {
            joinCode: exampleJoinCode,
            groupName: "TestGroup1",
            groupLeaderId: exampleGroupLeader,
            expectedPeople: 1,
            groupMemberIds: [exampleGroupLeader],
            meetingTime: exampleMeetingTime,  // Default to current time for now,
            activityType: exampleActivityType
        };
        const newGroup = await groupModel.create(newGroupData)
        const invalidActivity = {
          name: "Killer Ice Cream",
          address: "3659 W 4th Ave, Vancouver",
          rating: 4.5,
          userRatingsTotal: 292,
          priceLevel: 2,
          type: "cafe",
          latitude: 49.2725,
          longitude: -123.1840,
          businessStatus: "OPERATIONAL",
          isOpenNow: true
        }
        await expect(groupModel.updateSelectedActivity(exampleJoinCode, invalidActivity as any)).rejects.toThrow(
            'Invalid activity data'
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

    app.get('/group/activities',(req, res, next) => groupController.getActivities(req, res));
    app.post('/group/activities/select', (req, res, next) => groupController.selectActivity(req, res));
    
  });

  afterAll(async () => {
    await mongoose.connection.close();
  });

  describe('GET /group/activities', () => {
  it('should return 200 and a list of activities for a valid group', async () => {
    const exampleGroupLeader = {
      id: "68fbe599d84728c6da2_test",
      name: "Group Leader",
      email: "group.leader@example.com"
    };
    const exampleMeetingTime = "2026-11-02T12:30:00Z";
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);
    const exampleMidpoint = "49.2827 -123.1207";
    const exampleGroupData = {
      joinCode: exampleJoinCode,
      groupName: "Group With Activities",
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 5,
      groupMemberIds: [exampleGroupLeader],
      meetingTime: exampleMeetingTime,
      activityType: "CAFE",
      midpoint: "some midpoint"
    }
    // Create a group with a midpoint
    const testGroup = await groupModel.create(exampleGroupData);

    const res = await request(app).get(`/group/activities?joinCode=${exampleJoinCode}`);

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Fetched activities successfully');
    expect(res.body.data).toBeInstanceOf(Array);
    expect(res.body.data).toHaveLength(3); // Assuming the default activities are returned
    expect(res.body.data[0]).toHaveProperty('name');
  });

  it('should return 404 if the group does not exist', async () => {
    const res = await request(app).get('/group/activities?joinCode=nonexistent123');

    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('message', 'Group not found');
  });

  it('should return 404 if the group does not have a midpoint', async () => {
    const exampleGroupLeader = {
      id: "68fbe599d84728c6da2_test",
      name: "Group Leader",
      email: "group.leader@example.com"
    };
    const exampleMeetingTime = "2026-11-02T12:30:00Z";
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);

    // Create a group without a midpoint
    const testGroup = await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: "Group Without Midpoint",
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 5,
      groupMemberIds: [exampleGroupLeader],
      meetingTime: exampleMeetingTime,
      activityType: "CAFE",
    });

    const res = await request(app).get(`/group/activities?joinCode=${exampleJoinCode}`);

    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('message', 'No midpoint available for this group');
  });
});

describe('POST /group/activities/select', () => {
  it('should return 200 and update the selected activity for a valid group', async () => {
    const exampleGroupLeader = {
      id: "68fbe599d84728c6da2_test",
      name: "Group Leader",
      email: "group.leader@example.com"
    };
    const exampleMeetingTime = "2026-11-02T12:30:00Z";
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);

    // Create a group
    const testGroup = await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: "Group For Activity Selection",
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 5,
      groupMemberIds: [exampleGroupLeader],
      meetingTime: exampleMeetingTime,
      activityType: "CAFE",
    });

    const activity = {
      name: "Selected Activity",
      placeId: "place123",
      address: "123 Main St, Vancouver",
      rating: 4.5,
      userRatingsTotal: 100,
      priceLevel: 2,
      type: "cafe",
      latitude: 49.2827,
      longitude: -123.1207,
      businessStatus: "OPERATIONAL",
      isOpenNow: true,
    };

    const res = await request(app)
      .post('/group/activities/select')
      .send({ joinCode: exampleJoinCode, activity });

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Activity selected successfully');
    expect(res.body.data).toHaveProperty('selectedActivity');
    expect(res.body.data.selectedActivity).toMatchObject(activity);
  });

  it('should return 404 if the group does not exist', async () => {
    const activity = {
      name: "Selected Activity",
      placeId: "place123",
      address: "123 Main St, Vancouver",
      rating: 4.5,
      userRatingsTotal: 100,
      priceLevel: 2,
      type: "cafe",
      latitude: 49.2827,
      longitude: -123.1207,
      businessStatus: "OPERATIONAL",
      isOpenNow: true,
    };
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);

    const res = await request(app)
      .post('/group/activities/select')
      .send({ joinCode: exampleJoinCode, activity });

    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('message', 'Group not found');
  });

  it('should return 400 if the activity is invalid', async () => {
    const exampleGroupLeader = {
      id: "68fbe599d84728c6da2_test",
      name: "Group Leader",
      email: "group.leader@example.com"
    };
    const exampleMeetingTime = "2026-11-02T12:30:00Z";
    const exampleJoinCode = Math.random().toString(36).slice(2, 8);

    // Create a group
    const testGroup = await groupModel.create({
      joinCode: exampleJoinCode,
      groupName: "Group For Invalid Activity",
      groupLeaderId: exampleGroupLeader,
      expectedPeople: 5,
      groupMemberIds: [exampleGroupLeader],
      meetingTime: exampleMeetingTime,
      activityType: "CAFE",
    });

    const invalidActivity = {
      name: "Invalid Activity",
      address: "123 Main St, Vancouver",
    };

    const res = await request(app)
      .post('/group/activities/select')
      .send({ joinCode: exampleJoinCode, activity: invalidActivity });

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Activity must have placeId and name');
  });
});
  
});
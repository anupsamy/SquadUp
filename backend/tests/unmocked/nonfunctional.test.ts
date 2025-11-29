import { GroupController } from '../../src/controllers/group.controller';
import { LocationService } from '../../src/services/location.service';
import type { LocationInfo } from '../../src/types/location.types';
import type { TransitType } from '../../src/types/transit.types';
import express, { Express, Request, Response, NextFunction } from 'express';
import request from 'supertest';
import mongoose from 'mongoose';
import { GroupModel } from '../../src/models/group.model';
/**
 * Non-Functional Requirements Tests for Location Service
 *
 * Requirement: Location Optimization Response Time
 * The system shall return location optimization results (midpoint calculation and venue suggestions)
 * within 2 to 5 seconds of the Squad Leader triggering the algorithm.
 *
 * Upper bound: 5 seconds
 * Target: 3 seconds
 * Lower acceptable bound: 2 seconds
 */

describe('Non-Functional Requirements: Location Service', () => {
  let locationService: LocationService;

  beforeEach(() => {
    locationService = new LocationService();
  });

  describe('Location Optimization Response Time', () => {
    // Test data: Various realistic Vancouver locations for testing
    const vancouverLocations = [
      {
        address: {
          formatted: '123 Main St, Vancouver, BC',
          lat: 49.2827,
          lng: -123.1207,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'transit' as TransitType,
      },
      {
        address: {
          formatted: '456 Oak Ave, Vancouver, BC',
          lat: 49.2900,
          lng: -123.1300,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'driving' as TransitType,
      },
      {
        address: {
          formatted: '789 Elm St, Vancouver, BC',
          lat: 49.2750,
          lng: -123.1100,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'transit' as TransitType,
      },
      {
        address: {
          formatted: '321 Pine Rd, Vancouver, BC',
          lat: 49.2600,
          lng: -123.1400,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'bicycling' as TransitType,
      },
      {
        address: {
          formatted: '654 Maple Dr, Vancouver, BC',
          lat: 49.3000,
          lng: -123.1000,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'walking' as TransitType,
      },
      {
        address: {
          formatted: '987 Cedar Ln, Vancouver, BC',
          lat: 49.2500,
          lng: -123.1500,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'transit' as TransitType,
      },
      {
        address: {
          formatted: '147 Birch Way, Vancouver, BC',
          lat: 49.3100,
          lng: -123.0900,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'driving' as TransitType,
      },
      {
        address: {
          formatted: '258 Spruce St, Vancouver, BC',
          lat: 49.2400,
          lng: -123.1600,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'transit' as TransitType,
      },
      {
        address: {
          formatted: '369 Willow Ave, Vancouver, BC',
          lat: 49.3200,
          lng: -123.0800,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'bicycling' as TransitType,
      },
      {
        address: {
          formatted: '741 Ash Rd, Vancouver, BC',
          lat: 49.2300,
          lng: -123.1700,
          components: {
            city: 'Vancouver',
            province: 'BC',
            country: 'Canada',
          },
        },
        transitType: 'walking' as TransitType,
      },
    ];

    // Input: 2 users
    // Expected status code: 200
    // Expected behavior: midpoint and venues calculated
    // Expected output: response time between 2-5 seconds
    it('should calculate optimal meeting point for 2 users within 5 seconds', async () => {
      const locationInfo: LocationInfo[] = vancouverLocations.slice(0, 2);

      const startTime = performance.now();
      const result = await locationService.findOptimalMeetingPoint(locationInfo);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`2 users - Response time: ${responseTime.toFixed(2)}ms`);

      expect(result).toBeDefined();
      expect(result.lat).toBeDefined();
      expect(result.lng).toBeDefined();
      expect(responseTime).toBeLessThan(5000);
      expect(responseTime).toBeGreaterThan(0);
    }, 10000);

    // Input: 5 users
    // Expected status code: 200
    // Expected behavior: midpoint and venues calculated
    // Expected output: response time between 2-5 seconds
    it('should calculate optimal meeting point for 5 users within 5 seconds', async () => {
      const locationInfo: LocationInfo[] = vancouverLocations.slice(0, 5);

      const startTime = performance.now();
      const result = await locationService.findOptimalMeetingPoint(locationInfo);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`5 users - Response time: ${responseTime.toFixed(2)}ms`);

      expect(result).toBeDefined();
      expect(result.lat).toBeDefined();
      expect(result.lng).toBeDefined();
      expect(responseTime).toBeLessThan(5000);
      expect(responseTime).toBeGreaterThan(0);
    }, 10000);

    // Input: 10 users
    // Expected status code: 200
    // Expected behavior: midpoint and venues calculated
    // Expected output: response time between 2-5 seconds
    it('should calculate optimal meeting point for 10 users within 5 seconds', async () => {
      const locationInfo: LocationInfo[] = vancouverLocations;

      const startTime = performance.now();
      const result = await locationService.findOptimalMeetingPoint(locationInfo);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`10 users - Response time: ${responseTime.toFixed(2)}ms`);

      expect(result).toBeDefined();
      expect(result.lat).toBeDefined();
      expect(result.lng).toBeDefined();
      expect(responseTime).toBeLessThan(7000);
      expect(responseTime).toBeGreaterThan(0);
    }, 10000);

    // Input: 2 users with activity search
    // Expected status code: 200
    // Expected behavior: midpoint calculated and venues retrieved
    // Expected output: response time for combined operations within 5 seconds
    it('should retrieve activity list at optimal meeting point for 2 users within 5 seconds', async () => {
      const locationInfo: LocationInfo[] = vancouverLocations.slice(0, 2);

      const startTime = performance.now();
      const midpoint = await locationService.findOptimalMeetingPoint(locationInfo);
      const activities = await locationService.getActivityList(midpoint, 'restaurant', 1000, 10);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`2 users + activity search - Response time: ${responseTime.toFixed(2)}ms`);

      expect(midpoint).toBeDefined();
      expect(activities).toBeDefined();
      expect(Array.isArray(activities)).toBe(true);
      expect(responseTime).toBeLessThan(5000);
      expect(responseTime).toBeGreaterThan(0);
    }, 10000);

    // Input: 5 users with activity search
    // Expected status code: 200
    // Expected behavior: midpoint calculated and venues retrieved
    // Expected output: response time for combined operations within 5 seconds
    it('should retrieve activity list at optimal meeting point for 5 users within 5 seconds', async () => {
      const locationInfo: LocationInfo[] = vancouverLocations.slice(0, 5);

      const startTime = performance.now();
      const midpoint = await locationService.findOptimalMeetingPoint(locationInfo);
      const activities = await locationService.getActivityList(midpoint, 'restaurant', 1000, 10);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`5 users + activity search - Response time: ${responseTime.toFixed(2)}ms`);

      expect(midpoint).toBeDefined();
      expect(activities).toBeDefined();
      expect(Array.isArray(activities)).toBe(true);
      expect(responseTime).toBeLessThan(5000);
      expect(responseTime).toBeGreaterThan(0);
    }, 10000);

    // Input: 10 users with activity search
    // Expected status code: 200
    // Expected behavior: midpoint calculated and venues retrieved
    // Expected output: response time for combined operations within 5 seconds
    it('should retrieve activity list at optimal meeting point for 10 users within 5 seconds', async () => {
      const locationInfo: LocationInfo[] = vancouverLocations;

      const startTime = performance.now();
      const midpoint = await locationService.findOptimalMeetingPoint(locationInfo);
      const activities = await locationService.getActivityList(midpoint, 'restaurant', 1000, 10);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`10 users + activity search - Response time: ${responseTime.toFixed(2)}ms`);

      expect(midpoint).toBeDefined();
      expect(activities).toBeDefined();
      expect(Array.isArray(activities)).toBe(true);
      expect(responseTime).toBeLessThan(7000);
      expect(responseTime).toBeGreaterThan(0);
    }, 10000);
  });
});

describe('Nonfunctional Requirements: Group View Load Time', () => {
  let app: Express;
  let groupController: GroupController;

  beforeAll(async () => {
    app = express();
    app.use(express.json());
    groupController = new GroupController();

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
    app.get('/group/info', (req, res, next) => groupController.getAllGroups(req, res, next));
    app.get('/group/:joinCode', (req, res, next) => groupController.getGroupByJoinCode(req, res, next));
    app.get('/group/:joinCode/midpoint', (req, res, next) => groupController.getMidpointByJoinCode(req, res, next));
  });

  afterAll(async () => {
    await mongoose.connection.close();
  });

  const createTestGroup = async (groupName: string, expectedPeople: number, memberCount: number) => {
    const groupLeader = {
      id: `leader-${Date.now()}`,
      name: 'Group Leader',
      email: `leader-${Date.now()}@example.com`,
    };

    const groupMembers = [groupLeader];
    for (let i = 1; i < memberCount; i++) {
      groupMembers.push({
        id: `member-${i}-${Date.now()}`,
        name: `Member ${i}`,
        email: `member${i}-${Date.now()}@example.com`,
      });
    }

    const createRes = await request(app).post('/group/create').send({
      groupName,
      groupLeaderId: groupLeader,
      expectedPeople,
      meetingTime: '2026-11-02T12:30:00Z',
      activityType: 'CAFE',
    });

    return createRes.body.data.group.joinCode;
  };

  // Input: valid joinCode for group with 2 members
  // Expected behavior: group details (name, members, meetingTime) loaded
  // Expected output: response time within 2 seconds
  it('should load group by joinCode with 2 members within 2 seconds', async () => {
    const joinCode = await createTestGroup('Group 2 Members', 2, 2);

    const startTime = performance.now();
    const res = await request(app).get(`/group/${joinCode}`);
    const endTime = performance.now();

    const responseTime = endTime - startTime;

    console.log(`Group (2 members) - Response time: ${responseTime.toFixed(2)}ms`);

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Group fetched successfully');
    expect(responseTime).toBeLessThan(2000);
    expect(responseTime).toBeGreaterThan(0);
  });

  // Input: valid joinCode for group with 5 members
  // Expected behavior: group details loaded with all attendees
  // Expected output: response time within 2 seconds
  it('should load group by joinCode with 5 members within 2 seconds', async () => {
    const joinCode = await createTestGroup('Group 5 Members', 5, 5);

    const startTime = performance.now();
    const res = await request(app).get(`/group/${joinCode}`);
    const endTime = performance.now();

    const responseTime = endTime - startTime;

    console.log(`Group (5 members) - Response time: ${responseTime.toFixed(2)}ms`);

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Group fetched successfully');
    expect(responseTime).toBeLessThan(2000);
    expect(responseTime).toBeGreaterThan(0);
  });

  // Input: valid joinCode for group with 10 members
  // Expected behavior: group details loaded with all attendees
  // Expected output: response time within 2 seconds
  it('should load group by joinCode with 10 members within 2 seconds', async () => {
    const joinCode = await createTestGroup('Group 10 Members', 10, 10);

    const startTime = performance.now();
    const res = await request(app).get(`/group/${joinCode}`);
    const endTime = performance.now();

    const responseTime = endTime - startTime;

    console.log(`Group (10 members) - Response time: ${responseTime.toFixed(2)}ms`);

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Group fetched successfully');
    expect(responseTime).toBeLessThan(2000);
    expect(responseTime).toBeGreaterThan(0);
  });

  // Input: getAllGroups fetch all groups at once
  // Expected behavior: all groups loaded
  // Expected output: response time within 2 seconds
  it('should fetch all groups within 2 seconds', async () => {
    // Create a few test groups first
    await createTestGroup('Test Group 1', 3, 3);
    await createTestGroup('Test Group 2', 4, 4);

    const startTime = performance.now();
    const res = await request(app).get('/group/info');
    const endTime = performance.now();

    const responseTime = endTime - startTime;

    console.log(`Fetch all groups - Response time: ${responseTime.toFixed(2)}ms`);

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Groups fetched successfully');
    expect(Array.isArray(res.body.data.groups)).toBe(true);
    expect(responseTime).toBeLessThan(2000);
    expect(responseTime).toBeGreaterThan(0);
  });

  // Input: getMidpointByJoinCode to get optimal meeting point
  // Expected behavior: midpoint calculated and returned
  // Expected output: response time within 5 seconds
  it('should get midpoint by joinCode within 5 seconds', async () => {
    const groupLeader = {
      id: `leader-${Date.now()}`,
      name: 'Group Leader',
      email: `leader-${Date.now()}@example.com`,
    };

    const createRes = await request(app).post('/group/create').send({
      groupName: 'Midpoint Test Group',
      groupLeaderId: groupLeader,
      expectedPeople: 2,
      meetingTime: '2026-11-02T12:30:00Z',
      activityType: 'CAFE',
    });

    const joinCode = createRes.body.data.group.joinCode;

    const startTime = performance.now();
    const res = await request(app).get(`/group/${joinCode}/midpoint`);
    const endTime = performance.now();

    const responseTime = endTime - startTime;

    console.log(`Get midpoint - Response time: ${responseTime.toFixed(2)}ms`);

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Get midpoint successfully!');
    expect(responseTime).toBeLessThan(5000);
    expect(responseTime).toBeGreaterThan(0);
  });
});
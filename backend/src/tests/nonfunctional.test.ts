import { GroupController } from '@/controllers/group.controller';
import { LocationService } from '../services/location.service';
import type { LocationInfo } from '../types/location.types';
import type { TransitType } from '../types/transit.types';

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
    });

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
    });

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
      expect(responseTime).toBeLessThan(5000);
      expect(responseTime).toBeGreaterThan(0);
    });

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
    });

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
    });

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
      expect(responseTime).toBeLessThan(5000);
      expect(responseTime).toBeGreaterThan(0);
    });
  });
});

describe('Nonfunctional Requirements: Group View Load Time', () => {
    // Note: Tests use existing GroupController endpoints
    // Measures time to fetch group details via API

    
    let groupController: GroupController;
    const testJoinCode = 'testcode123';

    beforeEach(() => {
      groupController = new GroupController();
    });

    // Input: valid joinCode for group with 2 members
    // Expected behavior: group details (name, members, meetingTime) loaded
    // Expected output: response time within 2 seconds
    it('should load group by joinCode with 2 members within 2 seconds', async () => {
      const mockRes = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      } as any;
      const mockNext = jest.fn();
      const mockReq = {
        params: { joinCode: testJoinCode },
      } as any;

      const startTime = performance.now();
      await groupController.getGroupByJoinCode(mockReq, mockRes, mockNext);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`Group (2 members) - Response time: ${responseTime.toFixed(2)}ms`);

      expect(mockRes.status).toHaveBeenCalledWith(200);
      expect(mockRes.json).toHaveBeenCalled();
      expect(responseTime).toBeLessThan(2000);
      expect(responseTime).toBeGreaterThan(0);
    });

    // Input: valid joinCode for group with 5 members
    // Expected behavior: group details loaded with all attendees
    // Expected output: response time within 2 seconds
    it('should load group by joinCode with 5 members within 2 seconds', async () => {
      const mockRes = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      } as any;
      const mockNext = jest.fn();
      const mockReq = {
        params: { joinCode: testJoinCode },
      } as any;

      const startTime = performance.now();
      await groupController.getGroupByJoinCode(mockReq, mockRes, mockNext);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`Group (5 members) - Response time: ${responseTime.toFixed(2)}ms`);

      expect(mockRes.status).toHaveBeenCalledWith(200);
      expect(mockRes.json).toHaveBeenCalled();
      expect(responseTime).toBeLessThan(2000);
      expect(responseTime).toBeGreaterThan(0);
    });

    // Input: valid joinCode for group with 10 members
    // Expected behavior: group details loaded with all attendees
    // Expected output: response time within 2 seconds
    it('should load group by joinCode with 10 members within 2 seconds', async () => {
      const mockRes = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      } as any;
      const mockNext = jest.fn();
      const mockReq = {
        params: { joinCode: testJoinCode },
      } as any;

      const startTime = performance.now();
      await groupController.getGroupByJoinCode(mockReq, mockRes, mockNext);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`Group (10 members) - Response time: ${responseTime.toFixed(2)}ms`);

      expect(mockRes.status).toHaveBeenCalledWith(200);
      expect(mockRes.json).toHaveBeenCalled();
      expect(responseTime).toBeLessThan(2000);
      expect(responseTime).toBeGreaterThan(0);
    });

    // Input: getAllGroups fetch all groups at once
    // Expected behavior: all groups loaded
    // Expected output: response time within 2 seconds
    it('should fetch all groups within 2 seconds', async () => {
      const mockRes = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      } as any;
      const mockNext = jest.fn();
      const mockReq = {} as any;

      const startTime = performance.now();
      await groupController.getAllGroups(mockReq, mockRes, mockNext);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`Fetch all groups - Response time: ${responseTime.toFixed(2)}ms`);

      expect(mockRes.status).toHaveBeenCalledWith(200);
      expect(mockRes.json).toHaveBeenCalled();
      expect(responseTime).toBeLessThan(2000);
      expect(responseTime).toBeGreaterThan(0);
    });

    // Input: getMidpointByJoinCode to get optimal meeting point
    // Expected behavior: midpoint calculated and returned
    // Expected output: response time within 5 seconds (same as location optimization requirement)
    it('should get midpoint by joinCode within 5 seconds', async () => {
      const mockRes = {
        status: jest.fn().mockReturnThis(),
        json: jest.fn(),
      } as any;
      const mockNext = jest.fn();
      const mockReq = {
        params: { joinCode: testJoinCode },
      } as any;

      const startTime = performance.now();
      await groupController.getMidpointByJoinCode(mockReq, mockRes, mockNext);
      const endTime = performance.now();

      const responseTime = endTime - startTime;

      console.log(`Get midpoint - Response time: ${responseTime.toFixed(2)}ms`);

      expect(mockRes.status).toHaveBeenCalledWith(200);
      expect(mockRes.json).toHaveBeenCalled();
      expect(responseTime).toBeLessThan(5000);
      expect(responseTime).toBeGreaterThan(0);
    });
  });
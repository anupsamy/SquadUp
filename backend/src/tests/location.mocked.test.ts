import { LocationService } from '../services/location.service';
import { Client } from '@googlemaps/google-maps-services-js';
import type { LocationInfo, GeoLocation} from '../types/location.types';
import type { TransitType } from '../types/transit.types';

jest.mock('@googlemaps/google-maps-services-js');

const locationFixtures = {
  // GeoLocation fixtures for getGeographicMidpoint
  singlePoint: [
    { lat: 49.2827, lng: -123.1207, transitType: 'transit' as const }
  ],

  twoPointsSimple: [
    { lat: 49.0, lng: -123.0, transitType: 'transit' as const },
    { lat: 51.0, lng: -125.0, transitType: 'transit' as const }
  ],

  threePointsTriangle: [
    { lat: 49.2, lng: -123.1, transitType: 'transit' as const },
    { lat: 49.3, lng: -123.2, transitType: 'transit' as const },
    { lat: 49.1, lng: -123.0, transitType: 'transit' as const }
  ],

  // LocationInfo fixtures for findOptimalMeetingPoint
  locationInfoSingle: [
    {
      address: {
        formatted: '123 Main St, Vancouver, BC',
        lat: 49.2827,
        lng: -123.1207
      },
      transitType: 'transit' as TransitType
    }
  ] as LocationInfo[],

  locationInfoTwo: [
    {
      address: {
        formatted: '1 Street, City A',
        lat: 49.0,
        lng: -123.0
      },
      transitType: 'transit' as TransitType
    },
    {
      address: {
        formatted: '2 Street, City B',
        lat: 51.0,
        lng: -125.0
      },
      transitType: 'transit' as TransitType
    }
  ] as LocationInfo[],

  locationInfoThree: [
    {
      address: {
        formatted: '1 Street, City A',
        lat: 49.2,
        lng: -123.1
      },
      transitType: 'transit' as TransitType
    },
    {
      address: {
        formatted: '2 Street, City B',
        lat: 49.3,
        lng: -123.2
      },
      transitType: 'transit' as TransitType
    },
    {
      address: {
        formatted: '3 Street, City C',
        lat: 49.1,
        lng: -123.0
      },
      transitType: 'transit' as TransitType
    }
  ] as LocationInfo[],

  expectedMidpoints: {
    singlePoint: { lat: 49.2827, lng: -123.1207 },
    twoPointsSimple: { lat: 50.0, lng: -124.0 },
    threePointsTriangle: { lat: 49.2, lng: -123.1 },
  }
};

describe('Mocked: LocationService', () => {
  let locationService: LocationService;
  let mockMapsClient: jest.Mocked<Client>;

  beforeEach(() => {
    jest.clearAllMocks();
    mockMapsClient = new Client({}) as jest.Mocked<Client>;
    locationService = new LocationService();
  });

  describe('getTravelTime', () => {
    // Input: valid origin and destination coordinates
    // Expected behavior: calls distance matrix API and converts seconds to minutes
    // Expected output: duration in minutes
    it('should return travel time in minutes', async () => {
      const origin = { lat: 49.2827, lng: -123.1207 };
      const destination = { lat: 49.3, lng: -123.2 };

      mockMapsClient.distancematrix = jest.fn().mockResolvedValue({
        data: {
          rows: [{
            elements: [{
              status: 'OK',
              duration: { value: 600 } // 600 seconds = 10 minutes
            }]
          }]
        }
      });

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getTravelTime(origin, destination);

      expect(result).toBe(10);
    });

    // Input: valid coordinates but API returns error status
    // Expected behavior: returns Infinity
    // Expected output: Infinity
    it('should return Infinity when Distance Matrix API returns error', async () => {
      const origin = { lat: 49.2827, lng: -123.1207 };
      const destination = { lat: 49.3, lng: -123.2 };

      mockMapsClient.distancematrix = jest.fn().mockResolvedValue({
        data: {
          rows: [{
            elements: [{
              status: 'ZERO_RESULTS'
            }]
          }]
        }
      });

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getTravelTime(origin, destination);

      expect(result).toBe(Infinity);
    });

    // Input: API call throws exception
    // Expected behavior: catches error and returns Infinity
    // Expected output: Infinity
    it('should return Infinity when API call throws error', async () => {
      const origin = { lat: 49.2827, lng: -123.1207 };
      const destination = { lat: 49.3, lng: -123.2 };

      mockMapsClient.distancematrix = jest.fn().mockRejectedValue(
        new Error('API Error')
      );

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getTravelTime(origin, destination);

      expect(result).toBe(Infinity);
    });

    // Input: zero duration from API
    // Expected behavior: returns 0
    // Expected output: 0
    it('should handle zero duration correctly', async () => {
      const origin = { lat: 49.2827, lng: -123.1207 };
      const destination = { lat: 49.2827, lng: -123.1207 }; // Same location

      mockMapsClient.distancematrix = jest.fn().mockResolvedValue({
        data: {
          rows: [{
            elements: [{
              status: 'OK',
              duration: { value: 0 }
            }]
          }]
        }
      });

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getTravelTime(origin, destination);

      expect(result).toBe(0);
    });
  });

  describe('getGeographicMidpoint', () => {
    // Input: single coordinate
    // Expected behavior: returns the same coordinate
    // Expected output: input coordinate
    it('should return same coordinate for single point', () => {
      const coords = locationFixtures.singlePoint;

      const result = locationService.getGeographicMidpoint(coords);

      expect(result.lat).toBeCloseTo(coords[0].lat, 5);
      expect(result.lng).toBeCloseTo(coords[0].lng, 5);
    });

    // Input: two points at different latitudes and longitudes
    // Expected behavior: returns approximate midpoint
    // Expected output: coordinates near the expected midpoint
    it('should calculate midpoint for two points', () => {
      const coords = locationFixtures.twoPointsSimple;
      const expected = locationFixtures.expectedMidpoints.twoPointsSimple;

      const result = locationService.getGeographicMidpoint(coords);

      const distance = Math.sqrt(
        (result.lat - expected.lat) ** 2 +
        (result.lng - expected.lng) ** 2
      );

      expect(distance).toBeLessThan(0.1);
    });

    // Input: three points forming triangle
    // Expected behavior: returns approximate center of triangle
    // Expected output: coordinates near the expected midpoint
    it('should calculate midpoint for three points', () => {
      const coords = locationFixtures.threePointsTriangle;
      const expected = locationFixtures.expectedMidpoints.threePointsTriangle;

      const result = locationService.getGeographicMidpoint(coords);

      const distance = Math.sqrt(
        (result.lat - expected.lat) ** 2 +
        (result.lng - expected.lng) ** 2
      );

      expect(distance).toBeLessThan(0.1);
    });
  });

  describe('getActivityList', () => {
    const mockLocation = { lat: 49.2827, lng: -123.1207 };

    // Input: valid location and type with successful API response
    // Expected behavior: maps API results to Activity objects
    // Expected output: array of Activity objects
    it('should return activity list when API succeeds', async () => {
      mockMapsClient.placesNearby = jest.fn().mockResolvedValue({
        data: {
          status: 'OK',
          results: [
            {
              name: 'Restaurant A',
              place_id: 'place123',
              vicinity: '123 Main St',
              rating: 4.5,
              user_ratings_total: 100,
              price_level: 2,
              types: ['restaurant'],
              geometry: { location: { lat: 49.28, lng: -123.12 } },
              opening_hours: { open_now: true },
              business_status: 'OPERATIONAL'
            }
          ]
        }
      });

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getActivityList(mockLocation, 'restaurant', 1000, 10);

      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('Restaurant A');
      expect(result[0].placeId).toBe('place123');
      expect(result[0].rating).toBe(4.5);
      expect(result[0].isOpenNow).toBe(true);
    });

    // Input: valid location but API returns error status
    // Expected behavior: returns empty array
    // Expected output: empty array
    it('should return empty array when API returns error', async () => {
      mockMapsClient.placesNearby = jest.fn().mockResolvedValue({
        data: {
          status: 'ZERO_RESULTS'
        }
      });

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getActivityList(mockLocation);

      expect(result).toEqual([]);
    });

    // Input: API response with more results than maxResults
    // Expected behavior: returns only maxResults items
    // Expected output: array with length equal to maxResults
    it('should respect maxResults limit', async () => {
      const places = Array.from({ length: 15 }, (_, i) => ({
        name: `Place ${i}`,
        place_id: `place${i}`,
        vicinity: `${i} Street`,
        geometry: { location: { lat: 49.28, lng: -123.12 } },
        types: ['restaurant'],
        business_status: 'OPERATIONAL'
      }));

      mockMapsClient.placesNearby = jest.fn().mockResolvedValue({
        data: {
          status: 'OK',
          results: places
        }
      });

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getActivityList(mockLocation, 'restaurant', 1000, 5);

      expect(result).toHaveLength(5);
    });

    // Input: API response with places missing required fields
    // Expected behavior: filters out incomplete places
    // Expected output: only valid Activity objects
    it('should filter out places with missing required fields', async () => {
      mockMapsClient.placesNearby = jest.fn().mockResolvedValue({
        data: {
          status: 'OK',
          results: [
            {
              name: 'Valid Place',
              place_id: 'place1',
              vicinity: '1 Street',
              geometry: { location: { lat: 49.28, lng: -123.12 } },
              business_status: 'OPERATIONAL'
            },
            {
              // Missing name
              place_id: 'place2',
              vicinity: '2 Street',
              geometry: { location: { lat: 49.29, lng: -123.13 } }
            },
            {
              name: 'Place without geometry',
              place_id: 'place3',
              vicinity: '3 Street',
              // Missing geometry
            }
          ]
        }
      });

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getActivityList(mockLocation);

      expect(result).toHaveLength(1);
      expect(result[0].name).toBe('Valid Place');
    });

    // Input: API call throws exception
    // Expected behavior: catches error and returns empty array
    // Expected output: empty array
    it('should return empty array when API throws error', async () => {
      mockMapsClient.placesNearby = jest.fn().mockRejectedValue(
        new Error('API Error')
      );

      (Client as unknown as jest.Mock).mockImplementation(() => mockMapsClient);
      locationService = new LocationService();

      const result = await locationService.getActivityList(mockLocation);

      expect(result).toEqual([]);
    });
  });

  describe('findOptimalMeetingPoint', () => {
    // Input: single location
    // Expected behavior: returns that location or its geographic midpoint
    // Expected output: location approximately matching the input
    it('should return location near single point', async () => {
      const locationInfo = locationFixtures.locationInfoSingle;

      jest.spyOn(locationService, 'getTravelTime').mockResolvedValue(10);

      const result = await locationService.findOptimalMeetingPoint(locationInfo);

      const distance = Math.sqrt(
        (result.lat - 49.2827) ** 2 +
        (result.lng - -123.1207) ** 2
      );

      expect(distance).toBeLessThan(0.01);
    });

    // Input: two locations equidistant
    // Expected behavior: converges to approximate midpoint
    // Expected output: coordinates near expected midpoint
    it('should find optimal point between two locations', async () => {
      const locationInfo = locationFixtures.locationInfoTwo;

      jest.spyOn(locationService, 'getTravelTime').mockResolvedValue(10);

      const result = await locationService.findOptimalMeetingPoint(locationInfo);

      const expected = locationFixtures.expectedMidpoints.twoPointsSimple;
      const distance = Math.sqrt(
        (result.lat - expected.lat) ** 2 +
        (result.lng - expected.lng) ** 2
      );

      expect(distance).toBeLessThan(0.1);
    });

    // Input: three locations with all at same location
    // Expected behavior: returns that location
    // Expected output: location matching input locations
    it('should return location when all points are same', async () => {
      const lat = 49.2827;
      const lng = -123.1207;
      const locationInfo: LocationInfo[] = [
        {
          address: { formatted: 'A', lat, lng },
          transitType: 'transit' as TransitType
        },
        {
          address: { formatted: 'B', lat, lng },
          transitType: 'transit' as TransitType
        },
        {
          address: { formatted: 'C', lat, lng },
          transitType: 'transit' as TransitType
        }
      ];

      jest.spyOn(locationService, 'getTravelTime').mockResolvedValue(0);

      const result = await locationService.findOptimalMeetingPoint(locationInfo);

      expect(result.lat).toBeCloseTo(lat, 4);
      expect(result.lng).toBeCloseTo(lng, 4);
    });

    // Input: locations where all travel times are zero (edge case)
    // Expected behavior: falls back to geographic midpoint
    // Expected output: valid location within bounds
    it('should handle zero travel times gracefully', async () => {
      const locationInfo = locationFixtures.locationInfoTwo;

      jest.spyOn(locationService, 'getTravelTime').mockResolvedValue(0);

      const result = await locationService.findOptimalMeetingPoint(locationInfo);

      expect(typeof result.lat).toBe('number');
      expect(typeof result.lng).toBe('number');
      expect(Number.isFinite(result.lat)).toBe(true);
      expect(Number.isFinite(result.lng)).toBe(true);
    });

    // // Input: locations with missing coordinates
    // // Expected behavior: filters out invalid locations and computes with valid ones
    // // Expected output: midpoint of valid locations
    // it('should filter out locations with missing coordinates', async () => {
    //   const locationInfo: LocationInfo[] = [
    //     {
    //       address: {
    //         formatted: 'Valid 1',
    //         lat: 49.2,
    //         lng: -123.1
    //       },
    //       transitType: 'transit' as TransitType
    //     },
    //     {
    //       address: {
    //         formatted: 'Invalid',
    //         lat: undefined,
    //         lng: undefined
    //       },
    //       transitType: 'transit' as TransitType
    //     },
    //     {
    //       address: {
    //         formatted: 'Valid 2',
    //         lat: 49.3,
    //         lng: -123.2
    //       },
    //       transitType: 'transit' as TransitType
    //     }
    //   ];

    //   jest.spyOn(locationService, 'getTravelTime').mockResolvedValue(10);

    //   const result = await locationService.findOptimalMeetingPoint(locationInfo);

    //   const expected = locationFixtures.expectedMidpoints.twoPointsSimple;
    //   const distance = Math.sqrt(
    //     (result.lat - expected.lat) ** 2 +
    //     (result.lng - expected.lng) ** 2
    //   );

    //   expect(distance).toBeLessThan(0.1);
    // });

    // Input: algorithm with small maxIterations
    // Expected behavior: terminates within maxIterations
    // Expected output: valid location found
    it('should terminate within maxIterations', async () => {
      const locationInfo = locationFixtures.locationInfoTwo;

      jest.spyOn(locationService, 'getTravelTime').mockResolvedValue(10);

      const result = await locationService.findOptimalMeetingPoint(locationInfo, 2);

      expect(typeof result.lat).toBe('number');
      expect(typeof result.lng).toBe('number');
    });
  });
});
import { locationService } from '../../src/services/location.service';

jest.mock('../../src/utils/logger.util');

describe('Mocked: Location Service Error Handling', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    delete process.env.MAPS_API_KEY;
  });

  describe('LocationService.getTravelTime', () => {
    // Mocked behavior: MAPS_API_KEY environment variable is not set
    // Input: valid origin and destination coordinates
    // Expected behavior: error is caught and Infinity is returned
    // Expected output: Infinity (fallback value for unreachable destination)
    it('should return Infinity when MAPS_API_KEY is not set', async () => {
      const origin = { lat: 49.28, lng: -123.12 };
      const destination = { lat: 49.29, lng: -123.13 };

      const result = await locationService.getTravelTime(origin, destination);

      expect(result).toBe(Infinity);
    });
  });

  describe('LocationService.getActivityList', () => {
    // Mocked behavior: MAPS_API_KEY environment variable is not set
    // Input: valid location, type, and radius
    // Expected behavior: error is caught and empty array is returned
    // Expected output: empty array (no activities found)
    it('should return empty array when MAPS_API_KEY is not set', async () => {
      const location = { lat: 49.28, lng: -123.12 };

      const result = await locationService.getActivityList(location, 'restaurant');

      expect(result).toEqual([]);
    });
  });
});
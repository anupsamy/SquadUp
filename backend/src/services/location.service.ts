import { Client, TravelMode } from '@googlemaps/google-maps-services-js';
import type { LocationInfo, GeoLocation } from '../types/location.types';
import { format } from 'path';
import { Activity } from '../types/group.types';
import logger from '../utils/logger.util';

export class LocationService {
  private mapsClient: Client;

  constructor() {
    this.mapsClient = new Client({});
  }

  async getTravelTime(
    origin: GeoLocation,
    destination: GeoLocation
  ): Promise<number> {
    try {
      const apiKey = process.env.MAPS_API_KEY;
      if (!apiKey) {
        throw new Error('MAPS_API_KEY is not configured');
      }
      // Google Maps API expects mode as TravelMode enum
      // TransitType values ("driving", "walking", "bicycling", "transit") match the API expectations
      // Map TransitType to TravelMode enum
      const transitTypeToTravelMode: Record<string, TravelMode> = {
        driving: TravelMode.driving,
        walking: TravelMode.walking,
        bicycling: TravelMode.bicycling,
        transit: TravelMode.transit,
      };
      const travelMode = origin.transitType
        ? transitTypeToTravelMode[origin.transitType] || TravelMode.driving
        : TravelMode.driving;
      const response = await this.mapsClient.distancematrix({
        params: {
          origins: [`${origin.lat},${origin.lng}`],
          destinations: [`${destination.lat},${destination.lng}`],
          key: apiKey,
          mode: travelMode,
        },
      });

      const element = response.data.rows[0].elements[0];
      if (element.status !== 'OK')
        throw new Error(`Distance Matrix API error: ${element.status}`);

      const durationSeconds = element.duration.value;
      const SEC_TO_MIN = 60;
      return durationSeconds / SEC_TO_MIN;
    } catch (err) {
      console.error('Error fetching travel time:', err);
      return Infinity;
    }
  }

  // set to private
  getGeographicMidpoint(coords: GeoLocation[]): GeoLocation {
    const DEG_TO_RAD = Math.PI / 180;
    const RAD_TO_DEG = 180 / Math.PI;

    let x = 0,
      y = 0,
      z = 0;
    for (const { lat, lng } of coords) {
      const latRad = lat * DEG_TO_RAD;
      const lonRad = lng * DEG_TO_RAD;

      x += Math.cos(latRad) * Math.cos(lonRad);
      y += Math.cos(latRad) * Math.sin(lonRad);
      z += Math.sin(latRad);
    }

    const total = coords.length;
    x /= total;
    y /= total;
    z /= total;

    const lonMid = Math.atan2(y, x);
    const hyp = Math.sqrt(x * x + y * y);
    const latMid = Math.atan2(z, hyp);

    return { lat: latMid * RAD_TO_DEG, lng: lonMid * RAD_TO_DEG };
  }
  async getActivityList(
    location: GeoLocation,
    type: string = 'restaurant',
    radius: number = 1000,
    maxResults: number = 10
  ): Promise<Activity[]> {
    try {
      const apiKey = process.env.MAPS_API_KEY;
      if (!apiKey) {
        throw new Error('MAPS_API_KEY is not configured');
      }
      const response = await this.mapsClient.placesNearby({
        params: {
          location: `${location.lat},${location.lng}`,
          radius,
          type,
          key: apiKey,
        },
      });

      if (response.data.status !== 'OK') return [];

      // console.log("getting activities list", response.data.results);
      // console.log("First object details", response.data.results[0]);

      return (response.data.results || [])
        .map(place => {
          const loc = place.geometry?.location;
          if (!place.name || !loc) return null;

          const primaryType = place.types?.[0] || 'establishment';
          const openNow = place.opening_hours?.open_now ?? false;

          return {
            name: place.name,
            placeId: place.place_id,
            address: place.vicinity,
            rating: place.rating ?? 0,
            userRatingsTotal: place.user_ratings_total ?? 0,
            priceLevel: place.price_level ?? 0,
            type: primaryType,
            latitude: loc.lat,
            longitude: loc.lng,
            businessStatus: place.business_status ?? 'UNKNOWN',
            isOpenNow: openNow,
          };
        })
        .filter((p): p is Activity => p !== null)
        .slice(0, maxResults);
    } catch (err) {
      logger.error('Error fetching nearby places:', err);
      return [];
    }
  }

  // set to private
  async findOptimalMeetingPoint(
    locationInfo: LocationInfo[],
    maxIterations = 20,
    epsilon = 1e-5
  ): Promise<GeoLocation> {
    let geoLocation: GeoLocation[] = locationInfo
      .filter(loc => loc.address.lat != null && loc.address.lng != null)
      .map(loc => {
        const lat = loc.address.lat ?? 0;
        const lng = loc.address.lng ?? 0;
        return {
          lat,
          lng,
          transitType: loc.transitType,
        };
      });
    let midpoint = this.getGeographicMidpoint(geoLocation);

    for (let i = 0; i < maxIterations; i++) {
      const travelTimes = await Promise.all(
        geoLocation.map(u => this.getTravelTime(u, midpoint))
      );

      let totalWeight = 0;
      let newLat = 0,
        newLng = 0;

      // Helper function to safely access array elements and prevent object injection
      const safeGetArrayElement = <T>(arr: T[], index: number): T | null => {
        if (
          !Array.isArray(arr) ||
          typeof index !== 'number' ||
          index < 0 ||
          index >= arr.length
        ) {
          return null;
        }
        // Use slice to avoid dynamic property access with bracket notation
        const element = arr.slice(index, index + 1)[0];
        return element ?? null;
      };

      for (let j = 0; j < geoLocation.length; j++) {
        // Validate array access to prevent object injection: ensure index is in bounds for both arrays
        if (
          !Array.isArray(travelTimes) ||
          !Array.isArray(geoLocation) ||
          typeof j !== 'number' ||
          j < 0 ||
          j >= travelTimes.length ||
          j >= geoLocation.length
        ) {
          continue;
        }

        // Use helper function to safely access array element and prevent object injection
        const rawWeightValue = safeGetArrayElement(travelTimes, j);
        if (rawWeightValue === null) {
          continue;
        }
        const rawWeight =
          typeof rawWeightValue === 'number' ? rawWeightValue : 0;
        // Validate weight to prevent object injection: ensure it's a finite number and non-negative
        const weight = isFinite(rawWeight) && rawWeight >= 0 ? rawWeight : 0;

        // Use helper function to safely access array element and prevent object injection
        const geoLocationItemValue = safeGetArrayElement(geoLocation, j);
        if (
          geoLocationItemValue === null ||
          typeof geoLocationItemValue !== 'object'
        ) {
          continue;
        }
        const geoLocationItem = geoLocationItemValue;
        // TypeScript guarantees lat and lng exist on GeoLocation
        const rawLat = geoLocationItem.lat;
        const rawLng = geoLocationItem.lng;
        const lat =
          typeof rawLat === 'number' &&
          isFinite(rawLat) &&
          rawLat >= -90 &&
          rawLat <= 90
            ? rawLat
            : 0;
        const lng =
          typeof rawLng === 'number' &&
          isFinite(rawLng) &&
          rawLng >= -180 &&
          rawLng <= 180
            ? rawLng
            : 0;

        totalWeight += weight;
        // Use validated lat and lng variables instead of accessing geoLocation[j] again
        newLat += lat * weight;
        newLng += lng * weight;
      }
      if (totalWeight === 0) {
        // Fallback to a simple geographic midpoint if weights are zero
        return this.getGeographicMidpoint(geoLocation);
      }

      const updatedMidpoint = {
        lat: newLat / totalWeight,
        lng: newLng / totalWeight,
      };

      const delta = Math.sqrt(
        (updatedMidpoint.lat - midpoint.lat) ** 2 +
          (updatedMidpoint.lng - midpoint.lng) ** 2
      );
      midpoint = updatedMidpoint;

      if (delta < epsilon) break;
    }

    return midpoint;
  }
}

export const locationService = new LocationService();

import { Client } from "@googlemaps/google-maps-services-js";
import type { LocationInfo, GeoLocation } from "../types/location.types";
import { Activity } from "../types/group.types";

export class LocationService {
  private mapsClient: Client;

  constructor() {
    this.mapsClient = new Client({});
  }

  async getTravelTime(origin: GeoLocation, destination: GeoLocation): Promise<number> {
    try {
      const mapsApiKey = process.env.MAPS_API_KEY;
      if (!mapsApiKey) {
        throw new Error('MAPS_API_KEY environment variable is not set');
      }
      const response = await this.mapsClient.distancematrix({
        params: {
          origins: [`${origin.lat},${origin.lng}`],
          destinations: [`${destination.lat},${destination.lng}`],
          key: mapsApiKey,
          mode: origin.transitType as unknown,
        },
      });

      const element = response.data.rows[0].elements[0];
      if (element.status !== "OK") throw new Error(`Distance Matrix API error: ${element.status}`);

      const durationSeconds = element.duration.value;
      const SEC_TO_MIN = 60;
      return durationSeconds / SEC_TO_MIN;
    } catch (err) {
      console.error("Error fetching travel time:", err);
      return Infinity;
    }
  }

  // set to private
  getGeographicMidpoint(coords: GeoLocation[]): GeoLocation {
    const DEG_TO_RAD = Math.PI / 180;
    const RAD_TO_DEG = 180 / Math.PI;

    let x = 0; let y = 0; let z = 0;
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
  type = "restaurant",
  radius = 1000,
  maxResults = 10
): Promise<Activity[]> {
  try {
    const response = await this.mapsClient.placesNearby({
      params: {
        location: `${location.lat},${location.lng}`,
        radius,
        type,
        key: process.env.MAPS_API_KEY!,
      },
    });

    if (response.data.status !== "OK") return [];

    // console.log("getting activities list", response.data.results);
    // console.log("First object details", response.data.results[0]);

    const results = response.data.results;
    const resultsArray: unknown[] = Array.isArray(results) ? results : [];
    
    return resultsArray
      .map((place: unknown): Activity | null => {
        if (typeof place !== 'object' || place === null) return null;
        
        const placeObj = place as {
          name?: unknown;
          place_id?: unknown;
          vicinity?: unknown;
          rating?: unknown;
          user_ratings_total?: unknown;
          price_level?: unknown;
          types?: unknown[];
          opening_hours?: { open_now?: unknown };
          business_status?: unknown;
          geometry?: { location?: { lat?: unknown; lng?: unknown } };
        };
        
        const loc = placeObj.geometry?.location;
        if (!placeObj.name || typeof placeObj.name !== 'string' || !loc) return null;
        
        if (typeof loc.lat !== 'number' || typeof loc.lng !== 'number') return null;

        const primaryType = (Array.isArray(placeObj.types) && typeof placeObj.types[0] === 'string') 
          ? placeObj.types[0] 
          : "establishment";
        const openNow = typeof placeObj.opening_hours?.open_now === 'boolean' 
          ? placeObj.opening_hours.open_now 
          : false;

        return {
          name: placeObj.name,
          placeId: typeof placeObj.place_id === 'string' ? placeObj.place_id : '',
          address: typeof placeObj.vicinity === 'string' ? placeObj.vicinity : '',
          rating: typeof placeObj.rating === 'number' ? placeObj.rating : 0,
          userRatingsTotal: typeof placeObj.user_ratings_total === 'number' ? placeObj.user_ratings_total : 0,
          priceLevel: typeof placeObj.price_level === 'number' ? placeObj.price_level : 0,
          type: primaryType,
          latitude: loc.lat,
          longitude: loc.lng,
          businessStatus: typeof placeObj.business_status === 'string' ? placeObj.business_status : "UNKNOWN",
          isOpenNow: openNow,
        };
      })
      .filter((p): p is Activity => p !== null)
      .slice(0, maxResults);

  } catch (err) {
    console.error("Error fetching nearby places:", err);
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
        const lat = loc.address.lat;
        const lng = loc.address.lng;
        if (lat == null || lng == null) {
          throw new Error('Address coordinates are required');
        }
        return {
          lat,
          lng,
          transitType: loc.transitType
        };
      });
  let midpoint = this.getGeographicMidpoint(geoLocation);

  for (let i = 0; i < maxIterations; i++) {
    const travelTimes = await Promise.all(
      geoLocation.map(u => this.getTravelTime(u, midpoint))
    );

    let totalWeight = 0;
    let newLat = 0; let newLng = 0;

    for (let j = 0; j < geoLocation.length; j++) {
      //const weight = 1 / (travelTimes[j] + 1e-6); //should be travel time, not 1/traveltime
      // Validate array access to prevent object injection: ensure index is in bounds for both arrays
      if (!Array.isArray(travelTimes) || !Array.isArray(geoLocation) || j < 0 || j >= travelTimes.length || j >= geoLocation.length) {
        continue;
      }
      // Validate array element access to prevent object injection
      const rawWeightValue = travelTimes[j];
      const rawWeight = typeof rawWeightValue === 'number' ? rawWeightValue : 0;
      // Validate weight to prevent object injection: ensure it's a finite number and non-negative
      const weight = isFinite(rawWeight) && rawWeight >= 0 ? rawWeight : 0;
      
      // Validate array element access to prevent object injection
      const geoLocationItemValue = geoLocation[j];
      const geoLocationItem = geoLocationItemValue;
      // TypeScript guarantees lat and lng exist on GeoLocation
      const rawLat = geoLocationItem.lat;
      const rawLng = geoLocationItem.lng;
      const lat = typeof rawLat === 'number' && isFinite(rawLat) && rawLat >= -90 && rawLat <= 90 ? rawLat : 0;
      const lng = typeof rawLng === 'number' && isFinite(rawLng) && rawLng >= -180 && rawLng <= 180 ? rawLng : 0;
      
      totalWeight += weight;
      newLat += lat * weight;
      newLng += lng * weight;
    }
    if (totalWeight === 0) {
          // Fallback to a simple geographic midpoint if weights are zero
          return this.getGeographicMidpoint(geoLocation);
    }

    const updatedMidpoint = { lat: newLat / totalWeight, lng: newLng / totalWeight };

    const delta = Math.sqrt(
      (updatedMidpoint.lat - midpoint.lat) ** 2 + (updatedMidpoint.lng - midpoint.lng) ** 2
    );
    midpoint = updatedMidpoint;

    if (delta < epsilon) break;
  }

  return midpoint;
}

//   async findMultipleMeetingPoints(
//   users: UserLocation[],
//   transitType: string,
//   k: number = 3,
//   maxIterations: number = 20,
//   epsilon: number = 1e-5
// ): Promise<UserLocation[]> {

//   if (users.length <= k) return users.map(u => ({ lat: u.lat, lng: u.lng }));
//   let centroids: UserLocation[] = users.slice(0, k).map(u => ({ lat: u.lat, lng: u.lng }));

//   for (let iter = 0; iter < maxIterations; iter++) {
//     const clusters: UserLocation[][] = Array.from({ length: k }, () => []);

//     for (const user of users) {
//       const times = await Promise.all(centroids.map(c => this.getTravelTime(user, c, transitType)));
//       const nearestIndex = times.indexOf(Math.min(...times));
//       clusters[nearestIndex].push(user);
//     }

//     let converged = true;
//     for (let i = 0; i < k; i++) {
//       if (clusters[i].length === 0) continue;

//       let newCentroid = await this.findOptimalMeetingPoint(clusters[i], transitType, 10, epsilon);
//       const delta = Math.sqrt(
//         (newCentroid.lat - centroids[i].lat) ** 2 + (newCentroid.lng - centroids[i].lng) ** 2
//       );

//       if (delta > epsilon) converged = false;
//       centroids[i] = newCentroid;
//     }

//     if (converged) break;
//   }

//   return centroids;
// }

}

export const locationService = new LocationService();

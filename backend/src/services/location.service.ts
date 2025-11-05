import { Client } from "@googlemaps/google-maps-services-js";
import type { LocationInfo, GeoLocation } from "../types/location.types";
import { format } from "path";
import { Activity } from "../types/group.types";

export class LocationService {
  private mapsClient: Client;

  constructor() {
    this.mapsClient = new Client({});
  }

  async getTravelTime(origin: GeoLocation, destination: GeoLocation): Promise<number> {
    try {
      const response = await this.mapsClient.distancematrix({
        params: {
          origins: [`${origin.lat},${origin.lng}`],
          destinations: [`${destination.lat},${destination.lng}`],
          key: process.env.MAPS_API_KEY!,
          mode: origin.transitType as any,
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

    let x = 0, y = 0, z = 0;
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
  type: string = "restaurant",
  radius: number = 1000,
  maxResults: number = 10
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

    return (response.data.results || [])
      .map(place => {
        const loc = place.geometry?.location;
        if (!place.name || !loc) return null;

        const primaryType = place.types?.[0] || "establishment";
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
          businessStatus: place.business_status ?? "UNKNOWN",
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
  .filter(loc => loc.address.lat && loc.address.lng)
  .map(loc => ({
        lat: loc.address.lat!,
        lng: loc.address.lng!,
        transitType: loc.transitType
      }));
  let midpoint = this.getGeographicMidpoint(geoLocation);

  for (let i = 0; i < maxIterations; i++) {
    const travelTimes = await Promise.all(
      geoLocation.map(u => this.getTravelTime(u, midpoint))
    );

    let totalWeight = 0;
    let newLat = 0, newLng = 0;

    for (let j = 0; j < geoLocation.length; j++) {
      //const weight = 1 / (travelTimes[j] + 1e-6); //should be travel time, not 1/traveltime
      const weight = travelTimes[j];
      totalWeight += weight;
      newLat += geoLocation[j].lat * weight;
      newLng += geoLocation[j].lng * weight;
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

}

export const locationService = new LocationService();

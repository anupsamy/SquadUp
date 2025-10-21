import { Client } from "@googlemaps/google-maps-services-js";
import type { UserLocation } from "../types/location.types";

export class LocationService {
  private mapsClient: Client;

  constructor() {
    this.mapsClient = new Client({});
  }

  async getTravelTime(origin: UserLocation, destination: UserLocation, transitType: string): Promise<number> {
    try {
      const response = await this.mapsClient.distancematrix({
        params: {
          origins: [`${origin.lat},${origin.lng}`],
          destinations: [`${destination.lat},${destination.lng}`],
          key: process.env.MAPS_API_KEY!,
          mode: transitType as any,
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

  getGeographicMidpoint(coords: UserLocation[]): UserLocation {
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

  async getTopNearbyPlaces(
  location: UserLocation,
  type: string = "restaurant",
  radius: number = 5000,
  maxResults: number = 5
): Promise<{ name: string; lat: number; lng: number }[]> {
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

    return (response.data.results || [])
      .map(place => {
        const loc = place.geometry?.location;
        if (!place.name || !loc) return null;
        return {
          name: place.name,
          lat: loc.lat,
          lng: loc.lng,
        };
      })
      .filter((p): p is { name: string; lat: number; lng: number } => p !== null)
      .slice(0, maxResults);

  } catch (err) {
    console.error("Error fetching nearby places:", err);
    return [];
  }
}

  async findOptimalMeetingPoint(
  users: UserLocation[],
  transitType: string,
  maxIterations = 20,
  epsilon = 1e-5
): Promise<UserLocation> {
  let midpoint = this.getGeographicMidpoint(users);

  for (let i = 0; i < maxIterations; i++) {
    const travelTimes = await Promise.all(
      users.map(u => this.getTravelTime(u, midpoint, transitType))
    );

    let totalWeight = 0;
    let newLat = 0, newLng = 0;

    for (let j = 0; j < users.length; j++) {
      const weight = 1 / (travelTimes[j] + 1e-6);
      totalWeight += weight;
      newLat += users[j].lat * weight;
      newLng += users[j].lng * weight;
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

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

  async findOptimalMeetingPoint(
    users: UserLocation[],
    transitType: string,
    maxIterations = 10
  ): Promise<UserLocation> {
    let coords = users.map(u => ({ lat: u.lat, lng: u.lng }));
    let midpoint = this.getGeographicMidpoint(coords);

    for (let i = 0; i < maxIterations; i++) {
      const travelTimes = await Promise.all(
        users.map(u => this.getTravelTime({ lat: u.lat, lng: u.lng }, midpoint, transitType))
      );

      let totalWeight = 0;
      let newLat = 0, newLng = 0;

      for (let j = 0; j < users.length; j++) {
        const weight = travelTimes[j]; 
        totalWeight += weight;
        newLat += users[j].lat * weight;
        newLng += users[j].lng * weight;
      }

      midpoint = { lat: newLat / totalWeight, lng: newLng / totalWeight };
    }

    return midpoint;
  }
}

export const locationService = new LocationService();

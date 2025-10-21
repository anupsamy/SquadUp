import {Client} from "@googlemaps/google-maps-services-js";

// Remeber to install @googlemaps/google-maps-services-js

interface LatLng {
  lat: number;
  lng: number;
}
//export class LocationService(location: [number, number]): Promise<[number, number]> {
  // Location service methods would go here
  // Call get user address in the same group
  // Calculate the midpoint
  // Get the travel time from each user to the midpoint
  // Iterative improvement to minimize total travel time
  // Return the optimal meeting location
//}

// This will calculate the geographic midpoint given an array of latitude and longitude coordinates
// This will be called after querying user locations from the database - groupController
export function getGeographicMidpoint(coords: LatLng[]): LatLng {
  if (coords.length === 0) throw new Error("No coordinates provided");

  let x = 0, y = 0, z = 0;

  for (const { lat, lng } of coords) {
    const latRad = (lat * Math.PI) / 180;
    const lonRad = (lng * Math.PI) / 180;

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

  return {
    lat: (latMid * 180) / Math.PI,
    lng: (lonMid * 180) / Math.PI,
  };
}

async function getUserTravelTime(src: LatLng, dest: LatLng): Promise<string> {


  return "15 mins"; // Placeholder
}
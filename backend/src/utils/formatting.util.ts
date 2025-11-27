import { GeoLocation } from "../types/location.types";


export const GeoLocationToMidpoint = (geoLocation: GeoLocation): string => {
  return `${geoLocation.lat},${geoLocation.lng}`;
}

export const latLngFromMidpoint = (
  midpoint: string
): { lat: number; lng: number } => {
  const parts = midpoint.trim().split(' ');
  if (parts.length !== 2) {
    throw new Error(`Invalid midpoint string: "${midpoint}"`);
  }

  const lat = parseFloat(parts[0]);
  const lng = parseFloat(parts[1]);

  if (Number.isNaN(lat) || Number.isNaN(lng)) {
    throw new Error(`Invalid numeric midpoint: "${midpoint}"`);
  }

  return { lat, lng };
};
import type { Address } from "./address.types";
import { Activity } from "./group.types";
import type { TransitType } from "./transit.types";

export interface LocationInfo {
  address: Address;
  transitType: TransitType;
}

export interface GeoLocation {
    formatted?: String,
    lat: number,
    lng: number,
    transitType?: TransitType
}

// types/location.ts (update existing)
export type getLocationResponse = {
  message: string;
  data?: {
    midpoint: {
      location: {
        lat: number;
        lng: number;
      }
    };
    activities?: Activity[];
  };
};
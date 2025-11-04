import type { Address } from "./address.types";
import { Activity } from "./group.types";
import type { TransitType } from "./transit.types";

export interface LocationInfo {
  address: Address;
  transitType: TransitType;
}

export interface GeoLocation {
    formatted?: string,
    lat: number,
    lng: number,
    transitType?: TransitType
}

// export interface Activity {
//   name: string;
//   placeId: string;
//   address: string;
//   rating: number;
//   userRatingsTotal: number;
//   priceLevel: number;
//   type: string;
//   latitude: number;
//   longitude: number;
//   businessStatus: string;
//   isOpenNow: boolean;
// }

// types/location.ts (update existing)
export interface getLocationResponse {
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
}
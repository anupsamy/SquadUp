import type { Address } from "./address.types";
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

export type getGeoLocationResponse = {
  message: string;
    data?: {
      lat: number,
      lng: number,
    };
}

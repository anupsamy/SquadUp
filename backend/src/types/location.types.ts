import type { Address } from "./address.types";
import type { TransitType } from "./transit.types";

export interface LocationInfo {
  address: Address;
  transitType: TransitType;
}

export interface GeoLocation {
    lat: number,
    lng: number,
    transitType?: TransitType
}

import { z } from "zod";

export interface Address {
  formatted: string;  // e.g. "1600 Amphitheatre Pkwy, Mountain View, CA"
  placeId?: string;   // from Google Places API
  lat?: number;
  lng?: number;
  components?: {
    streetNumber?: string;
    route?: string;
    city?: string;
    province?: string;
    country?: string;
    postalCode?: string;
  };
}

export const addressSchema = z.object({
  formatted: z.string().min(1, "Formatted address is required"),
  placeId: z.string().optional(),
  lat: z.number().optional(),
  lng: z.number().optional(),
  components: z
    .object({
      streetNumber: z.string().optional(),
      route: z.string().optional(),
      city: z.string().optional(),
      province: z.string().optional(),
      country: z.string().optional(),
      postalCode: z.string().optional(),
    })
    .optional(),
});
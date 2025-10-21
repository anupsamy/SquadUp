import { z } from "zod";
import mongoose from 'mongoose';

const AddressComponentsSchema = new mongoose.Schema(
  {
    streetNumber: { type: String, required: false },
    route: { type: String, required: false },
    city: { type: String, required: false },
    province: { type: String, required: false },
    country: { type: String, required: false },
    postalCode: { type: String, required: false },
  },
  { _id: false } // prevents nested _id fields
);

export const mongoAddressSchema = new mongoose.Schema(
  {
    formatted: { type: String, required: true },
    placeId: { type: String, required: false },
    lat: { type: Number, required: false },
    lng: { type: Number, required: false },
    components: { type: AddressComponentsSchema, required: false },
  },
  { _id: false }
);

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

export const zodAddressSchema = z.object({
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
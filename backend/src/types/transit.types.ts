import { z } from 'zod';
export const TRANSIT_TYPES = [
  'driving',
  'walking',
  'bicycling',
  'transit',
] as const;

export type TransitType = (typeof TRANSIT_TYPES)[number];

export const transitTypeSchema = z.enum(TRANSIT_TYPES);

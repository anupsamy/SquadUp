import mongoose, { Document } from 'mongoose';
import z from 'zod';
import {Address, zodAddressSchema} from './address.types';
import { TransitType, transitTypeSchema } from './transit.types';

// User model
// ------------------------------------------------------------
export interface IUser extends Document {
  _id: mongoose.Types.ObjectId;
  googleId: string;
  email: string;
  name: string;
  profilePicture?: string;
  address?: Address;
  transitType?: TransitType;
  bio: string;
  createdAt: Date;
  updatedAt: Date;
}

// Zod schemas
// ------------------------------------------------------------
export const createUserSchema = z.object({
  email: z.string().min(1),
  name: z.string().min(1),
  googleId: z.string().min(1),
  profilePicture: z.string().optional(),
  address: zodAddressSchema.optional(),
  transitType: transitTypeSchema.optional(),
});

export const updateProfileSchema = z.object({
  name: z.string().min(1).optional(),
  address: zodAddressSchema.optional(),
  transitType: transitTypeSchema.optional(),
  profilePicture: z.string().min(1).optional(),
});

// Request types
// ------------------------------------------------------------
export interface GetProfileResponse {
  message: string;
  data?: {
    user: IUser;
  };
}

export type UpdateProfileRequest = z.infer<typeof updateProfileSchema>;

// Generic types
// ------------------------------------------------------------
export interface GoogleUserInfo {
  googleId: string;
  email: string;
  name: string;
  profilePicture?: string;
}

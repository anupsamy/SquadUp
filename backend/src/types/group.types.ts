import mongoose, { Schema, Document } from 'mongoose';
import z from 'zod';
import { UserModel, userModel } from '../user.model';
import { GoogleUserInfo } from '../types/user.types';
import {Address} from './address.types';
import {TransitType, transitTypeSchema } from './transit.types';
import { GeoLocation } from './location.types';

// Group model
// ------------------------------------------------------------
export interface IGroup extends Document {
    _id: mongoose.Types.ObjectId;
    groupName:string;
    meetingTime: string;
    joinCode: string;
    groupLeaderId: GroupUser;
    expectedPeople: number;
    groupMemberIds: GroupUser[]; //Change to object of users later maybe,
    midpoint: string,
    activityType: string,
    selectedActivity?: Activity;
    createdAt: Date;
  }


// Zod schemas
// ------------------------------------------------------------
const addressSchema = z.object({
  formatted: z.string().min(1, "Formatted address is required"),
  lat: z.number().optional(),
  lng: z.number().optional(),
});

export const basicGroupSchema = z.object({
  joinCode: z.string().min(6, 'Join code is required'),
  groupName: z.string().min(1, 'Group name is required'),
  meetingTime: z.string().min(1, 'Meeting time is required'),
  groupLeaderId: z.object({
    id: z.string().min(1, 'User ID is required'),
    name: z.string().min(1, "Name is required"),
    email: z.string().min(1, "Email is required"),
    address: addressSchema.optional(),
    transitType: transitTypeSchema.optional()
  }),
  expectedPeople: z.number().int().min(1, 'Expected people must be at least 1'),
  groupMemberIds: z.array(z.object({
    id: z.string().min(1, 'User ID is required'),
    name: z.string().min(1, "Name is required"),
    email: z.string().min(1, "Email is required"),
    address: addressSchema.optional(),
    transitType: transitTypeSchema.optional()
  })).optional(),
  midpoint: z.string().default('').optional(),
  activityType: z.string().min(1, 'Activity type is required')

});

export const createGroupSchema = z.object({
  groupName: z.string().min(1, 'Group name is required'),
  meetingTime: z.string().min(1, 'Meeting time is required'),
  groupLeaderId: z.object({
    id: z.string().min(1, 'User ID is required'),
    name: z.string().min(1, "Name is required"),
    email: z.string().min(1, "Email is required"),
    address: addressSchema.optional(),
    transitType: transitTypeSchema.optional()
  }),
  expectedPeople: z.number().int().min(1, 'Expected people must be at least 1'),
  activityType: z.string().min(1, 'Activity type is required')
});

export const updateGroupSchema = z.object({
  joinCode: z.string().min(6, 'Join code is required'),
  expectedPeople: z.number().max(100).optional(),
   groupMemberIds: z.array(z.object({
    id: z.string().min(1, 'User ID is required'),
    name: z.string().min(1, "Name is required"),
    email: z.string().min(1, "Email is required"),
    address: addressSchema.optional(),
    transitType: transitTypeSchema.optional()
  })).optional(),
  meetingTime: z.string().optional(),
  midpoint: z.string().default("").optional(),
  activityType: z.string().optional()
});

//Activity model

export interface Activity {
  name: string;
  placeId: string;
  address: string;
  rating: number;
  userRatingsTotal: number;
  priceLevel: number;
  type: string;
  latitude: number;
  longitude: number;
  businessStatus: string;
  isOpenNow: boolean;
};

export const activitySchema = new Schema({
  name: { type: String, required: true },
  placeId: { type: String, required: true },
  address: { type: String, required: true },
  rating: { type: Number, required: true },
  userRatingsTotal: { type: Number, required: true },
  priceLevel: { type: Number, required: true },
  type: { type: String, required: true },
  latitude: { type: Number, required: true },
  longitude: { type: Number, required: true },
  businessStatus: { type: String, required: true },
  isOpenNow: { type: Boolean, required: true },
}, { _id: false }); // _id: false prevents creating an _id for subdocument

export const activityZodSchema = z.object({
  name: z.string(),
  placeId: z.string(),
  address: z.string(),
  rating: z.number(),
  userRatingsTotal: z.number(),
  priceLevel: z.number(),
  type: z.string(),
  latitude: z.number(),
  longitude: z.number(),
  businessStatus: z.string(),
  isOpenNow: z.boolean(),
});

//Activity model

export interface Activity {
  name: string;
  placeId: string;
  address: string;
  rating: number;
  userRatingsTotal: number;
  priceLevel: number;
  type: string;
  latitude: number;
  longitude: number;
  businessStatus: string;
  isOpenNow: boolean;
}

// Request types
// ------------------------------------------------------------
export type GetGroupResponse = {
  message: string;
  data?: {
    group: IGroup;
  };
};

export type GetAllGroupsResponse = {
  message: string;
  data?: {
    groups: IGroup[];
  };
};

export type CreateGroupRequest = z.infer<typeof createGroupSchema>;
export type UpdateGroupRequest = z.infer<typeof updateGroupSchema>;

// Generic types
// ------------------------------------------------------------
export type BasicGroupInfo = {
    joinCode: string;
    groupName: string;
    meetingTime: string;
    groupLeaderId: GroupUser;
    expectedPeople: number;
    groupMemberIds?: GroupUser[];
    activityType: string
};

export type CreateGroupInfo = {
  groupName: string;
  meetingTime: string;
  groupLeaderId: GroupUser;
  expectedPeople: number;
  activityType: string;
};

export type UpdateInfo = {
    joinCode: string;
    expectedPeople: number;
    groupMemberIds: GroupUser[];
};

export type GroupUser = {
  id: string;
  name: string;
  email: string;
  address?: Address,
  transitType?: TransitType
}






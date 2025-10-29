import mongoose, { Document } from 'mongoose';
import z from 'zod';
import { HOBBIES } from '../hobbies';
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
  midpoint: z.string().default('').optional()

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
  midpoint: z.string().default('').optional()
});

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
};

export type CreateGroupInfo = {
  groupName: string;
  meetingTime: string;
  groupLeaderId: GroupUser;
  expectedPeople: number;
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






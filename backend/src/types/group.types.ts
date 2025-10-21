import mongoose, { Document } from 'mongoose';
import z from 'zod';
import { HOBBIES } from '../hobbies';
import { UserModel, userModel } from '../user.model';
import { GoogleUserInfo } from '../types/user.types';

// Group model
// ------------------------------------------------------------
export interface IGroup extends Document {
    _id: mongoose.Types.ObjectId;
    groupName:string;
    meetingTime: string;
    joinCode: string;
    groupLeaderId: GroupUser;
    expectedPeople: number;
    groupMemberIds: String[]; //Change to object of users later maybe
    createdAt: Date;
  }


// Zod schemas
// ------------------------------------------------------------
export const basicGroupSchema = z.object({
  joinCode: z.string().min(6, 'Join code is required'),
  groupName: z.string().min(1, 'Group name is required'),
  meetingTime: z.string().min(1, 'Meeting time is required'),
  groupLeaderId: z.object({
    id: z.string().min(1, 'User ID is required'),
    name: z.string().min(1, "Name is required"),
    email: z.string().min(1, "Email is required")
  }),
  expectedPeople: z.number().int().min(1, 'Expected people must be at least 1'),
  groupMemberIds: z.array(z.string()).default([]).optional(),
});

export const createGroupSchema = z.object({
  groupName: z.string().min(1, 'Group name is required'),
  meetingTime: z.string().min(1, 'Meeting time is required'),
  groupLeaderId: z.object({
    id: z.string().min(1, 'User ID is required'),
    name: z.string().min(1, "Name is required"),
    email: z.string().min(1, "Email is required")
  }),
  expectedPeople: z.number().int().min(1, 'Expected people must be at least 1'),
});

export const updateGroupSchema = z.object({
  expectedPeople: z.number().max(100).optional(),
  groupMemberIds: z.array(z.string()).default([]).optional(),
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
    groupMemberIds?: String[];
};

export type CreateGroupInfo = {
  groupName: string;
  meetingTime: string;
  groupLeaderId: GroupUser;
  expectedPeople: number;
};

export type UpdateInfo = {
    expectedPeople: number;
    groupMemberIds: String[];
};

export type GroupUser = {
  id: string;
  name: string;
  email: string;
}




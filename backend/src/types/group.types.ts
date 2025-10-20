import mongoose, { Document } from 'mongoose';
import z from 'zod';
import { HOBBIES } from '../hobbies';
import { userModel } from '../user.model';

// Group model
// ------------------------------------------------------------
export interface IGroup extends Document {
    _id: mongoose.Types.ObjectId;
    groupName:string;
    meetingTime: string;
    joinCode: string;
    groupLeaderId: string;
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
  groupLeaderId: z.string().min(1, 'Group leader ID is required'),
  expectedPeople: z.number().int().min(1, 'Expected people must be at least 1'),
  groupMemberIds: z.array(z.string()).default([]).optional(),
});

export const createGroupSchema = z.object({
  groupName: z.string().min(1, 'Group name is required'),
  meetingTime: z.string().min(1, 'Meeting time is required'),
  groupLeaderId: z.string().min(1, 'Group leader ID is required'),
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
export type CreateGroupRequest = z.infer<typeof createGroupSchema>;
export type UpdateGroupRequest = z.infer<typeof updateGroupSchema>;

// Generic types
// ------------------------------------------------------------
export type BasicGroupInfo = {
    joinCode: string;
    groupName: string;
    meetingTime: string;
    groupLeaderId: string;
    expectedPeople: number;
    groupMemberIds?: String[];
};

export type CreateGroupInfo = {
  groupName: string;
  meetingTime: string;
  groupLeaderId: string;
  expectedPeople: number;
};

export type UpdateInfo = {
    expectedPeople: number;
    groupMemberIds: String[];
};


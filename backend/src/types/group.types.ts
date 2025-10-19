import mongoose, { Document } from 'mongoose';
import z from 'zod';
import { HOBBIES } from '../hobbies';
import { userModel } from '../user.model';

// Group model
// ------------------------------------------------------------
export interface IGroup extends Document {
    _id: mongoose.Types.ObjectId;
    meetingTime: Date
    joinCode: string;
    groupLeader: string;
    expectedPeople: number;
    groupMemberIds: String[]; //Change to object of users later maybe
    createdAt: Date;
  }


// Zod schemas
// ------------------------------------------------------------
export const createGroupSchema = z.object({
  joinCode: z.string().min(5),
  groupLeaderId: z.string().min(1),
  expectedPeople: z.number().max(100),
  groupMemberIds: z.array(z.string()).default([]), //Change to object of users later maybe
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

export type UpdateGroupRequest = z.infer<typeof updateGroupSchema>;

// Generic types
// ------------------------------------------------------------
export type BasicGroupInfo = {
    meetingTime: Date
    groupLeader: string;
    expectedPeople: number;
};

export type UpdateInfo = {
    expectedPeople: number;
    groupMemberIds: String[];
};


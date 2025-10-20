import mongoose, { Schema } from 'mongoose';
import { z } from 'zod';

import { HOBBIES } from './hobbies';
import {
    BasicGroupInfo,
  basicGroupSchema,
  CreateGroupInfo,
  createGroupSchema,
  IGroup,
  updateGroupSchema,
} from './types/group.types';
import logger from './utils/logger.util';


const groupSchema = new Schema<IGroup>(
    {
      groupName: {
        type: String,
        required: true,
        unique: true,
        index: true,
      },
        meetingTime: {
        type: String,
        required: true,
        unique: true,
        index: true,
      },
        joinCode: {
        type: String,
        required: true,
        unique: true,
        lowercase: true,
        trim: true,
      },
        groupLeaderId: {
        type: String,
        required: true,
        trim: true,
      },
      expectedPeople: {
        type: Number,
        required: false,
        trim: true,
      },
      groupMemberIds: {
        type: new Array<String>,
        required: false,
        trim: true,
      },
    },
    {
      timestamps: true,
    }
);

export class GroupModel {
    private group: mongoose.Model<IGroup>;
  
    constructor() {
      this.group = mongoose.model<IGroup>('Group', groupSchema);
    }
  
    async create(groupInfo: BasicGroupInfo): Promise<IGroup> {
      try {
        console.error('GroupModel BasicGroupInfo:', groupInfo);
        const validatedData = basicGroupSchema.parse(groupInfo);

  
        return await this.group.create(validatedData);
      } catch (error) {
        if (error instanceof z.ZodError) {
          console.error('Validation error:', error.issues);
          throw new Error('Invalid update data');
        }
        console.error('Error updating user:', error);
        throw new Error('Failed to update user');
      }
    }
  
    async update(
      groupId: mongoose.Types.ObjectId,
      group: Partial<IGroup>
    ): Promise<IGroup | null> {
      try {
        const validatedData = updateGroupSchema.parse(group);
  
        const updatedGroup = await this.group.findByIdAndUpdate(
          groupId,
          validatedData,
          {
            new: true,
          }
        );
        return updatedGroup;
      } catch (error) {
        logger.error('Error updating group:', error);
        throw new Error('Failed to update group');
      }
    }
  
    async delete(groupId: mongoose.Types.ObjectId): Promise<void> {
      try {
        await this.group.findByIdAndDelete(groupId);
      } catch (error) {
        logger.error('Error deleting group:', error);
        throw new Error('Failed to delete group');
      }
    }
  
    async findById(_id: mongoose.Types.ObjectId): Promise<IGroup | null> { //NOTE: check if group by google id makes sesne
      try {
        const group = await this.group.findOne({ _id });
  
        if (!group) {
          return null;
        }
  
        return group;
      } catch (error) {
        console.error('Error finding group by Google ID:', error);
        throw new Error('Failed to find group');
      }
    }
  }
  
  export const groupModel = new GroupModel();
  
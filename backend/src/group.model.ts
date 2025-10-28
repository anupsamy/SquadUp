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
  GroupUser,
} from './types/group.types';
import {userModel, UserModel} from './user.model';
import {GoogleUserInfo} from './types/user.types';
import logger from './utils/logger.util';


const groupSchema = new Schema<IGroup>(
    {
      groupName: {
        type: String,
        required: true,
      },
        meetingTime: {
        type: String,
        required: true,
      },
        joinCode: {
        type: String,
        required: true,
        unique: true,
        index: true,
        lowercase: true,
        trim: true,
      },
        groupLeaderId: {
        type: {
          id: String, // Define the fields of GoogleUserInfo
          name: String,
          email: String,
        },
        required: true,
      },
      expectedPeople: {
        type: Number,
        required: false,
        trim: true,
      },
      groupMemberIds: {
        type: [{
          id: String, // Define the fields of GoogleUserInfo
          name: String,
          email: String,
        },
      ],
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
        console.error('GroupModel ValidatedData:', validatedData);

  
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

    async findAll(): Promise<IGroup[]> {
      try {
        const groups = await this.group.find(); // Fetch all groups
        console.error('GroupModel findAll 1:', groups[4].groupMemberIds);
        console.error('GroupModel findAll 2:', groups[4]);
        return groups;
      } catch (error) {
        logger.error('Error fetching all groups:', error);
        throw new Error('Failed to fetch all groups');
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

    async updateGroupByJoinCode(
      joinCode: string,
      group: Partial<IGroup>
    ): Promise<IGroup | null> {
      try {
        console.error('GroupModel joinCode:', joinCode);
        console.error('GroupModel update by joinCode group:', group);
        const validatedData = updateGroupSchema.parse(group);
        console.error('GroupModel validatedData:', validatedData);
        const updatedGroup = await this.group.findOneAndUpdate(
          {joinCode},
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
  
    async delete(joinCode:string): Promise<void> {
      try {
        const deletedGroup = await this.group.findOneAndDelete({ joinCode });
        if (!deletedGroup) {
            throw new Error(`Group with joinCode '${joinCode}' not found`);
        }
      } catch (error) {
        logger.error('Error deleting user:', error);
        throw new Error('Failed to delete user');
      }
    }

    async findByJoinCode(joinCode: string): Promise<IGroup | null> {
      try {
        const group = await this.group.findOne({ joinCode }); // Query the database
        console.error('GroupModel findByJoinCode:', group);
        return group;
      } catch (error) {
        logger.error('Error finding group by joinCode:', error);
        throw new Error('Failed to find group by joinCode');
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
  
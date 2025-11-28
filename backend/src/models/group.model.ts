import mongoose, { Schema } from 'mongoose';
import { z } from 'zod';
import {
  BasicGroupInfo,
  basicGroupSchema,
  CreateGroupInfo,
  createGroupSchema,
  IGroup,
  updateGroupSchema,
  activitySchema,
  activityZodSchema,
  GroupUser,
  Activity,
} from '../types/group.types';
import { addressSchema, userModel, UserModel } from '../models/user.model';
import { GoogleUserInfo } from '../types/user.types';
import logger from '../utils/logger.util';
import { LocationService } from '../services/location.service';
import { GeoLocation } from '../types/location.types';

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
        id: { type: String, required: true },
        name: { type: String, required: true },
        email: { type: String, required: true },
        address: {
          type: {
            formatted: { type: String, required: true },
            lat: Number,
            lng: Number,
          },
          required: false,
        },
        transitType: { type: String, required: false },
      },
      required: true,
    },

    expectedPeople: {
      type: Number,
      required: false,
      trim: true,
    },
    groupMemberIds: {
      type: [
        {
          id: { type: String, required: true },
          name: { type: String, required: true },
          email: { type: String, required: true },
          address: {
            type: {
              formatted: { type: String, required: true },
              lat: Number,
              lng: Number,
            },
            required: false,
          },
        transitType: { type: String, required: false, trim: true },
        travelTime: { type: String, required: false, trim: true }
        },
      ],
      required: false,
      trim: true,
    },
    midpoint: {
      type: String,
      required: false,
      trim: true,
    },
    autoMidpoint: {
      type: Boolean,
      required: true
    },
    selectedActivity: {
      type: activitySchema,
      required: false,
    },
    activityType: {
      type: String,
      required: true,
      trim: true,
    },
    activities: {
      type: [activitySchema],
      required: false,
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
      //console.error('GroupModel BasicGroupInfo:', groupInfo);
      //console.log('GroupModel.create - Input Data:', groupInfo);
      const validatedData = basicGroupSchema.parse(groupInfo);
      //console.error('GroupModel ValidatedData:', validatedData);

      return await this.group.create(validatedData);
    } catch (error) {
      if (error instanceof z.ZodError) {
        console.error('Validation error:', error.issues);
        throw new Error('Invalid update data');
      }
      console.error('Error updating user:', error);
      throw new Error('Failed to update group');
    }
  }

  async findUserGroups(userId: string): Promise<IGroup[]> {
    try {
      const groups = await this.group.find({
        $or: [{ 'groupLeaderId.id': userId }, { 'groupMemberIds.id': userId }],
      });
      return groups;
    } catch (error) {
      logger.error('Error fetching user groups:', error);
      throw new Error('Failed to fetch user groups');
    }
  }
  //unused
  // async update(
  //   groupId: mongoose.Types.ObjectId,
  //   group: Partial<IGroup>
  // ): Promise<IGroup | null> {
  //   try {
  //     const validatedData = updateGroupSchema.parse(group);
  //     // Type assertion for validated data to match Mongoose UpdateQuery type
  //     const typedValidatedData = validatedData as Partial<IGroup>;

  //     const updatedGroup = await this.group.findByIdAndUpdate(
  //       groupId,
  //       typedValidatedData,
  //       {
  //         new: true,
  //       }
  //     );

  //     return updatedGroup;
  //   } catch (error) {
  //     logger.error('Error updating group:', error);
  //     throw new Error('Failed to update group');
  //   }
  // }

  async updateGroupByJoinCode(
    joinCode: string,
    group: Partial<IGroup>
  ): Promise<IGroup | null> {
    try {
      const validatedData = updateGroupSchema.parse(group);
      const updatedGroup = await this.group.findOneAndUpdate(
        { joinCode },
        validatedData,
        {
          new: true,
        }
      );
      logger.info(
        `Model update result for ${joinCode}:`,
        JSON.stringify(updatedGroup)
      );
      return updatedGroup;
    } catch (error) {
      if (error instanceof z.ZodError) {
        //console.error('Validation error:', error.issues);
        throw new Error('Invalid update data');
      }
      //logger.error('Error updating group:', error);
      throw new Error('Failed to update group');
    }
  }

  async delete(joinCode: string): Promise<void> {
    try {
      const deletedGroup = await this.group.findOneAndDelete({ joinCode });
      if (!deletedGroup) {
        throw new Error(`Group with joinCode '${joinCode}' not found`);
      }
    } catch (error) {
      //logger.error('Error deleting user:', error);
      throw new Error('Failed to delete group');
    }
  }

  async findByJoinCode(joinCode: string): Promise<IGroup | null> {
    try {
      const group = await this.group.findOne({ joinCode });
      return group;
    } catch (error) {
      throw new Error('Failed to find group by joinCode');
    }
  }

  async updateSelectedActivity(
    joinCode: string,
    activity: Activity
  ): Promise<IGroup | null> {
    try {
      const validatedActivity = activityZodSchema.parse(activity);

      const updatedGroup = await this.group.findOneAndUpdate(
        { joinCode },
        { selectedActivity: validatedActivity },
        { new: true }
      );

      if (!updatedGroup) {
        throw new Error(`Group with joinCode '${joinCode}' not found`);
      }

      return updatedGroup;
    } catch (error) {
      if (error instanceof z.ZodError) {
        //logger.error('Validation error for activity:', error.issues);
        throw new Error('Invalid activity data');
      }
      //logger.error('Error updating selected activity:', error);
      throw new Error('Failed to update selected activity');
    }
  }

  async removeUserFromGroup(joinCode: string, userId: string): Promise<void> {
    try {
      const result = await this.group.findOneAndUpdate(
        { joinCode },
        { $pull: { groupMemberIds: { id: userId } } }
      );

      if (!result) {
        throw new Error(`Group with joinCode '${joinCode}' not found`);
      }
    } catch (error) {
      logger.error('Error removing user from group:', error);
      throw new Error('Failed to remove user from group');
    }
  }

  async transferLeadership(
    joinCode: string,
    newLeader: GroupUser,
    remainingMembers: GroupUser[]
  ): Promise<void> {
    try {
      const result = await this.group.findOneAndUpdate(
        { joinCode },
        {
          groupLeaderId: newLeader,
          groupMemberIds: remainingMembers,
        }
      );

      if (!result) {
        throw new Error(`Group with joinCode '${joinCode}' not found`);
      }
    } catch (error) {
      logger.error('Error transferring leadership:', error);
      throw new Error('Failed to transfer leadership');
    }
  }

  async deleteGroup(joinCode: string): Promise<void> {
    try {
      const result = await this.group.findOneAndDelete({ joinCode });

      if (!result) {
        throw new Error(`Group with joinCode '${joinCode}' not found`);
      }
    } catch (error) {
      logger.error('Error deleting group:', error);
      throw new Error('Failed to delete group');
    }
  }

  async updateMemberTravelTime(
    group: IGroup | null,
    locationService: LocationService,
    hasSelect: boolean = false
  ): Promise<IGroup | null> {
    try {
       if (!group) return group;

      const joinCode = group.joinCode;
      const selectedActivity = group.selectedActivity;
      const unknownTravelTime = 'N/A'

      const existingGroupMemberIds = group.groupMemberIds;
      if (!existingGroupMemberIds) return group;

      // assign "N/A" if no activity has been selected
      if (!selectedActivity || !hasSelect) {
        const updatedMembers: GroupUser[] = (group.groupMemberIds ?? []).map((user: GroupUser) => ({
          id: user.id,
          name: user.name,
          address: user.address,
          email: user.email,
          transitType: user.transitType,
          travelTime: unknownTravelTime
        }));

        return await groupModel.updateGroupByJoinCode(group.joinCode, {
          joinCode: group.joinCode,
          groupMemberIds: updatedMembers,
        });
      }

      const location: GeoLocation = {
        lat: selectedActivity.latitude,
        lng: selectedActivity.longitude,
      };

      // Update travel time
      const updatedMembers: GroupUser[] = await Promise.all(
        (existingGroupMemberIds).map(async (user: GroupUser) => {
          if (user.address?.lat != null && user.address?.lng != null) {
            const travelTime = await locationService.getTravelTime(
              { lat: user.address.lat, lng: user.address.lng, transitType: user.transitType },
              location
            );

            return {
              id: user.id,
              name: user.name,
              address: user.address,
              email: user.email,
              transitType: user.transitType,
              travelTime: travelTime.toFixed(2).toString()
            };
          }
          return user;
        })
      );

      // Update in database
      const updatedGroup = await groupModel.updateGroupByJoinCode(joinCode, {
        joinCode,
        groupMemberIds: updatedMembers,
      });

      return updatedGroup;
    } catch (error) {
      console.error('Error: ', error);
      throw new Error('Failed to update member travel time');
    }
  }
}

export const groupModel = new GroupModel();

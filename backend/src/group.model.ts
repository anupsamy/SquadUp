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
  activitySchema,
  activityZodSchema,
  GroupUser,
  Activity,
} from './types/group.types';
import {addressSchema, userModel, UserModel} from './user.model';
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
            id: { type: String, required: true },
            name: { type: String, required: true },
            email: { type: String, required: true },
            address: {
              type: {
                formatted: { type: String, required: true },
                lat: Number,
                lng: Number
              },
              required: false
            },
            transitType: { type: String, required: false }
          },
          required: true
        },

      expectedPeople: {
        type: Number,
        required: false,
        trim: true,
      },
      groupMemberIds: {
          type: [{
            id: { type: String, required: true },
            name: { type: String, required: true },
            email: { type: String, required: true },
            address: {
              type: {
                formatted: { type: String, required: true },
                lat: Number,
                lng: Number
              },
              required: false
            },
            transitType: { type: String, required: false, trim: true }
          }],
          required: false,
          trim: true
        },
      midpoint: {
        type: String,
        required: false,
        trim: true
      },
    selectedActivity: {
      type: activitySchema,
      required: false,
    },
    activityType: {
      type: String,
      required: true,
      trim: true
    }
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

    async findAll(): Promise<IGroup[]> {
      try {
        const groups = await this.group.find(); // Fetch all groups
        // console.error('GroupModel findAll 1:', groups[4].groupMemberIds);
        // console.error('GroupModel findAll 2:', groups[4]);
        return groups;
      } catch (error) {
        //logger.error('Error fetching all groups:', error);
        throw new Error('Failed to fetch all groups');
      }
    }


    async updateGroupByJoinCode(
      joinCode: string,
      group: Partial<IGroup>
    ): Promise<IGroup | null> {
      try {
        //console.error('GroupModel joinCode:', joinCode);
        //console.error('GroupModel update by joinCode group:', group);
        const validatedData = updateGroupSchema.parse(group);
        //console.error('GroupModel validatedData:', validatedData);
        const updatedGroup = await this.group.findOneAndUpdate(
          {joinCode},
          validatedData,
          {
            new: true,
          }
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

    async delete(joinCode:string): Promise<void> {
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
        const group = await this.group.findOne({ joinCode }); // Query the database
        //console.error('GroupModel findByJoinCode:', group);
        return group;
      } catch (error) {
        //logger.error('Error finding group by joinCode:', error);
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

    //TODO REMOVE
    async getActivities(joinCode: string): Promise<Activity[]> {
      try {
        // Verify the group exists (optional but good practice)
        const group = await this.group.findOne({ joinCode });
        if (!group) {
          throw new Error(`Group with joinCode '${joinCode}' not found`);
        }

        // Return hardcoded dummy data
        return this.getDefaultActivities();
      } catch (error) {
        //logger.error('Error getting activities:', error);
        throw new Error('Failed to get activities');
      }
    }

    //TODO REMOVE
    private getDefaultActivities(): Activity[] {
      return [
        {
          name: "Sushi Palace one",
          placeId: "ChIJN1t_tDeuEmsRUsoyG83frY58",
          address: "5678 Oak St, Vancouver",
          rating: 4.7,
          userRatingsTotal: 512,
          priceLevel: 3,
          type: "restaurant",
          latitude: 49.2627,
          longitude: -123.1407,
          businessStatus: "OPERATIONAL",
          isOpenNow: true
        },
        {
          name: "Pizza Garden two",
          placeId: "ChIJN1t_tDeuEmsRUsoyG83frY47",
          address: "1234 Main St, Vancouver",
          rating: 4.3,
          userRatingsTotal: 256,
          priceLevel: 2,
          type: "restaurant",
          latitude: 49.2827,
          longitude: -123.1207,
          businessStatus: "OPERATIONAL",
          isOpenNow: true
        },
        {
          name: "Brew Bros Coffee three",
          placeId: "ChIJN1t_tDeuEmsRUsoyG83frY59",
          address: "9010 Broadway, Vancouver",
          rating: 4.5,
          userRatingsTotal: 318,
          priceLevel: 1,
          type: "cafe",
          latitude: 49.275,
          longitude: -123.13,
          businessStatus: "OPERATIONAL",
          isOpenNow: true
        }
      ];
    }


    async leaveGroup(joinCode: string, userId: string): Promise<{ success: boolean; deleted: boolean; newLeader?: GroupUser }> {
      try {
        const group = await this.group.findOne({ joinCode });

        if (!group) {
          throw new Error(`Group with joinCode '${joinCode}' not found`);
        }

        // Check if the user is the group leader
        const isLeader = group.groupLeaderId.id === userId;

        // Remove user from group members
        const updatedMembers = (group.groupMemberIds || []).filter(member => member.id !== userId);

        // If the user is the leader and there are other members, transfer leadership
        if (isLeader && updatedMembers.length > 0) {
          // Transfer leadership to the first member (next person who joined)
          const newLeader = updatedMembers[0];
          const remainingMembers = updatedMembers.slice(1);

          const updatedGroup = await this.group.findOneAndUpdate(
            { joinCode },
            {
              groupLeaderId: newLeader,
              groupMemberIds: remainingMembers
            },
            { new: true }
          );

          return {
            success: true,
            deleted: false,
            newLeader: newLeader
          };
        }
        // If the user is the leader and there are no other members, delete the group
        else if (isLeader && updatedMembers.length === 0) {
          await this.group.findOneAndDelete({ joinCode });
          return {
            success: true,
            deleted: true
          };
        }
        // If the user is not the leader, just remove them from members
        else {
          const updatedGroup = await this.group.findOneAndUpdate(
            { joinCode },
            { groupMemberIds: updatedMembers },
            { new: true }
          );

          return {
            success: true,
            deleted: false
          };
        }
      } catch (error) {
        logger.error('Error leaving group:', error);
        throw new Error('Failed to leave group');
      }
    }
  }

  export const groupModel = new GroupModel();

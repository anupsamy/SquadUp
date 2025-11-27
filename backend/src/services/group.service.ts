// services/group.service.ts

import logger from '../utils/logger.util';
import { groupModel } from '../models/group.model';
import { BasicGroupInfo, GroupUser, IGroup } from '../types/group.types';
import { AppError, AppErrorFactory } from '../utils/appError.util';
import { TransitType } from '../types/transit.types';

export class GroupService {
  private static instance: GroupService;

  private constructor() {}

  static getInstance(): GroupService {
    if (!GroupService.instance) {
      GroupService.instance = new GroupService();
    }
    return GroupService.instance;
  }

  async createGroup(
    groupData: Omit<BasicGroupInfo, 'joinCode'>
  ): Promise<IGroup> {
    try {
      const joinCode = this.generateJoinCode();

      const groupInfo: BasicGroupInfo = {
        joinCode,
        ...groupData,
      };

      const newGroup = await groupModel.create(groupInfo);
      logger.info(
        `Group created: ${groupData.groupName} (joinCode: ${joinCode})`
      );
      return newGroup;
    } catch (error) {
      logger.error('Failed to create group:', error);
      throw AppErrorFactory.internalServerError(
        'Failed to create group',
        error instanceof Error ? error.message : undefined
      );
    }
  }

  private generateJoinCode(): string {
    return Math.random().toString(36).slice(2, 8).toUpperCase();
  }

  async getUserGroups(userId: string): Promise<IGroup[]> {
    try {
      const groups = await groupModel.findUserGroups(userId);
      logger.info(`Fetched ${groups.length} groups for user ${userId}`);
      return groups;
    } catch (error) {
      logger.error('Failed to fetch user groups:', error);
      throw AppErrorFactory.internalServerError(
        'Failed to fetch groups',
        error instanceof Error ? error.message : undefined
      );
    }
  }

  async getGroupByJoinCode(joinCode: string): Promise<IGroup> {
    try {
      const group = await groupModel.findByJoinCode(joinCode);

      if (!group) {
        throw AppErrorFactory.notFound('Group', `joinCode '${joinCode}'`);
      }

      logger.info(`Fetched group by joinCode: ${joinCode}`);
      return group;
    } catch (error) {
      if (error instanceof AppError) throw error;
      logger.error('Failed to fetch group by joinCode:', error);

      throw AppErrorFactory.internalServerError(
        'Failed to fetch group by joinCode',
        error instanceof Error ? error.message : undefined
      );
    }
  }
  async joinGroupByJoinCode(
    joinCode: string,
    userId: string,
    userInfo: GroupUser
  ): Promise<IGroup> {
    try {
      const group = await groupModel.findByJoinCode(joinCode);

      if (!group) {
        throw AppErrorFactory.notFound('Group', `joinCode '${joinCode}'`);
      }

      // Check if user is already a member or leader
      const isLeader = group.groupLeaderId.id === userId;
      const isMember = (group.groupMemberIds ?? []).some(
        member => member.id === userId
      );

      if (isLeader || isMember) {
        throw AppErrorFactory.conflict('User is already in this group');
      }

      const updatedGroup = await groupModel.updateGroupByJoinCode(joinCode, {
        joinCode,
        groupMemberIds: [...(group.groupMemberIds ?? []), userInfo],
      });

      if (!updatedGroup) {
        throw AppErrorFactory.notFound('Group', `joinCode '${joinCode}'`);
      }

      logger.info(`User ${userId} joined group ${joinCode}`);
      return updatedGroup;
    } catch (error) {
      if (error instanceof AppError) throw error;
      logger.error('Failed to join group:', error);
      throw AppErrorFactory.internalServerError(
        'Failed to join group',
        error instanceof Error ? error.message : undefined
      );
    }
  }
  async updateGroupSettings(
    joinCode: string,
    userId: string,
    updates: {
      address?: { formatted: string; lat?: number; lng?: number };
      transitType?: TransitType;
      meetingTime?: string;
      expectedPeople?: number;
    }
  ): Promise<IGroup> {
    try {
      const group = await groupModel.findByJoinCode(joinCode);

      if (!group) {
        throw AppErrorFactory.notFound('Group', `joinCode '${joinCode}'`);
      }

      const isLeader = group.groupLeaderId.id === userId;
      const memberIndex = (group.groupMemberIds ?? []).findIndex(
        member => member.id === userId
      );
      const isMember = memberIndex !== -1;

      if (!isLeader && !isMember) {
        throw AppErrorFactory.notFound('User', 'in this group');
      }

      // Build update object
      const updateData: Partial<IGroup> = {};

      // Update leader-only fields ONLY if user is leader
      if (isLeader) {
        if (updates.meetingTime !== undefined) {
          updateData.meetingTime = updates.meetingTime;
        }
        if (updates.expectedPeople !== undefined) {
          updateData.expectedPeople = updates.expectedPeople;
        }
      }

      // Update user fields (address/transitType)
      const hasUserFields =
        updates.address !== undefined || updates.transitType !== undefined;
      if (hasUserFields) {
        if (isLeader) {
          // Update leader info in groupLeaderId
          updateData.groupLeaderId = { ...group.groupLeaderId };
          if (updates.address) {
            updateData.groupLeaderId.address = updates.address;
          }
          if (updates.transitType) {
            updateData.groupLeaderId.transitType = updates.transitType;
          }
        }

        // Update in groupMemberIds array (leader is also in this array)
        if (isMember) {
          const updatedMembers = [...(group.groupMemberIds ?? [])];
          const member = updatedMembers[memberIndex];
          if (updates.address) {
            member.address = updates.address;
          }
          if (updates.transitType) {
            member.transitType = updates.transitType;
          }
          updateData.groupMemberIds = updatedMembers;
        }
      }

      const updatedGroup = await groupModel.updateGroupByJoinCode(joinCode, {
        ...updateData,
        joinCode,
      });

      if (!updatedGroup) {
        throw AppErrorFactory.notFound('Group', `joinCode '${joinCode}'`);
      }

      logger.info(`User ${userId} updated group settings for ${joinCode}`);
      return updatedGroup;
    } catch (error) {
      if (error instanceof AppError) throw error;
      logger.error('Failed to update group settings:', error);
      throw AppErrorFactory.internalServerError(
        'Failed to update group settings',
        error instanceof Error ? error.message : undefined
      );
    }
  }

  async deleteGroupByJoinCode(joinCode: string, userId: string): Promise<void> {
    try {
      const group = await groupModel.findByJoinCode(joinCode);

      if (!group) {
        throw AppErrorFactory.notFound('Group', `joinCode '${joinCode}'`);
      }

      const isLeader = group.groupLeaderId.id === userId;

      if (!isLeader) {
        throw AppErrorFactory.forbidden(
          'Only the group leader can delete the group'
        );
      }

      await groupModel.delete(joinCode);

      logger.info(`User ${userId} deleted group ${joinCode}`);
    } catch (error) {
      if (error instanceof AppError) throw error;
      logger.error('Failed to delete group:', error);
      throw AppErrorFactory.internalServerError(
        'Failed to delete group',
        error instanceof Error ? error.message : undefined
      );
    }
  }
}

export const groupService = GroupService.getInstance();

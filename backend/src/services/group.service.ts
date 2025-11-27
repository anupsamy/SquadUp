// services/group.service.ts

import logger from '../utils/logger.util';
import { groupModel } from '../models/group.model';
import { BasicGroupInfo, IGroup } from '../types/group.types';
import { AppErrorFactory } from '../utils/appError.util';

export class GroupService {
  private static instance: GroupService;

  private constructor() {}

  static getInstance(): GroupService {
    if (!GroupService.instance) {
      GroupService.instance = new GroupService();
    }
    return GroupService.instance;
  }

  /**
   * Create a new group
   */
  async createGroup(groupData: Omit<BasicGroupInfo, 'joinCode'>): Promise<IGroup> {
    try {
      const joinCode = this.generateJoinCode();

      const groupInfo: BasicGroupInfo = {
        joinCode,
        ...groupData,
      };

      const newGroup = await groupModel.create(groupInfo);
      logger.info(`Group created: ${groupData.groupName} (joinCode: ${joinCode})`);
      return newGroup;
    } catch (error) {
      logger.error('Failed to create group:', error);
      throw AppErrorFactory.internalServerError(
        'Failed to create group',
        error instanceof Error ? error.message : undefined
      );
    }
  }

  /**
   * Generate a unique join code
   */
  private generateJoinCode(): string {
    return Math.random().toString(36).slice(2, 8).toUpperCase();
  }
}

export const groupService = GroupService.getInstance();
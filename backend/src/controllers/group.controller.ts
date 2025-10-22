import { NextFunction, Request, Response } from 'express';

import { GetProfileResponse, UpdateProfileRequest } from '../types/user.types';
import logger from '../utils/logger.util';
import { MediaService } from '../services/media.service';
import { groupModel } from '../group.model';
import { UserModel } from '../user.model';
import { GetGroupResponse, UpdateGroupRequest, CreateGroupRequest, GetAllGroupsResponse } from '../types/group.types';

export class GroupController {
  async createGroup(
    req: Request<unknown, unknown, CreateGroupRequest>,
    res: Response<GetGroupResponse>,
    next: NextFunction
  ) {
    try {
      const {groupName, meetingTime, groupLeaderId, expectedPeople} = req.body;
      const joinCode = Math.random().toString(36).slice(2, 8);

      // Use the GroupModel to create the group
      const newGroup = await groupModel.create({
        joinCode,
        groupName,
        groupLeaderId: groupLeaderId,
        expectedPeople,
        groupMemberIds: [],
        meetingTime: meetingTime, // Default to current time for now
      });
      console.error('GroupController newGroup:', newGroup);
      res.status(201).json({
        message: 'Group ${groupName} created successfully',
        data: {
          group: newGroup,
        }
      });
    } catch (error) {
      logger.error('Failed to create group:', error);
      next(error);
    }
  }

  async getAllGroups(req: Request, res: Response<GetAllGroupsResponse>, next: NextFunction) {
    try {
      // Fetch all groups from the database
      const groups = await groupModel.findAll();
      console.error('GroupController getAllGroups:', groups);

      res.status(200).json({
        message: 'Groups fetched successfully',
        data: { groups },
      });
    } catch (error) {
      logger.error('Failed to fetch groups:', error);
      next(error);
    }
  }


  async getGroupByJoinCode(
    req: Request<{ joinCode: string }>, // Define the route parameter type
    res: Response<GetGroupResponse>,
    next: NextFunction
  ) {
    try {
      const { joinCode } = req.params; // Extract the joinCode from the route parameters
  
      // Query the database for the group with the given joinCode
      const group = await groupModel.findByJoinCode(joinCode);
      console.error('GroupController getGroupByJoinCode:', group);
  
      if (!group) {
        return res.status(404).json({
          message: `Group with joinCode '${joinCode}' not found`,
        });
      }
  
      res.status(200).json({
        message: 'Group fetched successfully',
        data: { group },
      });
    } catch (error) {
      logger.error('Failed to fetch group by joinCode:', error);
      next(error);
    }
  }


  getGroup(req: Request, res: Response<GetGroupResponse>) {
    const group = req.group!;
    res.status(200).json({
      message: 'Group fetched successfully',
      data: { group },
    });
  }

  async updateGroup(
    req: Request<unknown, unknown, UpdateGroupRequest>,
    res: Response<GetGroupResponse>,
    next: NextFunction
  ) {
    try {
      const group = req.group!;

      const updatedGroup = await groupModel.update(group._id, req.body);

      if (!updatedGroup) {
        return res.status(404).json({
          message: 'Group not found',
        });
      }

      res.status(200).json({
        message: 'Group info updated successfully',
        data: { group: updatedGroup },
      });
    } catch (error) {
      logger.error('Failed to update group info:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to update group info',
        });
      }

      next(error);
    }
  }

  async deleteGroup(req: Request, res: Response, next: NextFunction) {
    try {
      const group = req.group!;

      //await MediaService.deleteAllUserImages(user._id.toString());

      await groupModel.delete(group._id); //NOTE: see if anything else needs to be removed first

      res.status(200).json({
        message: 'group deleted successfully',
      });
    } catch (error) {
      logger.error('Failed to delete group:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to delete group',
        });
      }

      next(error);
    }
  }
}

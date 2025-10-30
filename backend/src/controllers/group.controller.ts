import { NextFunction, Request, Response } from 'express';

import { GetProfileResponse, UpdateProfileRequest } from '../types/user.types';
import logger from '../utils/logger.util';
import { MediaService } from '../services/media.service';
import { groupModel } from '../group.model';
import { userModel } from '../user.model';
import { GetGroupResponse, UpdateGroupRequest, CreateGroupRequest, GetAllGroupsResponse, IGroup } from '../types/group.types';
import { locationService } from '../services/location.service';
import { getLocationResponse, LocationInfo } from '../types/location.types';

export class GroupController {
  async createGroup(
    req: Request<unknown, unknown, CreateGroupRequest>,
    res: Response<GetGroupResponse>,
    next: NextFunction
  ) {
    try {
      const {groupName, meetingTime, groupLeaderId, expectedPeople, activityType} = req.body;
      console.log(activityType);
      const joinCode = Math.random().toString(36).slice(2, 8);

      // Use the GroupModel to create the group
      const newGroup = await groupModel.create({
        joinCode,
        groupName,
        groupLeaderId: groupLeaderId,
        expectedPeople,
        groupMemberIds: [groupLeaderId],
        meetingTime: meetingTime,  // Default to current time for now,
        activityType: activityType
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
      // console.error('GroupController getAllGroups:', groups);
      // console.error('GroupController groups[4].members:', groups[4].groupMemberIds);
      //   console.error('GroupController groups[4]:', groups[4]);
      const sanitizedGroups:IGroup[] = groups.map(group => ({
      ...group.toObject(),
      groupMemberIds: group.groupMemberIds || [], // Replace null with an empty array
      }));
      // console.error('GroupController sanitizedGroups:', sanitizedGroups[4]);

      res.status(200).json({
        message: 'Groups fetched successfully',
        data: { groups: sanitizedGroups },
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
        data: {
          group: {
            ...group.toObject(),
            groupMemberIds: group.groupMemberIds || [], // Replace null with an empty array
          },
      }});
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

  async updateGroupByJoinCode(
    req: Request<unknown, unknown, UpdateGroupRequest>,
    res: Response<GetGroupResponse>,
    next: NextFunction
  ) {
    try {
      const {joinCode, expectedPeople, groupMemberIds} = req.body;
      console.error('GroupController updateByJoincode joinCode:', joinCode);
      console.error('GroupController updateByJoincode groupMembers:', groupMemberIds);
      console.error('GroupController updateByJoincode expectedPeople:', expectedPeople);
      const updatedGroup = await groupModel.updateGroupByJoinCode(joinCode,
        {joinCode, expectedPeople,
        groupMemberIds: groupMemberIds || []});

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

  async deleteGroupByJoinCode(
    req: Request<{joinCode: string}>,
    res: Response,
    next: NextFunction) {
    try {
      const {joinCode} = req.params;

      //await MediaService.deleteAllUserImages(user._id.toString());

      await groupModel.delete(joinCode); //NOTE: see if anything else needs to be removed first

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

  async getMidpointByJoinCode(
    req: Request<{ joinCode: string }>, // Define the route parameter type
    res: Response<getLocationResponse>,
    next: NextFunction
  ) {
    try {
      const { joinCode } = req.params; // Extract the joinCode from the route parameters

      // Query the database for the group with the given joinCode
      const group = await groupModel.findByJoinCode(joinCode);

      if (!group) {
        throw new Error("Group not found");
      }

      if (group.midpoint) {
        const parts = group.midpoint.trim().split(" ");
        res.status(200).json({
          message: 'Get midpoint successfully!',
          data: {
            location: {
              lat: parseFloat(parts[0]),
              lng: parseFloat(parts[1]),
            }
        }});
      }

      const locationInfo: LocationInfo[] = group.groupMemberIds
      .filter(member => member.address && member.transitType)
      .map(member => ({
        address: member.address!,
        transitType: member.transitType!,
      }));

      const optimizedPoint = await locationService.findOptimalMeetingPoint(locationInfo);
      const activityList = await locationService.getActivityList(optimizedPoint);

      if (!group) {
        return res.status(404).json({
          message: `Group with joinCode '${joinCode}' not found`,
        });
      }

      const lat = optimizedPoint.lat;
      const lng = optimizedPoint.lng

      const midpoint = lat.toString() + ' ' + lng.toString();

      // Need error handler
      const updatedGroup = await groupModel.updateGroupByJoinCode(joinCode, {joinCode, midpoint});

      res.status(200).json({
        message: 'Get midpoint successfully!',
        data: {
          location: {
            lat: lat,
            lng: lng,
          }
      }});
    } catch (error) {
      logger.error('Failed to get midpoint joinCode:', error);
      next(error);
    }
  }

  async getActivities(req: Request, res: Response): Promise<void> {
  try {
    const { joinCode } = req.query;

    if (!joinCode || typeof joinCode !== 'string') {
      res.status(400).json({
        success: false,
        message: 'Join code is required',
      });
      return;
    }

    const activities = await groupModel.getActivities(joinCode);

    res.status(200).json({
      success: true,
      data: activities,
    });
  } catch (error) {
    logger.error('Error fetching activities:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch activities',
    });
  }
}

async selectActivity(req: Request, res: Response): Promise<void> {
  try {
    const { joinCode, placeId } = req.body;

    if (!joinCode || !placeId) {
      res.status(400).json({
        success: false,
        message: 'Join code and place ID are required',
      });
      return;
    }

    // First get the activities to find the one matching placeId
    const activities = await groupModel.getActivities(joinCode);
    const selectedActivity = activities.find(activity => activity.placeId === placeId);

    if (!selectedActivity) {
      res.status(404).json({
        success: false,
        message: 'Activity not found',
      });
      return;
    }

    // Update the group with the selected activity
    const updatedGroup = await groupModel.updateSelectedActivity(joinCode, selectedActivity);

    res.status(200).json({
      success: true,
      data: updatedGroup,
    });
  } catch (error) {
    logger.error('Error selecting activity:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to select activity',
    });
  }
}

async getMidpoints(req: Request, res: Response): Promise<void> {
  try {
    const joinCode = req.query.joinCode;

    if (!joinCode || typeof joinCode !== 'string') {
      res.status(400).json({
        success: false,
        message: 'Join code is required',
      });
      return;
    }

    // Verify the group exists
    const group = await groupModel.findByJoinCode(joinCode);
    if (!group) {
      res.status(404).json({
        success: false,
        message: 'Group not found',
      });
      return;
    }

    // Return dummy midpoint data (3 locations in Vancouver area)
    const midpoints = [
      { latitude: 49.2827, longitude: -123.1207 },
      { latitude: 49.2606, longitude: -123.2460 },
      { latitude: 49.2488, longitude: -123.1163 }
    ];

    res.status(200).json({
      success: true,
      data: midpoints,
    });
  } catch (error) {
    logger.error('Error fetching midpoints:', error);
    res.status(500).json({
      success: false,
      message: 'Failed to fetch midpoints',
    });
  }
}



  async leaveGroup(
    req: Request<{joinCode: string}, unknown, {userId: string}>,
    res: Response,
    next: NextFunction) {
    try {
      const {joinCode} = req.params;
      const {userId} = req.body;

      const result = await groupModel.leaveGroup(joinCode, userId);

      if (result.deleted) {
        res.status(200).json({
          message: 'Group deleted successfully as no members remain',
        });
      } else {
        res.status(200).json({
          message: 'Left group successfully',
          data: result.newLeader ? { newLeader: result.newLeader } : undefined,
        });
      }
    } catch (error) {
      logger.error('Failed to leave group:', error);

      if (error instanceof Error) {
        return res.status(500).json({
          message: error.message || 'Failed to leave group',
        });
      }

      next(error);
    }
  }
}

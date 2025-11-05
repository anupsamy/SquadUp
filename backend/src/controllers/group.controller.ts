import { NextFunction, Request, Response } from 'express';
import crypto from 'crypto';

import logger from '../utils/logger.util';
import { groupModel } from '../group.model';
import { GetGroupResponse, UpdateGroupRequest, CreateGroupRequest, GetAllGroupsResponse, IGroup, Activity, GroupUser } from '../types/group.types';
import { getWebSocketService } from '../services/websocket.service';
import { locationService } from '../services/location.service';
import { GeoLocation, getLocationResponse, LocationInfo } from '../types/location.types';
import { sendGroupJoinFCM, sendGroupLeaveFCM, sendActivitySelectedFCM } from '../services/fcm.service';

export class GroupController {
  async createGroup(
    req: Request<unknown, unknown, CreateGroupRequest>,
    res: Response<GetGroupResponse>,
    next: NextFunction
  ) {
    try {
      const {groupName, meetingTime, groupLeaderId, expectedPeople, activityType} = req.body;
      logger.debug('Creating group with activity type:', activityType);
      const randomBytes = crypto.randomBytes(4);
      const joinCode = randomBytes.readUInt32BE(0).toString(36).slice(0, 6).padStart(6, '0');

      // Use the GroupModel to create the group
      const newGroup = await groupModel.create({
        joinCode,
        groupName,
        groupLeaderId,
        expectedPeople,
        groupMemberIds: [groupLeaderId],
        meetingTime,  // Default to current time for now,
        activityType
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
      const sanitizedGroups: IGroup[] = groups.map((group) => {
        const groupObj = group.toObject() as unknown as IGroup;
        return {
          ...groupObj,
          groupMemberIds: group.groupMemberIds,
        } as IGroup;
      });
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
      // Validate joinCode is a string before use
    const validatedJoinCodeForFind: string = typeof joinCode === 'string' ? joinCode : '';
    if (!validatedJoinCodeForFind) {
      res.status(400).json({
        message: 'Invalid joinCode',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }
    const group = await groupModel.findByJoinCode(validatedJoinCodeForFind);
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
            groupMemberIds: group.groupMemberIds,
          },
        }});
    } catch (error) {
      logger.error('Failed to fetch group by joinCode:', error);
      next(error);
    }
  }


  getGroup(req: Request, res: Response<GetGroupResponse>) {
    const group = req.group;
    if (!group) {
      return res.status(404).json({
        message: 'Group not found',
      });
    }
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
      const group = req.group;
      if (!group) {
        return res.status(404).json({
          message: 'Group not found',
        });
      }

      const updatedGroup = await groupModel.update(group._id, req.body as Partial<IGroup>);

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

  async joinGroupByJoinCode(
    req: Request<unknown, unknown, UpdateGroupRequest>,
    res: Response<GetGroupResponse>,
    next: NextFunction
  ) {
    try {
      const {joinCode, expectedPeople, groupMemberIds} = req.body;

      if (!joinCode || typeof joinCode !== 'string') {
        return res.status(400).json({
          message: 'Join code is required and must be a string',
        });
      }

      // TypeScript now knows joinCode is a string
      const validatedJoinCode: string = joinCode;

      // Get the current group to compare member changes
      const currentGroup = await groupModel.findByJoinCode(validatedJoinCode);
      if (!currentGroup) {
        return res.status(404).json({
          message: 'Group not found',
        });
      }

      const updatedGroup = await groupModel.updateGroupByJoinCode(validatedJoinCode,
        {joinCode: validatedJoinCode, expectedPeople,
        groupMemberIds: groupMemberIds || []});

      if (!updatedGroup) {
        return res.status(404).json({
          message: 'Group not found',
        });
      }

      // Send WebSocket notifications for new members
      const wsService = getWebSocketService();
      if (wsService) {
        const validatedGroupMemberIds: GroupUser[] = Array.isArray(groupMemberIds) ? groupMemberIds : [];
        const currentMemberIds = currentGroup.groupMemberIds.map(member => {
          const memberId: string = typeof member.id === 'string' ? member.id : '';
          return memberId;
        });

        // Find new members (users who joined)
        const joinedMembers = validatedGroupMemberIds.filter(member => {
          const memberId: string = typeof member.id === 'string' ? member.id : '';
          return !currentMemberIds.includes(memberId);
        });

        // Send notifications for each new member
        joinedMembers.forEach(member => {
          const memberName: string = typeof member.name === 'string' ? member.name : '';
          const memberId: string = typeof member.id === 'string' ? member.id : '';
          wsService.notifyGroupJoin(
            validatedJoinCode,
            memberId,
            memberName,
            updatedGroup.groupName
          );
          // FCM topic notification (clients subscribe to topic == joinCode)
          sendGroupJoinFCM(validatedJoinCode, memberName, updatedGroup.groupName, memberId).catch((error: unknown) => {
            logger.error('Failed to send group join FCM notification:', error);
          });
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
      const {joinCode, expectedPeople, groupMemberIds, meetingTime} = req.body;
      // Validate joinCode is a string before use
      const validatedJoinCode: string = typeof joinCode === 'string' ? joinCode : '';
      if (!validatedJoinCode) {
        return res.status(400).json({
          message: 'Invalid joinCode',
        });
      }
      const updatedGroup = await groupModel.updateGroupByJoinCode(validatedJoinCode,
        {joinCode: validatedJoinCode, expectedPeople,
        groupMemberIds: groupMemberIds || [], meetingTime});

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

  async getMidpointByJoinCode( //TODO: decide whether to incorporate activities here
    req: Request<{ joinCode: string }>, // Define the route parameter type
    res: Response<getLocationResponse>,
    next: NextFunction
  ) {
    try {
      const { joinCode } = req.params; // Extract the joinCode from the route parameters

      // Query the database for the group with the given joinCode
      // Validate joinCode is a string before use
    const validatedJoinCodeForFind: string = typeof joinCode === 'string' ? joinCode : '';
    if (!validatedJoinCodeForFind) {
      res.status(400).json({
        message: 'Invalid joinCode',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }
    const group = await groupModel.findByJoinCode(validatedJoinCodeForFind);

      if (!group) {
        throw new Error("Group not found");
      }

      if (group.midpoint) {
        const parts = group.midpoint.trim().split(" ");
        res.status(200).json({
          message: 'Get midpoint successfully!',
          data: {
            midpoint: {
              location: {
                lat: parseFloat(parts[0]),
                lng: parseFloat(parts[1]),
              }
            }
        }});
      }

      const locationInfo: LocationInfo[] = group.groupMemberIds
      .filter(member => member.address != null && member.transitType != null)
      .map(member => {
        const address = member.address;
        const transitType = member.transitType;
        if (address == null || transitType == null) {
          throw new Error('Address and transit type are required');
        }
        return {
          address,
          transitType,
        };
      });

      const optimizedPoint = await locationService.findOptimalMeetingPoint(locationInfo);
      //const activityList = await locationService.getActivityList(optimizedPoint);
      const activityList: Activity[] = [];

      const lat = optimizedPoint.lat;
      const lng = optimizedPoint.lng

      const midpoint = lat.toString() + ' ' + lng.toString();

      const updatedGroup = await groupModel.updateGroupByJoinCode(joinCode, {joinCode, midpoint});
      if (!updatedGroup) {
        return res.status(500).json({
          message: 'Failed to update group midpoint',
        });
      }

      logger.debug('Activities List:', activityList);
      res.status(200).json({
        message: 'Get midpoint successfully!',
        data: {
          midpoint: {
            location: {
              lat,
              lng,
            }
          },
          activities: activityList
        }});
    } catch (error) {
      logger.error('Failed to get midpoint joinCode:', error);
      next(error);
    }
  }

async updateMidpointByJoinCode(
    req: Request<{ joinCode: string }>,
    res: Response<getLocationResponse>,
    next: NextFunction
  ) {
    try {
      const { joinCode } = req.params; // Extract the joinCode from the route parameters

      // Query the database for the group with the given joinCode
      // Validate joinCode is a string before use
    const validatedJoinCodeForFind: string = typeof joinCode === 'string' ? joinCode : '';
    if (!validatedJoinCodeForFind) {
      res.status(400).json({
        message: 'Invalid joinCode',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }
    const group = await groupModel.findByJoinCode(validatedJoinCodeForFind);

      if (!group) {
        throw new Error("Group not found");
      }

      const locationInfo: LocationInfo[] = group.groupMemberIds
      .filter(member => member.address != null && member.transitType != null)
      .map(member => {
        const address = member.address;
        const transitType = member.transitType;
        if (address == null || transitType == null) {
          throw new Error('Address and transit type are required');
        }
        return {
          address,
          transitType,
        };
      });

      const optimizedPoint = await locationService.findOptimalMeetingPoint(locationInfo);
      //const activityList = await locationService.getActivityList(optimizedPoint);
      const activityList: Activity[] = [];

      const lat = optimizedPoint.lat;
      const lng = optimizedPoint.lng

      const midpoint = lat.toString() + ' ' + lng.toString();

      // Need error handler
      const updatedGroup = await groupModel.updateGroupByJoinCode(joinCode, {joinCode, midpoint});
      if (!updatedGroup) {
        return res.status(500).json({
          message: 'Failed to update group midpoint',
        });
      }

      logger.debug('Activities List:', activityList);
      res.status(200).json({
        message: 'Get midpoint successfully!',
        data: {
          midpoint: {
            location: {
              lat,
              lng,
            }
          },
          activities: activityList,
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
        message: 'Join code is required',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }

    // Validate joinCode is a string before use
    const validatedJoinCodeForFind: string = typeof joinCode === 'string' ? joinCode : '';
    if (!validatedJoinCodeForFind) {
      res.status(400).json({
        message: 'Invalid joinCode',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }
    const group = await groupModel.findByJoinCode(validatedJoinCodeForFind);

    if (!group) {
      res.status(404).json({
        message: 'Group not found',
        data: null,
        error: 'NotFound',
        details: null,
      });
      return;
    }

    if (!group.midpoint) {
      res.status(404).json({
        message: 'No midpoint available for this group',
        data: null,
        error: 'NoMidpoint',
        details: null,
      });
      return;
    }

    const parts = group.midpoint.trim().split(' ');
    const location: GeoLocation = {
      lat: Number(parts[0]),
      lng: Number(parts[1]),
    };

    const activities = await locationService.getActivityList(location, group.activityType);

    res.status(200).json({
      message: 'Fetched activities successfully',
      data: activities ,
      error: null,
      details: null,
    });
  } catch (error) {
    logger.error('Error fetching activities:', error);
    res.status(500).json({
      message: 'Failed to fetch activities',
      data: null,
      error: error instanceof Error ? error.message : 'UnknownError',
      details: null,
    });
  }
}

// controller/activityController.ts
async selectActivity(req: Request, res: Response): Promise<void> {
  try {
    const { joinCode, activity } = req.body;

    if (!joinCode || !activity) {
      res.status(400).json({
        message: 'Join code and activity are required',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }

    if (typeof joinCode !== 'string') {
      res.status(400).json({
        message: 'Join code must be a string',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }

    // Validate required activity fields
    if (!activity.placeId || !activity.name) {
      res.status(400).json({
        message: 'Activity must have placeId and name',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }

    // Verify the group exists
    // Validate joinCode is a string before use
    const validatedJoinCodeForFind: string = typeof joinCode === 'string' ? joinCode : '';
    if (!validatedJoinCodeForFind) {
      res.status(400).json({
        message: 'Invalid joinCode',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }
    const group = await groupModel.findByJoinCode(validatedJoinCodeForFind);
    if (!group) {
      res.status(404).json({
        message: 'Group not found',
        data: null,
        error: 'NotFound',
        details: null,
      });
      return;
    }

    // Update the group with the selected activity
    // Validate activity type before passing to model
    const validatedActivity: Activity = typeof activity === 'object' && activity !== null && 'placeId' in activity && 'name' in activity
      ? (activity as Activity)
      : {
          placeId: '',
          name: '',
          address: '',
          rating: 0,
          userRatingsTotal: 0,
          priceLevel: 0,
          type: '',
          latitude: 0,
          longitude: 0,
          businessStatus: '',
          isOpenNow: false,
        };
    const updatedGroup = await groupModel.updateSelectedActivity(joinCode, validatedActivity);

    // Send notifications to group members
    const wsService = getWebSocketService();
    if (wsService && updatedGroup) {
      const leaderId = updatedGroup.groupLeaderId.id || '';
      const leaderName = updatedGroup.groupLeaderId.name || 'Group leader';
      const rawActivityName = activity.name;
      const activityName: string = typeof rawActivityName === 'string' ? rawActivityName : 'an activity';
      
      // Send WebSocket notification
      wsService.notifyGroupUpdate(
        joinCode,
        `${leaderName} selected "${activityName}" for the group`,
        {
          type: 'activity_selected',
          activity,
          leaderId,
          leaderName
        }
      );

      // Send FCM notification (will be suppressed in foreground on client side)
      const activityDataStr = JSON.stringify(activity);
      sendActivitySelectedFCM(
        joinCode,
        activityName,
        updatedGroup.groupName,
        leaderId,
        activityDataStr
      ).catch((error: unknown) => {
        logger.error('Failed to send activity selected FCM notification:', error);
      });
    }

    res.status(200).json({
      message: 'Activity selected successfully',
      data: updatedGroup,
      error: null,
      details: null,
    });
  } catch (error) {
    logger.error('Error selecting activity:', error);
    res.status(500).json({
      message: 'Failed to select activity',
      data: null,
      error: error instanceof Error ? error.message : 'UnknownError',
      details: null,
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
    // Validate joinCode is a string before use
    const validatedJoinCodeForFind: string = typeof joinCode === 'string' ? joinCode : '';
    if (!validatedJoinCodeForFind) {
      res.status(400).json({
        message: 'Invalid joinCode',
        data: null,
        error: 'ValidationError',
        details: null,
      });
      return;
    }
    const group = await groupModel.findByJoinCode(validatedJoinCodeForFind);
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

      // Get the current group to get user info before they leave
      const currentGroup = await groupModel.findByJoinCode(joinCode);
      if (!currentGroup) {
        return res.status(404).json({
          message: 'Group not found',
        });
      }

      // Find the user who is leaving
      const leavingUser = currentGroup.groupMemberIds.find(member => member.id === userId) ??
                         (currentGroup.groupLeaderId.id === userId ? currentGroup.groupLeaderId : null);

      const result = await groupModel.leaveGroup(joinCode, userId);

      // Send WebSocket notification for user leaving
      const wsService = getWebSocketService();
      if (wsService && leavingUser) {
        wsService.notifyGroupLeave(
          joinCode,
          leavingUser.id,
          leavingUser.name,
          currentGroup.groupName
        );
        // FCM topic notification (clients subscribe to topic == joinCode)
        sendGroupLeaveFCM(joinCode, leavingUser.name, currentGroup.groupName, leavingUser.id).catch((error: unknown) => {
          logger.error('Failed to send group leave FCM notification:', error);
        });
      }

      if (result.deleted) {
        // Notify about group deletion
        if (wsService) {
          wsService.notifyGroupUpdate(
            joinCode,
            `Group "${currentGroup.groupName}" has been deleted as no members remain`,
            { deleted: true }
          );
        }

        res.status(200).json({
          message: 'Group deleted successfully as no members remain',
        });
      } else {
        // Notify about leadership transfer if applicable
        if (wsService && result.newLeader) {
          wsService.notifyGroupUpdate(
            joinCode,
            `${result.newLeader.name} is now the new group leader`,
            { newLeader: result.newLeader }
          );
        }

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

  // Test endpoint for WebSocket notifications
  testWebSocketNotification(
    req: Request<{joinCode: string}>,
    res: Response,
    next: NextFunction) {
    try {
      const {joinCode} = req.params;
      const {message, type} = req.body;
      // Validate message is a string before use
      const validatedMessage: string = typeof message === 'string' ? message : 'Test notification from backend';

      const wsService = getWebSocketService();
      if (!wsService) {
        return res.status(500).json({
          message: 'WebSocket service not available',
        });
      }

      // Send a test notification
      wsService.notifyGroupUpdate(
        joinCode,
        validatedMessage,
        { type: type || 'test' }
      );

      res.status(200).json({
        message: 'Test notification sent successfully',
        data: wsService.getStats(),
      });
    } catch (error) {
      logger.error('Failed to send test notification:', error);
      next(error);
    }
  }
}

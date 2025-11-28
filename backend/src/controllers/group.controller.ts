import { NextFunction, Request, Response } from 'express';
import logger from '../utils/logger.util';
import { groupModel } from '../models/group.model';
import {
  GetGroupResponse,
  UpdateGroupRequest,
  CreateGroupRequest,
  GetAllGroupsResponse,
  IGroup,
  Activity,
  UpdateGroupSettingsRequest,
} from '../types/group.types';
import { locationService } from '../services/location.service';
import {
  GeoLocation,
  getLocationResponse,
  LocationInfo,
} from '../types/location.types';
import { Address } from '../types/address.types';
import { TRANSIT_TYPES, TransitType } from '../types/transit.types';
import {
  sendGroupJoinFCM,
  sendGroupLeaveFCM,
  sendActivitySelectedFCM,
} from '../services/fcm.service';
import { AppErrorFactory } from '../utils/appError.util';
import { groupService } from '../services/group.service';
import '../types/express.types';
import {
  validateJoinCode,
  validateTransitType,
  validateUserRequest,
} from '../utils/validation.util';
import { geolocFromMidpoint } from '../utils/formatting.util';

export class GroupController {
  async createGroup(
    req: Request<unknown, unknown, CreateGroupRequest>,
    res: Response<GetGroupResponse>
  ) {
    const {
      groupName,
      meetingTime,
      groupLeaderId,
      expectedPeople,
      activityType,
      autoMidpoint
    } = req.body;

    // Input validation
    if (!groupName || typeof groupName !== 'string') {
      throw AppErrorFactory.badRequest(
        'groupName is required and must be a string'
      );
    }
    if (!meetingTime || typeof meetingTime !== 'string') {
      throw AppErrorFactory.badRequest(
        'meetingTime is required and must be a string'
      );
    }
    if (!groupLeaderId || typeof groupLeaderId !== 'object') {
      throw AppErrorFactory.badRequest(
        'groupLeaderId is required and must be an object'
      );
    }
    if (!activityType || typeof activityType !== 'string') {
      throw AppErrorFactory.badRequest(
        'activityType is required and must be a string'
      );
    }

    const validateAutoMidpoint = autoMidpoint ?? false;


    // Call service to create group
    const newGroup = await groupService.createGroup({
      groupName,
      meetingTime,
      groupLeaderId,
      expectedPeople: expectedPeople || 0,
      groupMemberIds: [groupLeaderId],
      activityType,
      autoMidpoint: validateAutoMidpoint
    });

    res.status(201).json({
      message: `Group ${groupName} created successfully`,
      data: {
        group: newGroup,
      },
    });
  }

  async getAllGroups(req: Request, res: Response<GetAllGroupsResponse>) {
    const user = validateUserRequest(req.user);
    const userId = user.id;
    const groups = await groupService.getUserGroups(userId.toString());

    const sanitizedGroups: IGroup[] = groups.map(group => ({
      ...group.toObject(),
      groupMemberIds: group.groupMemberIds ?? [],
    }));

    res.status(200).json({
      message: 'Groups fetched successfully',
      data: { groups: sanitizedGroups },
    });
  }

  async getGroupByJoinCode(
    req: Request<{ joinCode: string }>,
    res: Response<GetGroupResponse>
  ) {
    const { joinCode } = req.params;
    const validJoinCode = validateJoinCode(joinCode);

    const group = await groupService.getGroupByJoinCode(joinCode);

    res.status(200).json({
      message: 'Group fetched successfully',
      data: {
        group: {
          ...group.toObject(),
          groupMemberIds: group.groupMemberIds || [],
        },
      },
    });
  }

  async joinGroupByJoinCode(
    req: Request<unknown, unknown, { joinCode: string }>,
    res: Response<GetGroupResponse>
  ) {
    const { joinCode } = req.body;
    const user = validateUserRequest(req.user);
    const validJoinCode = validateJoinCode(joinCode);

    const updatedGroup = await groupService.joinGroupByJoinCode(
      validJoinCode,
      user.id.toString(),
      {
        id: user.id.toString(),
        name: user.name,
        email: user.email,
        address: user.address,
        transitType: user.transitType,
      }
    );

    res.status(200).json({
      message: 'Joined group successfully',
      data: { group: updatedGroup },
    });
  }
  //TODO: add back to join grou in group service?
  // Send WebSocket notifications for new members
  /*const wsService = getWebSocketService();
      if (wsService) {
        const currentMemberIds = (currentGroup.groupMemberIds || []).map(member => member.id);
        const newMemberIds = (groupMemberIds || []).map(member => member.id);

        // Find new members (users who joined)
        const joinedMembers = (groupMemberIds || []).filter(member =>
          !currentMemberIds.includes(member.id)
        );

        // Send notifications for each new member
        joinedMembers.forEach(member => {
          wsService.notifyGroupJoin(
            joinCode,
            member.id,
            member.name,
            updatedGroup.groupName
          );
          // FCM topic notification (clients subscribe to topic == joinCode)
          void sendGroupJoinFCM(joinCode, member.name, updatedGroup.groupName, member.id);
        });
      }*/

  //only called from group settings page
  async updateGroupSettings(
    req: Request<unknown, unknown, UpdateGroupSettingsRequest>,
    res: Response<GetGroupResponse>
  ) {
    const { joinCode, address, transitType, meetingTime, expectedPeople, autoMidpoint, activityType } =
      req.body;
    logger.error(
      'Request in groupSettings:',
      JSON.stringify(req.body, null, 2)
    );

    const user = validateUserRequest(req.user);
    const validJoinCode = validateJoinCode(joinCode);
    const validTransitType = transitType
      ? validateTransitType(transitType)
      : undefined;

    const updatedGroup = await groupService.updateGroupSettings(
      validJoinCode,
      user.id.toString(),
      {
        address,
        transitType: validTransitType,
        meetingTime,
        expectedPeople,
        autoMidpoint,
        activityType
      }
    );
    res.status(200).json({
      message: 'Group settings updated successfully',
      data: { group: updatedGroup },
    });
  }

  async deleteGroupByJoinCode(
    req: Request<{ joinCode: string }>,
    res: Response
  ) {
    const { joinCode } = req.params;
    const user = validateUserRequest(req.user);

    validateJoinCode(joinCode);

    await groupService.deleteGroupByJoinCode(joinCode, user.id.toString());

    res.status(200).json({
      message: 'Group deleted successfully',
    });
  }
  async getMidpointByJoinCode(
    req: Request<{ joinCode: string }>,
    res: Response<getLocationResponse>
  ) {
    const { joinCode } = req.params;

    validateJoinCode(joinCode);

    const result = await groupService.getMidpointByJoinCode(joinCode);
    //TODO add the following to service
    /*
const updatedTravelTime = await groupModel.updateMemberTravelTime(updatedGroup, locationService);
    */

    res.status(200).json({
      message: 'Get midpoint successfully!',
      data: result,
    });
  }

  async updateMidpointByJoinCode(
    req: Request<{ joinCode: string }>,
    res: Response<getLocationResponse>
  ) {
    const { joinCode } = req.params;

    validateJoinCode(joinCode);

    await groupService.invalidateMidpoint(joinCode);
    const result = await groupService.getMidpointByJoinCode(joinCode);

    res.status(200).json({
      message: 'Updated midpoint successfully!',
      data: result,
    });
  }

  //TODO: figure out if we can delete (NEED TO BE ADDED BACK)
  // async getActivities(req: Request, res: Response): Promise<void> {
  //   const { joinCode } = req.query;

  //   const validJoinCode = validateJoinCode(joinCode);

  //   const group = await groupModel.findByJoinCode(validJoinCode);

  //   if (!group) {
  //     throw AppErrorFactory.notFound('Group', `joinCode '${joinCode}'`);
  //   }

  //   if (!group.midpoint) {
  //     throw AppErrorFactory.badRequest('Group', 'missing midpoint');
  //   }

  //   const location: GeoLocation = geolocFromMidpoint(group.midpoint);

  //   const activities = await locationService.getActivityList(
  //     location,
  //     group.activityType
  //   );

  //   res.status(200).json({
  //     message: 'Fetched activities successfully',
  //     data: activities,
  //   });
  // }

  async selectActivity(
    req: Request<unknown, unknown, { joinCode: string; activity: Activity }>,
    res: Response<GetGroupResponse>
  ) {
    const { joinCode, activity } = req.body;
    const user = validateUserRequest(req.user);

    const validJoinCode = validateJoinCode(joinCode);

    if (!activity || typeof activity !== 'object') {
      throw AppErrorFactory.badRequest(
        'activity is required and must be an object'
      );
    }

    if (!activity.placeId || !activity.name) {
      throw AppErrorFactory.badRequest('Activity must have placeId and name');
    }

    const updatedGroup = await groupService.selectActivity(
      validJoinCode,
      user.id.toString(),
      activity
    );

    res.status(200).json({
      message: 'Activity selected successfully',
      data: { group: updatedGroup },
    });
  }
  //BELOW FOR SELECT ACTIVITy
  // Send notifications to group members
  //const wsService = getWebSocketService();
  // if (wsService && updatedGroup) {
  //   const leaderId = updatedGroup.groupLeaderId.id || '';
  //   const leaderName = updatedGroup.groupLeaderId.name || 'Group leader';
  //   const rawActivityName = activity.name;
  //   const activityName: string = typeof rawActivityName === 'string' ? rawActivityName : 'an activity';

  //   // Send WebSocket notification
  //   wsService.notifyGroupUpdate(
  //     joinCode,
  //     `${leaderName} selected "${activityName}" for the group`,
  //     {
  //       type: 'activity_selected',
  //       activity,
  //       leaderId,
  //       leaderName
  //     }
  //   );

  //   // Send FCM notification (will be suppressed in foreground on client side)
  //   const activityDataStr = JSON.stringify(activity);
  //   sendActivitySelectedFCM(
  //     joinCode,
  //     activityName,
  //     updatedGroup.groupName,
  //     leaderId,
  //     activityDataStr
  //   ).catch((error: unknown) => {
  //     logger.error('Failed to send activity selected FCM notification:', error);
  //   });
  // }

  // Send notifications to group members
  /*const wsService = getWebSocketService();
    if (wsService && updatedGroup) {
      const leaderId = updatedGroup.groupLeaderId?.id || '';
      const leaderName = updatedGroup.groupLeaderId?.name || 'Group leader';
      const activityName = activity.name || 'an activity';

      // Send WebSocket notification
      wsService.notifyGroupUpdate(
        joinCode,
        `${leaderName} selected "${activityName}" for the group`,
        {
          type: 'activity_selected',
          activity: activity,
          leaderId: leaderId,
          leaderName: leaderName
        }
      );

      // Send FCM notification (will be suppressed in foreground on client side)
      const activityDataStr = JSON.stringify(activity);
      void sendActivitySelectedFCM(
        joinCode,
        activityName,
        updatedGroup.groupName,
        leaderId,
        activityDataStr
      );
    }*/

  async leaveGroup(req: Request<{ joinCode: string }>, res: Response) {
    const { joinCode } = req.params;
    const user = validateUserRequest(req.user);

    const validJoinCode = validateJoinCode(joinCode);

    await groupService.leaveGroup(validJoinCode, user.id.toString());

    res.status(200).json({
      message: 'Left group successfully',
    });
  }
  //leave group notif
  // Send WebSocket notification for user leaving
  /*const wsService = getWebSocketService();
      if (wsService && leavingUser) {
        wsService.notifyGroupLeave(
          joinCode,
          leavingUser.id,
          leavingUser.name,
          currentGroup.groupName
        );
        // FCM topic notification (clients subscribe to topic == joinCode)
        void sendGroupLeaveFCM(joinCode, leavingUser.name, currentGroup.groupName, leavingUser.id);
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
        }*/

  // Test endpoint for WebSocket notifications
  /* async testWebSocketNotification(
    req: Request<{joinCode: string}>,
    res: Response,
    next: NextFunction) {
    try {
      const {joinCode} = req.params;
      const {message, type} = req.body;

      const wsService = getWebSocketService();
      if (!wsService) {
        return res.status(500).json({
          message: 'WebSocket service not available',
        });
      }

      // Send a test notification
      wsService.notifyGroupUpdate(
        joinCode,
        message || 'Test notification from backend',
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
  }*/
}
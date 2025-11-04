import { Router } from 'express';
import { GroupController } from '../controllers/group.controller';
import { validateBody } from '../middleware/validation.middleware';
import { CreateGroupRequest, createGroupSchema, UpdateGroupRequest, updateGroupSchema } from '../types/group.types';

const router = Router();
const groupController = new GroupController();

router.get('/info', groupController.getAllGroups.bind(groupController));

router.get(
    '/activities',
    groupController.getActivities.bind(groupController)
);

router.get(
    '/midpoints',
    groupController.getMidpoints.bind(groupController)
);

router.post(
    '/activities/select',
    (req, res, next) => {
      void groupController.selectActivity(req, res);
    }
);

router.get(
    '/:joinCode', // Define the route parameter
    (req, res, next) => {
      void groupController.getGroupByJoinCode(req, res, next);
    }
);
// Route to create a group
router.post( //have seperate endpoint for updating?
    '/create',
    validateBody<CreateGroupRequest>(createGroupSchema), // Validate the request body
    (req, res, next) => {
      void groupController.createGroup(req, res, next);
    }
);

router.post( //have seperate endpoint for updating?
    '/join',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    (req, res, next) => {
      void groupController.joinGroupByJoinCode(req, res, next);
    }
);

router.post(
    '/update',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    groupController.updateGroupByJoinCode.bind(groupController)
);

router.delete(
    '/delete/:joinCode', // Define the route parameter
    (req, res, next) => {
      void groupController.deleteGroupByJoinCode(req, res, next);
    }
);

router.get(
    '/midpoint/:joinCode',
    groupController.getMidpointByJoinCode.bind(groupController)
);

router.post(
    '/midpoint/:joinCode',
    groupController.updateMidpointByJoinCode.bind(groupController)
);

router.post(
    '/leave/:joinCode', // Define the route parameter
    (req, res, next) => {
      void groupController.leaveGroup(req, res, next);
    }
);

// Test endpoint for WebSocket notifications
router.post(
    '/test-notification/:joinCode',
    (req, res, next) => {
      void groupController.testWebSocketNotification(req, res, next);
    }
);

export default router;
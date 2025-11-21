import { Router } from 'express';
import { GroupController } from '../controllers/group.controller';
import { validateBody } from '../middleware/validation.middleware';
import { CreateGroupRequest, createGroupSchema, UpdateGroupRequest, updateGroupSchema } from '../types/group.types';

const router = Router();
const groupController = new GroupController();

router.get('/info', (req, res, next) => {
  groupController.getAllGroups(req, res, next).catch((error: unknown) => {
    next(error);
  });
});

router.get(
    '/activities',
    groupController.getActivities.bind(groupController)
);

router.post(
    '/activities/select',
    groupController.selectActivity.bind(groupController)
);

router.get(
    '/:joinCode', // Define the route parameter
    (req, res, next) => {
      groupController.getGroupByJoinCode(req, res, next).catch((error: unknown) => {
        next(error);
      });
    }
);
// Route to create a group
router.post( //have seperate endpoint for updating?
    '/create',
    validateBody<CreateGroupRequest>(createGroupSchema), // Validate the request body
    groupController.createGroup
);

router.post( //have seperate endpoint for updating?
    '/join',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    (req, res, next) => {
      groupController.joinGroupByJoinCode(req, res, next).catch((error: unknown) => {
        next(error);
      });
    }
);

router.post(
    '/update',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    (req, res, next) => {
      groupController.updateGroupByJoinCode(req, res, next).catch((error: unknown) => {
        next(error);
      });
    }
);

router.delete(
    '/delete/:joinCode', // Define the route parameter
    (req, res, next) => {
      groupController.deleteGroupByJoinCode(req, res, next).catch((error: unknown) => {
        next(error);
      });
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
      groupController.leaveGroup(req, res, next).catch((error: unknown) => {
        next(error);
      });
    }
);

// Test endpoint for WebSocket notifications
//router.post(
  //  '/test-notification/:joinCode',
    //groupController.testWebSocketNotification.bind(groupController)
//);

export default router;
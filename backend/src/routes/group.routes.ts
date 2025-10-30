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
    groupController.selectActivity.bind(groupController)
);

router.get(
    '/:joinCode', // Define the route parameter
    groupController.getGroupByJoinCode.bind(groupController) // Bind the controller method
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
    groupController.joinGroupByJoinCode.bind(groupController)
);

router.post(
    '/update',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    groupController.updateGroupByJoinCode.bind(groupController)
);

router.delete(
    '/delete/:joinCode', // Define the route parameter
    groupController.deleteGroupByJoinCode.bind(groupController) // Bind the controller method
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
    groupController.leaveGroup.bind(groupController) // Bind the controller method
);

// Test endpoint for WebSocket notifications
router.post(
    '/test-notification/:joinCode',
    groupController.testWebSocketNotification.bind(groupController)
);

export default router;
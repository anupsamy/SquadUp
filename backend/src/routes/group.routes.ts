import { Router } from 'express';
import { GroupController } from '../controllers/group.controller';
import { validateBody } from '../middleware/validation.middleware';
import { CreateGroupRequest, createGroupSchema, UpdateGroupRequest, updateGroupSchema } from '../types/group.types';

const router = Router();
const groupController = new GroupController();

router.get('/info', groupController.getAllGroups.bind(groupController));

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
    '/update',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    groupController.updateGroup
);

export default router;
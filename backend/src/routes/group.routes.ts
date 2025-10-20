import { Router } from 'express';
import { GroupController } from '../controllers/group.controller';
import { validateBody } from '../middleware/validation.middleware';
import { CreateGroupRequest, createGroupSchema, UpdateGroupRequest, updateGroupSchema } from '../types/group.types';

const router = Router();
const groupController = new GroupController();

router.get('/group', groupController.getGroup);
// Route to create a group
router.post( //have seperate endpoint for updating?
    '/group',
    validateBody<CreateGroupRequest>(createGroupSchema), // Validate the request body
    groupController.createGroup
);

router.post( //have seperate endpoint for updating?
    '/group-update',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    groupController.updateGroup
);

export default router;
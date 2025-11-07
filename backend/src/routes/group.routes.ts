import { Router } from 'express';
import { GroupController } from '../controllers/group.controller';
import { validateBody } from '../middleware/validation.middleware';
import { CreateGroupRequest, createGroupSchema, UpdateGroupRequest, updateGroupSchema } from '../types/group.types';
import logger from '../utils/logger.util';

const router = Router();
const groupController = new GroupController();

router.get('/info', (req, res, next) => {
  groupController.getAllGroups(req, res).catch((error: unknown) => {
    next(error);
  });
});

router.get(
    '/activities',
    (req, res, next) => {
      groupController.getActivities(req, res).catch((error: unknown) => {
        next(error);
      });
    }
);

router.post(
    '/activities/select',
    (req, res) => {
      groupController.selectActivity(req, res).catch((error: unknown) => {
        // Error handling is done in the controller method
        logger.error('Unhandled error in selectActivity:', error);
      });
    }
);

router.get(
    '/:joinCode', // Define the route parameter
    (req, res, next) => {
      groupController.getGroupByJoinCode(req, res).catch((error: unknown) => {
        next(error);
      });
    }
);
// Route to create a group
router.post( //have seperate endpoint for updating?
    '/create',
    validateBody<CreateGroupRequest>(createGroupSchema), // Validate the request body
    (req, res, next) => {
      groupController.createGroup(req, res, next).catch((error: unknown) => {
        next(error);
      });
    }
);

router.post( //have seperate endpoint for updating?
    '/join',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    (req, res, next) => {
      groupController.joinGroupByJoinCode(req, res).catch((error: unknown) => {
        next(error);
      });
    }
);

router.post(
    '/update',
    validateBody<UpdateGroupRequest>(updateGroupSchema), // Validate the request body
    (req, res, next) => {
      groupController.updateGroupByJoinCode(req, res).catch((error: unknown) => {
        next(error);
      });
    }
);

router.delete(
    '/delete/:joinCode', // Define the route parameter
    (req, res, next) => {
      groupController.deleteGroupByJoinCode(req, res).catch((error: unknown) => {
        next(error);
      });
    }
);

router.get(
    '/midpoint/:joinCode',
    (req, res, next) => {
      groupController.getMidpointByJoinCode(req, res, next).catch((error: unknown) => {
        next(error);
      });
    }
);

router.post(
    '/midpoint/:joinCode',
    (req, res, next) => {
      groupController.updateMidpointByJoinCode(req, res, next).catch((error: unknown) => {
        next(error);
      });
    }
);

router.post(
    '/leave/:joinCode', // Define the route parameter
    (req, res, next) => {
      groupController.leaveGroup(req, res).catch((error: unknown) => {
        next(error);
      });
    }
);

export default router;
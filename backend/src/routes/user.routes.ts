import { Router } from 'express';

import { UserController } from '../controllers/user.controller';
import { UpdateProfileRequest, updateProfileSchema } from '../types/user.types';
import { validateBody } from '../middleware/validation.middleware';

const router = Router();
const userController = new UserController();

router.get('/profile', userController.getProfile);

router.post(
  '/profile',
  validateBody<UpdateProfileRequest>(updateProfileSchema),
  userController.updateProfile
);

router.delete('/profile', (req, res, next) => {
  void userController.deleteProfile(req, res, next);
});

export default router;

import { Router } from 'express';

import { UserController } from '../controllers/user.controller';
import { UpdateProfileRequest, updateProfileSchema } from '../types/user.types';
import { validateBody } from '../middleware/validation.middleware';

const router = Router();
const userController = new UserController();

router.get('/profile', (req, res, next) => {
  userController.getProfile(req, res);
});

router.post(
  '/profile',
  validateBody<UpdateProfileRequest>(updateProfileSchema),
  (req, res, next) => {
    userController.updateProfile(req, res, next).catch((error: unknown) => {
      next(error);
    });
  }
);

router.delete('/profile', (req, res, next) => {
  userController.deleteProfile(req, res, next).catch((error: unknown) => {
    next(error);
  });
});

export default router;

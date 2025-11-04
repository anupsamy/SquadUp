import { Router } from 'express';

import { AuthController } from '../controllers/auth.controller';
import { AuthenticateUserRequest, authenticateUserSchema } from '../types/auth.types';
import { validateBody } from '../middleware/validation.middleware';

const router = Router();
const authController = new AuthController();

router.post(
  '/signup',
  validateBody<AuthenticateUserRequest>(authenticateUserSchema),
  (req, res, next) => {
    authController.signUp(req, res, next).catch((error) => {
      if (next) {
        next(error);
      }
    });
  }
);

router.post(
  '/signin',
  validateBody(authenticateUserSchema),
  (req, res, next) => {
    authController.signIn(req, res, next).catch((error) => {
      if (next) {
        next(error);
      }
    });
  }
);

export default router;

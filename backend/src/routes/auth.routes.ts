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
    void authController.signUp(req, res, next);
  }
);

router.post(
  '/signin',
  validateBody(authenticateUserSchema),
  authController.signIn
);

export default router;

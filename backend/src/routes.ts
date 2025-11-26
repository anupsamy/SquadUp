import { Router } from 'express';

import { authenticateToken } from './middleware/auth.middleware';
import authRoutes from './routes/auth.routes';
import mediaRoutes from './routes/media.routes';
import usersRoutes from './routes/user.routes';
import testRoutes from './routes/test.routes';
import groupRoutes from './routes/group.routes'

const router = Router();

router.use('/auth', authRoutes);

router.use('/user', (req, res, next) => {
  authenticateToken(req, res, next).catch(next);
}, usersRoutes);

router.use('/media', (req, res, next) => {
  authenticateToken(req, res, next).catch(next);
}, mediaRoutes);

router.use('/test', testRoutes);

router.use('/group', (req, res, next) => {
  authenticateToken(req, res, next).catch(next);
}, groupRoutes);

export default router;

import { Router } from 'express';

import { authenticateToken } from './middleware/auth.middleware';
import authRoutes from './routes/auth.routes';
import hobbiesRoutes from './routes/hobbies.routes';
import mediaRoutes from './routes/media.routes';
import usersRoutes from './routes/user.routes';
import testRoutes from './routes/test.routes';
import newsRoutes from './routes/news.routes';
import groupRoutes from './routes/group.routes'

const router = Router();

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/news', newsRoutes);

router.use('/test', testRoutes);

router.use('/group', groupRoutes);

export default router;

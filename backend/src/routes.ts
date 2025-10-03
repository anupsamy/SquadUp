import { Router } from 'express';

import { authenticateToken } from './middleware/auth.middleware';
import authRoutes from './routes/auth.routes';
import hobbiesRoutes from './routes/hobbies.routes';
import mediaRoutes from './routes/media.routes';
import usersRoutes from './routes/user.routes';
import testRoutes from './routes/test.routes';
import newsRoutes from './routes/news.routes'

const router = Router();

router.use('/auth', authRoutes);

router.use('/hobbies', authenticateToken, hobbiesRoutes);

router.use('/user', authenticateToken, usersRoutes);

router.use('/media', authenticateToken, mediaRoutes);

router.use('/news', newsRoutes);

router.use('/test', testRoutes);

export default router;

import { Router } from 'express';

import { upload } from '../storage';
import { authenticateToken } from '../middleware/auth.middleware';
import { MediaController } from '../controllers/media.controller';

const router = Router();
const mediaController = new MediaController();

router.post(
  '/upload',
  authenticateToken,
  upload.single('media') as express.RequestHandler,
  (req, res, next) => {
    mediaController.uploadImage(req, res).catch((error: unknown) => {
      next(error);
    });
  }
);

export default router;

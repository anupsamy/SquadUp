import { Router, RequestHandler } from 'express';
import { upload } from '../storage';
import { authenticateToken } from '../middleware/auth.middleware';
import { MediaController } from '../controllers/media.controller';

const router = Router();
const mediaController = new MediaController();

router.post(
  '/upload',
  authenticateToken,
  upload.single('media') as RequestHandler,
  (req, res, next) => {
    mediaController.uploadImage(req, res, next).catch((error: unknown) => {
      next(error);
    });
  }
);

export default router;

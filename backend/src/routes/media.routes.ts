import { Router } from 'express';

import { upload } from '../storage';
import { authenticateToken } from '../middleware/auth.middleware';
import { MediaController } from '../controllers/media.controller';

const router = Router();
const mediaController = new MediaController();

router.post(
  '/upload',
  authenticateToken,
  upload.single('media'),
  (req, res, next) => {
    void mediaController.uploadImage(req, res, next);
  }
);

export default router;

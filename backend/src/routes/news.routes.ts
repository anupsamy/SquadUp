import { Router } from 'express';

import { NewsController } from '../controllers/news.controller';
import logger from '../utils/logger.util';

const router = Router();
const newsController = new NewsController();

router.post('/hobbies', (req, res) => {
  newsController.getNewsByHobbies(req, res).catch((error) => {
    // Error handling is done in the controller method
    logger.error('Unhandled error in getNewsByHobbies:', error);
  });
});

export default router;

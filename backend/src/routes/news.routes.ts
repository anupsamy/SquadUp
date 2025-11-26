import { Router } from 'express';

import { NewsController } from '../controllers/news.controller';

const router = Router();
const newsController = new NewsController();

router.post('/hobbies', (req, res, next) => {
  newsController.getNewsByHobbies(req, res).catch((error: unknown) => {
    next(error);
  });
});

export default router;

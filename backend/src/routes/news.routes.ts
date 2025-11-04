import { Router } from 'express';

import { NewsController } from '../controllers/news.controller';

const router = Router();
const newsController = new NewsController();

router.post('/hobbies', (req, res, next) => {
  void newsController.getNewsByHobbies(req, res);
});

export default router;

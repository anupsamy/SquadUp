import { Router } from 'express';

import { NewsController } from '../controllers/news.controller';

const router = Router();
const newsController = new NewsController();

router.post('/hobbies', newsController.getNewsByHobbies);

export default router;

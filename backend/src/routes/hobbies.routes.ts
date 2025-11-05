import { Router } from 'express';

import { HobbyController } from '../controllers/hobby.controller';

const router = Router();
const hobbyController = new HobbyController();

router.get('/', (req, res, next) => {
  hobbyController.getAllHobbies(req, res, next);
});

export default router;

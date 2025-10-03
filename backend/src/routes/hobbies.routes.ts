import { Router } from 'express';

import { HobbyController } from '../controllers/hobby.controller';

const router = Router();
const hobbyController = new HobbyController();

router.get('/', hobbyController.getAllHobbies);

export default router;

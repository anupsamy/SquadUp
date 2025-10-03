import { Router } from 'express';
import { TestController } from '../controllers/test.controller';

const router = Router();
const testController = new TestController();

router.get('/test', (req, res, next) => testController.check(req, res, next));

export default router;
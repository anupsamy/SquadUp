import { Router } from 'express';
import { TestController } from '../controllers/test.controller';
import { GroupController } from '../controllers/group.controller';

const router = Router();
const testController = new TestController();
const groupController = new GroupController();

router.get('/test', (req, res, next) => testController.check(req, res));

export default router;
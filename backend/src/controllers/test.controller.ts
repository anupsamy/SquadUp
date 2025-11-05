import { Request, Response } from 'express';

export class TestController {
  check(req: Request, res: Response) {
    return res.status(200).json({
      message: 'OK - service is running TEST',
    });
  }
}
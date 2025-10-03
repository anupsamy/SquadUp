import { Request, Response, NextFunction } from 'express';

export class TestController {
  async check(req: Request, res: Response, _next: NextFunction) {
    return res.status(200).json({
      message: 'OK - service is running TEST',
    });
  }
}
import { IUser } from './user.types';
import { Request } from 'express';

declare global {
  namespace Express {
    interface Request {
      user?: IUser;
    }
  }
}

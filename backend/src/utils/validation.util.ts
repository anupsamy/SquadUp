// utils/validation.util.ts

import { IUser } from '../types/user.types';
import { AppErrorFactory } from './appError.util';

export const validateUserRequest = (user: IUser | undefined): IUser => {
  if (!user) {
    throw AppErrorFactory.unauthorized('User not found in request');
  }

  if (!user.id || !user.name || !user.email) {
    throw AppErrorFactory.unauthorized('Invalid user data in request');
  }

  return user;
};

import { IUser } from '../types/user.types';
import { AppErrorFactory } from './appError.util';
import { TRANSIT_TYPES, TransitType } from '../types/transit.types';

export const validateUserRequest = (user: IUser | undefined): IUser => {
  if (!user) {
    throw AppErrorFactory.unauthorized('User not found in request');
  }

  if (!user.id || !user.name || !user.email) {
    throw AppErrorFactory.unauthorized('Invalid user data in request');
  }

  return user;
};

export const validateJoinCode = (joinCode: unknown): string => {
  if (!joinCode || typeof joinCode !== 'string') {
    throw AppErrorFactory.badRequest(
      'joinCode is required and must be a string'
    );
  }
  return joinCode;
};

export const validateTransitType = (transitType: unknown): TransitType => {
  if (!TRANSIT_TYPES.includes(transitType as TransitType)) {
    throw AppErrorFactory.badRequest(
      `Invalid transitType. Must be one of: ${TRANSIT_TYPES.join(', ')}`
    );
  }
  return transitType as TransitType;
};

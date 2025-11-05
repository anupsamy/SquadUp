import { z } from 'zod';

import { IUser } from '../types/user.types';
import { IGroup } from './group.types';

// Zod schemas
// ------------------------------------------------------------
export const authenticateUserSchema = z.object({
  idToken: z.string().min(1, 'Google token is required'),
});

// Request types
// ------------------------------------------------------------
export type AuthenticateUserRequest = z.infer<typeof authenticateUserSchema>;

export interface AuthenticateUserResponse {
  message: string;
  data?: AuthResult;
}

// Generic types
// ------------------------------------------------------------
export interface AuthResult {
  token: string;
  user: IUser;
}

declare module 'express-serve-static-core' {
  interface Request {
    user?: IUser;
    group?: IGroup;
  }
}

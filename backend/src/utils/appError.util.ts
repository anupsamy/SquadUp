export class AppError extends Error {
  constructor(
    public message: string,
    public statusCode: number,
    public errorCode: string,
    public details?: unknown
  ) {
    super(message);
    Object.setPrototypeOf(this, AppError.prototype);
  }

  toJSON() {
    return {
      message: this.message,
      error: this.errorCode,
      details: this.details || null,
    };
  }
}

// Common error factory functions for reuse
export const AppErrorFactory = {
  notFound: (resource: string, identifier?: string) =>
    new AppError(
      identifier
        ? `${resource} with ${identifier} not found`
        : `${resource} not found`,
      404,
      'NotFound'
    ),

  badRequest: (message: string, details?: unknown) =>
    new AppError(message, 400, 'ValidationError', details),

  internalServerError: (message: string, details?: unknown) =>
    new AppError(message, 500, 'InternalServerError', details),

  conflict: (message: string, details?: unknown) =>
    new AppError(message, 409, 'Conflict', details),

  unauthorized: (message: string = 'Unauthorized') =>
    new AppError(message, 401, 'Unauthorized'),

  forbidden: (message: string = 'Forbidden') =>
    new AppError(message, 403, 'Forbidden'),
};

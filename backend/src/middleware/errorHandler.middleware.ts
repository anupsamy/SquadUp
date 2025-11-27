import { Request, Response, NextFunction } from 'express';
import { AppError } from '../utils/appError.util';
import logger from '../utils/logger.util';

export const errorHandler = (
  err: Error | AppError,
  req: Request,
  res: Response,
  next: NextFunction
) => {
  // If it's our custom AppError, use its values
  if (err instanceof AppError) {
    logger.error(`[${err.statusCode}] ${err.errorCode}: ${err.message}`, {
      path: req.path,
      method: req.method,
      details: err.details,
    });

    return res.status(err.statusCode).json({
      message: err.message,
      data: null,
      error: err.errorCode,
      details: err.details || null,
    });
  }

  logger.error('Unexpected error:', {
    path: req.path,
    method: req.method,
    message: err.message,
    stack: err.stack,
  });

  res.status(500).json({
    message: 'An unexpected error occurred',
    data: null,
    error: 'InternalServerError',
    details: null,
  });
};

export const notFoundHandler = (req: Request, res: Response) => {
  res.status(404).json({
    error: 'Route not found',
    message: `Cannot ${req.method} ${req.originalUrl}`,
    timestamp: new Date().toISOString(),
    path: req.originalUrl,
    method: req.method,
  });
};
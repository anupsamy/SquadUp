import { Request, Response, NextFunction, RequestHandler } from 'express';
import { z, ZodError } from 'zod';

export const validateBody = <T>(schema: z.ZodSchema<T>): RequestHandler => {
  return (req: Request, res: Response, next: NextFunction) => {
    try {
      console.error('Request body:', req.body);
      const validatedData = schema.parse(req.body);

      req.body = validatedData;
      next();
    } catch (error) {
      if (error instanceof ZodError) {
        console.error('Validation error:', error.issues);
        return res.status(400).json({
          error: 'Validation error',
          message: 'Invalid input data',
          details: error.issues.map(issue => ({
            field: issue.path.join('.'),
            message: issue.message,
          })),
        });
      }

      console.error('Unexpected validation error:', error);
      return res.status(500).json({
        error: 'Internal server error',
        message: 'Validation processing failed',
      });
    }
  };
};

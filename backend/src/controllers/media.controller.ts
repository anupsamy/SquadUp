import { Request, Response } from 'express';

import logger from '../utils/logger.util';
import { MediaService } from '../services/media.service';
import { UploadImageRequest, UploadImageResponse } from '../types/media.types';
import { sanitizeInput } from '../utils/sanitizeInput.util';

export class MediaController {
  async uploadImage(
    req: Request<unknown, unknown, UploadImageRequest>,
    res: Response<UploadImageResponse>
    ) {
    try {
      if (!req.file) {
        return res.status(400).json({
          message: 'No file uploaded',
        });
      }

      const user = req.user;
      if (!user) {
        return res.status(401).json({
          message: 'User not authenticated',
        });
      }
      const rawFilePath = req.file.path;
      const filePath: string = typeof rawFilePath === 'string' ? rawFilePath : '';
      const sanitizedFilePath = sanitizeInput(filePath);
      const image = MediaService.saveImage(
        sanitizedFilePath,
        user._id.toString()
      );

      res.status(200).json({
        message: 'Image uploaded successfully',
        data: {
          image,
        },
      });
    } catch (error) {
      logger.error('Error uploading profile picture:', error);

      const message =
        error instanceof Error
          ? error.message
          : typeof error === 'string'
          ? error
          : 'Failed to upload profile picture';

      return res.status(500).json({ message });
    }
  }
}

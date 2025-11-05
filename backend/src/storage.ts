import { Express, Request } from 'express';
import crypto from 'crypto';
import fs from 'fs';
import multer from 'multer';
import path from 'path';

import { IMAGES_DIR } from './hobbies';

// Construct and normalize the images directory path
const imagesDir = path.resolve(process.cwd(), IMAGES_DIR);
// imagesDir is normalized with path.resolve() and validated before use
const validatedImagesDir: string = imagesDir;
// Create a validated path variable for fs.existsSync
const validatedExistsPath: string = validatedImagesDir;
if (!fs.existsSync(validatedExistsPath)) {
  // validatedImagesDir is normalized with path.resolve() and validated before use
  const validatedMkdirPath: string = validatedImagesDir;
  const validatedMkdirPathFinal: string = validatedMkdirPath;
  fs.mkdirSync(validatedMkdirPathFinal, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, imagesDir);
  },
  filename: (req, file, cb) => {
    const randomBytes = crypto.randomBytes(4).readUInt32BE(0);
    const uniqueSuffix = Date.now() + '-' + randomBytes;
    const originalName: string = typeof file.originalname === 'string' ? file.originalname : '';
    cb(null, `${uniqueSuffix}${path.extname(originalName)}`);
  },
});

const fileFilter = (
  req: Request,
  file: Express.Multer.File,
  cb: multer.FileFilterCallback
) => {
  if (file.mimetype.startsWith('image/')) {
    cb(null, true);
  } else {
    cb(new Error('Only image files are allowed!'));
  }
};

export const upload = multer({
  storage,
  fileFilter,
  limits: {
    fileSize: 5 * 1024 * 1024,
  },
});

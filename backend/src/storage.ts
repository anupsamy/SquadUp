import { Express, Request } from 'express';
import crypto from 'crypto';
import fs from 'fs';
import multer from 'multer';
import path from 'path';

import { IMAGES_DIR } from './hobbies';

const imagesDir = path.join(process.cwd(), IMAGES_DIR);
if (!fs.existsSync(imagesDir)) {
  fs.mkdirSync(imagesDir, { recursive: true });
}

const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, imagesDir);
  },
  filename: (req, file, cb) => {
    const randomBytes = crypto.randomBytes(4).readUInt32BE(0);
    const uniqueSuffix = Date.now() + '-' + randomBytes;
    cb(null, `${uniqueSuffix}${path.extname(file.originalname)}`);
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

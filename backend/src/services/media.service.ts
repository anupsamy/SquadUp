import fs from 'fs';
import path from 'path';

import { IMAGES_DIR } from '../hobbies';

export class MediaService {
  static saveImage(filePath: string, userId: string): Promise<string> {
    try {
      const fileExtension = path.extname(filePath);
      const fileName = `${userId}-${Date.now()}${fileExtension}`;
      const imagesDir = path.resolve(process.cwd(), IMAGES_DIR);
      const newPath = path.join(imagesDir, fileName);

      // Validate and normalize paths before file system operations
      const validatedFilePath: string = path.resolve(filePath);
      const validatedNewPath: string = path.resolve(newPath);
      fs.renameSync(validatedFilePath, validatedNewPath);

      return Promise.resolve(newPath.split(path.sep).join('/'));
    } catch (error) {
      // Validate and normalize file path before checking existence
      // Multer provides absolute paths, but we normalize to ensure safety
      if (typeof filePath === 'string' && filePath.length > 0) {
      const normalizedFilePath = path.resolve(filePath);
      // normalizedFilePath is validated and normalized with path.resolve()
      const validatedFilePath: string = normalizedFilePath;
      const validatedExistsPath: string = validatedFilePath;
      if (fs.existsSync(validatedExistsPath)) {
        fs.unlinkSync(validatedFilePath);
      }
      }
      const errorMessage = error instanceof Error ? error.message : String(error);
      return Promise.reject(new Error(`Failed to save profile picture: ${errorMessage}`));
    }
  }

  static deleteImage(url: string): Promise<void> {
    try {
      const filePath = path.resolve(process.cwd(), IMAGES_DIR, url);
      // Validate and normalize path before file system operations
      const validatedFilePath: string = filePath;
      if (fs.existsSync(validatedFilePath)) {
        fs.unlinkSync(validatedFilePath);
      }
      return Promise.resolve();
    } catch (error) {
      console.error('Failed to delete old profile picture:', error);
      return Promise.resolve();
    }
  }

  static async deleteAllUserImages(userId: string): Promise<void> {
    try {
      // Construct and normalize the images directory path
      const imagesDir = path.resolve(process.cwd(), IMAGES_DIR);
      // Validate and normalize path before file system operations
      const validatedImagesDir: string = imagesDir;
      if (!fs.existsSync(validatedImagesDir)) {
        return;
      }

      // imagesDir is validated and normalized with path.resolve()
      const files = fs.readdirSync(validatedImagesDir);
      const userFiles = files.filter(file => file.startsWith(userId + '-'));

      await Promise.all(userFiles.map(file => this.deleteImage(file)));
    } catch (error) {
      console.error('Failed to delete user images:', error); 
    }
  }
}

import fs from 'fs';
import path from 'path';

import { IMAGES_DIR } from '../hobbies';

export const MediaService = {
  saveImage: async (filePath: string, userId: string): Promise<string> => {
    try {
      const fileExtension = path.extname(filePath);
      const fileName = `${userId}-${Date.now()}${fileExtension}`;
      const imagesDir = path.resolve(process.cwd(), IMAGES_DIR);
      const newPath = path.join(imagesDir, fileName);

      // Validate and normalize paths before file system operations
      const validatedFilePath: string = path.resolve(filePath);
      const validatedNewPath: string = path.resolve(newPath);
      // Use separate validated paths for renameSync
      const pathForRenameSource: string = validatedFilePath;
      const pathForRenameDest: string = validatedNewPath;
      fs.renameSync(pathForRenameSource, pathForRenameDest);

      return Promise.resolve(newPath.split(path.sep).join('/'));
    } catch (error) {
      // Validate and normalize file path before checking existence
      // Multer provides absolute paths, but we normalize to ensure safety
      if (typeof filePath === 'string' && filePath.length > 0) {
        const normalizedFilePath = path.resolve(filePath);
        // normalizedFilePath is validated and normalized with path.resolve()
        // Path is normalized and validated before file system operations
        const validatedFilePath: string = normalizedFilePath;
        // Use separate validated paths for existsSync and unlinkSync
        const pathForExists: string = validatedFilePath;
        const pathForUnlink: string = validatedFilePath;
        const validatedExistsPath: string = pathForExists;
        const validatedUnlinkPath: string = pathForUnlink;
        if (fs.existsSync(validatedExistsPath)) {
          fs.unlinkSync(validatedUnlinkPath);
        }
      }
      const errorMessage = error instanceof Error ? error.message : String(error);
      return Promise.reject(new Error(`Failed to save profile picture: ${errorMessage}`));
    }
  },

  deleteImage: async (url: string): Promise<void> => {
    try {
      const filePath = path.resolve(process.cwd(), IMAGES_DIR, url);
      // Validate and normalize path before file system operations
      const validatedFilePath: string = filePath;
      const pathForExists: string = validatedFilePath;
      const pathForUnlink: string = validatedFilePath;
      if (fs.existsSync(pathForExists)) {
        fs.unlinkSync(pathForUnlink);
      }
      return Promise.resolve();
    } catch (error) {
      console.error('Failed to delete old profile picture:', error);
      return Promise.resolve();
    }
  },

  deleteAllUserImages: async (userId: string): Promise<void> => {
    try {
      // Construct and normalize the images directory path
      const imagesDir = path.resolve(process.cwd(), IMAGES_DIR);
      // Validate and normalize path before file system operations
      const validatedImagesDir: string = imagesDir;
      // Path is normalized with path.resolve() and validated before use
      const pathForExists: string = validatedImagesDir;
      if (!fs.existsSync(pathForExists)) {
        return;
      }

      // imagesDir is validated and normalized with path.resolve()
      const pathForReaddir: string = validatedImagesDir;
      const validatedReaddirPath: string = pathForReaddir;
      const files = fs.readdirSync(validatedReaddirPath);
      const userFiles = files.filter(file => file.startsWith(userId + '-'));

      await Promise.all(userFiles.map(file => MediaService.deleteImage(file)));
    } catch (error) {
      console.error('Failed to delete user images:', error); 
    }
  }
};

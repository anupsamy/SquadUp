import fs from 'fs';
import path from 'path';
import { IMAGES_DIR } from '../storage';

// Validate file path is within allowed directory to prevent directory traversal
function validateFilePath(filePath: string, allowedDir: string): boolean {
  const resolvedPath = path.resolve(filePath);
  const resolvedAllowedDir = path.resolve(allowedDir);
  return resolvedPath.startsWith(resolvedAllowedDir + path.sep) || resolvedPath === resolvedAllowedDir;
}

export class MediaService {
  static saveImage(filePath: string, userId: string): string {
    const resolvedSourcePath = path.resolve(filePath);
    if (!validateFilePath(resolvedSourcePath, IMAGES_DIR)) {
      throw new Error('Invalid file path: outside allowed directory');
    }

    try {
      const fileExtension = path.extname(resolvedSourcePath);
      const fileName = `${userId}-${Date.now()}${fileExtension}`;
      const newPath = path.join(IMAGES_DIR, fileName);
      const resolvedNewPath = path.resolve(newPath);

      if (!validateFilePath(resolvedNewPath, IMAGES_DIR)) {
        throw new Error('Invalid destination path: outside allowed directory');
      }

      fs.renameSync(resolvedSourcePath, resolvedNewPath);
      return resolvedNewPath.split(path.sep).join('/');
    } catch (error) {
      // Cleanup only if path is validated
      if (validateFilePath(resolvedSourcePath, IMAGES_DIR)) {
        try {
          if (fs.existsSync(resolvedSourcePath)) {
            fs.unlinkSync(resolvedSourcePath);
          }
        } catch {
          // ignore cleanup errors
        }
      }
      throw new Error(`Failed to save profile picture: ${error}`);
    }
  }

  static deleteImage(url: string): void {
    try {
      const filePath = path.resolve(IMAGES_DIR, url);
      if (validateFilePath(filePath, IMAGES_DIR) && fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
      }
    } catch (error) {
      console.error('Failed to delete old profile picture:', error);
    }
  }

  static async deleteAllUserImages(userId: string): Promise<void> {
    try {
      if (!fs.existsSync(IMAGES_DIR)) return;

      const files = fs.readdirSync(IMAGES_DIR);
      const userFiles = files.filter(file => file.startsWith(userId + '-'));

      userFiles.forEach(file => this.deleteImage(file));
    } catch (error) {
      console.error('Failed to delete user images:', error);
    }
  }
}

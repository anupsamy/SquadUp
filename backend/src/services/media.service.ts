import fs from 'fs';
import path from 'path';

import { IMAGES_DIR } from '../hobbies';

export class MediaService {
  static saveImage(filePath: string, userId: string): Promise<string> {
    try {
      const fileExtension = path.extname(filePath);
      const fileName = `${userId}-${Date.now()}${fileExtension}`;
      const newPath = path.join(IMAGES_DIR, fileName);

      fs.renameSync(filePath, newPath);

      return Promise.resolve(newPath.split(path.sep).join('/'));
    } catch (error) {
      if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
      }
      const errorMessage = error instanceof Error ? error.message : String(error);
      return Promise.reject(new Error(`Failed to save profile picture: ${errorMessage}`));
    }
  }

  static deleteImage(url: string): Promise<void> {
    try {
      const filePath = path.join(process.cwd(),IMAGES_DIR, url);
      if (fs.existsSync(filePath)) {
        fs.unlinkSync(filePath);
      }
      return Promise.resolve();
    } catch (error) {
      console.error('Failed to delete old profile picture:', error);
      return Promise.resolve();
    }
  }

  static async deleteAllUserImages(userId: string): Promise<void> {
    try {
      const imagesDir = path.join(process.cwd(), IMAGES_DIR);
      if (!fs.existsSync(imagesDir)) {
        return;
      }

      const files = fs.readdirSync(imagesDir);
      const userFiles = files.filter(file => file.startsWith(userId + '-'));

      await Promise.all(userFiles.map(file => this.deleteImage(file)));
    } catch (error) {
      console.error('Failed to delete user images:', error); 
    }
  }
}

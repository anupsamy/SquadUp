import fs from 'fs';
import path from 'path';
import { MediaService } from '../../src/services/media.service';
import { IMAGES_DIR } from '../../src/config/storage';

jest.mock('../../src/utils/logger.util');

describe('Unmocked: MediaService', () => {
  beforeAll(() => {
    if (!fs.existsSync(IMAGES_DIR)) {
      fs.mkdirSync(IMAGES_DIR, { recursive: true });
    }
  });

  //backend video comment commit
  afterEach(() => {
    if (fs.existsSync(IMAGES_DIR)) {
      const files = fs.readdirSync(IMAGES_DIR);
      files.forEach(file => {
        const filePath = path.join(IMAGES_DIR, file);
        if (fs.lstatSync(filePath).isFile()) {
          fs.unlinkSync(filePath);
        }
      });
    }
  });

  afterAll(() => {
    if (fs.existsSync(IMAGES_DIR)) {
      const files = fs.readdirSync(IMAGES_DIR);
      files.forEach(file => {
        const filePath = path.join(IMAGES_DIR, file);
        if (fs.lstatSync(filePath).isFile()) {
          fs.unlinkSync(filePath);
        }
      });
    }
  });

  describe('deleteImage', () => {
    it('should delete an image that exists', async () => {
      const testImageName = 'test-image.jpg';
      const testImagePath = path.join(IMAGES_DIR, testImageName);
      fs.writeFileSync(testImagePath, Buffer.from('fake image data'));

      expect(fs.existsSync(testImagePath)).toBe(true);

      await MediaService.deleteImage(testImageName);

      expect(fs.existsSync(testImagePath)).toBe(false);
    });

    it('should not throw error when deleting non-existent image', async () => {
      const nonExistentImage = 'non-existent-image.jpg';

      await expect(MediaService.deleteImage(nonExistentImage)).resolves.not.toThrow();
    });

    it('should delete image with nested path', async () => {
      const testImageName = 'uploads/test-nested-image.jpg';
      const testImagePath = path.join(IMAGES_DIR, testImageName);
      
      fs.mkdirSync(path.dirname(testImagePath), { recursive: true });
      fs.writeFileSync(testImagePath, Buffer.from('fake image data'));

      expect(fs.existsSync(testImagePath)).toBe(true);

      await MediaService.deleteImage(testImageName);

      expect(fs.existsSync(testImagePath)).toBe(false);
    });
  });

  describe('deleteAllUserImages', () => {
    it('should delete all images for a specific user', async () => {
      const userId = 'user-123';
      
      // Create multiple images for this user
      const image1 = path.join(IMAGES_DIR, `${userId}-1.jpg`);
      const image2 = path.join(IMAGES_DIR, `${userId}-2.jpg`);
      const image3 = path.join(IMAGES_DIR, `${userId}-3.jpg`);
      
      fs.writeFileSync(image1, Buffer.from('image 1'));
      fs.writeFileSync(image2, Buffer.from('image 2'));
      fs.writeFileSync(image3, Buffer.from('image 3'));

      expect(fs.existsSync(image1)).toBe(true);
      expect(fs.existsSync(image2)).toBe(true);
      expect(fs.existsSync(image3)).toBe(true);

      await MediaService.deleteAllUserImages(userId);

      expect(fs.existsSync(image1)).toBe(false);
      expect(fs.existsSync(image2)).toBe(false);
      expect(fs.existsSync(image3)).toBe(false);
    });

    it('should not delete images from other users', async () => {
      const userId1 = 'user-123';
      const userId2 = 'user-456';
      
      const user1Image = path.join(IMAGES_DIR, `${userId1}-1.jpg`);
      const user2Image = path.join(IMAGES_DIR, `${userId2}-1.jpg`);
      
      fs.writeFileSync(user1Image, Buffer.from('user 1 image'));
      fs.writeFileSync(user2Image, Buffer.from('user 2 image'));

      await MediaService.deleteAllUserImages(userId1);

      expect(fs.existsSync(user1Image)).toBe(false);
      expect(fs.existsSync(user2Image)).toBe(true);
    });

    it('should handle deletion when IMAGES_DIR does not exist', async () => {
      const userId = 'user-789';
      
      // Mock IMAGES_DIR to non-existent directory
      jest.spyOn(fs, 'existsSync').mockReturnValueOnce(false);

      await expect(MediaService.deleteAllUserImages(userId)).resolves.not.toThrow();
    });

    it('should handle error during deletion gracefully', async () => {
      const userId = 'user-999';
      const image = path.join(IMAGES_DIR, `${userId}-1.jpg`);
      
      fs.writeFileSync(image, Buffer.from('image'));

      // Mock unlinkSync to throw an error
      jest.spyOn(fs, 'unlinkSync').mockImplementationOnce(() => {
        throw new Error('Permission denied');
      });

      // Should not throw - the service catches errors
      await expect(MediaService.deleteAllUserImages(userId)).resolves.not.toThrow();
    });
  });
});
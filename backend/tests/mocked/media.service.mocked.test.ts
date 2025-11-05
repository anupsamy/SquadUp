import fs from 'fs';
import path from 'path';
import { MediaService } from '../../src/services/media.service';
import { IMAGES_DIR } from '../../src/storage';

jest.mock('../../src/utils/logger.util');

describe('Mocked: MediaService - Error Handling', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('saveImage - error handling', () => {
    it('should throw error when renameSync fails', async () => {
      const filePath = '/temp/test-image.jpg';
      const userId = 'user-123';

      jest.spyOn(fs, 'renameSync').mockImplementationOnce(() => {
        throw new Error('ENOENT: no such file or directory');
      });

      jest.spyOn(fs, 'existsSync').mockReturnValueOnce(true);
      jest.spyOn(fs, 'unlinkSync').mockImplementationOnce(() => {});

      await expect(MediaService.saveImage(filePath, userId)).rejects.toThrow(
        'Failed to save profile picture: Error: ENOENT: no such file or directory'
      );

      expect(fs.unlinkSync).toHaveBeenCalledWith(filePath);
    });

    it('should throw error and cleanup when file does not exist for cleanup', async () => {
      const filePath = '/temp/test-image.jpg';
      const userId = 'user-123';

      jest.spyOn(fs, 'renameSync').mockImplementationOnce(() => {
        throw new Error('Disk write error');
      });

      jest.spyOn(fs, 'existsSync').mockReturnValueOnce(false);
      jest.spyOn(fs, 'unlinkSync');

      await expect(MediaService.saveImage(filePath, userId)).rejects.toThrow(
        'Failed to save profile picture: Error: Disk write error'
      );

      expect(fs.unlinkSync).not.toHaveBeenCalled();
    });

    it('should throw error when extracting file extension fails', async () => {
      const filePath = null;
      const userId = 'user-123';

      jest.spyOn(fs, 'renameSync').mockImplementationOnce(() => {
        throw new Error('Cannot read property path');
      });

      jest.spyOn(fs, 'existsSync').mockReturnValueOnce(true);
      jest.spyOn(fs, 'unlinkSync').mockImplementationOnce(() => {});

      await expect(MediaService.saveImage(filePath as any, userId)).rejects.toThrow(
        'Failed to save profile picture: Error: Cannot read property path'
      );
    });
  });

  describe('deleteAllUserImages - error handling', () => {
    it('should handle readdirSync error gracefully', async () => {
  const userId = 'user-123';
  const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();

  jest.spyOn(fs, 'existsSync').mockReturnValueOnce(true);
  jest.spyOn(fs, 'readdirSync').mockImplementationOnce(() => {
    throw new Error('Permission denied');
  });

  await expect(MediaService.deleteAllUserImages(userId)).resolves.not.toThrow();

  expect(consoleErrorSpy).toHaveBeenCalledWith(
    'Failed to delete user images:',
    expect.any(Error)
  );

  consoleErrorSpy.mockRestore();
});

it('should handle deleteImage errors gracefully', async () => {
  const userId = 'user-123';
  const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();

  jest.spyOn(fs, 'existsSync').mockReturnValue(true);
  jest.spyOn(fs, 'readdirSync').mockReturnValueOnce([
    'user-123-1.jpg',
    'user-123-2.jpg',
    'other-user-1.jpg',
  ] as any);

  jest.spyOn(MediaService as any, 'deleteImage').mockRejectedValueOnce(
    new Error('Failed to delete image')
  );

  await expect(MediaService.deleteAllUserImages(userId)).resolves.not.toThrow();
});

it('should handle Promise.all rejection from deleteImage calls', async () => {
  const userId = 'user-456';
  const consoleErrorSpy = jest.spyOn(console, 'error').mockImplementation();

  jest.spyOn(fs, 'existsSync').mockReturnValueOnce(true);
  jest.spyOn(fs, 'readdirSync').mockReturnValueOnce([
    'user-456-1.jpg',
    'user-456-2.jpg',
  ] as any);

  jest.spyOn(MediaService as any, 'deleteImage')
    .mockRejectedValueOnce(new Error('Disk error'));

  await expect(MediaService.deleteAllUserImages(userId)).resolves.not.toThrow();

  expect(consoleErrorSpy).toHaveBeenCalled();
  consoleErrorSpy.mockRestore();
});
  });
});
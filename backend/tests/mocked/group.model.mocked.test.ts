import { groupModel } from '../../src/models/group.model';
import mongoose from 'mongoose';

jest.mock('../../src/utils/logger.util');

describe('Mocked: Group Model Error Handling', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('GroupModel.findAll', () => {
    // Mocked behavior: MongoDB find() throws a connection error
    // Input: attempting to fetch all groups
    // Expected behavior: error is caught and re-thrown as generic message
    // Expected output: "Failed to fetch all groups" error
    it('should throw error when database connection fails', async () => {
      const mockError = new Error('MongoDB connection refused');
      jest.spyOn(groupModel['group'], 'find' as any).mockRejectedValueOnce(mockError);

      await expect(groupModel.findAll()).rejects.toThrow('Failed to fetch all groups');
    });

    // Mocked behavior: MongoDB find() throws a query error
    // Input: attempting to fetch all groups
    // Expected behavior: error is caught and re-thrown as generic message
    // Expected output: "Failed to fetch all groups" error
    it('should throw error when database query fails', async () => {
      const mockError = new Error('Invalid query');
      jest.spyOn(groupModel['group'], 'find' as any).mockRejectedValueOnce(mockError);

      await expect(groupModel.findAll()).rejects.toThrow('Failed to fetch all groups');
    });
  });

  describe('GroupModel.findByJoinCode', () => {
    // Mocked behavior: MongoDB findOne() throws a connection error
    // Input: valid joinCode string
    // Expected behavior: error is caught and re-thrown as generic message
    // Expected output: "Failed to find group by joinCode" error
    it('should throw error when database connection fails', async () => {
      const mockError = new Error('MongoDB connection refused');
      jest.spyOn(groupModel['group'], 'findOne' as any).mockRejectedValueOnce(mockError);

      await expect(groupModel.findByJoinCode('test-code')).rejects.toThrow(
        'Failed to find group by joinCode'
      );
    });

    // Mocked behavior: MongoDB findOne() throws a query error
    // Input: valid joinCode string
    // Expected behavior: error is caught and re-thrown as generic message
    // Expected output: "Failed to find group by joinCode" error
    it('should throw error when database query fails', async () => {
      const mockError = new Error('Invalid query');
      jest.spyOn(groupModel['group'], 'findOne' as any).mockRejectedValueOnce(mockError);

      await expect(groupModel.findByJoinCode('test-code')).rejects.toThrow(
        'Failed to find group by joinCode'
      );
    });
  });

  describe('GroupModel.updateGroupByJoinCode', () => {
    // Mocked behavior: MongoDB findOneAndUpdate() throws a connection error
    // Input: valid joinCode and partial group data that passes Zod validation
    // Expected behavior: error is caught and re-thrown as generic message
    // Expected output: "Failed to update group" error
    it('should throw error when database connection fails', async () => {
      const mockError = new Error('MongoDB connection refused');
      jest.spyOn(groupModel['group'], 'findOneAndUpdate' as any).mockRejectedValueOnce(mockError);

      const updateData = { joinCode: 'test-code', expectedPeople: 5 };

      await expect(groupModel.updateGroupByJoinCode('test-code', updateData)).rejects.toThrow(
        'Failed to update group'
      );
    });

    // Mocked behavior: MongoDB findOneAndUpdate() throws a validation error
    // Input: valid joinCode but validation schema fails
    // Expected behavior: Zod validation error is caught and re-thrown as "Invalid update data"
    // Expected output: "Invalid update data" error
    it('should throw "Invalid update data" when Zod validation fails', async () => {
      const updateData = { expectedPeople: 'not-a-number' } as any;

      await expect(groupModel.updateGroupByJoinCode('test-code', updateData)).rejects.toThrow(
        'Invalid update data'
      );
    });

    // Mocked behavior: MongoDB findOneAndUpdate() throws a query error
    // Input: valid joinCode and partial group data that passes Zod validation
    // Expected behavior: error is caught and re-thrown as generic message
    // Expected output: "Failed to update group" error
    it('should throw error when database query fails', async () => {
      const mockError = new Error('Invalid query');
      jest.spyOn(groupModel['group'], 'findOneAndUpdate' as any).mockRejectedValueOnce(mockError);

      const updateData = { joinCode: 'test-code', expectedPeople: 5 };

      await expect(groupModel.updateGroupByJoinCode('test-code', updateData)).rejects.toThrow(
        'Failed to update group'
      );
    });
  });

  describe('GroupModel.leaveGroup - updatedGroup paths', () => {
    // Mocked behavior: findOneAndUpdate for leadership transfer throws error
    // Input: leader leaving group with other members
    // Expected behavior: error is caught and "Failed to leave group" is thrown
    // Expected output: "Failed to leave group" error
    it('should throw error when leadership transfer update fails', async () => {
      const exampleJoinCode = 'test-code';
      const userId = 'leader-id';

      const mockGroup = {
        joinCode: exampleJoinCode,
        groupLeaderId: { id: userId, name: 'Leader', email: 'leader@example.com' },
        groupMemberIds: [
          { id: userId, name: 'Leader', email: 'leader@example.com' },
          { id: 'member-id', name: 'Member', email: 'member@example.com' },
        ],
      };

      jest.spyOn(groupModel['group'], 'findOne' as any).mockResolvedValueOnce(mockGroup);
      jest.spyOn(groupModel['group'], 'findOneAndUpdate' as any).mockRejectedValueOnce(
        new Error('Database error during leadership transfer')
      );

      await expect(groupModel.leaveGroup(exampleJoinCode, userId)).rejects.toThrow(
        'Failed to leave group'
      );
    });

    // Mocked behavior: findOneAndUpdate returns null when transferring leadership
    // Input: leader leaving group with other members, but update fails
    // Expected behavior: error is caught and "Failed to leave group" is thrown
    // Expected output: "Failed to leave group" error
    it('should throw error when leadership transfer update returns null', async () => {
      const exampleJoinCode = 'test-code';
      const userId = 'leader-id';

      const mockGroup = {
        joinCode: exampleJoinCode,
        groupLeaderId: { id: userId, name: 'Leader', email: 'leader@example.com' },
        groupMemberIds: [
          { id: userId, name: 'Leader', email: 'leader@example.com' },
          { id: 'member-id', name: 'Member', email: 'member@example.com' },
        ],
      };

      jest.spyOn(groupModel['group'], 'findOne' as any).mockResolvedValueOnce(mockGroup);
      jest.spyOn(groupModel['group'], 'findOneAndUpdate' as any).mockResolvedValueOnce(null);

      await expect(groupModel.leaveGroup(exampleJoinCode, userId)).rejects.toThrow(
        'Failed to leave group'
      );
    });

    // Mocked behavior: findOneAndUpdate returns null when removing member from group
    // Input: non-leader user leaving group
    // Expected behavior: error is caught and "Failed to leave group" is thrown
    // Expected output: "Failed to leave group" error
    it('should throw error when updating group members fails (returns null)', async () => {
      const exampleJoinCode = 'test-code';
      const userId = 'member-id';

      const mockGroup = {
        joinCode: exampleJoinCode,
        groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
        groupMemberIds: [
          { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
          { id: userId, name: 'Member', email: 'member@example.com' },
        ],
      };

      jest.spyOn(groupModel['group'], 'findOne' as any).mockResolvedValueOnce(mockGroup);
      jest.spyOn(groupModel['group'], 'findOneAndUpdate' as any).mockResolvedValueOnce(null);

      await expect(groupModel.leaveGroup(exampleJoinCode, userId)).rejects.toThrow(
        'Failed to leave group'
      );
    });

    // Mocked behavior: findOneAndUpdate throws error when removing non-leader
    // Input: non-leader user leaving group
    // Expected behavior: error is caught and "Failed to leave group" is thrown
    // Expected output: "Failed to leave group" error
    it('should throw error when removing member from group fails', async () => {
      const exampleJoinCode = 'test-code';
      const userId = 'member-id';

      const mockGroup = {
        joinCode: exampleJoinCode,
        groupLeaderId: { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
        groupMemberIds: [
          { id: 'leader-id', name: 'Leader', email: 'leader@example.com' },
          { id: userId, name: 'Member', email: 'member@example.com' },
        ],
      };

      jest.spyOn(groupModel['group'], 'findOne' as any).mockResolvedValueOnce(mockGroup);
      jest.spyOn(groupModel['group'], 'findOneAndUpdate' as any).mockRejectedValueOnce(
        new Error('Database error when removing member')
      );

      await expect(groupModel.leaveGroup(exampleJoinCode, userId)).rejects.toThrow(
        'Failed to leave group'
      );
    });
  });
});
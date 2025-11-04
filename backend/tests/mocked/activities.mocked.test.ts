import request from 'supertest';
import express, { Express } from 'express';
import { GroupController } from '../../src/controllers/group.controller';
import { groupModel } from '../../src/group.model';
import { locationService } from '../../src/services/location.service';

jest.mock('../src/utils/logger.util'); // Mock logger
jest.mock('../src/group.model'); // Mock group model
jest.mock('../src/services/location.service'); // Mock location service

describe('Mocked: Activities Endpoints (With Mocks)', () => {
  let app: Express;
  let groupController: GroupController;

  beforeAll(() => {
    app = express();
    app.use(express.json());
    groupController = new GroupController();

    // Setup routes
    app.get('/group/activities', (req, res, next) => groupController.getActivities(req, res));
    app.post('/group/activities/select', (req, res, next) => groupController.selectActivity(req, res));
  });

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('GET /group/activities', () => {
  it('should return 200 and a list of activities for a valid group', async () => {
    const mockGroup = {
      joinCode: 'group1',
      midpoint: '49.2827 -123.1207',
      activityType: 'CAFE',
    };

    const mockActivities = [
      { name: 'Activity 1', placeId: 'place1' },
      { name: 'Activity 2', placeId: 'place2' },
    ];

    jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(mockGroup as any);
    jest.spyOn(locationService, 'getActivityList').mockResolvedValueOnce(mockActivities as any);

    const res = await request(app).get('/group/activities?joinCode=group1');

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Fetched activities successfully');
    expect(res.body.data).toEqual(mockActivities);
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
    expect(locationService.getActivityList).toHaveBeenCalledWith(
      { lat: 49.2827, lng: -123.1207 },
      'CAFE'
    );
  });

  it('should return 404 if the group does not exist', async () => {
    jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(null);

    const res = await request(app).get('/group/activities?joinCode=nonexistent');

    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('message', 'Group not found');
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('nonexistent');
  });

  it('should return 404 if the group does not have a midpoint', async () => {
    const mockGroup = {
      joinCode: 'group1',
      midpoint: null,
    };

    jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(mockGroup as any);

    const res = await request(app).get('/group/activities?joinCode=group1');

    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('message', 'No midpoint available for this group');
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
  });

  it('should return 500 if an error occurs', async () => {
    jest.spyOn(groupModel, 'findByJoinCode').mockRejectedValueOnce(new Error('Database error'));

    const res = await request(app).get('/group/activities?joinCode=group1');

    expect(res.status).toBe(500);
    expect(res.body).toHaveProperty('message', 'Failed to fetch activities');
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
  });
});
    describe('POST /group/activities/select', () => {
  it('should return 200 and update the selected activity for a valid group', async () => {
    const mockGroup = { joinCode: 'group1' };
    const mockActivity = {
      name: 'Selected Activity',
      placeId: 'place123',
      address: '123 Main St',
    };

    const updatedGroup = { ...mockGroup, selectedActivity: mockActivity };

    jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(mockGroup as any);
    jest.spyOn(groupModel, 'updateSelectedActivity').mockResolvedValueOnce(updatedGroup as any);

    const res = await request(app)
      .post('/group/activities/select')
      .send({ joinCode: 'group1', activity: mockActivity });

    expect(res.status).toBe(200);
    expect(res.body).toHaveProperty('message', 'Activity selected successfully');
    expect(res.body.data).toEqual(updatedGroup);
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
    expect(groupModel.updateSelectedActivity).toHaveBeenCalledWith('group1', mockActivity);
  });

  it('should return 404 if the group does not exist', async () => {
    jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(null);

    const mockActivity = {
      name: 'Selected Activity',
      placeId: 'place123',
      address: '123 Main St',
    };

    const res = await request(app)
      .post('/group/activities/select')
      .send({ joinCode: 'nonexistent', activity: mockActivity });

    expect(res.status).toBe(404);
    expect(res.body).toHaveProperty('message', 'Group not found');
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('nonexistent');
  });

  it('should return 400 if the activity is invalid', async () => {
    const mockGroup = { joinCode: 'group1' };

    jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(mockGroup as any);

    const invalidActivity = { name: 'Invalid Activity' }; // Missing `placeId`

    const res = await request(app)
      .post('/group/activities/select')
      .send({ joinCode: 'group1', activity: invalidActivity });

    expect(res.status).toBe(400);
    expect(res.body).toHaveProperty('message', 'Activity must have placeId and name');
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
  });

  it('should return 500 if an error occurs', async () => {
    const mockGroup = { joinCode: 'group1' };
    const mockActivity = {
      name: 'Selected Activity',
      placeId: 'place123',
      address: '123 Main St',
    };

    jest.spyOn(groupModel, 'findByJoinCode').mockResolvedValueOnce(mockGroup as any);
    jest.spyOn(groupModel, 'updateSelectedActivity').mockRejectedValueOnce(new Error('Database error'));

    const res = await request(app)
      .post('/group/activities/select')
      .send({ joinCode: 'group1', activity: mockActivity });

    expect(res.status).toBe(500);
    expect(res.body).toHaveProperty('message', 'Failed to select activity');
    expect(groupModel.findByJoinCode).toHaveBeenCalledWith('group1');
    expect(groupModel.updateSelectedActivity).toHaveBeenCalledWith('group1', mockActivity);
  });
});
  // Add tests here
});
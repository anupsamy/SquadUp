# Testing WebSocket Notifications with a Single User (AWS Backend)

This guide shows you how to test WebSocket notifications on the AWS backend without needing multiple users or devices.

## Method 1: Using the Test Script (Recommended)

### Step 1: Start the WebSocket Test Client

This script will connect to AWS and subscribe to a test group:

```bash
cd backend
node aws-websocket-test.js
```

You should see:
```
✅ Connected to AWS WebSocket server
📤 Subscribing to group: {...}
📥 Received message: {"type":"group_update",...}
```

### Step 2: Trigger a Test Notification

**In a separate terminal**, send a test notification:

**Using curl (Linux/Mac/Git Bash):**
```bash
curl -X POST http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/test/websocket-notification/test123 \
  -H "Content-Type: application/json" \
  -d '{"message": "Test notification from AWS", "type": "test"}'
```

**Using PowerShell (Windows):**
```powershell
Invoke-RestMethod -Uri "http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/test/websocket-notification/test123" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{"message": "Test notification from AWS", "type": "test"}'
```

You should see the notification appear in the first terminal running the test client.

---

## Method 2: Using the Android App as Client + API Endpoint

### Step 1: Connect via Android App

1. Open your Android app
2. The app should automatically connect to the WebSocket server (check logs)
3. Navigate to a group and ensure you're subscribed (the app should subscribe automatically)

### Step 2: Trigger Notifications via API

You can test different notification types:

**Test Group Update:**
```bash
curl -X POST http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/test/websocket-notification/YOUR_JOIN_CODE \
  -H "Content-Type: application/json" \
  -d '{"message": "Test group update", "type": "update"}'
```

**Test Group Join (simulate):**
```bash
# First, get a real join code from an existing group
# Then simulate a join by calling the update endpoint with a new member
curl -X POST http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/groups/update \
  -H "Content-Type: application/json" \
  -d '{
    "joinCode": "YOUR_JOIN_CODE",
    "expectedPeople": 5,
    "groupMemberIds": [
      {"id": "user1", "name": "User 1"},
      {"id": "user2", "name": "User 2"},
      {"id": "new-user", "name": "New User"}
    ]
  }'
```

**Test Group Leave:**
```bash
curl -X POST http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/groups/leave/YOUR_JOIN_CODE \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-id-to-leave"}'
```

---

## Method 3: Browser Console Test

You can also test directly from a browser console:

1. **Open browser developer console** (F12)
2. **Connect to WebSocket:**
```javascript
const ws = new WebSocket('ws://ec2-18-221-196-3.us-east-2.compute.amazonaws.com:80/ws');

ws.onopen = () => {
    console.log('Connected!');
    // Subscribe to a test group
    ws.send(JSON.stringify({
        type: 'subscribe',
        userId: 'browser-user-123',
        joinCode: 'test123'
    }));
};

ws.onmessage = (event) => {
    const msg = JSON.parse(event.data);
    console.log('📥 Received:', msg);
};

ws.onerror = (error) => {
    console.error('❌ Error:', error);
};
```

3. **Trigger notification** using the curl command above (Method 1, Step 2)

---

## Testing Different Notification Types

The test endpoint sends `group_update` notifications. To test other types (`group_join`, `group_leave`), you need to use the actual API endpoints:

### Test `group_join` Notification

```bash
# Create or update a group with a new member
curl -X POST http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/groups/update \
  -H "Content-Type: application/json" \
  -d '{
    "joinCode": "ABC123",
    "groupMemberIds": [
      {"id": "existing-user-1", "name": "Existing User"},
      {"id": "new-user-123", "name": "New User Joining"}
    ]
  }'
```

### Test `group_leave` Notification

```bash
curl -X POST http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/groups/leave/ABC123 \
  -H "Content-Type: application/json" \
  -d '{"userId": "user-id-to-leave"}'
```

---

## Troubleshooting

### "No subscribers found for group"

- Make sure you've subscribed to the group first using `{"type": "subscribe", "userId": "...", "joinCode": "..."}`
- Verify the `joinCode` matches between subscription and notification

### "Connection refused" or "WebSocket is closed"

- Check that the backend is running on AWS
- Verify the WebSocket path is `/ws`
- Check AWS security groups allow traffic on port 80

### Notifications not appearing

1. **Check subscription:**
   - Verify the client sent a subscribe message
   - Check server logs for subscription confirmation

2. **Verify WebSocket service is initialized:**
   ```bash
   # SSH into AWS and check logs
   docker-compose logs app | grep -i websocket
   ```

3. **Check notification was sent:**
   - The test endpoint returns stats: `{"totalClients": 1, "totalGroups": 1, ...}`
   - If `totalClients` is 0, no one is subscribed

---

## Quick Test Checklist

- [ ] Backend deployed to AWS
- [ ] WebSocket test client connected (`node aws-websocket-test.js`)
- [ ] Client subscribed to a group (`test123`)
- [ ] Sent test notification via curl/API
- [ ] Received notification in test client console

---

## Example Full Test Flow

```bash
# Terminal 1: Start WebSocket client
cd backend
node aws-websocket-test.js

# Terminal 2: Wait for "Subscribed to group" message, then send test
curl -X POST http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api/test/websocket-notification/test123 \
  -H "Content-Type: application/json" \
  -d '{"message": "Hello from AWS!", "type": "test"}'

# You should see in Terminal 1:
# 📥 Received message: {"type":"group_update","joinCode":"test123",...}
# 📢 Group update: Hello from AWS!
```


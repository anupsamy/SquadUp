# WebSocket Connection Troubleshooting Guide

## Current Issue
WebSocket connection to AWS server is failing with: `Connection state changed: false`

## Step-by-Step Troubleshooting

### Step 1: Verify Deployment Was Successful

Your deployment should have triggered when you pushed to `prod` branch. Check if the deployment completed:

1. Go to your GitHub Actions: `https://github.com/anupsamy/SquadUp/actions`
2. Look for the latest "Deploy to EC2" workflow run
3. Verify it completed successfully (green checkmark)

### Step 2: Check Server Status via SSH

SSH into your AWS server to verify the containers are running:

```bash
ssh -i your-key.pem ubuntu@ec2-18-221-196-3.us-east-2.compute.amazonaws.com
cd /home/ubuntu/SquadUp/backend

# Check if containers are running
docker-compose ps

# Check server logs for WebSocket service
docker-compose logs app | grep -i websocket

# Check if the server is listening on port 3000
netstat -tlnp | grep 3000
```

Expected output should show:
- Container `cpen321_app_M1` is running and "Up"
- WebSocket service logs showing "WebSocket server initialized"
- Server listening on port 3000

### Step 3: Check AWS Security Groups

**This is likely the issue!** AWS Security Groups might be blocking WebSocket traffic.

1. Go to AWS Console → EC2 → Security Groups
2. Find the security group attached to your EC2 instance
3. Check Inbound Rules:
   - **Type**: Custom TCP
   - **Port Range**: 3000 (or whatever port your server uses)
   - **Source**: 0.0.0.0/0 (or your specific IP)
   - **Description**: Allow WebSocket and HTTP traffic

If the rule doesn't exist, add it:
- Click "Edit inbound rules"
- Click "Add rule"
- Type: Custom TCP
- Port: 3000
- Source: 0.0.0.0/0 (or restrict to your IP for security)
- Save rules

### Step 4: Test HTTP API First

Before testing WebSocket, verify HTTP API is working:

```bash
# Test from your local machine
curl http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com:3000/api/groups/info
```

If this fails, the issue is with network connectivity or security groups, not WebSocket specifically.

### Step 5: Test WebSocket from Server

SSH into your server and test WebSocket locally:

```bash
ssh -i your-key.pem ubuntu@ec2-18-221-196-3.us-east-2.compute.amazonaws.com
cd /home/ubuntu/SquadUp/backend

# Install Node.js WebSocket test client if needed
npm install -g wscat

# Test WebSocket connection
wscat -c ws://localhost:3000/ws
```

If this works locally but not from outside, it's a security group issue.

### Step 6: Check Server Logs for Errors

Check if there are any errors in the server logs:

```bash
ssh -i your-key.pem ubuntu@ec2-18-221-196-3.us-east-2.compute.amazonaws.com
cd /home/ubuntu/SquadUp/backend

# Check recent logs
docker-compose logs --tail=100 app

# Monitor logs in real-time
docker-compose logs -f app
```

Look for:
- WebSocket initialization messages
- Connection errors
- Port binding issues

### Step 7: Verify WebSocket Service is Active

Ensure the WebSocket service is initialized in your server:

```bash
# Check if WebSocket endpoint responds
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" -H "Sec-WebSocket-Key: test" \
  http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com:3000/ws
```

You should see HTTP 101 Switching Protocols if WebSocket is working.

### Step 8: Rebuild and Redeploy

If all else fails, manually rebuild the container:

```bash
ssh -i your-key.pem ubuntu@ec2-18-221-196-3.us-east-2.compute.amazonaws.com
cd /home/ubuntu/SquadUp/backend

# Stop containers
docker-compose down

# Rebuild
docker-compose --env-file .env build

# Start containers
docker-compose --env-file .env up -d

# Verify
docker-compose ps
docker-compose logs app
```

### Step 9: Test with Updated Logging

The updated code now includes better error logging. Rebuild your Android app and check the logs:

```bash
cd frontend
./gradlew assembleStagingDebug
# Install on device and check logcat
adb logcat | grep WebSocket
```

You should now see detailed error messages like:
- Connection failed to: ws://...
- Error: Connection refused (or timeout, etc.)
- Response code: -1 (means no response, likely firewall/security group)

## Most Common Issues and Solutions

### Issue 1: Security Group Blocking Traffic
**Solution**: Add inbound rule for port 3000 in AWS Security Groups

### Issue 2: Server Not Running
**Solution**: SSH into server and restart Docker containers

### Issue 3: WebSocket Service Not Initialized
**Solution**: Check server logs, ensure WebSocket service code is deployed

### Issue 4: Wrong Port
**Solution**: Verify `PROD_PORT` in GitHub secrets matches your actual port

### Issue 5: Connection Timeout
**Solution**: Check if server has a firewall (iptables, ufw) blocking connections

## Quick Fix Commands

```bash
# Test security group (from local machine)
telnet ec2-18-221-196-3.us-east-2.compute.amazonaws.com 3000

# Check if port is open (from server)
ss -tlnp | grep 3000

# Test WebSocket from server
wscat -c ws://localhost:3000/ws
```

## Next Steps After Fix

Once WebSocket is working:

1. Test group join/leave notifications
2. Verify notifications are received in real-time
3. Monitor server logs for any errors
4. Test with multiple clients simultaneously

## Need Help?

If issues persist, check:
1. Server logs: `docker-compose logs app`
2. Security group configuration in AWS Console
3. Network connectivity: `ping ec2-18-221-196-3.us-east-2.compute.amazonaws.com`
4. Port accessibility: `telnet <server> 3000`

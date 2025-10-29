# WebSocket Debugging Commands for AWS EC2

## Step 1: Verify Files Exist on Server

```bash
ssh -i your-key.pem ubuntu@ec2-18-221-196-3.us-east-2.compute.amazonaws.com
cd /home/ubuntu/SquadUp/backend

# Check if WebSocket service source file exists
ls -la src/services/websocket.service.ts

# Check if compiled JavaScript exists
ls -la dist/services/websocket.service.js
```

## Step 2: Rebuild Container with New Error Logging

```bash
cd /home/ubuntu/SquadUp/backend

# Pull latest code (if using git on server)
# OR ensure the latest code is copied via deployment

# Rebuild container
docker-compose down
docker-compose --env-file .env build

# Check build output for any TypeScript errors
# Look for: "websocket.service.ts" in the build output

# Start container
docker-compose --env-file .env up -d
```

## Step 3: Check Full Server Logs

```bash
# Watch logs in real-time
docker-compose logs -f app

# Or check recent logs
docker-compose logs --tail=100 app

# Look for these messages:
# "🔧 Creating WebSocket server..."
# "✅ WebSocket server initialized successfully"
# OR error messages like:
# "❌ Failed to initialize WebSocket service"
# "❌ Error creating WebSocket server"
```

## Step 4: Verify Inside Container

```bash
# Check if the compiled file exists in the container
docker exec cpen321_app_M1 ls -la dist/services/

# Should show: websocket.service.js

# Check if the import can be resolved
docker exec cpen321_app_M1 node -e "require('./dist/services/websocket.service')"
# Should not error

# Check if 'ws' package is installed
docker exec cpen321_app_M1 npm list ws
# Should show ws package version
```

## Step 5: Test WebSocket Endpoint Directly

```bash
# From your local machine, test the upgrade
curl -i -N \
  -H "Connection: Upgrade" \
  -H "Upgrade: websocket" \
  -H "Sec-WebSocket-Version: 13" \
  -H "Sec-WebSocket-Key: test" \
  http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com:80/ws

# Should return HTTP 101 Switching Protocols if working
# OR HTTP 404 if WebSocket service not initialized
```

## Expected Behavior

After deploying the updated code with error logging, you should see in logs:

✅ **If working:**
- "🔧 Creating WebSocket server..."
- "✅ WebSocket server initialized successfully"
- "[INFO] WebSocket server initialized"
- "✅ WebSocket service initialization attempted"
- "🚀 Server running on port 80"
- "🔌 WebSocket server available at ws://localhost:80/ws"

❌ **If failing:**
- "❌ Failed to initialize WebSocket service: [error details]"
- OR "❌ Error creating WebSocket server: [error details]"

## Common Issues and Fixes

### Issue 1: File doesn't exist
**Solution:** Ensure `src/services/websocket.service.ts` exists on server. If not, trigger a new deployment.

### Issue 2: TypeScript compilation failed
**Solution:** Check build output for TypeScript errors. May need to fix imports or types.

### Issue 3: Missing 'ws' package
**Solution:** `npm install ws` in container or rebuild with proper dependencies.

### Issue 4: Import error
**Solution:** Check if `dist/services/websocket.service.js` exists and can be required.

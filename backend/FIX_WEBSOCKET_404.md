# Quick Fix: Rebuild Docker Container with WebSocket Code

## SSH into your EC2 server and run these commands:

```bash
cd /home/ubuntu/SquadUp/backend

# Stop containers
docker-compose down

# Verify the WebSocket service file exists
ls -la src/services/websocket.service.ts
# Should show the file exists

# Rebuild the container (this will compile TypeScript)
docker-compose --env-file .env build

# Verify the compiled file exists in dist
docker-compose run --rm app ls -la dist/services/websocket.service.js
# Should show the compiled JavaScript file

# Start containers
docker-compose --env-file .env up -d

# Check logs to see if WebSocket initialized
docker-compose logs app | grep -i "websocket\|Server running"
# Should show:
# "WebSocket server initialized"
# "🚀 Server running on port 80"
# "🔌 WebSocket server available at ws://localhost:80/ws"
```

## If WebSocket still doesn't initialize:

Check if the file was copied during deployment:
```bash
cd /home/ubuntu/SquadUp/backend
ls -la src/services/
# Should show websocket.service.ts
```

If the file doesn't exist, the deployment didn't copy it. You can:
1. Manually copy the file, OR
2. Trigger a new deployment by making a small change to backend files and pushing to prod

## Test WebSocket endpoint:

From inside the container:
```bash
# Check if /ws endpoint is handled
docker exec cpen321_app_M1 netstat -tlnp | grep 80

# Check server logs for WebSocket attempts
docker-compose logs -f app
```

# Quick Server Verification Script for AWS EC2

# SSH into your server and run these commands to verify WebSocket is working:

# 1. Check if the app container is running
docker-compose ps

# 2. Check full server logs (not just grep for websocket)
docker-compose logs app | tail -50 contain

# 3. Look for WebSocket initialization messages
docker-compose logs app | grep -i "websocket\|ws://"

# 4. Check if the server is listening on port 80
docker exec cpen321_app_M1 netstat -tlnp | grep 80

# 5. Test WebSocket connection from inside the container
docker exec cpen321_app_M1 wscat -c ws://localhost:80/ws 2>/dev/null || echo "wscat not installed, but that's okay"

# 6. Check environment variable
docker exec cpen321_app_M1 printenv | grep PORT

# The most important thing: Check if you see these log messages:
# "🚀 Server running on port 80"
# "🔌 WebSocket server available at ws://localhost:80/ws"
# "WebSocket server initialized"

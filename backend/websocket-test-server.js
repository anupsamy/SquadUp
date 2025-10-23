const WebSocket = require('ws');

// Create WebSocket server on port 8080
const wss = new WebSocket.Server({ port: 8080 });

console.log('WebSocket test server running on ws://localhost:8080');

wss.on('connection', function connection(ws) {
    console.log('Client connected');
    
    // Send welcome message
    ws.send(JSON.stringify({
        type: 'welcome',
        message: 'Connected to test server',
        timestamp: new Date().toISOString()
    }));

    // Echo back any message received
    ws.on('message', function incoming(message) {
        console.log('Received:', message.toString());
        
        // Echo the message back
        ws.send(JSON.stringify({
            type: 'echo',
            originalMessage: message.toString(),
            timestamp: new Date().toISOString()
        }));
    });

    // Send periodic test messages
    const interval = setInterval(() => {
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(JSON.stringify({
                type: 'notification',
                message: `Test notification ${Date.now()}`,
                timestamp: new Date().toISOString()
            }));
        }
    }, 5000); // Send every 5 seconds

    ws.on('close', function close() {
        console.log('Client disconnected');
        clearInterval(interval);
    });

    ws.on('error', function error(err) {
        console.error('WebSocket error:', err);
        clearInterval(interval);
    });
});

// Handle server errors
wss.on('error', function error(err) {
    console.error('Server error:', err);
});

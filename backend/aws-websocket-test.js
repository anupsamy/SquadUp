const WebSocket = require('ws');

// Configuration - Replace with your actual AWS server details
const AWS_SERVER_URL = 'ws://your-aws-server-url:your-port/ws'; // Replace with actual URL
const TEST_GROUP_CODE = 'test123';

console.log('🔌 WebSocket Test Client for AWS Backend');
console.log(`Connecting to: ${AWS_SERVER_URL}`);

// Create WebSocket connection
const ws = new WebSocket(AWS_SERVER_URL);

ws.on('open', function open() {
    console.log('✅ Connected to AWS WebSocket server');
    
    // Subscribe to a test group
    const subscribeMessage = {
        type: 'subscribe',
        userId: 'test-user-123',
        joinCode: TEST_GROUP_CODE
    };
    
    console.log('📤 Subscribing to group:', subscribeMessage);
    ws.send(JSON.stringify(subscribeMessage));
    
    // Send a ping after 2 seconds
    setTimeout(() => {
        console.log('📤 Sending ping...');
        ws.send(JSON.stringify({ type: 'ping' }));
    }, 2000);
});

ws.on('message', function message(data) {
    try {
        const parsed = JSON.parse(data.toString());
        console.log('📥 Received message:', JSON.stringify(parsed, null, 2));
        
        // Handle different message types
        switch (parsed.type) {
            case 'group_join':
                console.log(`👋 ${parsed.userName} joined group ${parsed.joinCode}`);
                break;
            case 'group_leave':
                console.log(`👋 ${parsed.userName} left group ${parsed.joinCode}`);
                break;
            case 'group_update':
                console.log(`📢 Group update: ${parsed.message}`);
                break;
            case 'pong':
                console.log('🏓 Received pong from server');
                break;
            case 'error':
                console.log(`❌ Error: ${parsed.message}`);
                break;
            default:
                console.log(`📨 Unknown message type: ${parsed.type}`);
        }
    } catch (error) {
        console.log('📥 Raw message:', data.toString());
    }
});

ws.on('close', function close(code, reason) {
    console.log(`❌ WebSocket closed: ${code} - ${reason}`);
});

ws.on('error', function error(err) {
    console.error('❌ WebSocket error:', err);
});

// Handle process termination
process.on('SIGINT', () => {
    console.log('\n👋 Closing WebSocket connection...');
    ws.close();
    process.exit(0);
});

console.log('💡 Instructions:');
console.log('1. Replace AWS_SERVER_URL with your actual AWS server WebSocket URL');
console.log('2. Deploy your updated backend code to AWS');
console.log('3. Run this script: node aws-websocket-test.js');
console.log('4. Test notifications using your AWS API endpoints');
console.log('');
console.log('Example AWS API test commands:');
console.log(`curl -X POST https://your-aws-server/api/groups/test-notification/${TEST_GROUP_CODE} \\`);
console.log('  -H "Content-Type: application/json" \\');
console.log('  -d \'{"message": "Test notification from AWS", "type": "test"}\'');
console.log('');
console.log('Press Ctrl+C to exit');

const WebSocket = require('ws');

// Configuration - AWS server details
const AWS_SERVER_URL = 'ws://ec2-18-221-196-3.us-east-2.compute.amazonaws.com:80/ws';
const AWS_API_URL = 'http://ec2-18-221-196-3.us-east-2.compute.amazonaws.com/api';
const TEST_GROUP_CODE = 'test123';
const TEST_USER_ID = 'test-user-123';

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
console.log('1. Make sure your backend is deployed to AWS');
console.log('2. Run this script: node aws-websocket-test.js');
console.log('3. In another terminal, test notifications using curl commands (see below)');
console.log('');
console.log('📝 Test Commands (run in another terminal while this script is running):');
console.log('');
console.log('Test 1: Send a group_update notification');
console.log(`curl -X POST ${AWS_API_URL}/test/websocket-notification/${TEST_GROUP_CODE} \\`);
console.log('  -H "Content-Type: application/json" \\');
console.log('  -d \'{"message": "Test notification from AWS", "type": "test"}\'');
console.log('');
console.log('Test 2: Test with a different join code (make sure to subscribe to it first)');
console.log('You can modify TEST_GROUP_CODE in this script and reconnect');
console.log('');
console.log('Press Ctrl+C to exit');

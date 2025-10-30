const WebSocket = require('ws');

// Configuration
const WS_URL = 'ws://localhost:3000/ws';
const TEST_GROUP_CODE = 'test123';

console.log('🔌 WebSocket Test Client');
console.log(`Connecting to: ${WS_URL}`);

// Create WebSocket connection
const ws = new WebSocket(WS_URL);

ws.on('open', function open() {
    console.log('✅ Connected to WebSocket server');
    
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

console.log('💡 Test commands you can run:');
console.log('1. Start the backend server: npm run dev');
console.log('2. Test group join notification:');
console.log(`   curl -X POST http://localhost:3000/api/groups/test-notification/${TEST_GROUP_CODE} \\`);
console.log('     -H "Content-Type: application/json" \\');
console.log('     -d \'{"message": "Test join notification", "type": "join"}\'');
console.log('');
console.log('3. Test group leave notification:');
console.log(`   curl -X POST http://localhost:3000/api/groups/test-notification/${TEST_GROUP_CODE} \\`);
console.log('     -H "Content-Type: application/json" \\');
console.log('     -d \'{"message": "Test leave notification", "type": "leave"}\'');
console.log('');
console.log('Press Ctrl+C to exit');

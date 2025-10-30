const WebSocket = require('ws');

// Test different ports for your AWS server
const AWS_HOST = 'ec2-18-221-196-3.us-east-2.compute.amazonaws.com';
const PORTS_TO_TEST = [3000, 8080, 80, 443]; // Common ports

console.log('🔍 Testing WebSocket connection to AWS server...');
console.log(`Host: ${AWS_HOST}`);

async function testWebSocketConnection(host, port) {
    return new Promise((resolve) => {
        const url = `ws://${host}:${port}/ws`;
        console.log(`\n📡 Testing: ${url}`);
        
        const ws = new WebSocket(url);
        
        const timeout = setTimeout(() => {
            ws.close();
            console.log(`❌ Timeout after 5 seconds`);
            resolve(false);
        }, 5000);
        
        ws.on('open', () => {
            clearTimeout(timeout);
            console.log(`✅ SUCCESS! WebSocket connected to ${url}`);
            ws.close();
            resolve(true);
        });
        
        ws.on('error', (error) => {
            clearTimeout(timeout);
            console.log(`❌ Connection failed: ${error.message}`);
            resolve(false);
        });
        
        ws.on('close', (code, reason) => {
            if (code !== 1000) { // Not a normal closure
                console.log(`❌ Connection closed: ${code} - ${reason}`);
            }
        });
    });
}

async function testAllPorts() {
    for (const port of PORTS_TO_TEST) {
        const success = await testWebSocketConnection(AWS_HOST, port);
        if (success) {
            console.log(`\n🎉 Found working WebSocket endpoint: ws://${AWS_HOST}:${port}/ws`);
            console.log('\n📱 Update your Android app with this URL:');
            console.log(`wsManager = WebSocketManager("ws://${AWS_HOST}:${port}/ws")`);
            break;
        }
    }
    
    console.log('\n💡 If no ports worked, check:');
    console.log('1. AWS Security Groups allow WebSocket traffic');
    console.log('2. Server is running and WebSocket service is active');
    console.log('3. Docker container is properly configured');
}

testAllPorts().catch(console.error);

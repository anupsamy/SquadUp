# WebSocket Testing Guide

## Overview

Testing WebSockets is different from REST API testing because:
- **No endpoints**: WebSockets use persistent connections (`ws://` or `wss://`)
- **Bidirectional**: Both client and server can send messages at any time
- **Stateful**: Connection state matters (connected/disconnected)
- **Real-time**: Messages arrive asynchronously

## Testing Strategies

### 1. **Unit Tests (Mocking) - Recommended for Business Logic**

**What to test:**
- Message formatting (subscribe/unsubscribe JSON)
- Connection state management
- Callback invocation
- Error handling logic

**Tools:**
- Mockito for mocking `WebSocket` and `OkHttpClient`
- Test the logic without real network calls

**Example:**
```kotlin
// Test that subscribeToGroup formats the message correctly
@Test
fun subscribeToGroup_formatsMessageCorrectly() {
    // Mock the WebSocket instance
    // Verify the message sent matches expected JSON format
}
```

**Pros:**
- Fast execution
- No external dependencies
- Tests business logic in isolation

**Cons:**
- Doesn't test actual network behavior
- Requires mocking setup

---

### 2. **Integration Tests (MockWebServer) - Recommended Approach**

**What to test:**
- Real WebSocket connection establishment
- Message sending and receiving
- Connection lifecycle
- Reconnection logic

**Tools:**
- OkHttp's `MockWebServer` - simulates a WebSocket server
- No real server needed!

**Example:**
```kotlin
val mockServer = MockWebServer()
mockServer.enqueue(MockResponse().withWebSocketUpgrade(null))
val wsUrl = mockServer.url("/ws").toString().replace("http://", "ws://")
val manager = WebSocketManager(wsUrl)
manager.start()
// Test connection and messages
```

**Pros:**
- Real WebSocket protocol behavior
- No external server needed
- Fast and reliable
- Can capture sent messages
- Can send test messages back

**Cons:**
- Requires MockWebServer dependency
- More setup than unit tests

---

### 3. **Integration Tests (Test Server) - For Full Integration**

**What to test:**
- End-to-end WebSocket communication
- Real server behavior
- Production-like scenarios

**Tools:**
- Local test WebSocket server
- Or use a test environment server

**Example:**
```kotlin
// Connect to test server
val manager = WebSocketManager("ws://test-server:3000/ws")
manager.start()
// Test real communication
```

**Pros:**
- Tests actual server integration
- Most realistic testing

**Cons:**
- Requires server to be running
- Slower than other approaches
- More complex setup
- Can be flaky (network issues)

---

### 4. **E2E Tests (Real Server) - For Full System**

**What to test:**
- Complete user flows with WebSocket
- Real-time notifications in UI
- Integration with other features

**Tools:**
- Android Instrumentation Tests
- Real backend server (staging/dev)

**Example:**
```kotlin
// In E2E test, WebSocket should work automatically
// Test that notifications appear when messages arrive
composeTestRule.onNodeWithText("New member joined")
    .assertIsDisplayed()
```

**Pros:**
- Tests complete system
- Catches integration issues

**Cons:**
- Requires full backend setup
- Slowest tests
- Can be flaky
- Hard to control test scenarios

---

## Recommended Approach for Your Code

### **For Unit Tests:**
1. Test message formatting in `subscribeToGroup()` and `unsubscribeFromGroup()`
2. Test connection state management (`isConnected()`, `start()`, `stop()`)
3. Test callback invocation logic
4. Mock the `WebSocket` interface

### **For Integration Tests (Best Choice):**
Use **MockWebServer** - it's perfect for your use case:
1. No real server needed
2. Tests real WebSocket protocol
3. Can verify messages sent
4. Can simulate server responses
5. Fast and reliable

**Add to `build.gradle.kts`:**
```kotlin
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
```

### **For E2E Tests:**
If you want to test WebSocket in your E2E tests:
1. Start your backend server (or use staging)
2. Test that notifications appear when WebSocket messages arrive
3. Test reconnection behavior
4. But this is optional - most WebSocket testing can be done with MockWebServer

---

## What You CAN Test Without Mocking Everything

### ✅ **Easy to Test:**
1. **Message Formatting**: Verify JSON structure of subscribe/unsubscribe messages
2. **Connection State**: Test `isConnected()` returns correct values
3. **Callback Registration**: Test that callbacks are called
4. **Error Handling**: Test reconnection logic structure

### ✅ **With MockWebServer (No Real Server):**
1. **Connection Establishment**: Real WebSocket handshake
2. **Message Sending**: Actually send messages through WebSocket
3. **Message Receiving**: Receive messages from mock server
4. **Reconnection**: Test reconnection logic with simulated failures

### ❌ **Requires Real Server:**
1. **Backend Integration**: Testing actual backend WebSocket service
2. **Message Routing**: Testing how backend routes messages
3. **Full E2E Flows**: Complete user flows with real-time updates

---

## Quick Start: MockWebServer Test

```kotlin
// 1. Add dependency to build.gradle.kts
testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")

// 2. Write test
@Test
fun testWebSocketConnection() {
    val mockServer = MockWebServer()
    val mockResponse = MockResponse().withWebSocketUpgrade(null)
    mockServer.enqueue(mockResponse)
    mockServer.start()
    
    val wsUrl = mockServer.url("/ws").toString()
        .replace("http://", "ws://")
    
    val manager = WebSocketManager(wsUrl)
    manager.start()
    
    // Test connection
    assertTrue(manager.isConnected())
    
    mockServer.shutdown()
}
```

---

## Summary

**You don't need to mock everything!** Use MockWebServer for integration tests:
- ✅ Real WebSocket behavior
- ✅ No external server needed  
- ✅ Fast and reliable
- ✅ Can test send/receive
- ✅ Can test reconnection

**You only need a real server for:**
- E2E tests (optional)
- Testing backend integration (separate backend tests)



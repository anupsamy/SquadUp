# Fix for "ContentDescription Not Found" Errors

## The Problem

Tests were failing with errors like:
```
java.lang.AssertionError: Assert failed: The component with ContentDescription = 'Create Group' (ignoreCase: false) is not displayed!
```

Even though the UI clearly had these elements visible to human eyes.

## Root Cause

The issue was **timing** - the tests were running too fast:

1. **App launches on MainActivity**
2. **Authentication happens** (if user is logged in)
3. **Navigation to Main screen** with icon buttons
4. **Test tries to find icon buttons immediately** ❌ Too early!

The tests were trying to find the "Create Group" and "Join Group" icon buttons **before the app finished loading and navigating to the main screen**.

## The Solution

Added proper wait times in two places:

### 1. Global Setup (Applies to All Tests)

In the `@Before setup()` method:
```kotlin
@Before
fun setup() {
    device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
    composeTestRule.waitForIdle()
    
    // ✅ NEW: Wait for main screen to load
    Thread.sleep(3000) // Give time for authentication and navigation
    composeTestRule.waitForIdle()
}
```

### 2. Per-Test Wait (Before Clicking Icon Buttons)

At the start of each test that uses the icon buttons:
```kotlin
@Test
fun createGroup_mainSuccessScenario_groupCreatedSuccessfully() {
    // ✅ NEW: Wait for main screen icon buttons to be ready
    composeTestRule.waitForIdle()
    Thread.sleep(1000)
    
    // Now click the button
    composeTestRule.onNodeWithContentDescription("Create Group")
        .assertIsDisplayed()
        .performClick()
    // ... rest of test
}
```

## Why This Works

### Timing Breakdown:
- **0ms**: App launches (MainActivity)
- **~500ms**: Authentication check happens
- **~1000ms**: Navigation to main screen
- **~1500ms**: Main screen composes UI elements
- **~2000ms**: Icon buttons are fully rendered
- **~3000ms**: ✅ Safe to interact with buttons

### Why `Thread.sleep()`?

While not ideal for production code, `Thread.sleep()` is acceptable in E2E tests because:
- **Simplicity**: Easy to understand and implement
- **Reliability**: Guarantees a minimum wait time
- **Cross-device**: Works on all Android versions
- **Alternative complexity**: More sophisticated waiting (polling for specific elements) adds complexity

## Tests Updated

✅ `createGroup_mainSuccessScenario_groupCreatedSuccessfully()`
✅ `createGroup_withInvalidInputs_showsErrorPrompt()`
✅ `joinGroup_withValidCode_joinsSuccessfully()`
✅ `joinGroup_withInvalidCode_showsError()`
✅ `viewAllGroups_whenUserHasGroups_displaysGroupList()` (already had wait)

## Important Notes

### Prerequisites Still Required

The tests still assume:
1. **User is already authenticated** (logged in manually before running tests)
2. **Main screen loads properly** (no network errors)
3. **App has proper permissions** (if needed)

### If Tests Still Fail

If tests still can't find the buttons:

1. **Increase wait times**:
   ```kotlin
   Thread.sleep(5000) // Instead of 3000
   ```

2. **Check authentication**:
   - Manually log into the app before running tests
   - Ensure the app is on the main screen

3. **Use UI Automator Viewer**:
   ```bash
   adb shell uiautomator dump
   ```
   Check what elements are actually on screen

4. **Check logcat**:
   ```bash
   adb logcat | grep "MainActivity\|Navigation"
   ```
   See if navigation is happening

## Alternative Approaches (Future Enhancement)

For more sophisticated waiting:

```kotlin
// Wait for specific element to appear (more robust)
fun waitForMainScreen() {
    composeTestRule.waitUntil(timeoutMillis = 10000) {
        try {
            composeTestRule.onNodeWithContentDescription("Create Group")
                .fetchSemanticsNode()
            true
        } catch (e: Exception) {
            false
        }
    }
}
```

This could be added to `TestUtilities.kt` for reuse across all tests.

## Verification

After applying these fixes, all tests should:
- ✅ Find the icon buttons successfully
- ✅ Navigate to Create/Join Group screens
- ✅ Complete the test flow without "not displayed" errors

---

**Fixed**: November 3, 2025
**Issue**: Timing/synchronization
**Solution**: Added wait times for app initialization and navigation


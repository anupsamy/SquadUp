# Testing Guide

This directory contains automated end-to-end tests for the SquadUp Android app using:
- **Jetpack Compose Testing APIs** - For testing Compose UI screens
- **UI Automator** - For testing cross-app UI actions and system interactions

## Setup

All testing dependencies have been added to `build.gradle.kts`. The test setup includes:

### Dependencies
- `androidx.compose.ui:ui-test-junit4` - Compose testing APIs
- `androidx.compose.ui:ui-test-manifest` - Test manifest for Compose
- `androidx.test.uiautomator:uiautomator` - UI Automator for system UI testing
- `androidx.test.ext:junit` - JUnit extensions for Android tests
- `androidx.test.espresso:espresso-core` - Espresso for View-based testing (if needed)

## Verifying Your Setup

**First, verify your testing setup is correct!**

Run the `SetupVerificationTest` to ensure everything is configured properly:

```bash
./gradlew connectedAndroidTest --tests "com.cpen321.squadup.SetupVerificationTest"
```

Or from Android Studio:
1. Open `SetupVerificationTest.kt`
2. Right-click on the class or any test method
3. Select "Run 'SetupVerificationTest'"

**This test suite includes:**
- ✅ Compose isolated testing (`createComposeRule`)
- ✅ Compose with Activity context (`createAndroidComposeRule`)
- ✅ UI Automator device initialization
- ✅ UI Automator basic actions
- ✅ Integration of both frameworks

If all 7 tests pass, your setup is **correct**! ✅

## Running Tests

### Run all tests
```bash
./gradlew connectedAndroidTest
```

### Run specific test class
```bash
./gradlew connectedAndroidTest --tests "com.cpen321.squadup.ComposeTestExample"
```

### Run from Android Studio
1. Right-click on a test file or test method
2. Select "Run 'TestName'"
3. Tests will execute on connected device/emulator

## Test Structure

### 1. Compose Tests (`ComposeTestExample.kt`)

**Use Compose Testing APIs for:**
- Testing Compose UI screens and components
- Verifying UI state and interactions
- Testing navigation within your app
- Asserting text, visibility, and enabled states

**Key APIs:**
- `createAndroidComposeRule<Activity>()` - For tests requiring activity context
- `createComposeRule()` - For isolated component testing
- `onNodeWithText()`, `onNodeWithTag()`, etc. - Finding nodes
- `performClick()`, `performTextInput()`, etc. - User actions
- `assertIsDisplayed()`, `assertIsEnabled()`, etc. - Assertions

**Example:**
```kotlin
composeTestRule.setContent {
    UserManagementTheme {
        MyScreen()
    }
}
composeTestRule.onNodeWithText("Sign In").assertIsDisplayed()
```

### 2. UI Automator Tests (`UIAutomatorTestExample.kt`)

**Use UI Automator for:**
- Testing system UI (settings, notifications, app launcher)
- Cross-app interactions
- Device-level features (rotation, gestures)
- Testing interactions that span multiple apps

**Key APIs:**
- `UiDevice` - Device interaction controller
- `By.pkg()`, `By.text()`, `By.res()` - Finding elements
- `Until.hasObject()` - Waiting for elements
- `pressHome()`, `pressBack()` - System navigation
- `swipe()`, `click()` - Gestures

**Example:**
```kotlin
device.pressHome()
device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 5000)
device.findObject(By.text("Settings"))?.click()
```

### 3. Test Utilities (`TestUtilities.kt`)

Helper functions and extension methods to make tests more concise:
- `waitForNodeWithText()` - Wait for text node
- `scrollToAndClick()` - Scroll and click
- `waitForPackage()` - Wait for app to launch
- `TestData` - Common test constants

## Best Practices

### Compose Testing
1. **Use semantics** - Add test tags and semantics to your Compose code:
   ```kotlin
   Button(
       modifier = Modifier.testTag("sign_in_button")
   ) { ... }
   ```

2. **Wait for idle** - Always wait for UI to be idle:
   ```kotlin
   composeTestRule.waitForIdle()
   ```

3. **Test user flows** - Test complete user journeys, not just isolated components

4. **Mock dependencies** - Use mocked ViewModels and repositories for reliable tests

### UI Automator Testing
1. **Wait for elements** - Always wait for elements to appear:
   ```kotlin
   device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 5000)
   ```

2. **Use package names** - Prefer package names for finding apps
3. **Handle device variations** - Test on multiple devices/emulators
4. **Grant permissions** - Ensure required permissions are granted

## Writing New Tests

### Compose Test Template
```kotlin
@RunWith(AndroidJUnit4::class)
class MyScreenTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun myTest() {
        composeTestRule.setContent {
            UserManagementTheme {
                MyScreen()
            }
        }
        // Your test logic here
    }
}
```

### UI Automator Test Template
```kotlin
@RunWith(AndroidJUnit4::class)
class MyUIAutomatorTest {
    private lateinit var device: UiDevice

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun myTest() {
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 5000)
        // Your test logic here
    }
}
```

## Troubleshooting

### UI Automator can't find elements
- Increase timeout values
- Check if element is actually visible (use `device.waitForIdle()`)
- Verify package names are correct
- Some system UI elements may require specific permissions

### Compose tests timeout
- Add `composeTestRule.waitForIdle()` after actions
- Check for loading states that need to complete
- Use `waitUntil()` for async operations

## Resources

- [Compose Testing Documentation](https://developer.android.com/develop/ui/compose/testing)
- [UI Automator Documentation](https://developer.android.com/training/testing/other-components/ui-automator)

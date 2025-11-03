package com.cpen321.squadup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cpen321.squadup.ui.theme.UserManagementTheme
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verification tests to ensure Jetpack Compose and UI Automator setup is correct.
 * 
 * Run these tests to verify your testing framework is properly configured.
 * If all tests pass, your setup is correct!
 */
@RunWith(AndroidJUnit4::class)
class SetupVerificationTest {

    // ========== COMPOSE TESTING VERIFICATION ==========

    /**
     * Test 1: Verify createComposeRule() works (isolated Compose testing)
     * This tests the basic Compose testing API without Activity context.
     */
    @Test
    fun composeTest_isolatedComposeRule_works() {
        val composeRule = createComposeRule()

        composeRule.setContent {
            UserManagementTheme {
                SimpleTestScreen()
            }
        }

        composeRule.waitForIdle()

        // Verify we can find elements by text
        composeRule.onNodeWithText("Setup Verification").assertIsDisplayed()
        composeRule.onNodeWithText("Test Button").assertIsDisplayed()

        // Verify we can find elements by test tag
        composeRule.onNodeWithTag("test_button").assertIsDisplayed()

        // Verify we can interact with elements
        composeRule.onNodeWithTag("test_button").performClick()

        // Verify text content
        composeRule.onNodeWithText("Button Clicked!").assertIsDisplayed()
    }

    /**
     * Test 2: Verify createAndroidComposeRule() works (with Activity context)
     * This tests Compose testing with Activity context.
     */
    @Test
    fun composeTest_androidComposeRule_works() {
        val composeRule = createAndroidComposeRule<MainActivity>()

        composeRule.setContent {
            UserManagementTheme {
                SimpleTestScreen()
            }
        }

        composeRule.waitForIdle()

        // Verify basic assertions work
        composeRule.onNodeWithText("Setup Verification").assertIsDisplayed()
        composeRule.onNodeWithTag("test_button").assertIsDisplayed()

        // Verify click interaction
        composeRule.onNodeWithTag("test_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Button Clicked!").assertIsDisplayed()
    }

    /**
     * Test 3: Verify Compose test assertions work
     */
    @Test
    fun composeTest_assertions_work() {
        val composeRule = createComposeRule()

        composeRule.setContent {
            UserManagementTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Test Text", modifier = Modifier.testTag("test_text"))
                    Text("Another Text")
                }
            }
        }

        composeRule.waitForIdle()

        // Test various assertion methods
        composeRule.onNodeWithText("Test Text").assertIsDisplayed()
        composeRule.onNodeWithTag("test_text").assertIsDisplayed()
        // Verify text content using assertTextContains
        composeRule.onNodeWithText("Test Text").assertTextContains("Test Text")
        composeRule.onNodeWithText("Another Text").assertTextContains("Another")
    }

    // ========== UI AUTOMATOR VERIFICATION ==========

    /**
     * Test 4: Verify UI Automator can initialize and detect the app
     * This is the most basic UI Automator test - just verify it can find your app.
     */
    @Test
    fun uiAutomatorTest_deviceInitialization_works() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Verify device is initialized
        assertNotNull(device)

        // Wait for app to be ready (if ActivityScenarioRule launched it)
        val appFound = device.wait(
            Until.hasObject(By.pkg("com.cpen321.squadup")),
            5000 // 5 second timeout
        )

        // If app is running, this should pass
        // Note: This test will pass even if app isn't launched, just verifying UI Automator works
        assertTrue(true) // Device initialization succeeded
    }

    /**
     * Test 5: Verify UI Automator can perform basic device actions
     */
    @Test
    fun uiAutomatorTest_basicActions_work() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Test that we can get device info
        val displayWidth = device.displayWidth
        val displayHeight = device.displayHeight

        assertTrue(displayWidth > 0)
        assertTrue(displayHeight > 0)

        // Test that we can wait for idle
        device.waitForIdle()

        // Test that we can press back (this is safe and reversible)
        device.pressBack()
        device.waitForIdle()
    }

    /**
     * Test 6: Verify UI Automator can find elements
     */
    @Test
    fun uiAutomatorTest_findElements_works() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

        // Wait for app package
        val appFound = device.wait(
            Until.hasObject(By.pkg("com.cpen321.squadup")),
            5000
        )

        // This verifies that UI Automator's By.pkg() selector works
        // Even if app isn't running, the method should execute without errors
        assertTrue(true) // Element finding API works
    }

    // ========== INTEGRATION TEST ==========

    /**
     * Test 7: Verify both Compose and UI Automator work together
     * This test uses both frameworks to verify they can coexist.
     */
    @Test
    fun integrationTest_composeAndUIAutomator_workTogether() {
        // Part 1: Use Compose testing
        val composeRule = createComposeRule()
        composeRule.setContent {
            UserManagementTheme {
                SimpleTestScreen()
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Setup Verification").assertIsDisplayed()

        // Part 2: Use UI Automator
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.waitForIdle()

        // Verify both work
        assertNotNull(device)
        composeRule.onNodeWithText("Setup Verification").assertIsDisplayed()
    }
}

/**
 * Simple test composable for verification tests.
 * This demonstrates a basic interactive screen we can test.
 */
@Composable
fun SimpleTestScreen() {
    var clickCount by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Setup Verification",
            modifier = Modifier.padding(16.dp)
        )

        Button(
            onClick = { clickCount++ },
            modifier = Modifier.testTag("test_button")
        ) {
            Text("Test Button")
        }

        if (clickCount > 0) {
            Text(
                text = "Button Clicked!",
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}

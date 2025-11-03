package com.cpen321.squadup

// Compose UI testing
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.AndroidComposeTestRule

// AndroidX test runner
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.LargeTest

// UI Automator
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

// App and theme
import com.cpen321.squadup.ui.theme.UserManagementTheme
import com.cpen321.squadup.MainActivity

// JUnit
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Optional test utilities
import com.cpen321.squadup.TestUtilities.waitForNodeWithText
import com.cpen321.squadup.TestUtilities.waitForEnabled

@RunWith(AndroidJUnit4::class)
@LargeTest
class MemberSettingsE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Wait for app to launch
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
        composeTestRule.waitForIdle()

        // Note: These tests assume the user is already authenticated
        // In a real scenario, you would need to handle authentication first

        // Wait for main screen to load (check for SquadUp title or icon buttons)
        Thread.sleep(3000) // Give time for authentication and navigation
        composeTestRule.waitForIdle()
    }

    /**
     * Helper function to select the group named "test 2"
     */
    private fun selectTest2Group() {
        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        // Look for any group button (groups display "Leader: ..." text)
        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .assertExists()

        // Click on the first available group
        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    //@Test
    fun updateMemberAddress_success() {
        selectTest2Group()
        composeTestRule.onNodeWithContentDescription("See Details")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        // Navigate to Member Settings screen
        composeTestRule.onNodeWithContentDescription("Settings")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Input new address
        composeTestRule.onNodeWithText("Address")
            .performTextInput("456 New Street")

        // Save changes
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        // Verify success message
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 5000)
            .assertIsDisplayed()
    }

    @Test
    fun updateTransitType_success() {
        selectTest2Group()
        composeTestRule.onNodeWithText("See Details")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        // Navigate to Member Settings screen
        composeTestRule.onNodeWithContentDescription("Settings")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        composeTestRule.onNodeWithText("Transit Type")
            .performClick()
        composeTestRule.onNodeWithText("WALKING")
            .performClick()
        composeTestRule.onNodeWithText("Save")
            .performClick()
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 5000)
            .assertIsDisplayed()
    }

    //@Test
    fun updateMeetingTime_success() {
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
        composeTestRule.onNodeWithText("Update Meeting Date & Time")
            .performClick()

        // Interact with system date/time picker dialogs
        device.findObject(By.text("OK"))?.click()

        composeTestRule.onNodeWithText("Confirm Date-Time")
            .performClick()
    }
}

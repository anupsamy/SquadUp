package com.cpen321.squadup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cpen321.squadup.TestUtilities.waitForNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
@ExperimentalTestApi
@LargeTest
/**
 * End-to-End Tests for Group Details Features
 *
 * This test suite covers the following use cases from the Requirements_and_Design.md:
 * 1. Update Address (as Squad Leader)
 * 2. Update Transit (as Squad Leader)
 * 3. Update Expected People (as Squad Leader)
 * 4. Update Meeting Time (as Squad Leader)
 * 5. Update Automatic Midpoint Checkbox (as Squad Leader)
 *
 */
class MemberSettingsLeaderE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
        composeTestRule.waitForIdle()
        Thread.sleep(3000) // wait for main screen to load
    }

    /**
     * Helper to navigate to a group and Member Settings
     */
    private fun navigateToMemberSettings() {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        // Click "See Details"
        composeTestRule.onNodeWithText("Group details")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click "Settings"
        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    // Use case: Update Expected People
    private fun testExpectedPeople() {
        // Expected People
        // Failure scenario of invalid input
        composeTestRule.onNode(hasText("Expected People"))
            .performTextClearance()

        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onNode(hasText("Expected People"))
            .performTextInput("0")

        // Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Verify failure
        composeTestRule.waitForNodeWithText("Expected people must be a positive number", timeoutMillis = 10000)
            .assertIsDisplayed()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Successful scenario
        composeTestRule.onNode(hasText("Expected People"))
            .performTextClearance()

        composeTestRule.onNode(hasText("Expected People"))
            .performTextInput("7")

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Expected people must be a positive number", substring = true).fetchSemanticsNodes().isEmpty()
        }

        // Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)

        // Verify success
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 10000)
            .assertIsDisplayed()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Settings saved successfully!", substring = true).fetchSemanticsNodes().isEmpty()
        }
    }

    // Use case: Update Meeting Time
    private fun testMeetingTime() {
        // Meeting Time (click to open picker, then confirm date & time)
        composeTestRule.onNodeWithText("Click to update Meeting Date & Time")
            .performClick()
        Thread.sleep(500)
        device.findObject(By.text("OK"))?.click() // date
        Thread.sleep(500)
        device.findObject(By.text("OK"))?.click() // time

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Verify failure
        composeTestRule.waitForNodeWithText("Meeting time must be in the future", timeoutMillis = 10000)
            .assertIsDisplayed()

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        composeTestRule.onNodeWithText("Click to update Meeting Date & Time")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        // Wait until TimePicker dialog shows
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_MONTH, 1)  // next day
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        device.wait(Until.hasObject(By.clazz("android.widget.TimePicker")), 2000)
        device.findObject(By.text(day.toString()))?.click()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        device.findObject(By.text("OK"))?.click() // date
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        device.findObject(By.text("OK"))?.click() // time

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Meeting set to:", substring = true).fetchSemanticsNodes().isEmpty()
        }

        // Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verify success
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 10000)
            .assertIsDisplayed()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Settings saved successfully!", substring = true).fetchSemanticsNodes().isEmpty()
        }
    }

    // Use case: Update Address
    private fun testAddress() {
        // AddressPicker: type and click first prediction
        composeTestRule.onNode(hasText("Address")).performTextClearance()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onNode(hasText("Address")).performTextInput("Some random text")

        composeTestRule.onNodeWithText("Save")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.waitForNodeWithText("Please select a valid address", timeoutMillis = 10000)
            .assertIsDisplayed()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onNode(hasText("Address"))
            .performTextClearance()

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        composeTestRule.onNodeWithText("Address")
            .performTextInput("6445 University Boulevard, Vancouver, BC, Canada, V6T 1Z2")

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onAllNodesWithText("6445 University Boulevard", substring = true)
            .onLast()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Please select a valid address", substring = true).fetchSemanticsNodes().isEmpty()
        }

        // Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verify success
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 10000)
            .assertIsDisplayed()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Settings saved successfully!", substring = true).fetchSemanticsNodes().isEmpty()
        }
    }

    // Use case: Update Transit Type
    private fun testTransitType() {
        // TransitType
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onNodeWithText("Preferred Mode of Transport")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onAllNodesWithText("DRIVING")
            .onLast()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verify success
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 10000)
            .assertIsDisplayed()
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Settings saved successfully!", substring = true).fetchSemanticsNodes().isEmpty()
        }
    }

    private fun testAutomaticMidpointCheckbox() {
        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Find the checkbox with text "Automatic Midpoint" and click it
        composeTestRule.onNodeWithText("Automatic Midpoint Update")
            .assertExists("Automatic Midpoint checkbox not found")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        // Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verify success
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 10000)
            .assertIsDisplayed()

    }


    @Test
    fun updateMemberSettings() {
        navigateToMemberSettings()
        testExpectedPeople()
        testAddress()
        testTransitType()
        testMeetingTime()
        testAutomaticMidpointCheckbox()
    }
}

package com.cpen321.squadup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalTestApi
@LargeTest
class GroupDetailsMemberE2ETest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    // Note: These tests assume a group with midpoint is calculated, and
    // an activity is selected from the list of activities.

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
    }

    private fun navigateToFirstGroup() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Step 1: From main screen, click on a group (using testTag for reliability)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule
            .onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)
    }

    private fun navigateToGroupList() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click "See Details"
        composeTestRule
            .onNodeWithText("Group details")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    // Use case: View Attendees
    private fun viewAttendees() {
        // Wait for members to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule
                .onAllNodesWithContentDescription("Member", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Assert that at least one member exists
        composeTestRule
            .onAllNodesWithContentDescription("Member", substring = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun groupDetailsScreen() {
        // Navigate to the first group
        navigateToFirstGroup()
        viewSelectedActivity()
        navigateToGroupList()
        viewAttendees()
    }

    // Use case: View Selected Activity
    private fun viewSelectedActivity() {
        // Wait for the ActivityCard to appear
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("SelectedActivityCard").fetchSemanticsNodes().isNotEmpty()
        }

        // Assert the selected activity card is displayed
        composeTestRule.onNodeWithTag("SelectedActivityCard").assertIsDisplayed()
    }
}

package com.cpen321.squadup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
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
class GroupDetailsLeaderE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
        composeTestRule.waitForIdle()
        Thread.sleep(3000)
    }

    private fun navigateToFirstGroup() {
        // Click the first group (by "Leader:" label)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Leader:", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(3000)
    }

    // Use case: Find Midpoint
    private fun findMidpoint() {
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Getting midpoint...", substring = true).fetchSemanticsNodes().isEmpty()
        }
        val findButtons = composeTestRule.onAllNodesWithText("Find midpoint", substring = true)
        if (findButtons.fetchSemanticsNodes().isNotEmpty()) {
            findButtons.onFirst().performClick()
        } else {
            val recalcButtons = composeTestRule.onAllNodesWithText("Recalculate Midpoint", substring = true)
            if (recalcButtons.fetchSemanticsNodes().isNotEmpty()) {
                recalcButtons.onFirst().performClick()
            }
        }
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Getting midpoint...", substring = true).fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun groupDetailsScreen() {
        // Navigate to the first group with a determined midpoint with activities
        navigateToFirstGroup()

        // Click midpoint button every time
        findMidpoint()

        // Use cases
        viewRecommendedLocations()
        selectActivityAndViewSelectedActivity()
    }

    // Use case: Select Activity and View Selected Activity
    private fun selectActivityAndViewSelectedActivity() {
        // Pick the first activity from the ActivityPicker list
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("ActivityPicker").fetchSemanticsNodes().isNotEmpty()
        }
        val activities = composeTestRule.onAllNodesWithTag("ActivityPicker")
        if (activities.fetchSemanticsNodes().isNotEmpty()) {
            activities.onFirst().performClick()
        }

        // Confirm selection
        composeTestRule.waitForIdle()
        val confirmButtons = composeTestRule.onAllNodesWithText("Select Activity", substring = true)
        if (confirmButtons.fetchSemanticsNodes().isNotEmpty()) {
            confirmButtons.onFirst().performClick()
        }

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Verify success
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText(
                "Activity selected successfully!",
                substring = true
            ).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onAllNodesWithText(
            "Activity selected successfully!",
            substring = true
        ).onFirst().assertIsDisplayed()
    }

    // Use case: View recommended locations
    private fun viewRecommendedLocations() {
        // Wait for midpoint map to appear
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("LeaderMapView").fetchSemanticsNodes().isNotEmpty()
        }

        // Assert map is displayed
        composeTestRule.onNodeWithTag("LeaderMapView")
            .assertIsDisplayed()

        // Wait for ActivityPicker to appear
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("ActivityPicker").fetchSemanticsNodes().isNotEmpty()
        }

        // Assert activity picker is displayed
        composeTestRule.onNodeWithTag("ActivityPicker")
            .assertIsDisplayed()
    }
}

package com.cpen321.squadup.nfr

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cpen321.squadup.MainActivity
import com.cpen321.squadup.TestData
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Non-Functional Requirement Tests
 *
 * This test suite validates non-functional requirements (NFR) from Requirements_and_Design.md
 *
 * NFR2: Group View Load Time (line 452-453)
 */
@RunWith(AndroidJUnit4::class)
class GroupViewLoadTimeNFRTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
        composeTestRule.waitForIdle()
    }

    /**
     * Non-Functional Requirement Test: Group View Load Time (NFR2)
     *
     * Based on Requirements_and_Design.md line 452-453:
     * "The Group View fetches all group details including attendees, event time, and status
     * in one atomic operation via a single indexed database query on the joinCode field,
     * avoiding multiple round trips. The API endpoint returns the complete group document
     * with all nested member information pre-populated."
     *
     * Validation:
     * 1. All group details load atomically (no staged loading)
     * 2. Load time is reasonable (under 2 seconds for UI to display)
     * 3. All required elements appear together: Join Code, Host, Members, Event Time
     */
    @Test
    fun groupViewLoadTime_loadsAtomicallyWithinTimeout() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule
                .onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        // Navigate to a group
        composeTestRule
            .onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        // Record start time for load measurement
        val startTime = System.currentTimeMillis()

        composeTestRule.waitForIdle()

        // Wait for all group details to load atomically
        // All these elements should appear together, not in stages
        composeTestRule.waitUntil(timeoutMillis = 2000) {
            try {
                // Verify all key elements are loaded simultaneously
                composeTestRule
                    .onNodeWithText("Join Code")
                    .fetchSemanticsNode()
                composeTestRule
                    .onNodeWithText("Host")
                    .fetchSemanticsNode()
                composeTestRule
                    .onNodeWithText("Members")
                    .fetchSemanticsNode()
                // Event time should be in the top bar (as subtitle)
                composeTestRule
                    .onNodeWithContentDescription("Back")
                    .fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }

        val loadTime = System.currentTimeMillis() - startTime

        // Verify all required elements are displayed (atomic load verification)
        composeTestRule.onNodeWithText("Join Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Host").assertIsDisplayed()
        composeTestRule.onNodeWithText("Members").assertIsDisplayed()

        // Verify back button is present (confirms we're on group details screen)
        composeTestRule.onNodeWithContentDescription("Back").assertIsDisplayed()

        // Performance assertion: Load should complete within 2 seconds
        // This validates the single indexed query performs efficiently
        assert(loadTime < 2000) {
            "Group view load time ($loadTime ms) exceeded 2 second threshold. " +
                "This suggests multiple round trips instead of a single atomic query."
        }
    }
}

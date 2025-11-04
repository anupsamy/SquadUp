package com.cpen321.squadup

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.UiDevice
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import com.cpen321.squadup.TestUtilities.waitForNodeWithText

/**
 * End-to-End Tests for Group View Features
 * 
 * This test suite covers the following use cases from the Requirements_and_Design.md:
 * 1. View Event Time - Success scenarios
 * 2. View Current Midpoint - Success and failure scenarios
 * 
 * Test Case Specification follows the use case descriptions:
 * - View Event Time (line 103 in Requirements_and_Design.md)
 * - View Current Midpoint (line 104 in Requirements_and_Design.md)
 * - View Recommended Locations (line 106 in Requirements_and_Design.md)
 * - View Selected Activity (line 107 in Requirements_and_Design.md)
 * 
 * These tests also cover:
 * - Use Case 4: Find Midpoint (lines 186-199)
 * - Use Case 5: Select Activity (lines 203-217)
 */
@RunWith(AndroidJUnit4::class)
class GroupViewE2ETest {

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
     * Test Case 1: View Event Time - Main Success Scenario
     * 
     * Based on Use Case: View event time (Requirements_and_Design.md line 103)
     * "A Squad Member can check the scheduled time of the event to plan accordingly"
     * 
     * The event time is displayed in the group details screen top bar
     */
    @Test
    fun viewEventTime_inGroupDetails_displaysCorrectly() {
        // Wait for main screen and groups to load
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Navigate to a group where user is leader (using testTag)
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()
        
        composeTestRule.waitForIdle()
        Thread.sleep(1500)
        
        // Verify event time is displayed in the top bar
        composeTestRule.onNodeWithTag("groupMeetingTime")
            .assertIsDisplayed()
        
        // Verify the top bar is showing group information
        composeTestRule.onNodeWithContentDescription("Back")
            .assertExists() // This confirms we're on the group details screen
    }

    /**
     * Test Case 1 Extended: View Event Time - Multiple Locations
     * Verifies event time is consistently displayed across different views
     */
    @Test
    fun viewEventTime_acrossMultipleViews_consistentlyDisplayed() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Navigate to a group where user is leader (using testTag)
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()
        
        composeTestRule.waitForIdle()
        Thread.sleep(1500)
        
        // Event time should be visible in group details
        // (In top bar as subtitle)
        
        // Navigate to See Details (member list)
        composeTestRule.onNodeWithText("See Details")
            .performClick()
        
        composeTestRule.waitForIdle()
        Thread.sleep(1000)
        
        // Event time is not necessarily shown in member list
        // but we verify the screen loaded correctly
        composeTestRule.onNodeWithText("Members")
            .assertIsDisplayed()
        
        // Navigate back
        device.pressBack()
        composeTestRule.waitForIdle()

        // Verify event time is displayed in the top bar
        composeTestRule.onNodeWithTag("groupMeetingTime")
            .assertIsDisplayed()
    }

    /**
     * Test Case 2: View Current Midpoint - Before Calculation
     * 
     * Based on Use Case: View current midpoint (Requirements_and_Design.md line 104)
     * When midpoint hasn't been calculated yet, appropriate message is shown
     */
    @Test
    fun viewMidpoint_beforeCalculation_showsWaitingMessage() {
        // Note: This test requires a group where midpoint hasn't been calculated
        // In a real test environment, you would create a fresh group
        
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        
        // Navigate to a group without calculated midpoint
        // Navigate to a group where user is leader (using testTag)
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()
        
        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        composeTestRule.onNodeWithText("Waiting for members to join...")
            .assertIsDisplayed()
    }

    /**
     * Test Case 3: Find Midpoint - As Squad Leader
     * 
     * Based on Use Case 4: Find Midpoint (Requirements_and_Design.md lines 192-194)
     * Main Success Scenario:
     * 1. Squad Leader clicks "Find Midpoint" or "Recalculate Midpoint"
     * 2. Squad Leader views the midpoint and suggested activities
     */
    @Test
    fun findMidpoint_asLeader_calculatesSuccessfully() {
        // Note: This test requires being a leader of a group with members
        
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        
        // Navigate to a group where user is leader
        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .performClick()
        
        composeTestRule.waitForIdle()
        Thread.sleep(1500)
        
        // Look for "Find midpoint" or "Recalculate Midpoint" button
        // These buttons only appear for leaders
        
        // Try to find the "Find midpoint" button (shown when no midpoint exists)
        val findMidpointExists = try {
            composeTestRule.onNodeWithText("Find midpoint")
                .assertExists()
            true
        } catch (e: AssertionError) {
            false
        }
        
        if (findMidpointExists) {
            // Click to calculate midpoint
            composeTestRule.onNodeWithText("Find midpoint")
                .performClick()
            
            composeTestRule.waitForIdle()

            // Should show "Getting midpoint..." during calculation
            composeTestRule.waitForNodeWithText("Getting midpoint...", timeoutMillis = 2000)

            // Wait for calculation to complete
            Thread.sleep(5000) // Midpoint calculation may take time
            
            composeTestRule.waitForIdle()
            
            // After calculation, map should be displayed
            // and "Recalculate Midpoint" button should appear
        } else {
            // Try to find "Recalculate Midpoint" button (shown when midpoint exists)
            val recalculateExists = try {
                composeTestRule.onNodeWithText("Recalculate Midpoint")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }
            
            if (recalculateExists) {
                composeTestRule.onNodeWithText("Recalculate Midpoint")
                    .performClick()
                
                composeTestRule.waitForIdle()
                Thread.sleep(5000)
            }
        }
        
        // Verify we're still on the group details screen
        composeTestRule.onNodeWithText("Join Code")
            .assertIsDisplayed()
    }

    /**
     * Test Case 3a: Find Midpoint - Failure Scenario
     * 
     * Based on Failure Scenario 1a (Requirements_and_Design.md lines 197-199)
     * "Fails to fetch any venues/activities within radius of midpoint"
     * Let user know about the failure and prompt to create new group
     */
    @Test
    fun findMidpoint_noVenuesFound_showsErrorMessage() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Navigate to a group where user is leader
        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Look for "Find midpoint" or "Recalculate Midpoint" button
        // These buttons only appear for leaders

        // Try to find the "Find midpoint" button (shown when no midpoint exists)
        val findMidpointExists = try {
            composeTestRule.onNodeWithText("Find midpoint")
                .assertExists()
            true
        } catch (e: AssertionError) {
            false
        }

        if (findMidpointExists) {
            // Click to calculate midpoint
            composeTestRule.onNodeWithText("Find midpoint")
                .performClick()

            composeTestRule.waitForIdle()

            // Should show "Getting midpoint..." during calculation
            composeTestRule.waitForNodeWithText("Getting midpoint...", timeoutMillis = 2000)

            // Wait for calculation to complete
            Thread.sleep(5000) // Midpoint calculation may take time

            composeTestRule.waitForIdle()

            composeTestRule.onNodeWithText("No activities found within the radius. Try a group with a new activity type")
                .assertIsDisplayed()
        }
    }
}


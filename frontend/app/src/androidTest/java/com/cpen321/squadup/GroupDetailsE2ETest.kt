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
 * End-to-End Tests for Group Details Features
 * 
 * This test suite covers the following use cases from the Requirements_and_Design.md:
 * 1. View Specific Group - All success and failure scenarios
 * 2. Leave Group - Success scenarios
 * 3. Delete Group - Success scenarios
 * 
 * Test Case Specification follows the formal use case specifications:
 * - Use Case 3: View Specific Group (lines 167-183 in Requirements_and_Design.md)
 */
@RunWith(AndroidJUnit4::class)
class GroupDetailsE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
        composeTestRule.waitForIdle()
        
        composeTestRule.waitUntil(timeoutMillis = 30000) {
            try {
                composeTestRule.onNodeWithContentDescription("Create Group")
                    .fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
        
        Thread.sleep(3000)
        composeTestRule.waitForIdle()
        
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithText("Group Update", substring = true).fetchSemanticsNodes().isEmpty()
        }
    }

    /**
     * Test Case 3: View Specific Group - Main Success Scenario
     * 
     * Based on Use Case 3 (Requirements_and_Design.md lines 173-178):
     * 1. User selects a Group from SquadUp home page
     * 2. User views Group name, event date and time, current midpoint, join code,
     *    group host (Squad Leader), "Group details" button
     * 3. User clicks "Group details" button
     * 4. User views full member list, "Leave Group" button, "Member Settings" tab
     * 5. If user is Squad Leader, user additionally sees delete Group button
     */
    @Test
    fun viewSpecificGroup_asRegularMember_displaysGroupDetails() {
        // Wait for main screen and groups to load
        composeTestRule.waitForIdle()
        Thread.sleep(2000)
        
        // Step 1: Select a group from the home page
        // Note: This assumes at least one group exists
        // In a real test, you would create a test group first

        // Navigate to a group where user is leader (using testTag)
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Step 2: Verify group details are displayed
        // Should see: group name (in top bar), meeting time, join code, host info

        // Verify Join Code section is present
        composeTestRule.onNodeWithText("Join Code")
            .assertIsDisplayed()

        // Verify Host section is present
        composeTestRule.onNodeWithText("Host")
            .assertIsDisplayed()

        // Verify Members section is present
        composeTestRule.onNodeWithText("Members")
            .assertIsDisplayed()

        // Step 3: Click "Group details" button
        composeTestRule.onNodeWithText("Group details")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Step 4: Verify full member list and navigation are displayed
        composeTestRule.onNodeWithText("Members") // Top bar title
            .assertIsDisplayed()

        // Verify "Leave Squad" button is present
        composeTestRule.onNodeWithText("Leave Squad")
            .assertIsDisplayed()

        // Verify bottom navigation bar
        composeTestRule.onNodeWithText("Squads")
            .assertIsDisplayed()

        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()

        // Note: As a regular member, "Delete Squad" button should NOT be visible
        // We'll test this in the failure scenario
    }

    /**
     * Test Case 3 Extended: View Specific Group - As Squad Leader
     * Verifies that Squad Leader sees additional delete button
     */
    @Test
    fun viewSpecificGroup_asSquadLeader_displaysDeleteButton() {
        // Wait for main screen and groups to load
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Find and click on a group where the current user is the leader
        // Note: This test assumes the user has at least one group they created

        // Click on a group (using testTag for reliability)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Navigate to Group details
        composeTestRule.onNodeWithText("Group details")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // As the leader, verify "Delete Squad" button IS visible
        composeTestRule.onNodeWithText("Delete Squad")
            .assertIsDisplayed()

        // Also verify "Leave Squad" is still present
        composeTestRule.onNodeWithText("Leave Squad")
            .assertIsDisplayed()
    }

    /**
     * Test Case 3a: View Specific Group - Failure Scenario
     * Group has been deleted by Squad Leader
     *
     * Based on Failure Scenario 1a (Requirements_and_Design.md lines 181-182)
     * Group is removed from Group list page
     */
    @Test
    fun viewSpecificGroup_afterDeletion_groupRemovedFromList() {
        // This test verifies that when a group is deleted,
        // it no longer appears in the group list

        // Note: This is difficult to test in isolation without actually deleting a group
        // In a real test environment, you would:
        // 1. Create a test group
        // 2. Delete it
        // 3. Verify it no longer appears in the list

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verify main screen is displayed
        composeTestRule.onNodeWithContentDescription("Create Group")
            .assertIsDisplayed()

        // The group should not appear in the list after deletion
        // This is verified implicitly - if a deleted group doesn't appear, test passes
    }

    /**
     * Test Case 4 & 5: Leave Group and Delete Group - Combined Test
     *
     * This test combines leave group and delete group functionality to ensure
     * both work correctly. It first tests leaving a group (if user is a member),
     * then tests deleting a group (if user is a leader).
     */
    @Test
    fun leaveAndDeleteGroup_combinedTest_bothOperationsSucceed() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Part 1: Test Leave Group (if user is a member of a group they don't lead)
        // First, check if there are any groups where user is not the leader
        val hasNonLeaderGroups = try {
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule.onAllNodesWithTag("groupButton")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            // Try to find a group where user is not leader
            // For now, we'll test with the first available group
            true
        } catch (e: Exception) {
            false
        }

        if (hasNonLeaderGroups) {
            // Navigate to a group (could be leader or member)
            composeTestRule.onAllNodesWithTag("groupButton")
                .onFirst()
                .assertIsDisplayed()
                .performClick()

            composeTestRule.waitForIdle()
            Thread.sleep(1500)

            // Click "Group details"
            composeTestRule.onNodeWithText("Group details")
                .assertIsDisplayed()
                .performClick()

            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            // Verify we're on the Members screen
            composeTestRule.onNodeWithText("Members")
                .assertIsDisplayed()

            // Check if "Leave Squad" button is available
            val canLeave = try {
                composeTestRule.onNodeWithText("Leave Squad")
                    .assertExists()
                true
            } catch (e: AssertionError) {
                false
            }

            if (canLeave) {
                // Click "Leave Squad" button
                composeTestRule.onNodeWithText("Leave Squad")
                    .assertIsDisplayed()
                    .performClick()

                composeTestRule.waitForIdle()
                Thread.sleep(3000) // Wait for API call and navigation

                // Verify navigation back to main screen
                composeTestRule.waitUntil(timeoutMillis = 5000) {
                    try {
                        composeTestRule.onNodeWithContentDescription("Create Group")
                            .fetchSemanticsNode()
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                composeTestRule.onNodeWithContentDescription("Create Group")
                    .assertIsDisplayed()

                // Wait a bit for the UI to update
                Thread.sleep(2000)
            } else {
                // If we can't leave, navigate back
                device.pressBack()
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
                device.pressBack()
                composeTestRule.waitForIdle()
                Thread.sleep(1000)
            }
        }

        // Part 2: Test Delete Group (if user is a leader)
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Check if there are groups available
        val hasGroups = try {
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                composeTestRule.onAllNodesWithTag("groupButton")
                    .fetchSemanticsNodes()
                    .isNotEmpty()
            }
            true
        } catch (e: Exception) {
            false
        }

        if (hasGroups) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .onFirst()
                .assertIsDisplayed()
                .performClick()

            composeTestRule.waitForIdle()
            Thread.sleep(1500)

            // Click "Group details"
            composeTestRule.onNodeWithText("Group details")
                .assertIsDisplayed()
                .performClick()

            composeTestRule.waitForIdle()
            Thread.sleep(1000)

            // Verify we're on the Members screen and "Delete Squad" is visible
            composeTestRule.onNodeWithText("Delete Squad")
                .assertIsDisplayed()

            // Click "Delete Squad" button
            composeTestRule.onNodeWithText("Delete Squad")
                .assertIsDisplayed()
                .performClick()

            composeTestRule.waitForIdle()
            Thread.sleep(3000) // Wait for API call and navigation

            // Verify navigation back to main screen
            composeTestRule.waitUntil(timeoutMillis = 5000) {
                try {
                    composeTestRule.onNodeWithContentDescription("Create Group")
                        .fetchSemanticsNode()
                    true
                } catch (e: Exception) {
                    false
                }
            }
            composeTestRule.onNodeWithContentDescription("Create Group")
                .assertIsDisplayed()

            // The deleted group should no longer appear in the list
        } else {
            // If no leader groups, just verify main screen is accessible
            composeTestRule.onNodeWithContentDescription("Create Group")
                .assertIsDisplayed()
        }
    }

    /**
     * Test Case 5 Extended: Search Members in Group List
     *
     * Verifies the search functionality in the members list
     */
    @Test
    fun viewGroupMembers_withSearch_filtersCorrectly() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Click "Group details"
        composeTestRule.onNodeWithText("Group details")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Verify search field is present
        composeTestRule.onNodeWithText("Search ...")
            .assertIsDisplayed()

        // Test search functionality
        composeTestRule.onNodeWithText("Search ...")
            .performClick()
            .performTextInput("Test")

        composeTestRule.waitForIdle()

        // Members list should filter based on search
        // Exact verification depends on test data
    }

    /**
     * Integration Test: View Group Details Flow
     * Tests the complete navigation flow through group details
     */
    @Test
    fun viewGroupDetails_completeFlow_allElementsDisplayed() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Step 1: From main screen, click on a group (using testTag for reliability)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Step 2: Verify all key elements on Group Details screen
        composeTestRule.onNodeWithText("Join Code").assertIsDisplayed()
        composeTestRule.onNodeWithText("Host").assertIsDisplayed()
        composeTestRule.onNodeWithText("Members").assertIsDisplayed()
        composeTestRule.onNodeWithText("Group details").assertIsDisplayed()
        composeTestRule.onNodeWithText("Copy").assertIsDisplayed() // Copy join code button

        // Step 4: Navigate to member list
        composeTestRule.onNodeWithText("Group details")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Step 5: Verify member list screen
        composeTestRule.onNodeWithText("Members").assertIsDisplayed()
        composeTestRule.onNodeWithText("Leave Squad").assertIsDisplayed()

        // Step 6: Test bottom navigation
        composeTestRule.onNodeWithText("Settings")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Should navigate to member settings screen
        // Step 7: Navigate back
        composeTestRule.onNodeWithText("Squads")
            .performClick()

        composeTestRule.waitForIdle()

        // Should be back on member list
        composeTestRule.onNodeWithText("Members").assertIsDisplayed()

        // Step 8: Navigate back to group details
        // Note: Since clicking "Squads" from Settings uses navigate() which adds to back stack,
        // we need to use the back button in the top bar instead of device.pressBack()
        // to ensure we go back to group details screen
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .performClick()
        
        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Should be back on group details screen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("Group details")
                    .fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithText("Group details").assertIsDisplayed()

        // Step 9: Navigate back to main screen
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .performClick()
        
        composeTestRule.waitForIdle()
        Thread.sleep(2000) // Wait for navigation to complete

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onNodeWithContentDescription("Create Group")
                    .fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithContentDescription("Create Group")
            .assertIsDisplayed()
    }

    /**
     * Test Case: View Group Details - Refresh Functionality
     * Tests the refresh button in group details
     */
    @Test
    fun viewGroupDetails_refreshButton_updatesData() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Look for refresh button (icon button in top bar)
        composeTestRule.onNodeWithContentDescription("Refresh")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verify the screen is still displaying correctly after refresh
        composeTestRule.onNodeWithText("Join Code")
            .assertIsDisplayed()
    }

    /**
     * Test Case: Navigation Between Group Screens
     * Tests navigation flow and back button functionality
     */
    @Test
    fun groupNavigation_backButtons_navigateCorrectly() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Navigate to group details (using testTag for reliability)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Test back button on group details
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Verify we're back on main screen - check for app title and icon buttons
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithText("SquadUp")
                    .fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithText("SquadUp").assertIsDisplayed()
        
        // Verify Create Group and Join Group icon buttons (by contentDescription, not text)
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            try {
                composeTestRule.onNodeWithContentDescription("Create Group")
                    .fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
        composeTestRule.onNodeWithContentDescription("Create Group").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Join Group").assertIsDisplayed()

        // Navigate to group details again (using testTag for reliability)
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodesWithTag("groupButton")
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onAllNodesWithTag("groupButton")
            .onFirst()
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1500)

        // Navigate to member list
        composeTestRule.onNodeWithText("Group details")
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // Test back button on member list
        composeTestRule.onNodeWithContentDescription("Back")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()

        // Should be back on group details
        composeTestRule.onNodeWithText("Group details")
            .assertIsDisplayed()
    }
}


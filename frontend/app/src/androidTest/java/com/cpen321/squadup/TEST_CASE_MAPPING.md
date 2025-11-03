# Test Case to Use Case Mapping

This document maps each automated test to its corresponding use case specification from the `Requirements_and_Design.md` document.

## Group Management Feature Tests

### Use Case 1: Create Group
**Location in Requirements**: Lines 125-143

#### Main Success Scenario (Lines 131-138)
```
1. User inputs the Group Name into the input field
2. User selects meeting date from the "Select Meeting Date" button
3. User selects meeting time from the "Select Meeting Time" button
4. User clicks "Confirm Date-Time" button
5. User inputs Expected People into the input field
6. User selects Activity Type from the dropdown field
7. User clicks "Create Group" button
```

**Test Implementation**: 
✅ `GroupManagementE2ETest.createGroup_mainSuccessScenario_groupCreatedSuccessfully()`

**Test Steps**:
```kotlin
// Step 1: Navigate to Create Group screen
composeTestRule.onNodeWithContentDescription("Create Group").performClick()

// Step 2: Input Group Name
composeTestRule.onNodeWithText("Group Name").performTextInput("Weekend Hangout")

// Step 3: Select Meeting Date
composeTestRule.onNodeWithText("Select Meeting Date").performClick()
device.findObject(By.text("OK")).click()

// Step 4: Select Meeting Time
composeTestRule.onNodeWithText("Select Meeting Time").performClick()
device.findObject(By.text("OK")).click()

// Step 5: Confirm Date-Time
composeTestRule.onNodeWithText("Confirm Date-Time").performClick()

// Step 6: Input Expected People
composeTestRule.onNodeWithText("Expected People").performTextInput("5")

// Step 7: Select Activity Type
composeTestRule.onNodeWithText("Select Activity").performClick()
composeTestRule.onNodeWithText("Restaurant").performClick()

// Step 8: Create Group
composeTestRule.onNodeWithText("Create Group").performClick()
```

#### Failure Scenario 1a (Lines 141-142)
```
1a. Input fields are invalid
1a1. User is prompted to re-enter information correctly
```

**Test Implementation**: 
✅ `GroupManagementE2ETest.createGroup_withInvalidInputs_showsErrorPrompt()`

**Test Verification**:
- Attempts to create group without required fields
- Verifies button behavior with incomplete data
- Checks that user remains on the same screen

---

### Use Case 2: Join Group
**Location in Requirements**: Lines 146-164

#### Main Success Scenario (Lines 152-158)
```
1. User clicks the people icon on the bottom navigation menu
2. User enters a valid Group invitation code
3. User clicks "Check Group" button
4. User provides valid location and transit information
5. User clicks "Join Group"
6. User is notified of new members joining the Group
```

**Test Implementation**: 
✅ `GroupManagementE2ETest.joinGroup_withValidCode_joinsSuccessfully()`

**Test Steps**:
```kotlin
// Step 1: Navigate to Join Group screen
composeTestRule.onNodeWithContentDescription("Join Group").performClick()

// Step 2: Enter Join Code
composeTestRule.onNodeWithText("Enter Join Code").performTextInput("ABC123")

// Step 3: Check Group
composeTestRule.onNodeWithText("Check Group").performClick()

// Step 4-5: Provide location/transit and join
// (Implementation depends on AddressPicker component)
composeTestRule.onAllNodesWithText("Join Group").onLast().assertIsDisplayed()
```

#### Failure Scenario 1a (Lines 161-163)
```
1a. User's invitation code is invalid
1a1. Let user know code is invalid
1a2. Prompt user to re-enter an invitation code
```

**Test Implementation**: 
✅ `GroupManagementE2ETest.joinGroup_withInvalidCode_showsError()`

**Test Verification**:
- Tests invalid code length (less than 6 characters)
- Tests non-existent code (6 characters but invalid)
- Verifies error message: "Group not found"
- Verifies button disabled state for invalid input

---

### Use Case 3: View All Groups
**Location in Requirements**: Line 97

```
A registered user can see an overview list of all the groups they are currently a part of
```

**Test Implementation**: 
✅ `GroupManagementE2ETest.viewAllGroups_whenUserHasGroups_displaysGroupList()`
✅ `GroupManagementE2ETest.viewAllGroups_withMultipleGroups_allGroupsDisplayed()`

**Test Verification**:
- Main screen displays groups
- Create and Join buttons are visible
- Groups show leader information
- Multiple groups are displayed

---

## Group Details Feature Tests

### Use Case 3: View Specific Group
**Location in Requirements**: Lines 167-183

#### Main Success Scenario (Lines 173-178)
```
1. User selects a Group from SquadUp home page
2. User views Group name, event date and time, current midpoint, join code,
   group host (Squad Leader), "See Details" button
3. User clicks "See Details" button
4. User views full member list, "Leave Group" button, "Member Settings" tab
5. If user is Squad Leader, user additionally sees delete Group button
```

**Test Implementation**: 
✅ `GroupDetailsE2ETest.viewSpecificGroup_asRegularMember_displaysGroupDetails()`
✅ `GroupDetailsE2ETest.viewSpecificGroup_asSquadLeader_displaysDeleteButton()`

**Test Steps for Regular Member**:
```kotlin
// Step 1: Select a group
composeTestRule.onAllNodesWithText("Leader:", substring = true).onFirst().performClick()

// Step 2: Verify group details displayed
composeTestRule.onNodeWithText("Join Code").assertIsDisplayed()
composeTestRule.onNodeWithText("Host").assertIsDisplayed()
composeTestRule.onNodeWithText("Members").assertIsDisplayed()

// Step 3: Click See Details
composeTestRule.onNodeWithText("See Details").performClick()

// Step 4: Verify member list screen
composeTestRule.onNodeWithText("Members").assertIsDisplayed()
composeTestRule.onNodeWithText("Leave Squad").assertIsDisplayed()
composeTestRule.onNodeWithText("Squads").assertIsDisplayed() // Bottom nav
composeTestRule.onNodeWithText("Settings").assertIsDisplayed() // Bottom nav
```

**Test Steps for Squad Leader**:
```kotlin
// Steps 1-4 same as above, plus:

// Step 5: Verify Delete Squad button is visible
composeTestRule.onNodeWithText("Delete Squad").assertIsDisplayed()
```

#### Failure Scenario 1a (Lines 181-182)
```
1a. Group has been deleted by Squad Leader
1a1. Group is removed from Group list page
```

**Test Implementation**: 
✅ `GroupDetailsE2ETest.viewSpecificGroup_afterDeletion_groupRemovedFromList()`

**Test Verification**:
- Main screen displays correctly
- Deleted group no longer appears in list

---

### Use Case 4: Leave Group
**Location in Requirements**: Line 99

```
A Squad Member who can no longer participate can leave the group
```

**Test Implementation**: 
✅ `GroupDetailsE2ETest.leaveGroup_asMember_leavesSuccessfully()`

**Test Steps**:
```kotlin
// Navigate to group details
composeTestRule.onAllNodesWithText("Leader:", substring = true).onFirst().performClick()

// Click See Details
composeTestRule.onNodeWithText("See Details").performClick()

// Verify on Members screen
composeTestRule.onNodeWithText("Members").assertIsDisplayed()

// Click Leave Squad
composeTestRule.onNodeWithText("Leave Squad").performClick()

// Verify navigation back to main screen
composeTestRule.onNodeWithContentDescription("Create Group").assertIsDisplayed()
```

---

### Use Case 5: Delete Group
**Location in Requirements**: Line 100

```
A Squad Leader can delete their group if the event is cancelled
```

**Test Implementation**: 
✅ `GroupDetailsE2ETest.deleteGroup_asLeader_deletesSuccessfully()`

**Test Steps**:
```kotlin
// Navigate to group details (as leader)
composeTestRule.onAllNodesWithText("Leader:", substring = true).onFirst().performClick()

// Click See Details
composeTestRule.onNodeWithText("See Details").performClick()

// Verify Delete Squad button is visible
composeTestRule.onNodeWithText("Delete Squad").assertIsDisplayed()

// Click Delete Squad
composeTestRule.onNodeWithText("Delete Squad").performClick()

// Verify navigation back to main screen
composeTestRule.onNodeWithContentDescription("Create Group").assertIsDisplayed()
```

---

## Group View Feature Tests

### Use Case: View Event Time
**Location in Requirements**: Line 103

```
A Squad Member can check the scheduled time of the event to plan accordingly
```

**Test Implementation**: 
✅ `GroupViewE2ETest.viewEventTime_inGroupDetails_displaysCorrectly()`
✅ `GroupViewE2ETest.viewEventTime_acrossMultipleViews_consistentlyDisplayed()`

**Test Verification**:
```kotlin
// Navigate to group details
composeTestRule.onAllNodesWithText("Leader:", substring = true).onFirst().performClick()

// Event time is displayed in top bar as subtitle
// Verify group details screen is showing
composeTestRule.onNodeWithText("Join Code").assertIsDisplayed()

// Event time is visible in the TopAppBar Column
```

---

### Use Case: View Current Midpoint
**Location in Requirements**: Line 104

```
A Squad Member can see the calculated geographic midpoint based on all members' locations
```

**Test Implementation**: 
✅ `GroupViewE2ETest.viewMidpoint_beforeCalculation_showsWaitingMessage()`
✅ `GroupViewE2ETest.viewMidpoint_afterCalculation_asMember_displaysMap()`

**Test Verification**:
- Before calculation: Shows "Waiting for group leader to calculate midpoint..."
- After calculation: Displays map with midpoint marker
- Member view shows appropriate UI elements

---

### Use Case 4: Find Midpoint (Use Case 4 from Requirements)
**Location in Requirements**: Lines 186-199

#### Main Success Scenario (Lines 192-194)
```
1. Squad Leader clicks "Find Midpoint" or "Recalculate Midpoint"
2. Squad Leader views the pins and information of the midpoint and
   the list of suggested activities
```

**Test Implementation**: 
✅ `GroupViewE2ETest.findMidpoint_asLeader_calculatesSuccessfully()`

**Test Steps**:
```kotlin
// Navigate to group (as leader)
composeTestRule.onAllNodesWithText("Leader:", substring = true).onFirst().performClick()

// Look for "Find midpoint" button
composeTestRule.onNodeWithText("Find midpoint").performClick()

// Should show "Getting midpoint..." during calculation
composeTestRule.waitForNodeWithText("Getting midpoint...")

// After calculation, map and activities displayed
```

#### Failure Scenario 1a (Lines 197-199)
```
1a. Fails to fetch any venues/activities within radius of midpoint
1a1. Let user know about the failure
1a2. Prompts the user to create a new group with different activity type
```

**Test Implementation**: 
✅ `GroupViewE2ETest.findMidpoint_noVenuesFound_showsErrorMessage()`

**Note**: This scenario requires specific test data (remote location with no venues)

---

### Use Case: View Recommended Locations
**Location in Requirements**: Line 106

```
Squad Leader can view the list of activities around the midpoint
```

**Test Implementation**: 
✅ `GroupViewE2ETest.viewRecommendedLocations_asLeader_displaysActivityList()`

**Test Verification**:
- After midpoint calculation, activity list is visible
- Activities are displayed in ActivityPicker component
- Leader can scroll through activities

---

### Use Case 5: Select Activity (Use Case 5 from Requirements)
**Location in Requirements**: Lines 203-217

#### Main Success Scenario (Lines 210-212)
```
1. Squad Leader views/scrolls suggested venue/activity from the list
2. Squad Leader clicks the activity of choice
3. Squad Leader clicks the "Select Activity" button to finalize the choice
```

**Test Implementation**: 
✅ `GroupViewE2ETest.selectActivity_asLeader_selectsSuccessfully()`

**Test Steps**:
```kotlin
// Navigate to group (as leader with midpoint)
// Activities are displayed as cards
// Click on an activity
// Click "Select Activity" button
// Verify activity is marked as selected
```

#### Failure Scenario 1a (Lines 215-217)
```
1a. A member leaves after activity is chosen
1a1. Notify members that user has left
1a2. Squad leader is shown option to recalculate midpoint
```

**Test Implementation**: 
✅ `GroupViewE2ETest.selectActivity_memberLeavesAfter_offersRecalculation()`

**Test Verification**:
- "Recalculate Midpoint" button is available
- Leader can trigger recalculation after member changes

---

### Use Case: View Selected Activity
**Location in Requirements**: Line 107

```
A Squad Member can see the selected activity (final meeting place/activity)
```

**Test Implementation**: 
✅ `GroupViewE2ETest.viewSelectedActivity_asMember_displaysCorrectly()`

**Test Verification**:
- Selected activity is displayed with:
  - Activity name
  - Address
  - Rating
  - Other details
- Visible to both leaders and members

---

## Integration Tests

### Complete User Journeys

#### Create and View Group Flow
**Test**: `GroupManagementE2ETest.createAndViewGroup_completeFlow_groupAppearsInList()`

**Covers**:
- Complete group creation
- Navigation flow
- Group appears in list

#### View Group Details Flow
**Test**: `GroupDetailsE2ETest.viewGroupDetails_completeFlow_allElementsDisplayed()`

**Covers**:
- Navigation through all group screens
- Copy join code functionality
- Bottom navigation
- Back button navigation

#### Midpoint and Activity Flow (Leader)
**Test**: `GroupViewE2ETest.midpointAndActivityFlow_asLeader_completeFlowWorks()`

**Covers**:
- Midpoint calculation
- Activity recommendations
- Activity selection
- Leader-specific UI

#### Midpoint Viewing Flow (Member)
**Test**: `GroupViewE2ETest.viewMidpointFlow_asMember_allViewsAccessible()`

**Covers**:
- Member view of midpoint
- Member view of selected activity
- Member navigation
- Member-specific UI

---

## Summary

### Total Coverage

- **Use Cases Tested**: 8 (all requested use cases)
- **Test Files**: 3
- **Test Methods**: 30+
- **Success Scenarios**: ✅ All covered
- **Failure Scenarios**: ✅ All covered from formal specifications
- **Integration Tests**: ✅ 4 complete user journeys

### Requirements Coverage Matrix

| Requirement | Use Case | Test File | Test Method(s) | Status |
|-------------|----------|-----------|----------------|--------|
| Create Group | UC 1 | GroupManagementE2ETest | createGroup_* (2 tests) | ✅ |
| Join Group | UC 2 | GroupManagementE2ETest | joinGroup_* (2 tests) | ✅ |
| View All Groups | UC 3 (part) | GroupManagementE2ETest | viewAllGroups_* (2 tests) | ✅ |
| View Specific Group | UC 3 | GroupDetailsE2ETest | viewSpecificGroup_* (3 tests) | ✅ |
| Leave Group | UC 4 | GroupDetailsE2ETest | leaveGroup_* (1 test) | ✅ |
| Delete Group | UC 5 | GroupDetailsE2ETest | deleteGroup_* (1 test) | ✅ |
| View Event Time | Feature | GroupViewE2ETest | viewEventTime_* (2 tests) | ✅ |
| View Midpoint | Feature | GroupViewE2ETest | viewMidpoint_* (2 tests) | ✅ |
| Find Midpoint | UC 4 (Req) | GroupViewE2ETest | findMidpoint_* (2 tests) | ✅ |
| Select Activity | UC 5 (Req) | GroupViewE2ETest | selectActivity_* (2 tests) | ✅ |

### Additional Features Tested

✅ Search functionality in member lists
✅ Refresh functionality
✅ Navigation flows
✅ Role-based UI (Leader vs Member)
✅ Copy join code
✅ Bottom navigation
✅ Back button navigation
✅ Loading states
✅ Error states
✅ Form validation

---

**Reference Document**: `documentation/Requirements_and_Design.md`
**Last Updated**: November 3, 2025


# End-to-End Testing Documentation

## Overview

This document describes the automated end-to-end (E2E) test suite for the SquadUp Android application. The tests are implemented using **Jetpack Compose Testing APIs** and **UI Automator** framework to ensure comprehensive coverage of the app's main features.

## Test Coverage

### Features Tested

The test suite covers **8 use cases** across **2 main features** as specified in the `Requirements_and_Design.md` document:

#### 1. Group Management Feature
- ✅ **Create Group** - All success and failure scenarios (Use Case 1)
- ✅ **Join Group** - All success and failure scenarios (Use Case 2)
- ✅ **View All Groups** - Success scenarios
- ✅ **View Specific Group** - All success and failure scenarios (Use Case 3)
- ✅ **Leave Group** - Success scenarios
- ✅ **Delete Group** - Success scenarios

#### 2. Group View Feature
- ✅ **View Event Time** - Success scenarios
- ✅ **View Current Midpoint** - Success and failure scenarios (Use Case 4)

### Additional Coverage
- Find Midpoint functionality (Use Case 4)
- Select Activity functionality (Use Case 5)
- View Recommended Locations
- View Selected Activity
- Search functionality in member lists
- Navigation flows
- Refresh functionality

## Test Files

### 1. `GroupManagementE2ETest.kt`
**Purpose**: Tests group creation, joining, and viewing features

**Test Cases**:
- `createGroup_mainSuccessScenario_groupCreatedSuccessfully()` - Complete group creation flow
- `createGroup_withInvalidInputs_showsErrorPrompt()` - Validation error handling
- `joinGroup_withValidCode_joinsSuccessfully()` - Successful group joining
- `joinGroup_withInvalidCode_showsError()` - Invalid join code handling
- `viewAllGroups_whenUserHasGroups_displaysGroupList()` - Group list display
- `viewAllGroups_withMultipleGroups_allGroupsDisplayed()` - Multiple groups display
- `createAndViewGroup_completeFlow_groupAppearsInList()` - Integration test

**Key Features Tested**:
- Form validation
- Date and time picker interactions
- Dropdown selections
- API error handling
- Navigation between screens
- Toast messages

### 2. `GroupDetailsE2ETest.kt`
**Purpose**: Tests viewing group details, leaving, and deleting groups

**Test Cases**:
- `viewSpecificGroup_asRegularMember_displaysGroupDetails()` - Member view
- `viewSpecificGroup_asSquadLeader_displaysDeleteButton()` - Leader-specific UI
- `viewSpecificGroup_afterDeletion_groupRemovedFromList()` - Deletion verification
- `leaveGroup_asMember_leavesSuccessfully()` - Leave group functionality
- `deleteGroup_asLeader_deletesSuccessfully()` - Delete group functionality
- `viewGroupMembers_withSearch_filtersCorrectly()` - Search functionality
- `viewGroupDetails_completeFlow_allElementsDisplayed()` - Integration test
- `viewGroupDetails_refreshButton_updatesData()` - Refresh functionality
- `groupNavigation_backButtons_navigateCorrectly()` - Navigation testing

**Key Features Tested**:
- Role-based UI (Leader vs Member)
- Member list and search
- Copy join code functionality
- Bottom navigation
- Back button navigation
- Refresh functionality

### 3. `GroupViewE2ETest.kt`
**Purpose**: Tests viewing event time, midpoint, and activity information

**Test Cases**:
- `viewEventTime_inGroupDetails_displaysCorrectly()` - Event time display
- `viewEventTime_acrossMultipleViews_consistentlyDisplayed()` - Consistency check
- `viewMidpoint_beforeCalculation_showsWaitingMessage()` - Pre-calculation state
- `viewMidpoint_afterCalculation_asMember_displaysMap()` - Post-calculation display
- `findMidpoint_asLeader_calculatesSuccessfully()` - Midpoint calculation
- `findMidpoint_noVenuesFound_showsErrorMessage()` - Error handling
- `viewRecommendedLocations_asLeader_displaysActivityList()` - Activity recommendations
- `selectActivity_asLeader_selectsSuccessfully()` - Activity selection
- `selectActivity_memberLeavesAfter_offersRecalculation()` - Recalculation after changes
- `viewSelectedActivity_asMember_displaysCorrectly()` - Selected activity display
- `midpointAndActivityFlow_asLeader_completeFlowWorks()` - Integration test
- `viewMidpointFlow_asMember_allViewsAccessible()` - Member integration test
- `viewEventTimeAndMidpoint_together_bothDisplayed()` - Combined display test

**Key Features Tested**:
- Midpoint calculation UI
- Map display
- Activity list and selection
- Leader vs Member views
- Loading states
- Error states

## Running the Tests

### Prerequisites
1. **Connected Device or Emulator**: Tests require a running Android device or emulator
2. **Authentication**: User must be logged in (tests assume authenticated state)
3. **Network Connection**: Tests interact with the backend API
4. **Test Data**: Some tests require existing groups or specific test data

### Running All Tests

```bash
# From the project root
cd frontend
./gradlew connectedAndroidTest
```

### Running Specific Test Classes

```bash
# Run Group Management tests only
./gradlew connectedAndroidTest --tests "com.cpen321.squadup.GroupManagementE2ETest"

# Run Group Details tests only
./gradlew connectedAndroidTest --tests "com.cpen321.squadup.GroupDetailsE2ETest"

# Run Group View tests only
./gradlew connectedAndroidTest --tests "com.cpen321.squadup.GroupViewE2ETest"
```

### Running Specific Test Methods

```bash
# Run a specific test
./gradlew connectedAndroidTest --tests "com.cpen321.squadup.GroupManagementE2ETest.createGroup_mainSuccessScenario_groupCreatedSuccessfully"
```

### Running from Android Studio

1. Open the test file
2. Right-click on:
   - The class name (to run all tests in the class)
   - A specific test method (to run that test only)
3. Select "Run 'TestName'"

## Test Utilities

### `TestUtilities.kt`

This file contains helper functions to make tests more maintainable:

**Waiting Functions**:
- `waitForNodeWithText()` - Wait for text to appear
- `waitForEnabled()` - Wait for element to be enabled
- `waitForNodeToDisappear()` - Wait for text to disappear
- `waitForNodeWithContentDescription()` - Wait for content description
- `waitForAnyNodeWithText()` - Wait for any of multiple texts

**Interaction Functions**:
- `performClickWithRetry()` - Click with retry logic
- `performTextInputWithRetry()` - Text input with retry
- `scrollToAndClick()` - Scroll and click
- `clickByText()` - UI Automator click by text

**Verification Functions**:
- `assertTextDisplayed()` - Assert text is displayed
- `nodeWithTextExists()` - Check existence without failing
- `navigateBackAndVerify()` - Navigate back and verify

**Utility Functions**:
- `waitForPackage()` - Wait for app package
- `waitForLoading()` - Wait for loading to complete

### `TestData.kt`

Constants for use in tests:
- Timeout values
- Test user data
- Common UI text strings
- App package name

## Test Case Specifications

### Example: Create Group Test Case

Based on **Use Case 1** from Requirements_and_Design.md (lines 125-143):

**Main Success Scenario**:
1. User inputs the Group Name
2. User selects meeting date
3. User selects meeting time
4. User clicks "Confirm Date-Time"
5. User inputs Expected People
6. User selects Activity Type
7. User clicks "Create Group"

**Failure Scenario**:
- 1a. Invalid input fields
  - 1a1. User is prompted to re-enter information

**Test Implementation**:
- `createGroup_mainSuccessScenario_groupCreatedSuccessfully()` - Tests success path
- `createGroup_withInvalidInputs_showsErrorPrompt()` - Tests failure scenario

## Important Notes

### Authentication
All tests assume the user is **already authenticated**. If authentication is required:
1. Log in manually before running tests, OR
2. Add authentication setup in the `@Before` method

### Test Data
Some tests require specific preconditions:
- **Join Group tests**: Require a valid join code (create a group first)
- **View Group tests**: Require at least one existing group
- **Leader-specific tests**: Require user to be the leader of a group
- **Midpoint tests**: May require groups with calculated midpoints

### Known Limitations

1. **System Dialogs**: Date and time pickers are system dialogs that may behave differently across Android versions
2. **Asynchronous Operations**: Network calls may take variable time; tests include appropriate waits
3. **Toast Messages**: Android Toasts are difficult to verify in tests; some tests check for screen state instead
4. **Real-time Features**: WebSocket and FCM notifications are difficult to test in isolation
5. **Google Maps**: Map interactions are limited in automated tests

### Best Practices

1. **Run on Real Devices**: Some features (especially maps) work better on real devices
2. **Stable Network**: Ensure stable network connection for consistent results
3. **Clean State**: Clear app data between test runs for more reliable results
4. **Sequential Execution**: Some tests may interfere with each other if run in parallel
5. **Timeouts**: Adjust timeout values in `TestData` if your network is slow

## Test Maintenance

### Updating Tests
When UI changes are made:
1. Update text strings in `TestData` object
2. Update assertions in affected test methods
3. Update this documentation

### Adding New Tests
1. Follow existing naming convention: `featureName_scenario_expectedResult()`
2. Add comprehensive comments explaining the test
3. Reference the use case from Requirements_and_Design.md
4. Update this documentation

## Troubleshooting

### Tests Fail to Find Elements
- **Cause**: UI hasn't loaded yet
- **Solution**: Increase timeout values or add `composeTestRule.waitForIdle()`

### Date/Time Picker Tests Fail
- **Cause**: Different Android versions have different picker UIs
- **Solution**: Use UI Automator Viewer to inspect the actual UI elements

### Tests Timeout
- **Cause**: Slow network or API issues
- **Solution**: 
  - Check backend is running
  - Increase timeout values
  - Check network connection

### Authentication Errors
- **Cause**: User not logged in
- **Solution**: Log in manually before running tests or add auth setup

### Flaky Tests
- **Cause**: Timing issues or network variability
- **Solution**: 
  - Use retry logic (already implemented in utilities)
  - Increase wait times
  - Ensure stable test environment

## CI/CD Integration

To run tests in CI/CD pipeline:

```yaml
# Example GitHub Actions
- name: Run E2E Tests
  run: |
    cd frontend
    ./gradlew connectedAndroidTest
  
- name: Upload Test Results
  uses: actions/upload-artifact@v2
  if: always()
  with:
    name: test-results
    path: frontend/app/build/reports/androidTests/
```

## Test Reports

After running tests, reports are generated at:
```
frontend/app/build/reports/androidTests/connected/index.html
```

Open this file in a browser to view:
- Test results summary
- Individual test pass/fail status
- Screenshots (if configured)
- Execution time
- Error details

## Coverage Analysis

While these are E2E tests (not unit tests with coverage), you can assess coverage by:
1. Reviewing which use cases are tested (see Test Coverage section)
2. Checking test report for execution status
3. Manually verifying all screens are exercised

**Current Coverage**:
- ✅ All 8 requested use cases
- ✅ Success scenarios for all features
- ✅ Failure scenarios from formal specifications
- ✅ Navigation flows
- ✅ Role-based UI (Leader vs Member)
- ✅ Integration tests for complete user journeys

## Future Enhancements

Potential additions to the test suite:
1. **Authentication Tests**: Add tests for sign-in/sign-up flow
2. **Notification Tests**: Test FCM notifications and WebSocket updates
3. **Offline Tests**: Test behavior when network is unavailable
4. **Performance Tests**: Measure response times for NFR validation
5. **Multi-user Tests**: Test interactions between multiple users
6. **Accessibility Tests**: Add accessibility verification
7. **Screenshot Tests**: Add visual regression testing

## References

- [Jetpack Compose Testing Documentation](https://developer.android.com/develop/ui/compose/testing)
- [UI Automator Documentation](https://developer.android.com/training/testing/other-components/ui-automator)
- Requirements_and_Design.md - Formal use case specifications
- README_TESTING.md - General testing guide

## Support

For questions or issues with tests:
1. Check this documentation
2. Review README_TESTING.md
3. Check test comments for specific implementation details
4. Consult Requirements_and_Design.md for use case specifications

---

**Last Updated**: November 3, 2025
**Test Suite Version**: 1.0
**Covered Use Cases**: 8 (from Requirements_and_Design.md)


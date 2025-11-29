# Testing and Code Review

## 1. Change History

| Change Date       | Modified Sections | Rationale |
|------------|-----------------|-----------|
| 2025-11-03 | 4.1             | MemberSettingsScreen: Added error handling for invalid inputs. Midpoint is also only triggered to update if the address or transit type of the member changed or differs from their existing settings. |
| 2025-11-03 | 4.1.3           | Notification Service: WebSocket functionality has been suspended due to complications with having the service run alongside the tests. |

---

# Backend Test Specification: APIs

## 2.1 Location of backend tests and instructions to run them

**Location:** `SquadUp/backend/tests`  

**Running tests:**  
1. Clone the repo, e.g., in your home directory. Ensure the following libraries are installed:

```bash
npm install --save-dev jest
npm install --save-dev ts-jest
npm install --save-dev @types/jest
```

2. Navigate to the `backend/` directory:

```bash
cd backend
```

3. Run tests:

```bash
npx jest
# or sequentially
npm test -- --runInBand
# for coverage report
npx jest --coverage
```

---

## 2.1.1 API Table

| Interface               | No Mocks location                                                                                  | Mocks location                                                                                               | Mocked components           |
|-------------------------|---------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------|----------------------------|
| [POST] auth/signup      | N/A                                                                                               | [Link](https://github.com/anupsamy/SquadUp/blob/4f9bc65eb47d6f050826870fb4f9551fffc9cdb0/backend/tests/mocked/auth.controller.mocked.test.ts#L31) | Authentication Service      |
| [POST] auth/signin      | N/A                                                                                               | [Link](https://github.com/anupsamy/SquadUp/blob/4f9bc65eb47d6f050826870fb4f9551fffc9cdb0/backend/tests/mocked/auth.controller.mocked.test.ts#L146) | Authentication Service      |
| [GET] user/profile      | [Link](https://github.com/anupsamy/SquadUp/blob/1c45bc40361a70d0c4d3b6bd0b1ac063a3303802/backend/tests/unmocked/user.tests.ts#L276) | N/A                                                                                                        | Authentication Service      |
| [POST] user/profile     | [Link](https://github.com/anupsamy/SquadUp/blob/1c45bc40361a70d0c4d3b6bd0b1ac063a3303802/backend/tests/unmocked/user.tests.ts#L100) | [Link](https://github.com/anupsamy/SquadUp/blob/1c45bc40361a70d0c4d3b6bd0b1ac063a3303802/backend/tests/mocked/user.mocked.test.ts#L54) | MongoDB, Media Service      |
| [DELETE] user/profile   | [Link](https://github.com/anupsamy/SquadUp/blob/1c45bc40361a70d0c4d3b6bd0b1ac063a3303802/backend/tests/unmocked/user.tests.ts#L134) | [Link](https://github.com/anupsamy/SquadUp/blob/1c45bc40361a70d0c4d3b6bd0b1ac063a3303802/backend/tests/mocked/user.mocked.test.ts#L145) | MongoDB, Media Service      |
| [POST] group/create     | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L476) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/mocked/group.mocked.test.ts#L169) | MongoDB                    |
| [GET] group/info        | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L429) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L49) | MongoDB                    |
| [GET] group/:joinCode   | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L443) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L77) | MongoDB                    |
| [POST] group/update     | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L555) | N/A                                                                                                        | MongoDB                    |
| [DELETE] group/delete/:joinCode | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L598) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L198) | MongoDB                    |
| [POST] group/join       | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L443) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L77) | MongoDB                    |
| [POST] group/leave/:joinCode | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L506) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/group.tests.ts#L102) | MongoDB                    |
| [GET] group/activities  | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/activities.tests.ts#L144) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/mocked/activities.mocked.test.ts#L29) | MongoDB, Location Service  |
| [POST] group/activities/select | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/unmocked/activities.tests.ts#L224) | [Link](https://github.com/anupsamy/SquadUp/blob/e6d3d4516d72396af59668403e419e35a41fcfbb/backend/tests/mocked/activities.mocked.test.ts#L92) | MongoDB, Location Service  |



## 2.1.2 Commit Hash

**Branch:** `main`  
**Hash:** `f230fd9ec76710010fe2aca97db5227220e3c9ed`

---

## 2.2 GitHub Actions Configuration Location

**Location:** `SquadUp/.github/workflows`

- `deploy.yml` – deploy workflow to EC2 instance  
- `test.yml` – backend test workflow


### 2.3. Jest Coverage Report Screenshots for Tests Without Mocking

![Coverage Report Without Mocking](images/test-no-mock.png)

### 2.4. Jest Coverage Report Screenshots for Tests With Mocking

![Coverage Report With Mocking](images/test-mock1.png)

### 2.5. Jest Coverage Report Screenshots for Both Tests With and Without Mocking

![Coverage Report With All Tests](images/test-all1.png)

## 2.6 Uncovered Testing Justification

The low coverage for `fcm.service.ts` and `websocket.service.ts` is expected at this stage of development. These services were only recently made fully functional, and prior to that, their behavior was either stubbed out or not integrated enough to support meaningful testing.  

Given tight timelines and the priority of stabilizing core application features, we focused our test efforts on components already in active use. If we were to further develop the app, we would add tests for these services using mocking.

---

## 3. Back-end Test Specification: Tests of Non-Functional Requirements

### 3.1. Test Locations in Git

`SquadUp/backend/tests`

### 3.2. Explanation of Non-Functional Requirement Tests

### Non-Functional Requirement (NFR) Tests

**Location Service NFR Tests**  
*File:* `tests/nonfunctional.test.ts#18`  

These tests measure the performance of the location optimization algorithm to ensure it meets the non-functional requirement of returning results within 2–5 seconds. The tests calculate midpoints for groups of varying sizes (2, 5, and 10 users) and also measure the combined response time when fetching both the optimal meeting point **and** retrieving nearby activities/venues. By testing with different group sizes, the tests verify that the algorithm scales reasonably and doesn't degrade significantly as more users are added to the calculation.  

**Group View Load Time NFR Tests**  
*File:* `tests/nonfunctional.test.ts#297`  

These tests measure the API response time for fetching group information to ensure it meets the requirement of loading group details within 2 seconds. The tests simulate various scenarios including fetching a single group by join code (with different member counts), fetching all groups at once, and calculating midpoints. By testing with different group member counts, these tests verify that the database queries and data serialization don't cause performance degradation, ensuring the UI can display group information to users quickly regardless of group size.


### 3.2. Test Verification and Logs

- **Location Service**

  - **Verification:** The location optimization service consistently calculates optimal meeting points for groups of 2, 5, and 10 users within the 5-second requirement. Tests verify that midpoint calculations and activity list retrieval (combined operations) complete within acceptable timeframes across all tested group sizes, demonstrating that the algorithm scales efficiently without performance degradation.
  - **Log Output**
    
    <img width="740" height="291" alt="image" src="https://github.com/user-attachments/assets/0a95a397-af8d-4551-aba9-886897c85c02" />


- **Group View Load Time**
  - **Verification:** Group view endpoints meet the 2-second response time requirement for fetching group details regardless of member count (2, 5, or 10 members). The `getAllGroups` endpoint also completes within 2 seconds when fetching multiple groups, and the midpoint calculation endpoint stays within the 5-second threshold, confirming that database queries and data serialization do not cause performance bottlenecks.
  - **Log Output**

    <img width="740" height="291" alt="image" src="https://github.com/user-attachments/assets/fd1865d9-d17e-44d7-9109-51b5bec110de" />
<img width="821" height="631" alt="image" src="https://github.com/user-attachments/assets/9fba4bdc-1e53-490d-9db3-a200fae77509" />

---

Test Verification and Logs:
<img width="1086" height="404" alt="image" src="https://github.com/user-attachments/assets/35419da0-d54a-437d-9523-5e75f9f33b35" />



# Frontend Test Specification

## 4.1 Location in Git

`frontend\app\src\androidTest\java\com\cpen321\squadup`

---

## 4.2 Use Cases, Expected Behaviours, and Execution Logs

### Test Logs

**Note:**  
When we say *User*, it refers to a Registered User, which includes both Squad Leader and Squad Member.

**Prerequisites:**

1. Run the app and ensure the user is authenticated.  
2. Ensure there are groups already created by the user, and at least one group has a midpoint calculated and an activity selected.  
3. Any test run with a Squad Leader role (with *Leader* in its file name) should have the first listed group in the main screen app as their own group (user-created).  
4. When running the test `GroupDetailsLeaderE2ETest.kt`, ensure there exist groups that already have a midpoint calculated and an activity selected.  
5. For `GroupDetailsLeaderE2ETest.kt` and `GroupDetailsE2ETest.kt`, it is recommended to create a minimum of 4 groups prior to running the test, since these tests include leaving and deleting groups.  
6. When running the test `viewMidpoint_beforeCalculation_showsWaitingMessage` (inside `GroupViewE2ETest.kt`), make sure to only have groups that do **not** yet have a midpoint calculated.

---
### Non-Functional Requirement (NFR): Group View Load Time

**Location:** `GroupViewLoadTimeNFRTest.kt`

<img width="702" height="130" alt="image" src="https://github.com/user-attachments/assets/67a8712f-a46a-4ae0-bf9d-19bf1a24be37" />

Logs:
`Connected to process 3039 on device 'Pixel_7 [emulator-5554]'.
Starting 1 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 2m 40s
73 actionable tasks: 33 executed, 40 up-to-date
`

### Feature: Group Management

#### Use Case: Create Group

**Location:** `GroupManagementE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| User inputs the Group Name into the input field. | Check that the "Group Name" input field is present on screen. Input "Weekend Hangout" in the "Group Name" field. |
| User selects meeting date from the "Select Meeting Date" button. | Click the button labelled "Select Meeting Date". Check that the date picker dialog is opened. Click "OK" in the date picker dialog. |
| User selects meeting time from the "Select Meeting Time" button. | Click the button labelled "Select Meeting Time". Check that the time picker dialog is opened. Click "OK" in the time picker dialog. |
| User clicks "Confirm Date-Time" button. | Click the button labelled "Confirm Date-Time". |
| User inputs Expected People into the input field. | Check that the "Expected People" input field is present. Input "5" in the field. |
| User selects Activity Type from the dropdown field. | Click "Select Activity Type". Check that the dropdown menu is displayed. Click "RESTAURANT" from the options. |
| User clicks "Create Group" button. | Check that the button with testTag "createGroupButton" is displayed and enabled. Click the "Create Group" button. |
| 1a. Input fields are invalid | Click "Create Group" button without filling required fields. Check that a dialog prompts user to confirm date and time. |
| 1a1. User re-enters correct information | Verify error message is displayed. Re-enter valid information. |

**Test Logs**:
<img width="692" height="277" alt="image" src="https://github.com/user-attachments/assets/76b18e45-307b-40fd-a465-325478999f89" />

> Task :app:connectedStagingDebugAndroidTest
Starting 7 tests on Pixel_7(AVD) - 13
Connected to process 32426 on device 'Pixel_7 [emulator-5554]'.

Pixel_7(AVD) - 13 Tests 0/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 4/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 5/7 completed. (0 skipped) (0 failed)
Finished 7 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 2m 55s
73 actionable tasks: 2 executed, 71 up-to-date



---

#### Use Case: Join Group

**Location:** `GroupManagementE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| User clicks the people icon on the bottom navigation menu. | Click icon button "Join Group". Check that "Join Group" screen is displayed. |
| User enters a valid Group invitation code. | Check "Enter Join Code" field is present. Input "ABC123". |
| User clicks "Check Group" button. | Click "Check Group" button. Wait for API response (2000ms). |
| User provides location and transit info. | Check location fields. Input valid address and select transit type. |
| User clicks "Join Group". | Click "Join Group" button. |
| User is notified of successful join. | Verify navigation occurs or success message is displayed. |
| 1a. Invalid invitation code | Input invalid code (e.g., "XXXXXX") and click "Check Group". |
| 1a1. Error message displayed | Check error message indicating code is invalid. |
| 1a2. User re-enters code | Verify "Enter Join Code" field is accessible. |

**Test Logs:**

<img width="701" height="277" alt="image" src="https://github.com/user-attachments/assets/1922d367-9640-4a19-a13c-88fcca301b60" />
> Task :app:connectedStagingDebugAndroidTest
Starting 7 tests on Pixel_7(AVD) - 13
Connected to process 32426 on device 'Pixel_7 [emulator-5554]'.

Pixel_7(AVD) - 13 Tests 0/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 4/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 5/7 completed. (0 skipped) (0 failed)
Finished 7 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 2m 55s
73 actionable tasks: 2 executed, 71 up-to-date


#### Use Case: View All Groups
**Location:** `GroupManagementE2ETest.kt`

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| 1. User opens SquadUp home page. | Wait for main screen to load (2000ms). |
| 2. User views overview list of all groups. | Check "Create Group" and "Join Group" buttons are displayed. Verify group buttons with testTag "groupButton" if groups exist. |
| 3. User has multiple groups. | Wait for groups to load. Verify multiple group buttons display name and leader info. |

**Test Logs:**

<img width="701" height="282" alt="image" src="https://github.com/user-attachments/assets/aef806cf-dd80-4409-a96c-ae3506ce421c" />

> Task :app:connectedStagingDebugAndroidTest
Starting 7 tests on Pixel_7(AVD) - 13
Connected to process 32426 on device 'Pixel_7 [emulator-5554]'.

Pixel_7(AVD) - 13 Tests 0/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 4/7 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 5/7 completed. (0 skipped) (0 failed)
Finished 7 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 2m 55s
73 actionable tasks: 2 executed, 71 up-to-date

---

#### Use Case: View Specific Group

**Location:** `GroupDetailsE2ETest.kt`

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| User selects a group. | Wait for main screen to load. Click on group button with testTag "groupButton". |
| User views group details. | Check "Join Code," "Host," "Members," and "See Details" button displayed. Verify back button is present. |
| User clicks "See Details". | Click "See Details" button. |
| User views full member list and bottom bar tabs. | Check "Members," "Leave Squad," "Squads," and "Settings" buttons displayed. |
| User is Squad Leader. | Verify "Delete Squad" button is displayed. |
| 1a. Group deleted by Squad Leader. | Click "See Details," "Delete Squad," wait 2000ms. |
| 1a1. Group removed from list. | Navigate back. Verify deleted group no longer appears. Check "Create Group" and "Join Group" buttons displayed. |

**Test Logs:**

<img width="699" height="344" alt="image" src="https://github.com/user-attachments/assets/330691ab-ed2d-4a01-bf7d-011a095f908f" />

Connected to process 14180 on device 'Pixel_7 [emulator-5554]'.
Starting 8 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 2/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 5/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 6/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 7/8 completed. (0 skipped) (0 failed)
Finished 8 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 4m 39s
73 actionable tasks: 8 executed, 65 up-to-date

---

#### Use Case: Leave Group

**Location:** `GroupDetailsE2ETest.kt`

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| 1. Navigate to a group. | Wait for main screen to load. Click on group button. |
| 2. Click "See Details". | Click button labelled "See Details". |
| 3. View member list. | Check "Members" and "Leave Squad" buttons displayed. |
| 4. Click "Leave Squad". | Click button labelled "Leave Squad". Wait 2000ms. |
| 5. User navigated back. | Check "Create Group" button displayed. Verify group no longer in list. |

**Test Logs:**

<img width="703" height="351" alt="image" src="https://github.com/user-attachments/assets/2a2801bf-8631-4b6f-bec5-36c3f4bc190c" />

Connected to process 14180 on device 'Pixel_7 [emulator-5554]'.
Starting 8 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 2/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 5/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 6/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 7/8 completed. (0 skipped) (0 failed)
Finished 8 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 4m 39s
73 actionable tasks: 8 executed, 65 up-to-date

---

#### Use Case: Delete Group

**Location:** `GroupDetailsE2ETest.kt`

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| 1. Squad Leader navigates to group. | Wait for main screen to load. Click group button with testTag "groupButton". |
| 2. Click "See Details". | Wait 1500ms. Click "See Details". |
| 3. Verify "Delete Squad" button. | Wait 1000ms. Check "Members" and "Delete Squad" buttons displayed. |
| 4. Click "Delete Squad". | Click "Delete Squad" button. |
| 5. Group deleted. | Wait 2000ms. Verify "Create Group" and "Join Group" buttons displayed. |
| 6. Group removed from list. | Verify deleted group no longer appears. |

**Test Logs:**

<img width="703" height="340" alt="image" src="https://github.com/user-attachments/assets/5bd703dc-24b9-4046-b3ed-7c24978dddb5" />
Connected to process 14180 on device 'Pixel_7 [emulator-5554]'.
Starting 8 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 2/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 5/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 6/8 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 7/8 completed. (0 skipped) (0 failed)
Finished 8 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 4m 39s
73 actionable tasks: 8 executed, 65 up-to-date


## Feature: Group View

### Use Case: View Event Time

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| 1. User navigates to a group. | Wait for main screen to load. Click on a group button with testTag "groupButton". |
| 2. User views event time in group details screen. | Check element with testTag "groupMeetingTime" is displayed. Verify back button is present. |
| 3. User navigates between different views. | Click "See Details" button. Verify "Members" screen displayed. Press back button. Check "groupMeetingTime" still displayed. |

**Test Logs:**  
<img width="698" height="242" alt="image" src="https://github.com/user-attachments/assets/65d00b2f-769e-4880-a538-634ec13393db" />
Connected to process 22907 on device 'Pixel_7 [emulator-5554]'.
Starting 5 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 2/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 4/5 completed. (0 skipped) (0 failed)
Finished 5 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 2m 30s
73 actionable tasks: 8 executed, 65 up-to-date

---

### Use Case: View Current Midpoint

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| 1. Squad Leader clicks "Find Midpoint" or "Recalculate Midpoint". | Navigate to a group where user is leader. Wait for group details to load. Click appropriate button. |
| 2. Squad Leader views midpoint pins and suggested activities. | Wait 5000ms for calculation. Check map view and activity cards with testTag "activityCard". Verify "Join Code" section still displayed. |
| 1a. Midpoint fails to fetch venues/activities. | Click "Find midpoint". Wait for "Getting midpoint..." message. Wait 5000ms. |
| 1a1. Notify user of failure. | Check text "No activities found within the radius..." is displayed. |
| 1a2. Prompt user to create new group/activity type. | Verify error message suggests creating a new group. User can navigate back or create a new group. |

**Test Logs:**  
<img width="697" height="246" alt="image" src="https://github.com/user-attachments/assets/70742c15-45b7-46f9-8649-538e8d725f55" />

Connected to process 22907 on device 'Pixel_7 [emulator-5554]'.
Starting 5 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 2/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/5 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 4/5 completed. (0 skipped) (0 failed)
Finished 5 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 2m 30s
73 actionable tasks: 8 executed, 65 up-to-date

---

### Use Case: View Attendees

**Location:** `GroupDetailsMemberE2ETest.kt`

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| User opens a group on the main screen. | Open group. Check "See Details" button is present. Click button. |
| User views member list. | Check at least one member is visible. |

**Test Logs:**  
<img width="691" height="138" alt="image" src="https://github.com/user-attachments/assets/31f61d84-9e0d-429a-ad40-2c544c2be1cf" />
> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13
Connected to process 11714 on device 'Pixel_7 [emulator-5554]'.

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 40s
73 actionable tasks: 2 executed, 71 up-to-date
<img width="694" height="104" alt="image" src="https://github.com/user-attachments/assets/231afe25-3b23-407c-a374-3293e3558017" />
> Task :app:connectedStagingDebugAndroidTest
Starting 6 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/6 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 1/6 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 2/6 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 3/6 completed. (0 skipped) (0 failed)
Pixel_7(AVD) - 13 Tests 5/6 completed. (0 skipped) (0 failed)
Finished 6 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 1m 29s
73 actionable tasks: 8 executed, 65 up-to-date

---

### Use Case: View Selected Activity

**Location:** `GroupDetailsMemberE2ETest.kt`

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| Squad Member opens a group. | Open a group they are a member of. |
| User sees map with location pins and selected activity card. | Check activity box is present and loaded. Check activity is displayed and not empty. |

**Test Logs:**  
<img width="698" height="136" alt="image" src="https://github.com/user-attachments/assets/7946d128-a3fe-42dd-afff-a09e7bfa3349" />
> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13
Connected to process 11714 on device 'Pixel_7 [emulator-5554]'.

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 40s
73 actionable tasks: 2 executed, 71 up-to-date


---

### Use Case: View Recommended Locations

**Location:** `GroupDetailsLeaderE2ETest.kt`

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| Squad Leader opens a group. | Open their group on the main screen. |
| User sees map with recommended activities list. | Check node with tag "ActivityPicker" or activities list is present and loaded. |

**Test Logs:**  
<img width="703" height="111" alt="image" src="https://github.com/user-attachments/assets/ea517262-58b2-4d06-acd7-4ee516b85e46" />

> Task :app:createStagingDebugAndroidTestApkListingFileRedirect UP-TO-DATE
Connected to process 8661 on device 'Pixel_7 [emulator-5554]'.

> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 33s
73 actionable tasks: 2 executed, 71 up-to-date


## Feature: Update Group


### Use Case: Update Expected People

**Location:** `MemberSettingsLeaderE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| Squad Leader clicks and opens their group on the main screen. | Open their group on the main screen. |
| The app shows Group Details Screen which has a “See Details” button. The button is enabled and the Squad Leader clicks the button. | Check that the button is present on screen. Click the button “See Details”. |
| The app shows the screen with a navigation bar at the bottom of the screen with two items. The Squad Leader clicks the “Settings” option. | Check that the navigation bar item is present on screen. Click the option. |
| 4a. Squad Leader input invalid string of numbers (e.g., 0) for “Expected People” then click the “Save” button. | Input “0” into the “Expected People” text field. Check that button is present on screen. Click the button. |
| 4a1. The app displays an error message prompting the user for the expected format. | Check that a dialog is opened with the message “Expected people must be a positive number.” |
| The Squad Leader inputs a valid string of number for “Expected People”  (e.g., 7) and clicks “Save” button | Input “7” into the “Expected People” text field. Check that button is present on screen. Click the button. |
| The app confirms action success | Check that a dialog is opened with the message “Settings saved successfully!”. |

**Test Logs:**  
<img width="699" height="143" alt="image" src="https://github.com/user-attachments/assets/29090ab5-513b-4e3f-9276-5ce34a6f970a" />
> Task :app:createStagingDebugAndroidTestApkListingFileRedirect UP-TO-DATE
Connected to process 6521 on device 'Pixel_7 [emulator-5554]'.

> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 1m 55s
73 actionable tasks: 8 executed, 65 up-to-date

---

### Use Case: Update Event Time
**Location:** `MemberSettingsLeaderE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| Squad Leader clicks and opens their group on the main screen. | Open their group on the main screen. |
| Click “See Details” button. | Check that the button is present. Click it. |
| Click “Settings” option in navigation bar. | Check that the navigation bar item is present. Click it. |
| Click “Click to update Meeting Date & Time” button. | Check button is present. Click button. |
| 5a. Squad Leader selects a past date/time | Click “OK” twice to set current date/time. |
| 5a1. App displays an error | Check that a dialog is opened with the message “Meeting time must be in the future.” |
| 5b. Squad Leader selects a future date/time and clicks “Save” | Use DatePicker to choose next day. Click “OK”. Click “Save”. |
| App confirms action success | Check that a dialog is opened with the message “Settings saved successfully!”. |

**Test Logs:**  
<img width="703" height="137" alt="image" src="https://github.com/user-attachments/assets/4fc7dfae-b319-4ba3-a192-04ccb165c9f7" />
> Task :app:createStagingDebugAndroidTestApkListingFileRedirect UP-TO-DATE
Connected to process 6521 on device 'Pixel_7 [emulator-5554]'.

> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 1m 55s
73 actionable tasks: 8 executed, 65 up-to-date


---

## Use Case: Update Midpoint Automatically

**Location:** `MemberSettingsLeaderE2ETest.kt`

### Expected Behaviours

| Scenario Steps | Test Case Steps |
|----------------|----------------|
| Squad Leader clicks and opens their group on the main screen. | Open their group on the main screen. |
| The app shows Group Details Screen which has a “Group details” button. The button is enabled and the Squad Leader clicks the button. | Check that the button is present on screen. Click the button “Group details”. |
| The app shows the screen with a navigation bar at the bottom of the screen with two items. The Squad Leader clicks the “Settings” option. | Check that the navigation bar item is present on screen. Click the option. |
| Squad Leader clicks “Automatic Midpoint Update” checkbox | Check that the checkbox is present on screen. Click the checkbox. |
| The app confirms action success | Check that a dialog is opened with the message “Settings saved successfully!”. |

### Test Logs

<img width="705" height="143" alt="image" src="https://github.com/user-attachments/assets/e46001a1-a6d1-4ee7-8cba-e694eefdcd60" />
> Task :app:createStagingDebugAndroidTestApkListingFileRedirect UP-TO-DATE
Connected to process 6521 on device 'Pixel_7 [emulator-5554]'.

> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 1m 55s
73 actionable tasks: 8 executed, 65 up-to-date


### Use Case: Update Member Address

**Location:** `MemberSettingsLeaderE2ETest.kt`
**Location:** `MemberSettingsMemberE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| User opens group and clicks “See Details”. | Open group. Click “See Details” button. |
| Click “Settings” option. | Check that navigation bar item is present. Click option. |
| 5a. User inputs invalid address or does not choose from dropdown, clicks “Save” | Input “Some random text” into address field. Check button present. Click “Save”. |
| 5a1. App displays error | Check dialog shows “Please select a valid address.” |
| 5b. User inputs valid address and selects suggestion, clicks “Save” | Input “6445 University Boulevard, Vancouver, BC” and select first suggestion. Click “Save”. |
| App confirms action success | Check that a dialog is opened with “Settings saved successfully!”. |

**Test Logs:**  
1. Squad Leader
<img width="706" height="138" alt="image" src="https://github.com/user-attachments/assets/f2a2d4ef-1d0b-418a-807a-32ab81e80608" />

> Task :app:createStagingDebugAndroidTestApkListingFileRedirect UP-TO-DATE
Connected to process 6521 on device 'Pixel_7 [emulator-5554]'.

> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 1m 55s
73 actionable tasks: 8 executed, 65 up-to-date

2. Squad Member
 <img width="701" height="152" alt="image" src="https://github.com/user-attachments/assets/3d9e854b-1e13-4bd8-8d0c-9daf43b6093c" />
> Task :app:createStagingDebugAndroidTestApkListingFileRedirect UP-TO-DATE
Connected to process 7481 on device 'Pixel_7 [emulator-5554]'.

> Task :app:connectedStagingDebugAndroidTest
Starting 1 tests on Pixel_7(AVD) - 13

Pixel_7(AVD) - 13 Tests 0/1 completed. (0 skipped) (0 failed)
Finished 1 tests on Pixel_7(AVD) - 13

BUILD SUCCESSFUL in 1m 10s
73 actionable tasks: 2 executed, 71 up-to-date

---

### Use Case: Update Member Transit

**Location:** `MemberSettingsLeaderE2ETest.kt`
**Location:** `MemberSettingsMemberE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| User opens group and clicks “See Details”. | Open group. Click “See Details” button. |
| Click “Settings” option. | Check navigation bar item present. Click option. |
| Click “Preferred Mode of Transport” text field | Check field is present. Click field. |
| Select option and click “Save” | Choose “DRIVING”. Click “Save”. |
| App confirms action success | Check dialog shows “Settings saved successfully!”. |

**Test Logs:**  
<img width="715" height="429" alt="image" src="https://github.com/user-attachments/assets/9be23196-4c2e-4c1e-a0cf-ae7d0720cc11" />
<img width="715" height="449" alt="image" src="https://github.com/user-attachments/assets/fd71e04f-57f9-4802-8a15-f1fd09bd0c91" />


---

### Use Case: Find Midpoint

**Location:** `GroupDetailsLeaderE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| Squad Leader opens group | Open group on main screen. |
| Click “Find Midpoint” or “Recalculate Midpoint” | Check button is present. Click button. |
| App calculates midpoint | Check text box shows “Getting midpoint…”. Wait for it to disappear. |
| Screen updates with map and pins | Check map appears with location pins. |

**Test Logs:**  
<img width="722" height="401" alt="image" src="https://github.com/user-attachments/assets/0cb93027-5daa-4039-8c8b-27ff5dddde2f" />

<img width="720" height="384" alt="image" src="https://github.com/user-attachments/assets/97caf075-42ae-4306-a282-b8ee44364607" />

---

### Use Case: Select Activity

**Location:** `GroupDetailsLeaderE2ETest.kt`

**Expected Behaviours:**

| **Scenario Steps** | **Test Case Steps** |
| ----------------- | ------------------ |
| Squad Leader opens group | Open group on main screen. |
| Midpoint calculated; view recommended activities | Check "ActivityPicker" present and loaded. |
| Select an activity | Select first item in the list. |
| Confirm selection | Check “Select Activity” button present. Click button. |
| App confirms action success | Check dialog shows “Activity selected successfully!”. |

**Test Logs:**  
![Update Expected People Logs](images/m4-logs/UG-select-activity.png)


---

## 5. Automated Code Review Results

### 5.1. Commit Hash Where Codacy Ran

`2e0fdf4365b7ab126a4a9c5f4e8583f44953bce9`


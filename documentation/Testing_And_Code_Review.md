# Testing and Code Review

## 1. Change History

| **Change Date**   | **Modified Sections** | **Rationale** |
| ----------------- | --------------------- | ------------- |
| _Nothing to show_ |

---

## 2. Back-end Test Specification: APIs

### 2.1. Locations of Back-end Tests and Instructions to Run Them

| **Interface**                      | **Describe Group Location, No Mocks**     | **Describe Group Location, With Mocks**            | **Mocked Components**     |
| ---------------------------------- | ----------------------------------------- | -------------------------------------------------- | ------------------------- |
| **POST /auth/signup**              | N/A                                       | `tests/mocked/auth.controller.mocked.test.ts#L31`  | Authentication Service    |
| **POST /auth/signin**              | N/A                                       | `tests/mocked/auth.controller.mocked.test.ts#L146` | Authentication Service    |
| **GET /user/profile**              | `tests/unmocked/user.tests.ts#L276`       | N/A                                                | Authentication Service    |
| **POST /user/profile**             | `tests/unmocked/user.tests.ts#L100`       | `tests/mocked/user.mocked.test.ts#L54`             | MongoDB, Media Service    |
| **DELETE /user/profile**           | `tests/unmocked/user.tests.ts#L134`       | `tests/mocked/user.mocked.test.ts#L145`            | MongoDB, Media Service    |
| **POST /group/create**             | `tests/unmocked/group.tests.ts#L476`      | `tests/mocked/group.mocked.test.ts#L169`           | MongoDB                   |
| **GET /group/info**                | `tests/unmocked/group.tests.ts#L429`      | `tests/unmocked/group.tests.ts#L49`                | MongoDB                   |
| **GET /group/:joinCode**           | `tests/unmocked/group.tests.ts#L443`      | `tests/unmocked/group.tests.ts#L77`                | MongoDB                   |
| **POST /group/update**             | `tests/unmocked/group.tests.ts#L555`      | N/A                                                | MongoDB                   |
| **DELETE /group/delete/:joinCode** | `tests/unmocked/group.tests.ts#L598`      | `tests/unmocked/group.tests.ts#L198`               | MongoDB                   |
| **POST /group/join**               | `tests/unmocked/group.tests.ts#L443`      | `tests/unmocked/group.tests.ts#L77`                | MongoDB                   |
| **POST /group/leave/:joinCode**    | `tests/unmocked/group.tests.ts#L506`      | `tests/unmocked/group.tests.ts#L102`               | MongoDB                   |
| **GET /group/activities**          | `tests/unmocked/activities.tests.ts#L144` | `tests/mocked/activities.mocked.test.ts#L29`       | MongoDB, Location Service |
| **POST /group/activities/select**  | `tests/unmocked/activities.tests.ts#L224` | `tests/mocked/activities.mocked.test.ts#L92`       | MongoDB, Location Service |


#### 2.1.2. Commit Hash Where Tests Run

`[Insert Commit SHA here]`

#### 2.1.3. Explanation on How to Run the Tests

1. **Clone the Repository**:

2. **Install Required Libraries**:

   - Ensure the following libraries are installed:

   ```npm install --save-dev jest``` \
   ```npm install --save-dev ts-jest``` \
   ```npm install --save-dev @types/jest```

3. **Navigate to the Back-end Directory**:

   ```cd backend```

4. **Run the Tests**:

   ```npx jest```

5. **Run the Tests with Coverage Report**:

   ```npx jest --coverage```

### 2.2. GitHub Actions Configuration Location

`~/.github/workflows/backend-tests.yml`

### 2.3. Jest Coverage Report Screenshots for Tests Without Mocking

![Coverage Report Without Mocking](images/test-no-mock.png)

### 2.4. Jest Coverage Report Screenshots for Tests With Mocking

![Coverage Report With Mocking](images/test-mock.png)

### 2.5. Jest Coverage Report Screenshots for Both Tests With and Without Mocking

![Coverage Report With All Tests](images/test-all.png)

---

## 3. Back-end Test Specification: Tests of Non-Functional Requirements

### 3.1. Test Locations in Git

| **Non-Functional Requirement**  | **Location in Git**                              |
| ------------------------------- | ------------------------------------------------ |
| **Location Service**            | tests/nonfunctional.test.ts#18                  |
| **Group View Load Time**        | tests/nonfunctional.test.ts#297                 |

### 3.2. Explanation of Non-Functional Requirement Tests

**Location Service**:

These tests measure the performance of the location optimization algorithm to ensure it meets the non-functional requirement of returning results within 2-5 seconds. The tests calculate midpoints for groups of varying sizes (2, 5, and 10 users) and also measure the combined response time when fetching both the optimal meeting point AND retrieving nearby activities/venues. By testing with different group sizes, the tests verify that the algorithm scales reasonably and doesn't degrade significantly as more users are added to the calculation.

**Group View Load Time**:

These tests measure the API response time for fetching group information to ensure it meets the requirement of loading group details within 2 seconds. The tests simulate various scenarios including fetching a single group by join code (with different member counts), fetching all groups at once, and calculating midpoints. By testing with different group member counts, these tests verify that the database queries and data serialization don't cause performance degradation, ensuring the UI can display group information to users quickly regardless of group size.


### 3.2. Test Verification and Logs

- **Performance (Response Time)**

  - **Verification:** This test suite simulates multiple concurrent API calls using Jest along with a load-testing utility to mimic real-world user behavior. The focus is on key endpoints such as user login and study group search to ensure that each call completes within the target response time of 2 seconds under normal load. The test logs capture metrics such as average response time, maximum response time, and error rates. These logs are then analyzed to identify any performance bottlenecks, ensuring the system can handle expected traffic without degradation in user experience.
  - **Log Output**
    ```
    [Placeholder for response time test logs]
    ```

- **Chat Data Security**
  - **Verification:** ...
  - **Log Output**
    ```
    [Placeholder for chat security test logs]
    ```

---

## 4. Front-end Test Specification

### 4.1. Location in Git of Front-end Test Suite:

`frontend/src/androidTest/java/com/studygroupfinder/`

### 4.2. Tests

- **Use Case: Login**

  - **Expected Behaviors:**
    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | 1. The user opens "Add Todo Items" screen. | Open "Add Todo Items" screen. |
    | 2. The app shows an input text field and an "Add" button. The add button is disabled. | Check that the text field is present on screen.<br>Check that the button labelled "Add" is present on screen.<br>Check that the "Add" button is disabled. |
    | 3a. The user inputs an ill-formatted string. | Input "_^_^^OQ#$" in the text field. |
    | 3a1. The app displays an error message prompting the user for the expected format. | Check that a dialog is opened with the text: "Please use only alphanumeric characters ". |
    | 3. The user inputs a new item for the list and the add button becomes enabled. | Input "buy milk" in the text field.<br>Check that the button labelled "add" is enabled. |
    | 4. The user presses the "Add" button. | Click the button labelled "add ". |
    | 5. The screen refreshes and the new item is at the bottom of the todo list. | Check that a text box with the text "buy milk" is present on screen.<br>Input "buy chocolate" in the text field.<br>Click the button labelled "add".<br>Check that two text boxes are present on the screen with "buy milk" on top and "buy chocolate" at the bottom. |
    | 5a. The list exceeds the maximum todo-list size. | Repeat steps 3 to 5 ten times.<br>Check that a dialog is opened with the text: "You have too many items, try completing one first". |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **Use Case: ...**

  - **Expected Behaviors:**

    | **Scenario Steps** | **Test Case Steps** |
    | ------------------ | ------------------- |
    | ...                | ...                 |

  - **Test Logs:**
    ```
    [Placeholder for Espresso test execution logs]
    ```

- **...**

---

## 5. Automated Code Review Results

### 5.1. Commit Hash Where Codacy Ran

`[Insert Commit SHA here]`

### 5.2. Unfixed Issues per Codacy Category

_(Placeholder for screenshots of Codacy's Category Breakdown table in Overview)_

### 5.3. Unfixed Issues per Codacy Code Pattern

_(Placeholder for screenshots of Codacy's Issues page)_

### 5.4. Justifications for Unfixed Issues

- **Code Pattern: [Usage of Deprecated Modules](#)**

  1. **Issue**

     - **Location in Git:** [`src/services/chatService.js#L31`](#)
     - **Justification:** ...

  2. ...

- ...

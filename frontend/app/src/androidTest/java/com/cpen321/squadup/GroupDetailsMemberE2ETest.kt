package com.cpen321.squadup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
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

    private fun navigateToGroupList() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click "See Details"
        composeTestRule.onNodeWithText("See Details")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    private fun viewAttendees() {
        composeTestRule.onAllNodesWithContentDescription("Member", substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    @Test
    fun groupDetailsScreen() {
        // Navigate to the first group
        navigateToFirstGroup()
        viewSelectedActivity()
        navigateToGroupList()
        viewAttendees()
    }

    private fun viewSelectedActivity() {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("Selected Activity:").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("Selected Activity:").assertIsDisplayed()
    }
}

package com.cpen321.squadup

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
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
class GroupMidpointE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var device: UiDevice

    @Before
    fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        device.wait(Until.hasObject(By.pkg(TestData.APP_PACKAGE_NAME)), TestData.TEST_TIMEOUT_LONG)
        composeTestRule.waitForIdle()
        Thread.sleep(3000) // wait for main screen to load
    }

    private fun navigateToFirstGroup() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click the first group (by "Leader:" label)
        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)
    }

    // Click whichever midpoint button is visible
    private fun clickMidpointButton() {
        val findButtons = composeTestRule.onAllNodesWithText("Find midpoint", substring = true)
        if (findButtons.fetchSemanticsNodes().isNotEmpty()) {
            findButtons.onFirst().performClick()
        } else {
            val recalcButtons = composeTestRule.onAllNodesWithText("Recalculate Midpoint", substring = true)
            if (recalcButtons.fetchSemanticsNodes().isNotEmpty()) {
                recalcButtons.onFirst().performClick()
            }
        }
    }

    @Test
    fun viewMidpointAndRecommendedLocations() {
        // Navigate to the first group
        navigateToFirstGroup()

        // Click midpoint button every time
        clickMidpointButton()

        // Wait for midpoint map to appear
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

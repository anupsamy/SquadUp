import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.cpen321.squadup.MainActivity
import com.cpen321.squadup.TestData
import com.cpen321.squadup.TestUtilities.waitForNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalTestApi
@LargeTest
class MemberSettingsE2ETest {

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

    /**
     * Helper to navigate to a group and Member Settings
     */
    private fun navigateToMemberSettings() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click on the first group on main screen
        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .performClick()

        // Click "See Details"
        composeTestRule.onNodeWithText("See Details")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click "Settings"
        composeTestRule.onNodeWithText("Settings")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    @Test
    fun updateMemberSettings_success() {
        navigateToMemberSettings()

        // 4️⃣ Expected People
        composeTestRule.onNode(hasText("Expected People"))
            .performTextClearance()

        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onNode(hasText("Expected People"))
            .performTextInput("7")

        Thread.sleep(500)

        // 3️⃣ Meeting Time (click to open picker, then confirm date & time)
        composeTestRule.onNodeWithText("Update Meeting Date & Time")
            .performClick()
        Thread.sleep(500)
        device.findObject(By.text("OK"))?.click() // date
        Thread.sleep(500)
        device.findObject(By.text("OK"))?.click() // time

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // 1️⃣ AddressPicker: type and click first prediction
        composeTestRule.onNode(hasText("Address"))
            .performTextClearance()

        composeTestRule.waitForIdle()
        Thread.sleep(500)

        composeTestRule.onNodeWithText("Address")
            .performTextInput("6445 University Boulevard, Vancouver, BC, Canada, V6T 1Z2")

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.onAllNodesWithText("6445 University Boulevard", substring = true)
            .onLast()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        // 2️⃣ TransitType
        composeTestRule.onNodeWithText("Preferred Mode of Transport")
            .performClick()
        composeTestRule.waitForIdle()
        Thread.sleep(500)
        composeTestRule.onAllNodesWithText("DRIVING")
            .onLast()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(1000)

        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithText("Meeting set to:", substring = true).fetchSemanticsNodes().isEmpty()
        }

        // 5️⃣ Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // 6️⃣ Verify Snackbar
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 10000)
            .assertIsDisplayed()
    }
}

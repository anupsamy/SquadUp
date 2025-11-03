import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
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
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
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
     * Helper to navigate to "test 2" group and Member Settings
     */
    private fun navigateToMemberSettings() {
        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click on "test 2" group (or first group if needed)
        composeTestRule.onAllNodesWithText("Leader:", substring = true)
            .onFirst()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click "See Details"
        composeTestRule.onNodeWithText("See Details")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)

        // Click "Settings"
        composeTestRule.onNodeWithContentDescription("Settings")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        Thread.sleep(2000)
    }

    //@Test
    fun updateMemberSettings_success() {
        navigateToMemberSettings()

        // 1️⃣ AddressPicker: type and click first prediction
//        composeTestRule.onNodeWithText("Address")
//            .performTextInput("456 New Street")
//
//        composeTestRule.onAllNodesWithText("456 New Street", substring = true)
//            .onFirst()
//            .performClick()

        // 2️⃣ TransitType
        composeTestRule.onNodeWithText("Preferred Mode of Transport")
            .performClick()
        composeTestRule.onNodeWithText("WALKING")
            .performClick()

//        // 3️⃣ Meeting Time (click to open picker, then confirm date & time)
//        composeTestRule.onNodeWithText("Update Meeting Date & Time")
//            .performClick()
//        Thread.sleep(500)
//        device.findObject(By.text("OK"))?.click() // date
//        Thread.sleep(500)
//        device.findObject(By.text("OK"))?.click() // time
//
//        // 4️⃣ Expected People
//        composeTestRule.onNode(hasText("Expected People"))
//            .performTextInput("7")

        // 5️⃣ Save
        composeTestRule.onNodeWithText("Save")
            .assertIsEnabled()
            .performClick()

        // 6️⃣ Verify Snackbar
        composeTestRule.waitForNodeWithText("Settings saved successfully!", timeoutMillis = 5000)
            .assertIsDisplayed()
    }
}

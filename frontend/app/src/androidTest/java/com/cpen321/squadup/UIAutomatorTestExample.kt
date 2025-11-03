package com.cpen321.squadup

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Example UI Automator test demonstrating cross-app UI automation.
 * 
 * UI Automator is used for:
 * - Testing interactions with system UI (settings, notifications, app launcher)
 * - Testing interactions across multiple apps
 * - Testing device-level features (volume, brightness, etc.)
 * 
 * Reference: https://developer.android.com/training/testing/other-components/ui-automator
 */
@RunWith(AndroidJUnit4::class)
class UIAutomatorTestExample {

    private lateinit var device: UiDevice

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }

    @Test
    fun testOpenSettingsMenu() {
        // Wait for the app to launch
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Open the device settings menu
        // Note: This requires appropriate permissions and may vary by device
        val settingsButton = device.findObject(By.desc("Settings"))
        
        // Alternative: Use app intent to open settings
        val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ApplicationProvider.getApplicationContext<android.app.Application>()
            .startActivity(intent)

        // Wait for settings to open
        device.wait(Until.hasObject(By.pkg("com.android.settings")), 3000)

        // Verify settings screen is displayed
        val settingsTitle = device.findObject(By.text("Settings"))
        assertNotNull(settingsTitle)
    }

    @Test
    fun testAppLauncher() {
        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Press home button to go to launcher
        device.pressHome()

        // Wait for launcher
        device.wait(Until.hasObject(By.desc("Apps")), 3000)

        // Find and click on apps button/icon
        val appsButton = device.findObject(By.desc("Apps"))
        appsButton?.click()

        // Wait for app drawer to open
        device.wait(Until.hasObject(By.res("com.android.launcher:id/apps_list_view")), 3000)

        // Verify app is in launcher
        val squadUpApp = device.findObject(By.text("SquadUp"))
        assertNotNull(squadUpApp)
    }

    @Test
    fun testNotificationPanel() {
        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Open notification panel by swiping down from top
        device.openNotification()

        // Wait for notification panel to open
        device.wait(Until.hasObject(By.res("com.android.systemui:id/notification_container")), 3000)

        // Interact with notifications if needed
        // val notification = device.findObject(By.text("Your Notification"))
        // notification?.click()

        // Close notification panel
        device.pressBack()
    }

    @Test
    fun testSystemBackButton() {
        // Navigate within your app
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Press back button
        device.pressBack()

        // Wait for navigation to complete
        device.waitForIdle()
    }

    @Test
    fun testFindElementByResourceId() {
        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Find element by resource ID (package name + resource id)
        val element: UiObject2? = device.findObject(
            By.res("com.cpen321.squadup", "element_id")
        )

        // Interact with element
        element?.click()
    }

    @Test
    fun testFindElementByText() {
        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Find element by text content
        val button = device.findObject(By.text("Click Me"))

        // Verify element exists and perform action
        assertNotNull(button)
        button?.click()
    }

    @Test
    fun testWaitForElement() {
        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Wait for a specific element to appear (timeout in milliseconds)
        val element = device.wait(
            Until.findObject(By.text("Expected Text")),
            5000 // 5 second timeout
        )

        // Verify element appeared
        assertNotNull(element)
    }

    @Test
    fun testMultipleApps() {
        // Launch your app
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Navigate to home
        device.pressHome()

        // Launch another app (example: Chrome)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setPackage("com.android.chrome")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ApplicationProvider.getApplicationContext<android.app.Application>()
            .startActivity(intent)

        // Wait for Chrome to open
        device.wait(Until.hasObject(By.pkg("com.android.chrome")), 3000)

        // Perform actions in Chrome, then return to your app
        device.pressHome()
    }

    @Test
    fun testDeviceRotation() {
        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Rotate device to landscape
        device.setOrientationLeft()

        // Wait for rotation to complete
        device.waitForIdle()

        // Verify UI adapted to landscape
        // Your assertions here

        // Rotate back to portrait
        device.setOrientationNatural()
        device.waitForIdle()
    }

    @Test
    fun testTouchActions() {
        // Wait for app to be ready
        device.wait(Until.hasObject(By.pkg("com.cpen321.squadup")), 3000)

        // Perform swipe gesture
        device.swipe(
            device.displayWidth / 2,  // Start X
            device.displayHeight / 2, // Start Y
            device.displayWidth / 2,  // End X
            device.displayHeight / 4, // End Y
            10 // Steps (speed)
        )

        // Wait for gesture to complete
        device.waitForIdle()
    }

    /**
     * Helper function to wait for app to be ready
     */
    private fun waitForAppReady() {
        device.wait(
            Until.hasObject(By.pkg("com.cpen321.squadup")),
            5000
        )
    }
}

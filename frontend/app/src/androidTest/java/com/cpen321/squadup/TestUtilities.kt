package com.cpen321.squadup

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until

/**
 * Utility functions and extension methods for testing.
 * 
 * These helpers make writing tests more concise and maintainable.
 */
object TestUtilities {

    /**
     * Extension function to wait for a text node and assert it's displayed.
     */
    fun ComposeContentTestRule.waitForNodeWithText(
        text: String,
        timeoutMillis: Long = 5000
    ): SemanticsNodeInteraction {
        this.waitUntil(timeoutMillis) {
            try {
                // Try to fetch the semantics node to verify it exists
                this.onNodeWithText(text).fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
        return this.onNodeWithText(text)
    }

    /**
     * Extension function to assert text is displayed with better error messages.
     */
    fun SemanticsNodeInteraction.assertTextDisplayed(
        expectedText: String,
        substring: Boolean = false
    ) {
        if (substring) {
            assertTextContains(expectedText)
        } else {
            // Use assertTextContains for exact match checking
            assertTextContains(expectedText)
        }
        assertIsDisplayed()
    }

    /**
     * Wait for UI Automator device to find an element by package name.
     */
    fun UiDevice.waitForPackage(
        packageName: String,
        timeoutMillis: Long = 5000
    ): Boolean {
        return this.wait(
            Until.hasObject(androidx.test.uiautomator.By.pkg(packageName)),
            timeoutMillis
        )
    }

    /**
     * Helper to perform common click operations with retry logic.
     */
    fun SemanticsNodeInteraction.performClickWithRetry(
        maxAttempts: Int = 3
    ) {
        var attempts = 0
        while (attempts < maxAttempts) {
            try {
                performClick()
                return
            } catch (e: Exception) {
                attempts++
                if (attempts >= maxAttempts) throw e
                Thread.sleep(500)
            }
        }
    }

    /**
     * Scroll to element if needed and perform click.
     */
    fun SemanticsNodeInteraction.scrollToAndClick() {
        try {
            performScrollTo()
        } catch (e: Exception) {
            // Element might already be visible, continue
        }
        performClick()
    }

    /**
     * Wait for element to become enabled.
     */
    fun ComposeContentTestRule.waitForEnabled(
        text: String,
        timeoutMillis: Long = 5000
    ): SemanticsNodeInteraction {
        this.waitUntil(timeoutMillis) {
            try {
                this.onNodeWithText(text).assertIsEnabled()
                true
            } catch (e: Exception) {
                false
            }
        }
        return this.onNodeWithText(text)
    }
}

/**
 * Test data and constants for use in tests.
 */
object TestData {
    const val TEST_EMAIL = "test@example.com"
    const val TEST_PASSWORD = "testPassword123"
    const val TEST_USERNAME = "testuser"
    
    const val APP_PACKAGE_NAME = "com.cpen321.squadup"
    const val TEST_TIMEOUT_SHORT = 3000L
    const val TEST_TIMEOUT_MEDIUM = 5000L
    const val TEST_TIMEOUT_LONG = 10000L
}

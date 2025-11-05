package com.cpen321.squadup

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.uiautomator.By
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
            Until.hasObject(By.pkg(packageName)),
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

    /**
     * Wait for a node with specific text to disappear.
     */
    fun ComposeContentTestRule.waitForNodeToDisappear(
        text: String,
        timeoutMillis: Long = 5000
    ) {
        this.waitUntil(timeoutMillis) {
            try {
                this.onNodeWithText(text).fetchSemanticsNode()
                false // Node still exists
            } catch (e: Exception) {
                true // Node doesn't exist (disappeared)
            }
        }
    }

    /**
     * Extension function to wait for a node by content description.
     */
    fun ComposeContentTestRule.waitForNodeWithContentDescription(
        description: String,
        timeoutMillis: Long = 5000
    ): SemanticsNodeInteraction {
        this.waitUntil(timeoutMillis) {
            try {
                this.onNodeWithContentDescription(description).fetchSemanticsNode()
                true
            } catch (e: Exception) {
                false
            }
        }
        return this.onNodeWithContentDescription(description)
    }

    /**
     * Perform text input with retry logic for flaky keyboard input.
     */
    fun SemanticsNodeInteraction.performTextInputWithRetry(
        text: String,
        maxAttempts: Int = 3
    ): SemanticsNodeInteraction {
        var attempts = 0
        while (attempts < maxAttempts) {
            try {
                performTextInput(text)
                return this
            } catch (e: Exception) {
                attempts++
                if (attempts >= maxAttempts) throw e
                Thread.sleep(500)
            }
        }
        return this
    }

    /**
     * Click on UI Automator element by text with wait.
     */
    fun UiDevice.clickByText(
        text: String,
        timeoutMillis: Long = 5000
    ): Boolean {
        this.wait(Until.hasObject(By.text(text)), timeoutMillis)
        val element = this.findObject(By.text(text))
        if (element != null) {
            element.click()
            return true
        }
        return false
    }

    /**
     * Wait for multiple possible texts to appear (useful for dynamic content).
     */
    fun ComposeContentTestRule.waitForAnyNodeWithText(
        texts: List<String>,
        timeoutMillis: Long = 5000
    ): String? {
        val endTime = System.currentTimeMillis() + timeoutMillis
        while (System.currentTimeMillis() < endTime) {
            for (text in texts) {
                try {
                    this.onNodeWithText(text).fetchSemanticsNode()
                    return text
                } catch (e: Exception) {
                    // Continue checking
                }
            }
            Thread.sleep(100)
        }
        return null
    }

    /**
     * Navigate back and verify navigation succeeded by checking for a node.
     */
    fun UiDevice.navigateBackAndVerify(
        composeTestRule: ComposeContentTestRule,
        expectedText: String,
        timeoutMillis: Long = 3000
    ) {
        this.pressBack()
        composeTestRule.waitForIdle()
        composeTestRule.waitForNodeWithText(expectedText, timeoutMillis)
    }

    /**
     * Check if a node with text exists without failing.
     */
    fun ComposeContentTestRule.nodeWithTextExists(text: String): Boolean {
        return try {
            this.onNodeWithText(text).fetchSemanticsNode()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Wait for loading to complete (useful after API calls).
     */
    fun ComposeContentTestRule.waitForLoading(timeoutMillis: Long = 3000) {
        Thread.sleep(timeoutMillis)
        this.waitForIdle()
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
    
    // Group Management Test Data
    const val TEST_GROUP_NAME = "Test Group"
    const val TEST_EXPECTED_PEOPLE = "5"
    const val TEST_JOIN_CODE_LENGTH = 6
    const val INVALID_JOIN_CODE = "XXXXXX"
    
    // Common UI Text
    const val CREATE_GROUP_TEXT = "Create Group"
    const val JOIN_GROUP_TEXT = "Join Group"
    const val SEE_DETAILS_TEXT = "See Details"
    const val LEAVE_SQUAD_TEXT = "Leave Squad"
    const val DELETE_SQUAD_TEXT = "Delete Squad"
    const val JOIN_CODE_TEXT = "Join Code"
    const val MEMBERS_TEXT = "Members"
    const val HOST_TEXT = "Host"
}

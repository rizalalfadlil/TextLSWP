package com.rizalalfadlil.textwall

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent

/**
 * Accessibility service used to lock the screen via [GLOBAL_ACTION_LOCK_SCREEN].
 * This approach allows biometric unlock (fingerprint / face ID) after locking,
 * unlike DevicePolicyManager.lockNow() which enforces a secure lock.
 */
class ScreenOffAccessibilityService : AccessibilityService() {

    companion object {
        /** Non-null while the service is connected/running. */
        var instance: ScreenOffAccessibilityService? = null
            private set
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        val info = AccessibilityServiceInfo().apply {
            eventTypes = 0          // We don't listen to any events
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        }
        serviceInfo = info
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No-op: we only use this service to perform global actions
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        instance = null
        return super.onUnbind(intent)
    }
}

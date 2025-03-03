package io.sentry.android.fragment

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.sentry.IHub
import io.sentry.Integration
import io.sentry.SentryLevel.DEBUG
import io.sentry.SentryOptions
import java.io.Closeable

class FragmentLifecycleIntegration(
    private val application: Application,
    private val enableFragmentLifecycleBreadcrumbs: Boolean,
    private val enableAutoFragmentLifecycleTracing: Boolean
) :
    ActivityLifecycleCallbacks,
    Integration,
    Closeable {

    constructor(application: Application) : this(application, true, false)

    private lateinit var hub: IHub
    private lateinit var options: SentryOptions

    override fun register(hub: IHub, options: SentryOptions) {
        this.hub = hub
        this.options = options

        application.registerActivityLifecycleCallbacks(this)
        options.logger.log(DEBUG, "FragmentLifecycleIntegration installed.")
    }

    override fun close() {
        application.unregisterActivityLifecycleCallbacks(this)
        if (::options.isInitialized) {
            options.logger.log(DEBUG, "FragmentLifecycleIntegration removed.")
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        (activity as? FragmentActivity)
            ?.supportFragmentManager
            ?.registerFragmentLifecycleCallbacks(
                SentryFragmentLifecycleCallbacks(
                    hub = hub,
                    enableFragmentLifecycleBreadcrumbs = enableFragmentLifecycleBreadcrumbs,
                    enableAutoFragmentLifecycleTracing = enableAutoFragmentLifecycleTracing
                ),
                true
            )
    }

    override fun onActivityStarted(activity: Activity) {
        // no-op
    }

    override fun onActivityResumed(activity: Activity) {
        // no-op
    }

    override fun onActivityPaused(activity: Activity) {
        // no-op
    }

    override fun onActivityStopped(activity: Activity) {
        // no-op
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        // no-op
    }

    override fun onActivityDestroyed(activity: Activity) {
        /**
         * It is not needed to unregister [SentryFragmentLifecycleCallbacks] as
         * [androidx.fragment.app.FragmentManager] will do this on its own when it's destroyed.
         *
         * @see [androidx.fragment.app.FragmentManager.registerFragmentLifecycleCallbacks]
         */
        // no-op
    }
}

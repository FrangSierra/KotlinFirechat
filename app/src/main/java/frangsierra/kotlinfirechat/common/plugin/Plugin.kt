package frangsierra.kotlinfirechat.common.plugin

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent


/**
 * Base interface for al plugins loaded in the Activity using the component.
 * The interface mirrors Activity callbacks.
 */
interface Plugin {

    /**
     * Return plugin runtime information used by the activity to manage callbacks. This method
     * is only called once.
     */
    val properties: PluginProperties

    /** [Activity.onCreate] */
    fun onCreate(savedInstanceState: Bundle?)

    /** [Activity.onPostCreate] */
    fun onPostCreate()

    /** All the components have completed their onCreate method, it's safe to reference external views. */
    fun onPluginsCreated()

    /**
     * [Activity.onSaveInstanceState].
     * Every plugin has its own bundle to avoid key collisions.
     */
    fun onSaveInstanceState(outState: Bundle)

    /** [Activity.onDestroy] */
    fun onDestroy()

    /** [Activity.onStart] */
    fun onStart()

    /** [Activity.onResume] */
    fun onResume()

    /** [Activity.onPause] */
    fun onPause()

    /** [Activity.onStop] */
    fun onStop()

    /** [Activity.onBackPressed] */
    fun onBackPressed(cb: WrappedCallback<Nothing?, Nothing?>)

    /** [Activity.dispatchTouchEvent] */
    fun onDispatchTouchEvent(cb: WrappedCallback<MotionEvent, Boolean>)

    /** [Activity.onKeyDown] */
    fun onKeyDown(cb: WrappedCallback<KeyEvent, Boolean>)

    /** [Activity.onKeyUp] */
    fun onKeyUp(cb: WrappedCallback<KeyEvent, Boolean>)

    /** [Activity.onConfigurationChanged] */
    fun onConfigurationChanged(newConfig: Configuration)

    /** [Activity.onActivityResult] */
    fun onActivityResult(onActivityResultCallback:
                         WrappedCallback<PluginActivity.ActivityResult, Nothing?>)

    /** [Activity.onRequestPermissionsResult] */
    fun onRequestPermissionsResult(onPermissionsResultCallback:
                                   WrappedCallback<PluginActivity.RequestPermissionResult, Nothing?>)
}

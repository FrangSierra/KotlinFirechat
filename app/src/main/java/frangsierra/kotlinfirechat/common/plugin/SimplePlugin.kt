package frangsierra.kotlinfirechat.common.plugin

import android.content.res.Configuration
import android.os.Bundle
import android.support.annotation.CallSuper
import android.view.KeyEvent
import android.view.MotionEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class SimplePlugin : Plugin {

    private val disposables = CompositeDisposable()

    override val properties: PluginProperties
        get() = PluginProperties.DEFAULT

    override fun onCreate(savedInstanceState: Bundle?) {}

    override fun onPostCreate() {}

    override fun onPluginsCreated() {}

    override fun onSaveInstanceState(outState: Bundle) {}

    @CallSuper
    override fun onDestroy() {
        disposables.clear()
    }

    override fun onStart() {}

    override fun onResume() {}

    override fun onPause() {}

    override fun onStop() {}

    override fun onBackPressed(cb: WrappedCallback<Nothing?, Nothing?>) {}

    override fun onDispatchTouchEvent(cb: WrappedCallback<MotionEvent, Boolean>) {}

    override fun onKeyDown(cb: WrappedCallback<KeyEvent, Boolean>) {}

    override fun onKeyUp(cb: WrappedCallback<KeyEvent, Boolean>) {}

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onActivityResult(
            onActivityResultCallback:
            WrappedCallback<PluginActivity.ActivityResult, Nothing?>) {
    }

    override fun onRequestPermissionsResult(
            onPermissionsResultCallback:
            WrappedCallback<PluginActivity.RequestPermissionResult, Nothing?>) {
    }

    fun <T : Disposable> T.track(): T {
        disposables.add(this)
        return this
    }
}
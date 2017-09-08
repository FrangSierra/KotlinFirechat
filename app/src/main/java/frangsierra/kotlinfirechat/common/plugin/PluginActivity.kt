package frangsierra.kotlinfirechat.common.plugin

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import frangsierra.kotlinfirechat.common.flux.FluxActivity
import frangsierra.kotlinfirechat.common.log.Grove
import java.util.*


private const val TAG = "PluginActivity"
private const val LC_TAG = "LifeCycle"
private const val ARG_PLUGIN_SAVED_STATES = "pluginStates"

abstract class PluginActivity<T : Any> : FluxActivity<T>() {

    // Plugins
    private lateinit var pluginMap: Map<Class<*>, Plugin>
    private val pluginList = ArrayList<Plugin>()
    private val pluginBackList = ArrayList<Plugin>()
    private val pluginTouchList = ArrayList<Plugin>()

    // Reused events
    private val sharedTouchCallback: WrappedCallback<MotionEvent, Boolean>
    private val sharedKeyDownCallback: WrappedCallback<KeyEvent, Boolean>
    private val sharedKeyUpCallback: WrappedCallback<KeyEvent, Boolean>
    private val sharedBackEvent = WrappedCallback(null, null) { super.onBackPressed() }
    private val onActivityResultCallback = WrappedCallback(ActivityResult(0, 0, null), null)
    private val onPermissionsResultCallback = WrappedCallback(
            RequestPermissionResult(0, emptyArray(), intArrayOf()), null)

    init {
        val dummyMotionEvent = MotionEvent.obtain(0, 0, 0, 0.0f, 0.0f, 0)
        val dummyKeyEvent = KeyEvent(0, 0)

        sharedTouchCallback = WrappedCallback<MotionEvent, Boolean>(dummyMotionEvent, false)
        { super.dispatchTouchEvent(it) }

        sharedKeyDownCallback = WrappedCallback(dummyKeyEvent, false)
        { super.onKeyDown(it.keyCode, it) }

        sharedKeyUpCallback = WrappedCallback(dummyKeyEvent, false)
        { super.onKeyUp(it.keyCode, it) }

        dummyMotionEvent.recycle()
    }

    abstract fun createPluginMap(): Map<Class<*>, Plugin>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val now = System.currentTimeMillis()

        pluginMap = createPluginMap()

        //Register lifecycle, back and touch
        pluginList.addAll(pluginMap.values)
        pluginBackList.addAll(pluginMap.values)
        pluginList.filterTo(pluginTouchList) { it.properties.willHandleTouch }

        //Sort by priority
        Collections.sort(pluginList) { o1, o2 ->
            Integer.compare(o1.properties.lifecyclePriority, o2.properties.lifecyclePriority)
        }
        Collections.sort(pluginBackList) { o1, o2 ->
            Integer.compare(o1.properties.backPriority, o2.properties.backPriority)
        }
        Collections.sort(pluginTouchList) { o1, o2 ->
            Integer.compare(o1.properties.touchPriority, o2.properties.touchPriority)
        }

        //Restore plugin states
        var pluginSavedStates: ArrayList<Bundle>? = null
        if (savedInstanceState != null) {
            pluginSavedStates = savedInstanceState.getParcelableArrayList<Bundle>(ARG_PLUGIN_SAVED_STATES)
        }


        val pluginCount = pluginList.size
        val loadTimes = LongArray(pluginCount)

        Grove.tag(LC_TAG).d { "onCreate" }
        for (i in 0..pluginCount - 1) {
            var state: Bundle? = null
            if (pluginSavedStates != null) state = pluginSavedStates[i]
            loadTimes[i] = System.nanoTime()
            pluginList[i].onCreate(state)
            loadTimes[i] = System.nanoTime() - loadTimes[i] //Elapsed
        }

        val elapsed = System.currentTimeMillis() - now
        Grove.tag(LC_TAG).d { "┌ Activity with $pluginCount plugins loaded in $elapsed ms" }
        Grove.tag(LC_TAG).d { "├──────────────────────────────────────────" }
        for (i in 0..pluginCount - 1) {
            val plugin = pluginList[i]
            var boxChar = "├"
            if (plugin === pluginList[pluginCount - 1]) {
                boxChar = "└"
            }
            Grove.tag(LC_TAG)
                    .d {
                        "$boxChar %${plugin.javaClass.simpleName} " +
                                "- %${loadTimes[i] / 10000000} ms"
                    }
        }
    }

    ////////////////////////////////////////////////////////
    // Life-Cycle
    ////////////////////////////////////////////////////////

    public override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Grove.tag(LC_TAG).d { "onPostCreate" }
        pluginList.forEach(Plugin::onPostCreate)
        pluginList.forEach(Plugin::onPluginsCreated)
    }

    override fun onStart() {
        super.onStart()
        Grove.tag(LC_TAG).d { "onStart" }
        pluginList.forEach(Plugin::onStart)
    }

    override fun onResume() {
        super.onResume()
        Grove.tag(LC_TAG).d { "onResume" }
        pluginList.forEach(Plugin::onResume)
    }

    override fun onPause() {
        super.onPause()
        Grove.tag(LC_TAG).d { "onPause" }
        pluginList.forEach(Plugin::onPause)
    }

    override fun onStop() {
        super.onStop()
        Grove.tag(LC_TAG).d { "onStop" }
        pluginList.forEach(Plugin::onStop)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Grove.tag(LC_TAG).d { "onSaveInstanceState" }
        val states = ArrayList<Bundle>(pluginList.size)
        pluginList.forEach { plugin ->
            val pluginBundle = Bundle()
            plugin.onSaveInstanceState(pluginBundle)
            states.add(pluginBundle)
        }
        outState.putParcelableArrayList(ARG_PLUGIN_SAVED_STATES, states)
    }

    override fun onDestroy() {
        super.onDestroy()
        Grove.tag(LC_TAG).d { "onDestroy" }
        pluginList.forEach(Plugin::onDestroy)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Grove.tag(LC_TAG).d { "onConfigurationChanged" }
        pluginList.forEach { it.onConfigurationChanged(newConfig) }
    }

    ////////////////////////////////////////////////////////
    // Results
    ////////////////////////////////////////////////////////

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultCallback.set(ActivityResult(requestCode, resultCode, data), null)
        pluginList.forEach { it.onActivityResult(onActivityResultCallback) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onPermissionsResultCallback.set(RequestPermissionResult(requestCode, permissions, grantResults), null)
        pluginList.forEach { it.onRequestPermissionsResult(onPermissionsResultCallback) }
    }

    ////////////////////////////////////////////////////////
    // Hardware keys and touch
    ////////////////////////////////////////////////////////

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        Grove.tag(TAG).d { "onKeyUp [%$event]" }
        sharedKeyUpCallback.set(event, false)
        for (plugin in pluginList) {
            plugin.onKeyUp(sharedKeyUpCallback)
        }

        if (sharedKeyUpCallback.consumed) return sharedKeyUpCallback.returnValue
        else return super.onKeyUp(keyCode, event)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        Grove.tag(TAG).d { "onKeyDown [$event]" }
        sharedKeyDownCallback.set(event, false)
        for (plugin in pluginList) {
            plugin.onKeyDown(sharedKeyDownCallback)
        }
        if (sharedKeyDownCallback.consumed) return sharedKeyDownCallback.returnValue
        else return super.onKeyDown(keyCode, event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        sharedTouchCallback.set(ev, false)
        for (plugin in pluginTouchList) {
            plugin.onDispatchTouchEvent(sharedTouchCallback)
        }
        if (sharedTouchCallback.consumed) return sharedTouchCallback.returnValue
        return super.dispatchTouchEvent(ev)
    }

    override fun onBackPressed() {
        Grove.tag(TAG).d { "onBackPressed" }
        sharedBackEvent.set(null, null)
        for (plugin in pluginBackList) {
            plugin.onBackPressed(sharedBackEvent)
        }
        if (!sharedBackEvent.consumed) super.onBackPressed()
    }

    data class ActivityResult(val requestCode: Int,
                              val resultCode: Int,
                              val data: Intent?)

    @Suppress("ArrayInDataClass") //This is safe
    data class RequestPermissionResult(val requestCode: Int,
                                       val permissions: Array<out String>,
                                       val grantResults: IntArray)
}

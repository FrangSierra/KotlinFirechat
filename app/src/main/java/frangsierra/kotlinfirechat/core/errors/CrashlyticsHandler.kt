package frangsierra.kotlinfirechat.core.errors

import com.crashlytics.android.Crashlytics

class CrashlyticsHandler(val defaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler) : Thread.UncaughtExceptionHandler {

    val runtime by lazy { Runtime.getRuntime() }

    override fun uncaughtException(thread: Thread?, ex: Throwable?) {
        // Our custom logic goes here. For example calculate the memory heap
        val maxMemory = runtime.maxMemory()
        val freeMemory = runtime.freeMemory()
        val usedMemory = runtime.totalMemory() - freeMemory
        val availableMemory = maxMemory - usedMemory

        //Set values to Crashlytics
        Crashlytics.setLong("used_memory", usedMemory)
        Crashlytics.setLong("available_memory", availableMemory)

        // This will make Crashlytics do its job
        defaultUncaughtExceptionHandler.uncaughtException(thread, ex)
    }
}
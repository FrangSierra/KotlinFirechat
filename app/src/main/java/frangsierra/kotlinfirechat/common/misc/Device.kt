package frangsierra.kotlinfirechat.common.misc

import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.os.Build
import java.lang.reflect.Modifier


/**
 * Collect all fields of [Build] into a String.
 */
fun collectDeviceBuildInformation(context: Context): String {
    val sb = StringBuilder()
    sb.append(scanStaticFields(Build::class.java))
            .append(scanStaticFields(Build.VERSION::class.java))

    val activityManager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager?
    if (activityManager != null) {
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        sb.append("RAM: ").append(humanFileSize(memoryInfo.totalMem, false))
    }

    return sb.toString()
}

private fun scanStaticFields(clazz: Class<*>): String {
    val sb = StringBuilder()
    for (field in clazz.fields) {
        //Skip non-public or deprecated fields
        if (!Modifier.isPublic(field.modifiers)) continue
        if (field.getAnnotation(Deprecated::class.java) != null) continue
        try {
            val value = field.get(null) ?: continue
            if (value.javaClass.isArray) continue
            if (value.toString().trim { it <= ' ' }.isEmpty()) continue
            sb.append(field.name).append(": ").append(value.toString()).append("\n")
        } catch (ignored: Exception) {
            //Nothing to do
        }
    }
    return sb.toString()
}



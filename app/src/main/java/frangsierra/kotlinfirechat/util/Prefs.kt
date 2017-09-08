package gg.grizzlygrit.util

import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    val PREFS_FILENAME = "gg.grizzlygrit.prefs"
    val TUTORIAL_DONE = "tutorial_done"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var tutorialDone: Boolean
        get() = prefs.getBoolean(TUTORIAL_DONE, false)
        set(value) = prefs.edit().putBoolean(TUTORIAL_DONE, value).apply()
}

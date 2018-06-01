package frangsierra.kotlinfirechat.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    val PREFS_FILENAME = "kotlinfirechat.prefs"
    private val LOGGED_USER = "logged_user"
    private val LOGGED_USERNAME = "logged_username"
    val prefs: SharedPreferences = context.getSharedPreferences(PREFS_FILENAME, 0)

    var loggedUserId: String?
        get() {
            return prefs.getString(LOGGED_USER, null)
        }
        @SuppressLint("ApplySharedPref")
        set(value) {
            prefs.edit().putString(LOGGED_USER, value).commit()
        }

    var loggedUsername: String?
        get() {
            return prefs.getString(LOGGED_USERNAME, null)
        }
        @SuppressLint("ApplySharedPref")
        set(value) {
            prefs.edit().putString(LOGGED_USERNAME, value).commit()
        }
}

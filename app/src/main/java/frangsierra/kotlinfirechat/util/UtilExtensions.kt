package frangsierra.kotlinfirechat.util

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.widget.Toast

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun TextInputLayout.onError(errorText: String? = null, enable: Boolean = true) {
    isErrorEnabled = enable
    error = errorText
}
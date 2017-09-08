package frangsierra.kotlinfirechat.util

import android.content.Context
import android.support.design.widget.TextInputLayout
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import frangsierra.kotlinfirechat.R

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun TextInputLayout.onError(errorText: String? = null, enable: Boolean = true) {
    isErrorEnabled = enable
    error = errorText
}

fun Throwable.tryToGetLoginMessage(): Int {
    if (this is FirebaseAuthWeakPasswordException) {
        return R.string.error_weak_password
    } else if (this is FirebaseAuthInvalidCredentialsException) {
        return R.string.error_invalid_password
    } else if (this is FirebaseAuthUserCollisionException) {
        return R.string.error_email_already_exist
    } else if (this is FirebaseAuthInvalidUserException) {
        return R.string.error_invalid_account
    } else {
        return R.string.error_unknown
    }
}

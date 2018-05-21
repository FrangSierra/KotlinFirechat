package frangsierra.kotlinfirechat.util

import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import frangsierra.kotlinfirechat.R
import kotlinx.android.synthetic.main.progress_dialog_layout.view.*


/** [DialogFragment] displayed in the app for default purposes. */
private var customProgressDialog: AlertDialog? = null

fun AppCompatActivity.showProgressDialog(message: String) {
    val progressDialog = LayoutInflater.from(this)
            .inflate(R.layout.progress_dialog_layout, null)
    progressDialog.dialog_title.text = message
    customProgressDialog = AlertDialog.Builder(this)
            .createCustomAlertDialog(view = progressDialog, cancelable = false)

    customProgressDialog?.show()
}

fun AppCompatActivity.dismissProgressDialog() {
    customProgressDialog?.dismiss()
    customProgressDialog = null
}

fun Fragment.showProgressDialog(message: String) {
    (activity as? AppCompatActivity)?.showProgressDialog(message)
}

fun Fragment.dismissProgressDialog() {
    (activity as? AppCompatActivity)?.dismissProgressDialog()
}
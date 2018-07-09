package frangsierra.kotlinfirechat.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.design.widget.TextInputLayout
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import frangsierra.kotlinfirechat.R
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

const val TC_REQUEST_GALLERY: Int = 101
const val TC_REQUEST_CAMERA: Int = 102
const val INTENT_TARGET_TYPE: String = "image/*"
const val APP_IMAGE_FOLDER_PATH: String = "firechat/"
val JPG = ".jpg"

fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun TextInputLayout.onError(errorText: String? = null, enable: Boolean = true) {
    isErrorEnabled = enable
    error = errorText
}

fun RecyclerView.setLinearLayoutManager(context: Context, reverseLayout: Boolean = true, stackFromEnd: Boolean = true) {
    val linearLayoutManager = LinearLayoutManager(context)
    linearLayoutManager.reverseLayout = reverseLayout
    linearLayoutManager.stackFromEnd = stackFromEnd
    layoutManager = linearLayoutManager
}

fun Activity.showImageIntentDialog(outputFileUri: Uri) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setTitle(getString(R.string.choose_image_picker_text))
    builder.setItems(arrayOf(getString(R.string.gallery_text), getString(R.string.camera_text))) { _, which ->
        when (which) {
            0 -> {
                // GET IMAGE FROM THE GALLERY
                val chooser = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                    type = INTENT_TARGET_TYPE
                }, getString(R.string.choose_image_picture_text))
                startActivityForResult(chooser, TC_REQUEST_GALLERY)
            }
            1 -> {
                val cameraFolder: File = if (Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED)
                    File(Environment.getExternalStorageDirectory(), APP_IMAGE_FOLDER_PATH)
                else cacheDir

                if (!cameraFolder.exists())
                    cameraFolder.mkdirs()

                val getCameraImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                getCameraImage.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)

                startActivityForResult(getCameraImage, TC_REQUEST_CAMERA)
            }
        }
    }
    builder.show()
}

fun Long.getTimeAgoText(): String {
    val differenceTimeStamp = System.currentTimeMillis() - this
    val days = TimeUnit.MILLISECONDS.toDays(differenceTimeStamp)
    val hours = TimeUnit.MILLISECONDS.toHours(differenceTimeStamp)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(differenceTimeStamp)
    val formatString = "%d %s"
    return when {
        days.div(375) > 0 -> formatString.format(Locale.getDefault(), days.div(375), "y")
        days.div(30) > 0 -> formatString.format(Locale.getDefault(), days.div(30), "mon")
        days.div(7) > 0 -> formatString.format(Locale.getDefault(), days.div(7), "wk")
        days > 0 -> formatString.format(days, "d")
        hours > 0 -> formatString.format(hours, "h")
        minutes > 0 -> formatString.format(minutes, "min")
        else -> formatString.format(Locale.getDefault(), TimeUnit.MILLISECONDS.toSeconds(differenceTimeStamp).plus(1), "s")
    }
}

fun Context.generateUniqueFireUri(): Uri {
    // Determine Uri of camera image to save.
    //todo change name to new app
    val root = File(Environment.getExternalStorageDirectory().toString() + File.separator + "KotlinFirechat" + File.separator)
    root.mkdirs()
    val fileName = "${System.currentTimeMillis()}$JPG"
    val sdImageMainDirectory = File(root, fileName)
    return FileProvider.getUriForFile(this, "$packageName.provider", sdImageMainDirectory)
}

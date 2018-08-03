package frangsierra.kotlinfirechat.util

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import frangsierra.kotlinfirechat.R
import java.io.File

object AndroidUtils {
    private val INTENT_TARGET_TYPE: String = "image/*"
    private val APP_IMAGE_FOLDER_PATH: String = "kotlin_chat/"
    val TC_REQUEST_GALLERY: Int = 101
    val TC_REQUEST_CAMERA: Int = 102
    val FORMAT_JPG = ".jpg"

    /**
     * Create an alertdialog which allows the user to choose between pick an image from the gallery or
     * take a new one with his camera.
     */
    fun showImageIntentDialog(activity: Activity, outputFileUri: Uri) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
        builder.setTitle(activity.getString(R.string.choose_image_picker_text))
        builder.setItems(arrayOf(activity.getString(R.string.gallery_text), activity.getString(R.string.camera_text))) { _, which ->
            when (which) {
                0 -> {
                    // GET IMAGE FROM THE GALLERY
                    val chooser = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = INTENT_TARGET_TYPE
                    }, activity.getString(R.string.choose_image_picture_text))
                    activity.startActivityForResult(chooser, TC_REQUEST_GALLERY)
                }
                1 -> {
                    val cameraFolder: File = if (Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED)
                        File(Environment.getExternalStorageDirectory(), APP_IMAGE_FOLDER_PATH)
                    else activity.cacheDir

                    if (!cameraFolder.exists())
                        cameraFolder.mkdirs()

                    val getCameraImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    getCameraImage.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)

                    activity.startActivityForResult(getCameraImage, TC_REQUEST_CAMERA)
                }
            }
        }
        builder.show()
    }

    /**
     * Create an alertdialog which allows the user to choose between pick an image from the gallery or
     * take a new one with his camera.
     */
    fun showImageIntentDialogFromFragment(fragment: Fragment, outputFileUri: Uri, cancelListener: (DialogInterface) -> Unit = {}) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(fragment.activity!!)
        builder.setTitle(fragment.getString(R.string.choose_image_picker_text))
        builder.setOnCancelListener { cancelListener(it) }
        builder.setCancelable(true)
        builder.setItems(arrayOf(fragment.getString(R.string.gallery_text), fragment.getString(R.string.camera_text))) { _, which ->
            when (which) {
                0 -> {
                    // GET IMAGE FROM THE GALLERY
                    val chooser = Intent.createChooser(Intent(Intent.ACTION_GET_CONTENT).apply {
                        type = INTENT_TARGET_TYPE
                    }, fragment.getString(R.string.choose_image_picture_text))
                    fragment.startActivityForResult(chooser, TC_REQUEST_GALLERY)
                }
                1 -> {
                    val cameraFolder: File = if (Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED)
                        File(Environment.getExternalStorageDirectory(), APP_IMAGE_FOLDER_PATH)
                    else fragment.activity!!.cacheDir

                    if (!cameraFolder.exists())
                        cameraFolder.mkdirs()

                    val getCameraImage = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    getCameraImage.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)

                    fragment.startActivityForResult(getCameraImage, TC_REQUEST_CAMERA)
                }
            }
        }
        builder.show()
    }

    fun generateUniqueFireUri(context: Context): Uri {
        // Determine Uri of camera image to save.
        val root = File(Environment.getExternalStorageDirectory().toString() + File.separator + "GrizzlyGrit" + File.separator)
        root.mkdirs()
        val fileName = "${System.currentTimeMillis()}$FORMAT_JPG"
        val sdImageMainDirectory = File(root, fileName)
        return FileProvider.getUriForFile(context, "${context.packageName}.provider", sdImageMainDirectory)
    }
}
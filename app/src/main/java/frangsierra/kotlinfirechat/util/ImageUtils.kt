package frangsierra.kotlinfirechat.util

import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.net.Uri
import frangsierra.kotlinfirechat.core.flux.app
import io.reactivex.Single
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException

object ImageUtils {
    private val MAX_THUMB_SIZE = 150
    private val POST_MAX_IMAGE_SIZE = 1080

    fun resizeUriToThumbnail(uri: Uri, isUrl: Boolean = false): Single<ByteArray> {
        return Single.create { emitter ->
            try {
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true

                val stream = if (isUrl) java.net.URL(uri.toString()).openStream() else app.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(stream, null, o)

                var width_tmp = o.outWidth
                var height_tmp = o.outHeight
                var scale = 1

                while (true) {
                    if (width_tmp / 2 < MAX_THUMB_SIZE || height_tmp / 2 < MAX_THUMB_SIZE)
                        break
                    width_tmp /= 2
                    height_tmp /= 2
                    scale *= 2
                }

                val o2 = BitmapFactory.Options()
                o2.inSampleSize = scale
                val resizedStream = if (isUrl) java.net.URL(uri.toString()).openStream() else app.contentResolver.openInputStream(uri)
                val decodedResizedBitmap = BitmapFactory.decodeStream(resizedStream, null, o2)
                val bos = ByteArrayOutputStream()
                decodedResizedBitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
                emitter.onSuccess(bos.toByteArray())
            } catch (e: FileNotFoundException) {
                emitter.onError(e)
            }
        }
    }

    fun resizeUriToPostImage(uri: Uri, isUrl: Boolean = false): Single<ByteArray> {
        return Single.create { emitter ->
            try {
                val o = BitmapFactory.Options()
                o.inJustDecodeBounds = true

                val stream = if (isUrl) java.net.URL(uri.toString()).openStream() else app.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(stream, null, o)

                var width_tmp = o.outWidth
                var height_tmp = o.outHeight
                var scale = 1

                while (true) {
                    if (width_tmp < POST_MAX_IMAGE_SIZE || height_tmp < POST_MAX_IMAGE_SIZE)
                        break
                    width_tmp /= 2
                    height_tmp /= 2
                    scale *= 2
                }

                val o2 = BitmapFactory.Options()
                o2.inSampleSize = scale
                val resizedStream = if (isUrl) java.net.URL(uri.toString()).openStream() else app.contentResolver.openInputStream(uri)
                val decodedResizedBitmap = BitmapFactory.decodeStream(resizedStream, null, o2)
                val bos = ByteArrayOutputStream()
                decodedResizedBitmap.compress(CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
                emitter.onSuccess(bos.toByteArray())
            } catch (e: FileNotFoundException) {
                emitter.onError(e)
            }
        }
    }

}
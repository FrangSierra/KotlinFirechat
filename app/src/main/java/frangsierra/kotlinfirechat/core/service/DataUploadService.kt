package frangsierra.kotlinfirechat.core.service

import android.net.Uri
import android.os.Bundle
import com.firebase.jobdispatcher.*
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import frangsierra.kotlinfirechat.core.firebase.chatStorageMessageRef
import frangsierra.kotlinfirechat.core.firebase.messageDoc
import frangsierra.kotlinfirechat.util.ImageUtils.resizeUriToPostImage
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import mini.log.Grove

private const val MESSAGE_ID_SERVICE_KEY = "message_service_id"
private const val USER_ID_SERVICE_KEY = "user_service_id"
private const val UPLOAD_FILE_URL_KEY = "file_url_id"

class DataUploadService : JobService() {
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val firebaseFirestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onStartJob(job: JobParameters): Boolean {
        Grove.d { "Crash reporting job called ${job.tag}" }

        val extras = job.extras ?: throw NullPointerException("Bundle can't be null")

        val messageID = extras.getString(MESSAGE_ID_SERVICE_KEY)
        val userId = extras.getString(USER_ID_SERVICE_KEY)
        val fileUri = Uri.parse(extras.getString(UPLOAD_FILE_URL_KEY))

        uploadFile(job, messageID, userId, fileUri)
        return true
    }

    private fun uploadFile(job: JobParameters, messageId: String, userId: String, fileUrl: Uri) {
        resizeUriToPostImage(fileUrl).flatMap { bytes -> putBytes(firebaseStorage.chatStorageMessageRef(userId, messageId), bytes) }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        { task ->
                            try {
                                val uri = Tasks.await(task.storage.downloadUrl)
                                Tasks.await(firebaseFirestore.messageDoc(messageId).update("attachedImageUrl", uri.toString()))
                                jobFinished(job, false)
                            } catch (e: Exception) {
                                jobFinished(job, true)
                            }
                        }, {
                    jobFinished(job, true)
                })
    }

    /**
     * onStopJob is only called when we manually cancel the current jobs,
     * this happens when the user disconnects his wifi network.
     */
    override fun onStopJob(job: JobParameters): Boolean {
        Grove.d { "Crash reporting job stopped ${job.tag}" }
        //onStop should return true to re-schedule the canceled job.
        return true
    }
}

fun buildUploadJob(uri: String, userId: String, messageId: String, builder: Job.Builder): Job {
    val myExtrasBundle = Bundle()

    myExtrasBundle.putString(MESSAGE_ID_SERVICE_KEY, messageId)
    myExtrasBundle.putString(USER_ID_SERVICE_KEY, userId)
    myExtrasBundle.putString(UPLOAD_FILE_URL_KEY, uri)

    return builder
            .setService(DataUploadService::class.java) // the JobService that will be called
            .setTag("$messageId-$userId")        // uniquely identifies the job
            .setRecurring(false) // one-off job
            .setLifetime(Lifetime.FOREVER) // don't persist past a device reboot
            .setTrigger(Trigger.NOW) // start immediately
            .setReplaceCurrent(true)  // don't overwrite an existing job with the same tag
            .setRetryStrategy(RetryStrategy.DEFAULT_LINEAR) // retry with linear backoff
            .setExtras(myExtrasBundle)
            .build()
}

internal fun putBytes(storageRef: StorageReference, bytes: ByteArray): Single<UploadTask.TaskSnapshot> {
    return Single.create { emitter ->
        val taskSnapshotStorageTask = storageRef.putBytes(bytes)
                .addOnSuccessListener { taskSnapshot -> emitter.onSuccess(taskSnapshot) }
                .addOnFailureListener { e ->
                    if (!emitter.isDisposed) {
                        emitter.onError(e)
                    }
                }
        emitter.setCancellable { taskSnapshotStorageTask.cancel() }
    }
}

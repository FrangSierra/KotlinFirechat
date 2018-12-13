package frangsierra.kotlinfirechat.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import com.crashlytics.android.Crashlytics
import com.tbruyelle.rxpermissions2.RxPermissions
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.chat.store.ChatStore
import frangsierra.kotlinfirechat.chat.store.SendMessageAction
import frangsierra.kotlinfirechat.chat.store.StartListeningChatMessagesAction
import frangsierra.kotlinfirechat.chat.store.StopListeningChatMessagesAction
import frangsierra.kotlinfirechat.core.errors.ErrorHandler
import frangsierra.kotlinfirechat.core.flux.FluxActivity
import frangsierra.kotlinfirechat.profile.store.ProfileStore
import frangsierra.kotlinfirechat.session.LoginActivity
import frangsierra.kotlinfirechat.util.*
import kotlinx.android.synthetic.main.home_activity.*
import mini.TaskStatus
import javax.inject.Inject

class HomeActivity : FluxActivity() {

    @Inject
    lateinit var profileStore: ProfileStore
    @Inject
    lateinit var chatStore: ChatStore
    @Inject
    lateinit var errorHandler: ErrorHandler

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, HomeActivity::class.java)
    }

    private val messageAdapter = MessageAdapter()
    private var outputFileUri: Uri? = null
    //FIXME inject me if it's used somewhere else
    private val rxPermissionInstance: RxPermissions by lazy { RxPermissions(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity)
        initializeInterface()
        startListeningStoreChanges()
    }

    private fun initializeInterface() {
        messageRecycler.setLinearLayoutManager(this, reverseLayout = false, stackFromEnd = false)
        messageRecycler.adapter = messageAdapter
        sendButton.setOnClickListener { sendMessage() }
        photoPickerButton.setOnClickListener { requestPermissionsAndPickImage() }
    }

    private fun startListeningStoreChanges() {
        profileStore.flowable()
            .view { it.loadProfileTask }
            .subscribe {
                when (it.status) {
                    TaskStatus.RUNNING -> showProgressDialog("Loading user profile")
                    TaskStatus.SUCCESS -> dismissProgressDialog()
                    TaskStatus.ERROR -> goToLogin()
                }
            }.track()

        chatStore.flowable()
            .view { it.messages }
            .filter { it.isNotEmpty() }
            .subscribe { messageAdapter.updateMessages(it.values.toList()) }
            .track()
    }

    private fun sendMessage() {
        if (messageEditText.text.isEmpty()) {
            toast("You should add a text")
            return
        }
        sendButton.isEnabled = false
        dispatcher.dispatch(SendMessageAction(messageEditText.text.toString(), outputFileUri))
        messageEditText.text.clear()
        chatStore.flowable()
            .filterOne { it.sendMessageTask.isTerminal() } //Wait for request to finish
            .subscribe {
                if (it.sendMessageTask.isFailure()) {
                    errorHandler.handle(it.sendMessageTask.error)
                    toast("There was an error sending your message")
                }
                sendButton.isEnabled = true
            }.track()
    }

    private fun goToLogin() {
        dismissProgressDialog()
        val intent = LoginActivity.newIntent(this).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

    private fun requestPermissionsAndPickImage() {
        rxPermissionInstance.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
            .subscribe {
                if (it) {
                    outputFileUri = AndroidUtils.generateUniqueFireUri(this)
                    AndroidUtils.showImageIntentDialog(this, outputFileUri!!)
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                TC_REQUEST_GALLERY, TC_REQUEST_CAMERA ->
                    if (resultCode == Activity.RESULT_OK) {
                        val uri = if (requestCode == TC_REQUEST_GALLERY) data?.data else outputFileUri
                        if (uri == null) {
                            Crashlytics.log("Uri was null when updating profile picture and using code $resultCode")
                            toast(getString(R.string.error_picture_null))
                            return
                        }
                        onImageReady()
                    }
            }
        }
    }

    private fun onImageReady() {
        toast("your image have been attached")
        //TODO move to selector
        photoPickerButton.setColorFilter(ContextCompat.getColor(this, R.color.image_picked_color),
            android.graphics.PorterDuff.Mode.MULTIPLY)
    }

    override fun onStart() {
        super.onStart()
        dispatcher.dispatch(StartListeningChatMessagesAction())
    }

    override fun onStop() {
        super.onStop()
        dispatcher.dispatch(StopListeningChatMessagesAction())
    }
}
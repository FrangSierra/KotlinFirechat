package frangsierra.kotlinfirechat.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.chat.store.ChatStore
import frangsierra.kotlinfirechat.chat.store.SendMessageAction
import frangsierra.kotlinfirechat.chat.store.StartListeningChatMessagesAction
import frangsierra.kotlinfirechat.chat.store.StopListeningChatMessagesAction
import frangsierra.kotlinfirechat.core.flux.FluxActivity
import frangsierra.kotlinfirechat.profile.store.ProfileStore
import frangsierra.kotlinfirechat.session.LoginActivity
import frangsierra.kotlinfirechat.util.*
import kotlinx.android.synthetic.main.home_activity.*
import javax.inject.Inject

class HomeActivity : FluxActivity() {

    @Inject
    lateinit var profileStore: ProfileStore
    @Inject
    lateinit var chatStore: ChatStore

    companion object {
        fun newIntent(context: Context): Intent =
                Intent(context, HomeActivity::class.java)
    }

    private val messageAdapter = MessageAdapter()

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
    }

    private fun startListeningStoreChanges() {
        profileStore.flowable()
                .view { it.loadProfileTask }
                .subscribe {
                    when (it.status) {
                        TypedTask.Status.RUNNING -> showProgressDialog("Loading user profile")
                        TypedTask.Status.SUCCESS -> dismissProgressDialog()
                        TypedTask.Status.FAILURE -> goToLogin()
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
        dispatcher.dispatchOnUi(SendMessageAction(messageEditText.text.toString()))
        messageEditText.text.clear()
        chatStore.flowable()
                .filterOne { it.sendMessageTask.isTerminal() } //Wait for request to finish
                .subscribe {
                    if (it.sendMessageTask.isFailure()) {
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

    override fun onStart() {
        super.onStart()
        dispatcher.dispatch(StartListeningChatMessagesAction())
    }

    override fun onStop() {
        super.onStop()
        dispatcher.dispatch(StopListeningChatMessagesAction())
    }
}
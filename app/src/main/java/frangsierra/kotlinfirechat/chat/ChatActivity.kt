package frangsierra.kotlinfirechat.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.tbruyelle.rxpermissions2.RxPermissions
import frangsierra.kotlinfirechat.ProfileActivity
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.common.dagger.AppComponent
import frangsierra.kotlinfirechat.common.dagger.AppComponentFactory
import frangsierra.kotlinfirechat.common.flux.FluxActivity
import frangsierra.kotlinfirechat.session.SignOutAction
import frangsierra.kotlinfirechat.util.TC_REQUEST_CAMERA
import frangsierra.kotlinfirechat.util.TC_REQUEST_GALLERY
import frangsierra.kotlinfirechat.util.generateUniqueFireUri
import frangsierra.kotlinfirechat.util.showImageIntentDialog
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class ChatActivity : FluxActivity<AppComponent>() {
    override fun onCreateComponentFactory() = AppComponentFactory

    @Inject lateinit var chatStore: ChatStore
    private var outputFileUri: Uri? = null
    private val rxPermissionInstance: RxPermissions by lazy { RxPermissions(this) }
    private val adapter: MessageAdapter = MessageAdapter()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeInterface()
        startListeningStoreChanges()
    }

    private fun initializeInterface() {

        messageRecycler.layoutManager = LinearLayoutManager(this)
        messageRecycler.adapter = adapter

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sendButton.isEnabled = !s.toString().trim().isEmpty()
            }
        })

        sendButton.setOnClickListener {
                dispatcher.dispatch(SendMessageAction(messageEditText.text.toString(), outputFileUri))
                messageEditText.setText("")
        }
        photoPickerButton.setOnClickListener {
            rxPermissionInstance.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe {
                    if (it) {
                        outputFileUri = generateUniqueFireUri()
                        showImageIntentDialog(outputFileUri!!)
                    }
                }
        }
    }

    private fun startListeningStoreChanges(){
        chatStore.flowable()
            .map { it.messagesData }
            .distinctUntilChanged()
            .subscribe {
                if (it.isEmpty()){
                    //TODO manage no messages
                } else {
                    adapter.updateMessages(it.toList())
                }
            }.track()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                TC_REQUEST_GALLERY ->
                    if (resultCode == RESULT_OK) {
                        outputFileUri = data!!.data
                        //TODO use a custom XML with an selected tint.
                        photoPickerButton.setColorFilter(ContextCompat.getColor(this, R.color.image_picked_color),
                            android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
                TC_REQUEST_CAMERA ->
                    if (resultCode == RESULT_OK) {
                        photoPickerButton.setColorFilter(ContextCompat.getColor(this, R.color.image_picked_color),
                            android.graphics.PorterDuff.Mode.MULTIPLY);
                    }
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.action_logout -> dispatcher.dispatch(SignOutAction())
        }
        return true
    }
}

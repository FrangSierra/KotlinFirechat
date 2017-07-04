package frangsierra.kotlinfirechat

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import kotlinx.android.synthetic.main.activity_main.*

class ChatActivity : AppCompatActivity() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val adapter: MessageAdapter = MessageAdapter()
    private val currentUser by lazy { auth.currentUser }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
            currentUser?.let {
                val messageToSend = Message(messageEditText.text.toString(), it.displayName!!)
                val newKeyForMessage = FirebaseConstants.MESSAGE_DATA_REFERENCE.push()
                FirebaseConstants.MESSAGE_DATA_REFERENCE.child(newKeyForMessage.key).setValue(messageToSend)
                messageEditText.setText("")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.chat_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
            R.id.action_logout -> {
                auth.signOut(); startActivity(Intent(this, LoginActivity::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK })
            }
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        FirebaseConstants.MESSAGE_DATA_REFERENCE.addChildEventListener(childEventLister)
    }

    override fun onStop() {
        super.onStop()
        FirebaseConstants.MESSAGE_DATA_REFERENCE.removeEventListener(childEventLister)
    }

    private val childEventLister: ChildEventListener = object : ChildEventListener {
        override fun onCancelled(error: DatabaseError) {}

        override fun onChildMoved(snapshot: DataSnapshot, p1: String?) {}

        override fun onChildChanged(snapshot: DataSnapshot, p1: String?) {
            val updatedMessage = snapshot.getValue(Message::class.java)
            adapter.updateMessage(snapshot.key, updatedMessage)
        }

        override fun onChildAdded(snapshot: DataSnapshot, p1: String?) {
            val newMessage = snapshot.getValue(Message::class.java)
            adapter.addMessage(snapshot.key, newMessage)
        }

        override fun onChildRemoved(snapshot: DataSnapshot) {}

    }
}

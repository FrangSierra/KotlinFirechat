package frangsierra.kotlinfirechat

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants.PEOPLE_TABLE_SECONDNAME
import frangsierra.kotlinfirechat.common.firebase.FirebaseConstants.PEOPLE_TABLE_USERNAME
import frangsierra.kotlinfirechat.common.firebase.User
import kotlinx.android.synthetic.main.profile_settings.*

class ProfileActivity : AppCompatActivity() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val userListener: ValueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError?) {}

        override fun onDataChange(snapshot: DataSnapshot) {
            val currentUserValues = snapshot.getValue(User::class.java)
            currentUserValues?.let {
                userNameEditText.setText(it.userName)
                secondNameEditText.setText(it.secondName)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_settings)

        updateButton.setOnClickListener {
            if (userNameEditText.text.isEmpty()) return@setOnClickListener
            FirebaseConstants.USER_PROFILE_DATA_REFERENCE
                    .child(auth.currentUser!!.uid)
                    .child(PEOPLE_TABLE_USERNAME)
                    .setValue(userNameEditText.text.toString())

            if (secondNameEditText.text.isEmpty()) return@setOnClickListener
            FirebaseConstants.USER_PROFILE_DATA_REFERENCE
                    .child(auth.currentUser!!.uid)
                    .child(PEOPLE_TABLE_SECONDNAME)
                    .setValue(secondNameEditText.text.toString())
        }

    }

    override fun onStop() {
        super.onStop()
        FirebaseConstants.USER_PROFILE_DATA_REFERENCE
                .child(auth.currentUser!!.uid).removeEventListener(userListener)
    }

    override fun onStart() {
        super.onStart()
        FirebaseConstants.USER_PROFILE_DATA_REFERENCE
                .child(auth.currentUser!!.uid).addValueEventListener(userListener)
    }
}
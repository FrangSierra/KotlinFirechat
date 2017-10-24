package frangsierra.kotlinfirechat.messaging

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import frangsierra.kotlinfirechat.R
import frangsierra.kotlinfirechat.chat.ChatActivity
import frangsierra.kotlinfirechat.common.dagger.AppScope
import frangsierra.kotlinfirechat.common.log.Grove
import javax.inject.Inject

@AppScope
class MessagingController @Inject constructor() : FirebaseMessagingService(){
    private val MESSAGING_ID = 1001

    private val notificationManager by lazy { getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Grove.d { String.format("From: %s", remoteMessage!!.from) }

        if (remoteMessage != null && remoteMessage.data.isNotEmpty()) {
            Grove.d { String.format("Message data payload: %s", remoteMessage.data) }

            val pendingIntent = TaskStackBuilder.create(this)
                // add all of ChatActivity's parents to the stack,
                // followed by ChatActivity itself
                .addNextIntentWithParentStack(Intent(this, ChatActivity::class.java))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

            val notificationBuilder = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.leak_canary_icon)
                .setContentTitle("Uplogandi mensajes")
                .setContentText("Puede que tengas mensajes nuevos")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            notificationManager.notify(MESSAGING_ID, notificationBuilder.build())
        }
    }
}
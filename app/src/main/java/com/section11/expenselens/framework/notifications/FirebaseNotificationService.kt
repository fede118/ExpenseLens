package com.section11.expenselens.framework.notifications

import android.app.NotificationManager
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getActivity
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.section11.expenselens.R
import com.section11.expenselens.ui.MainActivity
import javax.inject.Inject

private const val CHANNEL = "INVITE_CHANNEL"

class FirebaseNotificationService @Inject constructor(): FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // todo check if need to update this on firestore
    }

    /**
     * For now just navigating to the home screen.
     * in the future we can:
     * val screen = data["screen"]
     * val householdId = data["householdId"]
     *
     * and put those extras in the bundle:
     * putExtra("screen", screen)
     * putExtra("householdId", householdId)
     *
     * to actually do something to show the accept/refuse household invite.
     *
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data.let {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = getActivity(
                this, 0, intent, FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )

            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL)
                .setSmallIcon(R.drawable.ic_launcher_round)
                .setContentTitle(remoteMessage.notification?.title)
                .setContentText(remoteMessage.notification?.body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(0, notificationBuilder.build())
        }
    }
}

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
import com.section11.expenselens.domain.usecase.NotificationsTokenUpdateUseCase
import com.section11.expenselens.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject


private const val CHANNEL = "INVITE_CHANNEL"

@AndroidEntryPoint
class FirebaseNotificationService : FirebaseMessagingService() {

    private val serviceJob = Job()

    @Inject lateinit var notificationsTokenUpdateUseCase: NotificationsTokenUpdateUseCase
    @Inject lateinit var dispatcher: CoroutineDispatcher
    private lateinit var serviceScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        serviceScope = CoroutineScope(dispatcher + serviceJob)
    }

    override fun onNewToken(token: String) {
        serviceScope.launch(dispatcher) {
            notificationsTokenUpdateUseCase.onNewNotificationToken(token)
        }
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

    override fun onDestroy() {
        serviceJob.cancel()
        super.onDestroy()
    }
}

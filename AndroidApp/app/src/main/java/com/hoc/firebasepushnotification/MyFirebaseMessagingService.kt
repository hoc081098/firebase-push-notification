package com.hoc.firebasepushnotification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by Peter Hoc on 10/01/2018.
 */


class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        remoteMessage?.run {
            sendNotification(
                    notification?.title,
                    notification?.body,
                    notification?.clickAction,
                    sentTime,
                    data
            )
        }
    }

    private fun sendNotification(title: String?, body: String?, clickAction: String?, sentTime: Long, data: Map<String, String>) {
        val clazz = try {
            Class.forName(clickAction)
        } catch (e: ClassNotFoundException) {
            Log.d(TAG, e.toString())
            return
        }

        val intent = Intent(this, clazz).apply {
            putExtra(SENDER_ID, data[SENDER_ID])
            putExtra(SENDER_NAME, data[SENDER_NAME])
            putExtra(SENDER_IMAGE_URL, data[SENDER_IMAGE_URL])

            putExtra(BODY, body)
            putExtra(SEND_TIME, sentTime)
        }

        val pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        NotificationCompat.Builder(this, MyApp.MY_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(pendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setWhen(sentTime)
                .setAutoCancel(true)
                .build()
                .let {
                    val id = System.currentTimeMillis().toInt()
                    (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(id, it)
                }
    }

    companion object {
        private const val TAG = "MyFirebaseInstance"
    }
}

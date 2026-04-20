package com.example.firebasecloudmessenging

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val CHANNEL_ID = "fcm_demo_channel"
    private val NOTIFICATION_ID = 1001

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        var title: String
        var body: String

        remoteMessage.notification?.let {
            title = it.title ?: "Nueva notificación"
            body = it.body ?: ""
            showNotification(title, body)
        } ?: run {
            remoteMessage.data.let { data ->
                if (data.isNotEmpty()) {
                    title = data["title"] ?: "Mensaje de datos"
                    body = data["body"] ?: "Contenido del mensaje"
                    showNotification(title, body)
                } else {
                    return
                }
            }
        }

        // Enviar broadcast a la actividad para actualizar UI
        val intent = Intent("com.example.firebasecloudmessenging.NEW_MESSAGE").apply {
            putExtra("title", title)
            putExtra("body", body)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("FCM Token actualizado: $token")
        // Aquí puedes enviar el token a tu servidor
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Canal FCM Demo",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para notificaciones push de FCM"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
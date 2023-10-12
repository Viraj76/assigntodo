package com.example.assigntodo.api

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.example.assigntodo.R
import com.example.assigntodo.models.Notification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class NotificationService : FirebaseMessagingService() {
    val channelId = "Assign To-Do"
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)


        val manager = getSystemService(Context.NOTIFICATION_SERVICE)
        createNotificationChannel(manager as NotificationManager)
        val notification  = NotificationCompat.Builder(this,channelId)
            .setContentTitle(message.data["title"])
            .setContentText(message.data["body"])
            .setAutoCancel(false)
            .setSmallIcon(R.drawable.appicon)
            .setContentIntent(null)
            .build()
        manager.notify(Random.nextInt() , notification)
    }

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(channelId,"assigntodo",NotificationManager.IMPORTANCE_HIGH)
        channel.description = "New Todo"
        channel.enableLights(true)
        notificationManager.createNotificationChannel(channel)
    }
}
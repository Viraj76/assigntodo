package com.example.assigntodo.api

import com.example.assigntodo.models.Notification
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=AAAANgpjT-8:APA91bFAMq-KSqEkWh1Jllo-oMWoMvsegLFKV1Ou-cqDT4-8z4_7DNeMPCHA-boZpks-gXRyyiMQvD_lmF-Dk5W8cQ_oDp852U-WIUD6HzVZwGn7mNBHElnoVbWvD-off7p1f4bY9-ma"
    )

    @POST("fcm/send")
    fun sendNotification(@Body notification : Notification) : Call<Notification>
}
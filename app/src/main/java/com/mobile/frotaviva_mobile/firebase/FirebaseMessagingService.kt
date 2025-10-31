package com.mobile.frotaviva_mobile

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.mobile.frotaviva_mobile.api.ApiService
import com.mobile.frotaviva_mobile.api.RetrofitClient
import com.mobile.frotaviva_mobile.model.TokenRegistrationRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCMService"
    private val CHANNEL_ID = "frotaviva_channel_01"

    override fun onNewToken(token: String) {
        Log.d(TAG, "Novo token gerado: $token")
        sendRegistrationToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(TAG, "Mensagem recebida de: ${remoteMessage.from}")

        remoteMessage.data.isNotEmpty().let {
            val title = remoteMessage.data["title"] ?: remoteMessage.notification?.title ?: "Novo Alerta"
            val body = remoteMessage.data["body"] ?: remoteMessage.notification?.body ?: "Verifique o aplicativo para detalhes."

            showNotification(title, body)
        }
    }

    private fun sendRegistrationToServer(token: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid ?: return

        val request = TokenRegistrationRequest(userId, token)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.registerToken(request)
                if (response.isSuccessful) {
                    Log.i(TAG, "Token FCM registrado com sucesso no backend para userId: $userId")
                } else {
                    Log.e(TAG, "Falha ao registrar token: ${response.code()}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "Erro de rede ao registrar token: ${e.message}")
            } catch (e: HttpException) {
                Log.e(TAG, "Erro HTTP ao registrar token: ${e.message}")
            }
        }
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Notificações de Manutenção e Alertas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal principal para alertas críticos e manutenções."
                enableLights(true)
                lightColor = Color.RED
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}

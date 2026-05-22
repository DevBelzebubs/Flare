package com.social.flare.features.notifications.data.remote

import android.util.Log
import okhttp3.*
import org.json.JSONObject

class NotificationWebSocketClient(
    private val client: OkHttpClient,
    private val onNotificationReceived: (JSONObject) -> Unit
) {
    private var webSocket: WebSocket? = null
    private val WEBSOCKET_URL = "ws://aun-no-hay-backend.com/notifications"
    fun connect(userId: String) {
        val request = Request.Builder()
            .url("$WEBSOCKET_URL?userId=$userId")
            .build()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Conectado al servidor de notificaciones")
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Mensaje recibido: $text")
                try {
                    val json = JSONObject(text)
                    onNotificationReceived(json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error de conexión", t)
                // Implementar lógica de reconexión
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Desconectado: $reason")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected or app backgrounded")
        webSocket = null
    }
}
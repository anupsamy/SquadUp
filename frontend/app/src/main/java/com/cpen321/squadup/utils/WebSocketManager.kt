package com.cpen321.squadup.utils

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import okio.ByteString
import java.util.concurrent.TimeUnit

class WebSocketManager(
    private val url: String,
) {
    companion object {
        @Volatile
        private var instance: WebSocketManager? = null

        fun getInstance(): WebSocketManager? = instance

        fun subscribeToGroup(
            userId: String,
            joinCode: String,
        ) {
            instance?.let { ws ->
                if (ws.isConnected()) {
                    val subscribeMessage =
                        """
                        {
                            "type": "subscribe",
                            "userId": "$userId",
                            "joinCode": "$joinCode"
                        }
                        """.trimIndent()
                    ws.sendMessage(subscribeMessage)
                    Log.d("WebSocket", "Subscribed to group $joinCode for user $userId")
                }
            }
        }

        fun unsubscribeFromGroup(
            userId: String,
            joinCode: String,
        ) {
            instance?.let { ws ->
                if (ws.isConnected()) {
                    val unsubscribeMessage =
                        """
                        {
                            "type": "unsubscribe",
                            "userId": "$userId",
                            "joinCode": "$joinCode"
                        }
                        """.trimIndent()
                    ws.sendMessage(unsubscribeMessage)
                    Log.d("WebSocket", "Unsubscribed from group $joinCode for user $userId")
                }
            }
        }
    }

    interface WebSocketListenerCallback {
        fun onMessageReceived(message: String)

        fun onConnectionStateChanged(isConnected: Boolean)
    }

    private var callback: WebSocketListenerCallback? = null

    init {
        instance = this
    }

    private val client =
        OkHttpClient
            .Builder()
            .pingInterval(30, TimeUnit.SECONDS)
            .build()
    private var webSocket: WebSocket? = null
    private var reconnectAttempts = 0
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isConnected = false

    fun setListener(callback: WebSocketListenerCallback) {
        this.callback = callback
    }

    fun start() {
        val request = Request.Builder().url(url).build()
        webSocket = client.newWebSocket(request, socketListener)
    }

    fun sendMessage(message: String) {
        webSocket?.send(message)
    }

    fun stop() {
        webSocket?.close(1000, "App closed")
        webSocket = null
        isConnected = false
    }

    fun isConnected(): Boolean = isConnected

    private val socketListener =
        object : WebSocketListener() {
            override fun onOpen(
                webSocket: WebSocket,
                response: Response,
            ) {
                reconnectAttempts = 0
                isConnected = true
                mainHandler.post { callback?.onConnectionStateChanged(true) }
                println("WebSocket opened!")
            }

            override fun onMessage(
                webSocket: WebSocket,
                text: String,
            ) {
                mainHandler.post { callback?.onMessageReceived(text) }
            }

            override fun onMessage(
                webSocket: WebSocket,
                bytes: ByteString,
            ) {
                mainHandler.post { callback?.onMessageReceived(bytes.hex()) }
            }

            override fun onClosing(
                webSocket: WebSocket,
                code: Int,
                reason: String,
            ) {
                isConnected = false
                mainHandler.post { callback?.onConnectionStateChanged(false) }
                println("WebSocket closing: $code / $reason")
                webSocket.close(1000, null)
            }

            override fun onClosed(
                webSocket: WebSocket,
                code: Int,
                reason: String,
            ) {
                isConnected = false
                mainHandler.post { callback?.onConnectionStateChanged(false) }
                println("WebSocket closed: $code / $reason")
            }

            override fun onFailure(
                webSocket: WebSocket,
                t: Throwable,
                response: Response?,
            ) {
                isConnected = false
                val errorMessage = t.message ?: "Unknown error"
                val responseCode = response?.code ?: -1
                val responseMessage = response?.message ?: "No response"

                Log.e("WebSocket", "Connection failed to: $url")
                Log.e("WebSocket", "Error: $errorMessage")
                Log.e("WebSocket", "Response code: $responseCode, message: $responseMessage")
                if (response != null) {
                    Log.e("WebSocket", "Response body: ${response.body?.string()}")
                }
                t.printStackTrace()

                mainHandler.post { callback?.onConnectionStateChanged(false) }
                attemptReconnect()
            }
        }

    private fun attemptReconnect() {
        if (reconnectAttempts < 5) {
            reconnectAttempts++
            mainHandler.postDelayed({ start() }, (2000L * reconnectAttempts))
            println("Reconnecting... attempt $reconnectAttempts")
        } else {
            println("Max reconnect attempts reached")
        }
    }
}

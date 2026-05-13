package com.fish.chat.plugin.intellijplugin.service

import com.fish.chat.plugin.intellijplugin.model.WsPacket
import com.fish.chat.plugin.intellijplugin.model.WsBody
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Logger
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class FishChatWebSocketClient {

    private val log = Logger.getInstance(FishChatWebSocketClient::class.java)
    private val gson = Gson()
    private var webSocket: WebSocket? = null
    private val http = HttpClient.newHttpClient()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private var heartbeatFuture: java.util.concurrent.ScheduledFuture<*>? = null

    var onMessage: ((WsPacket) -> Unit)? = null
    var onConnected: (() -> Unit)? = null
    var onDisconnected: ((String) -> Unit)? = null
    var onAck: ((WsPacket) -> Unit)? = null
    var onErrorPacket: ((WsPacket) -> Unit)? = null
    var onNotify: ((WsPacket) -> Unit)? = null
    var isConnected: Boolean = false
        private set
    private var isDisconnecting: Boolean = false

    fun connect(wsUrl: String, token: String) {
        val maskedToken = token.take(12) + "..."
        val msg = "[FishChat] WS connecting to $wsUrl?token=$maskedToken"
        log.info(msg)
        println(msg)

        // 如果已有连接，先主动断开
        if (webSocket != null) {
            disconnect()
        }

        val uri = URI.create("$wsUrl/ws?token=$token")
        isDisconnecting = false

        webSocket = http.newWebSocketBuilder()
            .buildAsync(uri, WebSocketListener())
            .join()
    }

    fun disconnect() {
        val msg = "[FishChat] WS disconnecting"
        log.info(msg)
        println(msg)
        isDisconnecting = true
        stopHeartbeat()
        try {
            webSocket?.sendClose(WebSocket.NORMAL_CLOSURE, "bye")?.join()
        } catch (e: Exception) {
            // ignore close errors
        }
        webSocket = null
        isConnected = false
        isDisconnecting = false
    }

    fun sendMessage(roomCode: String, roomType: String, msgType: String, content: String) {
        val packet = WsPacket(
            cmd = "MSG",
            reqCode = UUID.randomUUID().toString(),
            body = WsBody(
                roomCode = roomCode,
                roomType = roomType,
                msgType = msgType,
                content = content
            )
        )
        val json = gson.toJson(packet)
        println("[FishChat] WS --> $json")
        send(json)
    }

    fun sendHeartbeat() {
        send(gson.toJson(WsPacket(cmd = "HEARTBEAT")))
    }

    fun sendSync(lastTimestamp: Long) {
        val packet = WsPacket(
            cmd = "SYNC",
            reqCode = UUID.randomUUID().toString(),
            body = WsBody(
                roomCode = "",
                roomType = "PRIVATE",
                msgType = "TEXT",
                content = "",
                extra = mapOf("lastTimestamp" to lastTimestamp)
            )
        )
        val json = gson.toJson(packet)
        println("[FishChat] WS --> SYNC: $json")
        send(json)
    }

    private fun send(text: String) {
        webSocket?.sendText(text, true)?.join()
    }

    private fun startHeartbeat() {
        heartbeatFuture = scheduler.scheduleAtFixedRate(
            { sendHeartbeat() },
            30, 30, TimeUnit.SECONDS
        )
    }

    private fun stopHeartbeat() {
        heartbeatFuture?.cancel(false)
        heartbeatFuture = null
    }

    private inner class WebSocketListener : WebSocket.Listener {
        private val buffer = StringBuilder()

        override fun onOpen(webSocket: WebSocket) {
            isConnected = true
            val msg = "[FishChat] WS connected"
            log.info(msg)
            println(msg)
            startHeartbeat()
            onConnected?.invoke()
            webSocket.request(1)
        }

        override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
            buffer.append(data)
            if (last) {
                val text = buffer.toString()
                buffer.clear()
                println("[FishChat] WS <-- ${text.take(200)}")
                try {
                    val packet = gson.fromJson(text, WsPacket::class.java)
                    when (packet.cmd) {
                        "MSG" -> onMessage?.invoke(packet)
                        "HEARTBEAT" -> { /* heartbeat ack */ }
                        "ACK" -> {
                            println("[FishChat] WS ACK: reqCode=${packet.reqCode}")
                            onAck?.invoke(packet)
                        }
                        "NOTIFY" -> {
                            println("[FishChat] WS NOTIFY: ${packet.body?.content}")
                            onNotify?.invoke(packet)
                        }
                        "ERROR" -> {
                            val err = "[FishChat] WS error: ${packet.body?.content}"
                            log.warn(err)
                            println(err)
                            onErrorPacket?.invoke(packet)
                        }
                    }
                } catch (e: Exception) {
                    val err = "[FishChat] WS parse error: ${e.message}"
                    log.warn(err)
                    println(err)
                }
            }
            webSocket.request(1)
            return null
        }

        override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*> {
            isConnected = false
            val msg = "[FishChat] WS closed: code=$statusCode reason=$reason"
            log.info(msg)
            println(msg)
            stopHeartbeat()
            if (!isDisconnecting) {
                onDisconnected?.invoke(reason)
            }
            return CompletableFuture.completedFuture(null)
        }

        override fun onError(webSocket: WebSocket, error: Throwable) {
            val msg = "[FishChat] WS error: ${error.message}"
            log.warn(msg)
            println(msg)
            isConnected = false
            stopHeartbeat()
            if (!isDisconnecting) {
                onDisconnected?.invoke(error.message ?: "Unknown error")
            }
        }
    }
}

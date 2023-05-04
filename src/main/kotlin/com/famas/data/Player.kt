package com.famas.data

import com.famas.data.models.BaseModel
import com.famas.data.models.Ping
import com.famas.game
import com.famas.json
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class Player(
    val username: String,
    var socket: WebSocketSession,
    @SerialName("client_id")
    val clientId: String,
    var score: Int = 0,
    var rank: Int = 0,
    @SerialName("is_drawing")
    var isDrawing: Boolean = false
) {
    private var pingJob: Job? = null

    private var pingTime = 0L
    private var pongTime = 0L

    var isOnline = true

    @OptIn(DelicateCoroutinesApi::class)
    fun startPining() {
        pingJob?.cancel()
        pingJob = GlobalScope.launch {
            while (true) {
                println("Ping job")
                sendPing()
                delay(PING_FREQUENCY)
            }
        }
    }

    private suspend fun sendPing() {
        pingTime = System.currentTimeMillis()
        socket.send(Frame.Text(json.encodeToString(Ping() as BaseModel)))
        delay(PING_FREQUENCY)
        println("Pong time $username: $pongTime")
        if (pingTime - pongTime > PING_FREQUENCY) {
            isOnline = false
            game.playerLeft(clientId)
            pingJob?.cancel()
        }
    }

    fun receivedPong() {
        pongTime = System.currentTimeMillis()
        isOnline = true
    }

    fun disconnect() {
        pingJob?.cancel()
    }

    companion object {
        const val PING_FREQUENCY = 3000L
    }
}
package com.famas.data

import io.ktor.websocket.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
)
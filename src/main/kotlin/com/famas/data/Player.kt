package com.famas.data

import io.ktor.websocket.*

data class Player(
    val username: String,
    var socket: WebSocketSession,
    val clientId: String,
)
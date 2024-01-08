package com.famas.plugins

import io.ktor.server.application.*
import io.ktor.server.websocket.*

fun Application.configureSockets() {
    install(WebSockets)
}

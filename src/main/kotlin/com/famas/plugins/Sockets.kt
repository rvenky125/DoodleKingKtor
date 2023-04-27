package com.famas.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureSockets() {
    install(WebSockets)
}

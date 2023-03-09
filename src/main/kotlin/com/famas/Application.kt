package com.famas

import io.ktor.server.application.*
import com.famas.plugins.*
import com.famas.sessions.DoodleGameSession
import com.famas.util.Constants
import io.ktor.server.sessions.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val game = DoodleKingGame()

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    configureMonitoring()
    configureSockets()
    configureSerialization()
    configureSecurity()
    configureRouting()
}

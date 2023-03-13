package com.famas

import com.famas.data.models.JoinRoom
import io.ktor.server.application.*
import com.famas.plugins.*
import com.famas.sessions.DoodleGameSession
import com.famas.util.Constants
import io.ktor.server.sessions.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val game = DoodleKingGame()

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    println(Json.encodeToString(JoinRoom("", "")))
    configureMonitoring()
    configureSockets()
    configureSerialization()
    configureSecurity()
    configureRouting()
}

package com.famas.plugins

import com.famas.routes.createRoomRoute
import com.famas.routes.gameWebSocketRoute
import com.famas.routes.getRoomsRoute
import com.famas.routes.joinRoomRoute
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        createRoomRoute()
        getRoomsRoute()
        joinRoomRoute()

        gameWebSocketRoute()
    }
}

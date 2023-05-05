package com.famas.plugins

import com.famas.sessions.DoodleGameSession
import com.famas.util.Constants
import io.ktor.server.sessions.*
import io.ktor.server.response.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.routing.*
import io.ktor.util.*
import java.util.UUID

fun Application.configureSecurity() {
    install(Sessions) {
        cookie<DoodleGameSession>(Constants.SESSION_NAME)
    }

    intercept(Plugins) {
        if (call.sessions.get<DoodleGameSession>() == null) {
            val clientId = UUID.randomUUID().toString()
            call.sessions.set(DoodleGameSession(clientId, generateNonce()))
        }
    }
}

package com.famas.sessions

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class DoodleGameSession(
    @SerialName("client_id")
    val clientId: String,
    @SerialName("session_id")
    val sessionId: String
)
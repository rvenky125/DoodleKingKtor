package com.famas.data.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRoomRequest(
    val name: String,
    @SerialName("max_players")
    val maxPlayers: Int
)

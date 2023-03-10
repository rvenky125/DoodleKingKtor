package com.famas.data.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RoomResponse(
    val name: String,
    @SerialName("max_players")
    val maxPlayers: Int,
    @SerialName("player_count")
    val playerCount: Int,
    @SerialName("room_id")
    val roomId: String
)
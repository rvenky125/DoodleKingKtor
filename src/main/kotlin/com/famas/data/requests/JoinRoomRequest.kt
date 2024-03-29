package com.famas.data.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinRoomRequest(
    val username: String,
    @SerialName("room_id")
    val roomId: String
)

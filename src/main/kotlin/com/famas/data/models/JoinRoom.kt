package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JoinRoom(
    val username: String,
    @SerialName("room_id")
    val roomId: String,
): BaseModel(TYPE_JOIN_ROOM)
package com.famas.data.models

data class JoinRoom(
    val username: String,
    val roomId: String,
    val clientId: String
): BaseModel(TYPE_JOIN_ROOM)
package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val from: String,
    @SerialName("room_name")
    val roomName: String,
    val message: String,
    val timestamp: Long
) : BaseModel(TYPE_CHAT_MESSAGE)

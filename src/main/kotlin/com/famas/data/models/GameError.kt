package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameError(
    @SerialName("error_type")
    val errorType: Int
): BaseModel(TYPE_GAME_ERROR) {
    companion object {
        const val ERROR_ROOM_NOT_FOUND = 0
    }
}

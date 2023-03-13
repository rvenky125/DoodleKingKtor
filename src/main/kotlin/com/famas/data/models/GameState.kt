package com.famas.data.models

import com.famas.data.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    @SerialName("drawing_player")
    val drawingPlayer: String,
    val word: String
) : BaseModel(TYPE_GAME_STATE)
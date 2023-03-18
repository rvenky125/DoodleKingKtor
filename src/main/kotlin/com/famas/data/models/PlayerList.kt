package com.famas.data.models

import com.famas.data.Player
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(TYPE_PLAYER_LIST)
data class PlayerList(
    val players: List<PlayerData>
): BaseModel()

package com.famas.data.models

import com.famas.data.Room
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PhaseChange(
    var phase: Room.Phase?,
    var time: Long,
    @SerialName("drawing_player")
    val drawingPlayer: String? = null
): BaseModel(TYPE_PHASE_CHANGE)

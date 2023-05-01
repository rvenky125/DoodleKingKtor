package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(TYPE_DRAW_DATA)
data class DrawData(
    @SerialName("room_id")
    val roomId: String,
    val color: Int? = null,
    val thickness: Float? = null,
    @SerialName("to_x")
    val x: Float,
    @SerialName("to_y")
    val y: Float,
    @SerialName("path_event")
    val pathEvent: Int
) : BaseModel() {
    companion object {
        const val UPDATE = 0
        const val INSERT = 1
    }
}
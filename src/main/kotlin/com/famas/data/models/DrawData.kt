package com.famas.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName(TYPE_DRAW_DATA)
data class DrawData(
    @SerialName("room_id")
    val roomId: String,
    @SerialName("path")
    val path: Path
//    val color: Int,
//    val thickness: Float,
//    @SerialName("from_x")
//    val fromX: Float,
//    @SerialName("from_y")
//    val fromY: Float,
//    @SerialName("to_x")
//    val toX: Float,
//    @SerialName("to_y")
//    val toY: Float,
//    @SerialName("motion_event")
//    val motionEvent: Int
): BaseModel()

@Serializable
data class Path(
    val points: List<OffsetData>,
    val stroke: Float,
    val color: String
)

@Serializable
data class OffsetData(
    val x: Float,
    val y: Float
)
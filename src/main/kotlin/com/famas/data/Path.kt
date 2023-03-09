package com.famas.data

import kotlinx.serialization.Serializable

@Serializable
data class Path(
    val color: String,
    val points: List<Offset>
)

@Serializable
data class Offset(
    val x: Float,
    val y: Float
)
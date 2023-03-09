package com.famas.data.models

abstract class BaseModel(val type: String) {
    companion object {
        const val TYPE_CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
        const val TYPE_DRAW_DATA = "TYPE_DRAW_DATA"
        const val TYPE_GAME_ERROR = "TYPE_GAME_ERROR"
        const val TYPE_ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
        const val TYPE_JOIN_ROOM = "TYPE_JOIN_ROOM"
    }
}
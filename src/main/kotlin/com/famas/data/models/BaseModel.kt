package com.famas.data.models

import kotlinx.serialization.Serializable

@Serializable
abstract class BaseModel(val type: String)

const val TYPE_CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
const val TYPE_DRAW_DATA = "TYPE_DRAW_DATA"
const val TYPE_GAME_ERROR = "TYPE_GAME_ERROR"
const val TYPE_ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
const val TYPE_JOIN_ROOM = "TYPE_JOIN_ROOM"
const val TYPE_PHASE_CHANGE = "TYPE_PHASE_CHANGE"
const val TYPE_CHOSEN_WORD = "TYPE_CHOSEN_WORD"
const val TYPE_GAME_STATE = "TYPE_GAME_STATE"
const val TYPE_NEW_WORDS = "TYPE_GAME_STATE"


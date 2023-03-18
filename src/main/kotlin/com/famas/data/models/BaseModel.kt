package com.famas.data.models

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonClassDiscriminator("type")
abstract class BaseModel

const val TYPE_CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
const val TYPE_DRAW_DATA = "TYPE_DRAW_DATA"
const val TYPE_GAME_ERROR = "TYPE_GAME_ERROR"
const val TYPE_ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
const val TYPE_JOIN_ROOM = "TYPE_JOIN_ROOM"
const val TYPE_PHASE_CHANGE = "TYPE_PHASE_CHANGE"
const val TYPE_CHOSEN_WORD = "TYPE_CHOSEN_WORD"
const val TYPE_GAME_STATE = "TYPE_GAME_STATE"
const val TYPE_NEW_WORDS = "TYPE_NEW_WORDS"
const val TYPE_PLAYER_DATA = "TYPE_PLAYER_DATA"
const val TYPE_PLAYER_LIST = "TYPE_PLAYER_LIST"
package com.famas.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject

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


object BaseModelSerializer : JsonContentPolymorphicSerializer<BaseModel>(BaseModel::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["type"].toString()) {
        TYPE_ANNOUNCEMENT -> Announcement.serializer()
        TYPE_JOIN_ROOM -> JoinRoom.serializer()
        TYPE_DRAW_DATA -> DrawData.serializer()
        TYPE_CHAT_MESSAGE -> ChatMessage.serializer()
        TYPE_GAME_ERROR -> GameError.serializer()
        TYPE_CHOSEN_WORD -> ChosenWord.serializer()
        TYPE_PHASE_CHANGE -> PhaseChange.serializer()
        TYPE_GAME_STATE -> GameState.serializer()
        else -> BaseModel.serializer()
    }
}

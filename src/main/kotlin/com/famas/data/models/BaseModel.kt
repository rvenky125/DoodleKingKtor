package com.famas.data.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

@Serializable
abstract class BaseModel(val type: String) {
    companion object {
        const val TYPE_CHAT_MESSAGE = "TYPE_CHAT_MESSAGE"
        const val TYPE_DRAW_DATA = "TYPE_DRAW_DATA"
        const val TYPE_GAME_ERROR = "TYPE_GAME_ERROR"
        const val TYPE_ANNOUNCEMENT = "TYPE_ANNOUNCEMENT"
        const val TYPE_JOIN_ROOM = "TYPE_JOIN_ROOM"
        const val TYPE_PHASE_CHANGE = "TYPE_PHASE_CHANGE"
    }
}

object BaseModelSerializer : JsonContentPolymorphicSerializer<BaseModel>(BaseModel::class) {
    override fun selectDeserializer(element: JsonElement) = when (element.jsonObject["type"].toString()) {
        BaseModel.TYPE_ANNOUNCEMENT -> Announcement.serializer()
        BaseModel.TYPE_JOIN_ROOM -> JoinRoom.serializer()
        BaseModel.TYPE_DRAW_DATA -> DrawData.serializer()
        BaseModel.TYPE_CHAT_MESSAGE -> ChatMessage.serializer()
        BaseModel.TYPE_GAME_ERROR -> GameError.serializer()
        else -> BaseModel.serializer()
    }
}
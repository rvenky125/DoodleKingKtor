package com.famas

import com.famas.data.Room
import com.famas.data.models.*
import com.famas.plugins.*
import io.ktor.server.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

val game = DoodleKingGame()

val baseModelPolymorphicSerializer = SerializersModule {
    polymorphic(BaseModel::class) {
        subclass(JoinRoom::class, JoinRoom.serializer())
        subclass(Announcement::class, Announcement.serializer())
        subclass(DrawData::class, DrawData.serializer())
        subclass(ChatMessage::class, ChatMessage.serializer())
        subclass(GameError::class, GameError.serializer())
        subclass(ChosenWord::class, ChosenWord.serializer())
        subclass(PhaseChange::class, PhaseChange.serializer())
        subclass(GameState::class, GameState.serializer())
        subclass(GameError::class, GameError.serializer())
        subclass(NewWords::class, NewWords.serializer())
        subclass(Ping::class, Ping.serializer())
        subclass(PlayerData::class, PlayerData.serializer())
        subclass(PlayerList::class, PlayerList.serializer())
    }
}

val json = Json {
    encodeDefaults = true
    serializersModule = baseModelPolymorphicSerializer
}

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    println(json.encodeToString(PhaseChange(phase = Room.Phase.SHOW_WORD, time = 0L) as BaseModel))

    configureMonitoring()
    configureSockets()
    configureSerialization()
    configureSecurity()
    configureRouting()
}

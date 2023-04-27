package com.famas.routes

import com.famas.data.Player
import com.famas.data.Room
import com.famas.data.models.*
import com.famas.game
import com.famas.json
import com.famas.sessions.DoodleGameSession
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.*

fun Route.gameWebSocketRoute() {
    route("/ws/draw") {
        standardWebSocket { socket, clientId, message, payload ->
            when (payload) {
                is JoinRoom -> {
                    val room = game.rooms[payload.roomId]

                    if (room == null) {
                        val gameError = GameError(GameError.ERROR_ROOM_NOT_FOUND)
                        socket.send(Frame.Text(json.encodeToString(gameError as BaseModel)))
                        return@standardWebSocket
                    }

                    val player = Player(
                        payload.username,
                        socket, clientId
                    )
                    game.playerJoined(player)

                    if (!room.containsPlayer(player.username)) {
                        room.addPlayer(player.clientId, player.username, socket)
                    } else {
                        val playerInRoom = room.players.find {
                            it.clientId == clientId
                        }
                        playerInRoom?.socket = socket
                        playerInRoom?.startPining()
                    }
                }

                is DrawData -> {
                    val room = game.rooms[payload.roomId] ?: return@standardWebSocket

                    if (room.phase == Room.Phase.GAME_RUNNING) {
                        room.broadcastToAllExcept(message, clientId)
                        room.addSerializedDrawInfo(message)
                    }
                }

                is ChosenWord -> {
                    val room = game.rooms[payload.roomId] ?: return@standardWebSocket
                    room.setWordAndSwitchToGameRunning(payload.chosenWord)
                }

                is ChatMessage -> {
                    val room = game.rooms[payload.roomId] ?: return@standardWebSocket
                    if (room.checkWordAndNotifyPlayers(payload)) {
                        room.broadcast(message)
                    }
                }

                is Ping -> {
                    game.players[clientId]?.receivedPong()
                }

                is DisconnectRequest -> {
                    game.playerLeft(clientId, true)
                }
            }
        }
    }
}

fun Route.standardWebSocket(
    handleFrame: suspend (
        socket: DefaultWebSocketServerSession,
        clientId: String,
        message: String,
        payload: BaseModel
    ) -> Unit
) {
    webSocket {
        val session = call.sessions.get<DoodleGameSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }
        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    try {
                        val message = frame.readText()
                        val payload = json.decodeFromString<BaseModel>(message)
                        handleFrame(this, session.clientId, message, payload)
                    } catch (e: Exception) {
                        println("Failed to deserialize payload: ${frame.readText()}")
                        println("Error message: ${e.message}")
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            val playerWithClientId = game.getRoomWithClientId(session.clientId)?.players?.find {
                it.clientId == session.clientId
            }

            if (playerWithClientId != null) {
                game.playerLeft(session.clientId)
            }
        }
    }
}
package com.famas.routes

import com.famas.data.responses.BasicApiResponse
import com.famas.data.requests.CreateRoomRequest
import com.famas.data.Room
import com.famas.data.requests.JoinRoomRequest
import com.famas.data.responses.RoomResponse
import com.famas.game
import com.famas.util.Constants.MAX_ROOM_SIZE
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

fun Route.createRoomRoute() {
    route("/api/createRoom") {
        post {
            val request = call.receiveNullable<CreateRoomRequest>()

            if (request == null) {
                call.respond(BasicApiResponse<Unit>(false, "Please provide mandatory fields"))
                return@post
            }

            if (request.maxPlayers < 2) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(false, "The minimum room size should be 2")
                )
                return@post
            }

            if (request.maxPlayers > MAX_ROOM_SIZE) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse<Unit>(false, "The maximum room size is $MAX_ROOM_SIZE")
                )
                return@post
            }

            val roomId = UUID.randomUUID().toString()

            val room = Room(
                name = request.name,
                maxPlayers = request.maxPlayers,
                roomId = roomId
            )

            game.rooms[roomId] = room
            println("Room created with id: $roomId")

            call.respond(HttpStatusCode.OK, BasicApiResponse<Unit>(true))
        }
    }
}

fun Route.getRoomsRoute() {
    route("/api/getRooms") {
        get {
            val searchQuery = call.parameters["query"]
            if (searchQuery == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val roomsResult = game.rooms.filter {
                it.value.name.contains(searchQuery, ignoreCase = true)
            }

            val roomResponses = roomsResult.values.map {
                RoomResponse(it.name, it.maxPlayers, it.players.size, it.roomId)
            }.sortedBy { it.name }

            call.respond(HttpStatusCode.OK, roomResponses)
        }
    }
}

fun Route.joinRoomRoute() {
    route("/api/joinRoom") {
        post {
            val request = call.receiveNullable<JoinRoomRequest>()

            if (request == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val room = game.rooms[request.roomId]

            when {
                room == null -> {
                    call.respond(BasicApiResponse<Unit>(false, "failed to find the room with provided request"))
                }

                room.containsPlayer(username = request.username) -> {
                    call.respond(BasicApiResponse<Unit>(false, "User already exists"))
                }

                room.players.size >= room.maxPlayers -> {
                    call.respond(
                        HttpStatusCode.OK,
                        BasicApiResponse<Unit>(false, "This room is already full.")
                    )
                }

                else -> {
                    call.respond(HttpStatusCode.OK, BasicApiResponse<Room>(successful = true, message = "Joining room", data = room))
                }
            }
        }
    }
}
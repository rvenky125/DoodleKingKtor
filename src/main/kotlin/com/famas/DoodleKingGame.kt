package com.famas

import com.famas.data.Player
import com.famas.data.Room
import java.util.concurrent.ConcurrentHashMap

class DoodleKingGame {
    val players = ConcurrentHashMap<String, Player>()
    val rooms = ConcurrentHashMap<String, Room>()

    fun playerJoined(player: Player) {
        players[player.clientId] = player
        player.startPining()
    }

    fun getRoomWithClientId(clientId: String): Room? {
        val filteredRooms = rooms.filterValues { room ->
            room.players.find { player ->
                player.clientId == clientId
            } != null
        }
        return if(filteredRooms.values.isEmpty()) {
            null
        } else {
            filteredRooms.values.toList()[0]
        }
    }

    fun playerLeft(clientId: String, immediatelyDisconnect: Boolean = false) {
        val playersRoom = getRoomWithClientId(clientId)
        if (immediatelyDisconnect || players[clientId]?.isOnline == false) {
            println("Closing connection to ${players[clientId]?.username}")
            playersRoom?.removePlayer(clientId)
            players[clientId]?.disconnect()
            players.remove(clientId)
        }
    }
}
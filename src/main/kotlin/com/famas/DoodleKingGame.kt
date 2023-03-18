package com.famas

import com.famas.data.Player
import com.famas.data.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.ConcurrentHashMap

class DoodleKingGame {
    val players = ConcurrentHashMap<String, Player>()
    val rooms = ConcurrentHashMap<String, Room>()

    private val gameScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun playerJoined(player: Player) {
        players[player.clientId] = player
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
        if (immediatelyDisconnect) {
            println("Closing connection to ${players[clientId]?.username}")
            playersRoom?.removePlayer(clientId)
            players.remove(clientId)
        }
    }
}
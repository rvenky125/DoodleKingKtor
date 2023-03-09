package com.famas.data

import com.famas.data.models.Announcement
import io.ktor.websocket.*
import kotlinx.coroutines.isActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

data class Room(
    val name: String,
    val maxPlayers: Int,
    val roomId: String,
    var players: List<Player> = listOf()
) {
    private var phaseChangedListener: ((Phase) -> Unit)? = null

    var phase = Phase.WAITING_FOR_PLAYERS
        set(value) {
            synchronized(field) {
                field = value
                phaseChangedListener?.let { change ->
                    change(value)
                }
            }
        }

    private fun setPhaseChangeListener(listener: (Phase) -> Unit) {
        phaseChangedListener = listener
    }

    init {
        setPhaseChangeListener { phase ->
            when (phase) {
                Phase.WAITING_FOR_PLAYERS -> waitingForPlayers()
                Phase.WAITING_FOR_START -> waitingForStart()
                Phase.NEW_ROUND -> newRound()
                Phase.GAME_RUNNING -> gameRunning()
                Phase.SHOW_WORD -> showWord()
            }
        }
    }

    suspend fun addPlayer(clientId: String, username: String, socket: WebSocketSession): Player {
        val player = Player(username = username, socket = socket, clientId = clientId)
        players = players + player

        when {
            players.size == 1 -> {
                phase = Phase.WAITING_FOR_PLAYERS
            }

            players.size == 2 && phase == Phase.WAITING_FOR_PLAYERS -> {
                phase = Phase.WAITING_FOR_START
                players = players.shuffled()
            }

            phase == Phase.WAITING_FOR_START && players.size == maxPlayers -> {
                phase = Phase.NEW_ROUND
                players = players.shuffled()
            }
        }

        val announcement = Announcement(
            "$username joined the party!",
            System.currentTimeMillis(),
            Announcement.TYPE_PLAYER_JOINED
        )

        broadcastToAllExcept(Json.encodeToString(announcement), clientId)
        return player
    }

    private fun waitingForPlayers() {

    }

    private fun waitingForStart() {

    }

    private fun newRound() {

    }

    private fun gameRunning() {

    }

    private fun showWord() {

    }

    suspend fun broadcast(message: String) {
        players.forEach { player ->
            if (player.socket.isActive) {
                player.socket.send(Frame.Text(message))
            }
        }
    }

    suspend fun broadcastToAllExcept(message: String, clientId: String) {
        players.forEach { player ->
            if (player.socket.isActive && player.clientId != clientId) {
                player.socket.send(Frame.Text(message))
            }
        }
    }

    fun containsPlayer(username: String): Boolean {
        return players.find { it.username.lowercase() == username.lowercase() } != null
    }


    enum class Phase {
        WAITING_FOR_PLAYERS,
        WAITING_FOR_START,
        NEW_ROUND,
        GAME_RUNNING,
        SHOW_WORD
    }
}
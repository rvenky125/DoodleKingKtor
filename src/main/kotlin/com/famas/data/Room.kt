package com.famas.data

import com.famas.data.models.*
import com.famas.game
import com.famas.json
import com.famas.util.getRandomWords
import com.famas.util.transformToUnderscores
import com.famas.util.words
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.util.concurrent.ConcurrentHashMap

@Serializable
@OptIn(DelicateCoroutinesApi::class)
data class Room(
    val name: String,
    @SerialName("max_players")
    val maxPlayers: Int,
    @SerialName("room_id")
    val roomId: String,
    var players: List<Player> = listOf(),
) {

    private var timerJob: Job? = null
    private var drawingPlayer: Player? = null
    private var winningPlayers = listOf<String>()
    private var word: String? = null
    private var curWords: List<String>? = null
    var drawingPlayerIndex: Int = 0
    private var startTime = 0L

    private val playerRemoveJobs: ConcurrentHashMap<String, Job>
        get() = ConcurrentHashMap<String, Job>()
    private val leftPlayers: ConcurrentHashMap<String, Pair<Player, Int>>
        get() = ConcurrentHashMap<String, Pair<Player, Int>>()

    private var currentRoundDrawData: List<String> = listOf()

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

    private suspend fun sendCurRoundDrawInfoToPlayer(player: Player) {
        if (phase == Phase.GAME_RUNNING || phase == Phase.SHOW_WORD) {
            player.socket.send(Frame.Text(json.encodeToString(RoundDrawInfo(currentRoundDrawData) as BaseModel)))
        }
    }

//    fun addSerializedDrawInfo(drawAction: String) {
//        curRoundDrawData = curRoundDrawData + drawAction
//    }
//
//    private suspend fun finishOffDrawing() {
//        lastDrawData?.let {
//            if(curRoundDrawData.isNotEmpty() && it.motionEvent == 2) {
//                val finishDrawData = it.copy(motionEvent = 1)
//                broadcast(gson.toJson(finishDrawData))
//            }
//        }
//    }

    suspend fun addPlayer(clientId: String, username: String, socket: WebSocketSession): Player {
        var indexToAdd = players.size - 1
        val player = if(leftPlayers.containsKey(clientId)) {
            val leftPlayer = leftPlayers[clientId]
            leftPlayer?.first?.let {
                it.socket = socket
                it.isDrawing = drawingPlayer?.clientId == clientId
                indexToAdd = leftPlayer.second

                playerRemoveJobs[clientId]?.cancel()
                playerRemoveJobs.remove(clientId)
                leftPlayers.remove(clientId)
                it
            } ?: Player(username, socket, clientId)
        } else {
            Player(username, socket, clientId)
        }
        indexToAdd = when {
            players.isEmpty() -> 0
            indexToAdd >= players.size -> players.size - 1
            else -> indexToAdd
        }
        val tmpPlayers = players.toMutableList()
        tmpPlayers.add(indexToAdd, player)
        players = tmpPlayers.toList()

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
        sendWordToPlayer(player)
        broadcastPlayerStates()
        sendCurRoundDrawInfoToPlayer(player)
        broadcast(json.encodeToString(announcement as BaseModel))

        return player
    }

    private fun kill() {
        playerRemoveJobs.values.forEach { it.cancel() }
        timerJob?.cancel()
    }

    fun removePlayer(clientId: String) {
        val player = players.find { it.clientId == clientId } ?: return
        val index = players.indexOf(player)
        leftPlayers[clientId] = player to index
        players = players - player

        playerRemoveJobs[clientId] = GlobalScope.launch {
            delay(PLAYER_REMOVE_TIME)
            val playerToRemove = leftPlayers[clientId]
            leftPlayers.remove(clientId)
            playerToRemove?.let {
                players = players - it.first
            }
            playerRemoveJobs.remove(clientId)
        }

        val announcement = Announcement(
            "${player.username} left the party",
            System.currentTimeMillis(),
            Announcement.TYPE_PLAYER_LEFT
        )

        GlobalScope.launch {
            broadcastPlayerStates()
            broadcast(json.encodeToString(announcement as BaseModel))
            if (players.size == 1) {
                phase = Phase.WAITING_FOR_PLAYERS
                timerJob?.cancel()
            } else if (players.isEmpty()) {
                kill()
                game.rooms.remove(roomId)
            }
        }
    }

    private fun timerAndNotify(ms: Long) {
        timerJob?.cancel()

        timerJob = GlobalScope.launch {
            startTime = System.currentTimeMillis()
            val phaseChange = PhaseChange(
                phase = phase,
                time = ms,
                drawingPlayer = drawingPlayer?.username
            )

            repeat((ms / UPDATE_TIME_FREQUENCY).toInt()) {
                if (it != 0) {
                    phaseChange.phase = null
                }
                broadcast(json.encodeToString(phaseChange as BaseModel))
                phaseChange.time -= UPDATE_TIME_FREQUENCY
                delay(UPDATE_TIME_FREQUENCY)
            }
            phase = when(phase) {
                Phase.WAITING_FOR_START -> Phase.NEW_ROUND
                Phase.GAME_RUNNING ->  {
//                    finishOffDrawing()
                    Phase.SHOW_WORD
                }
                Phase.SHOW_WORD -> Phase.NEW_ROUND
                Phase.NEW_ROUND -> {
                    word = null
                    Phase.GAME_RUNNING
                }
                else -> Phase.WAITING_FOR_PLAYERS
            }
        }
    }

    fun setWordAndSwitchToGameRunning(word: String) {
        this.word = word
        phase = Phase.GAME_RUNNING
    }

    private fun isGuessCorrect(guess: ChatMessage): Boolean {
        return guess.matchesWord(
            word ?: return false
        ) && !winningPlayers.contains(guess.from) && guess.from != drawingPlayer?.username && phase == Phase.GAME_RUNNING
    }

    private fun waitingForPlayers() {
        GlobalScope.launch {
            val phaseChange = PhaseChange(
                Phase.WAITING_FOR_PLAYERS,
                DELAY_WAITING_FOR_START_TO_NEW_ROUND
            )
            broadcast(json.encodeToString(phaseChange as BaseModel))
        }
    }

    private fun waitingForStart() {
        GlobalScope.launch {
            timerAndNotify(DELAY_WAITING_FOR_START_TO_NEW_ROUND)
            val phaseChange = PhaseChange(
                Phase.WAITING_FOR_START,
                DELAY_WAITING_FOR_START_TO_NEW_ROUND
            )
            broadcast(json.encodeToString(phaseChange as BaseModel))
        }
    }

    private fun newRound() {
        currentRoundDrawData = listOf()
        curWords = getRandomWords(3)
        val newWords = NewWords(curWords!!)
        nextDrawingPlayer()
        GlobalScope.launch {
            broadcastPlayerStates()
            drawingPlayer?.socket?.send(Frame.Text(json.encodeToString(newWords as BaseModel)))
            timerAndNotify(DELAY_NEW_ROUND_TO_GAME_RUNNING)
        }
    }

    private fun addWinningPlayer(username: String): Boolean {
        winningPlayers = winningPlayers + username

        if (winningPlayers.size == players.size - 1) {
            phase = Phase.NEW_ROUND
            return true
        }
        return false
    }

    suspend fun checkWordAndNotifyPlayers(message: ChatMessage): Boolean {
        if (isGuessCorrect(message)) {
            val guessingTime = System.currentTimeMillis() - startTime
            val timePercentageLeft = 1f - guessingTime.toFloat() / DELAY_GAME_RUNNING_TO_SHOW_WORD
            val score = GUESS_SCORE_DEFAULT + GUESS_SCORE_PERCENTAGE_MULTIPLIER * timePercentageLeft
            val player = players.find { it.username == message.from }

            player?.let {
                it.score += score.toInt()
            }

            drawingPlayer?.let {
                it.score += GUESS_SCORE_FOR_DRAWING_PLAYER / players.size
            }

            broadcastPlayerStates()

            val announcement = Announcement(
                "${message.from} has guessed it",
                System.currentTimeMillis(),
                Announcement.TYPE_PLAYER_GUESSED_WORD
            )
            broadcast(json.encodeToString(announcement as BaseModel))
            val isRoundOver = addWinningPlayer(message.from)

            if (isRoundOver) {
                val roundOverAnnouncement = Announcement(
                    "Everybody guessed it! New round is starting...",
                    System.currentTimeMillis(),
                    Announcement.TYPE_EVERYBODY_GUESSED_IT
                )
                broadcast(json.encodeToString(roundOverAnnouncement as BaseModel))
            }

            return true
        }
        return false
    }

    private suspend fun sendWordToPlayer(player: Player) {
        val delay = when(phase) {
            Phase.WAITING_FOR_START -> DELAY_WAITING_FOR_START_TO_NEW_ROUND
            Phase.NEW_ROUND -> DELAY_NEW_ROUND_TO_GAME_RUNNING
            Phase.GAME_RUNNING -> DELAY_GAME_RUNNING_TO_SHOW_WORD
            Phase.SHOW_WORD -> DELAY_SHOW_WORD_TO_NEW_ROUND
            else -> 0L
        }
        val phaseChange = PhaseChange(phase, delay, drawingPlayer?.username)
        word?.let { curWord ->
            drawingPlayer?.let { drawingPlayer ->
                val gameState = GameState(
                    drawingPlayer = drawingPlayer.username,
                    word = if (player.isDrawing || phase == Phase.SHOW_WORD) {
                        curWord
                    } else {
                        curWord.transformToUnderscores()
                    }
                )
                player.socket.send(Frame.Text(json.encodeToString(gameState as BaseModel)))
            }
        }
        player.socket.send(Frame.Text(json.encodeToString(phaseChange as BaseModel)))
    }

    private fun nextDrawingPlayer() {
        drawingPlayer?.isDrawing = false
        if (players.isEmpty()) {
            return
        }

        drawingPlayer = if (drawingPlayerIndex <= players.size - 1) {
            players[drawingPlayerIndex]
        } else players.last()

        if (drawingPlayerIndex < players.size - 1) drawingPlayerIndex++
        else drawingPlayerIndex = 0
    }

    private fun gameRunning() {
        winningPlayers = listOf()
        val wordToSend = word ?: curWords?.random() ?: words.random()
        val wordWithUnderscores = wordToSend.transformToUnderscores()
        val drawingUsername = (drawingPlayer ?: players.random()).username
        val gameStateForDrawingPlayer = GameState(
            drawingPlayer = drawingUsername,
            word = wordToSend
        )
        val gameStateForGuessingPlayers = GameState(
            drawingPlayer = drawingUsername,
            word = wordWithUnderscores
        )

        GlobalScope.launch {
            broadcastToAllExcept(
                json.encodeToString(gameStateForGuessingPlayers as BaseModel),
                drawingPlayer?.clientId ?: players.random().clientId
            )
            drawingPlayer?.socket?.send(Frame.Text(json.encodeToString(gameStateForDrawingPlayer as BaseModel)))
            timerAndNotify(DELAY_GAME_RUNNING_TO_SHOW_WORD)
            println("Drawing phase in room $name started. It'll last ${DELAY_GAME_RUNNING_TO_SHOW_WORD / 1000}s")
        }
    }

    private fun showWord() {
        GlobalScope.launch {
            if (winningPlayers.isEmpty()) {
                drawingPlayer?.let {
                    it.score -= PENALTY_NOBODY_GUESSED_IT
                }
            }
            broadcastPlayerStates()
            word?.let {
                val chosenWord = ChosenWord(chosenWord = it, roomId = roomId)
                broadcast(json.encodeToString(chosenWord as BaseModel))
            }

            timerAndNotify(DELAY_SHOW_WORD_TO_NEW_ROUND)
            val phaseChange = PhaseChange(Phase.SHOW_WORD, DELAY_SHOW_WORD_TO_NEW_ROUND)
            broadcast(json.encodeToString(phaseChange as BaseModel))
        }
    }

    private suspend fun broadcastPlayerStates() {
        val playersList = players.sortedByDescending { it.score }.map {
            PlayerData(it.username, it.isDrawing, it.score, it.rank)
        }
        playersList.forEachIndexed { index, playerData ->
            playerData.rank = index + 1
        }
        broadcast(json.encodeToString(PlayerList(playersList) as BaseModel))
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


    companion object {

        const val PLAYER_REMOVE_TIME = 60000L

        const val UPDATE_TIME_FREQUENCY = 1000L
        const val DELAY_WAITING_FOR_START_TO_NEW_ROUND = 10000L
        const val DELAY_NEW_ROUND_TO_GAME_RUNNING = 20000L
        const val DELAY_GAME_RUNNING_TO_SHOW_WORD = 60000L

        const val DELAY_SHOW_WORD_TO_NEW_ROUND = 10000L
        const val PENALTY_NOBODY_GUESSED_IT = 50
        const val GUESS_SCORE_DEFAULT = 50
        const val GUESS_SCORE_PERCENTAGE_MULTIPLIER = 50
        const val GUESS_SCORE_FOR_DRAWING_PLAYER = 50
    }
}

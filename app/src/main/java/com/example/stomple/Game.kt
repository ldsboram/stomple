// Game.kt
package com.example.stomple

import com.example.stomple.models.Board
import com.example.stomple.models.Cell
import com.example.stomple.models.Player
import kotlin.math.max
import kotlin.random.Random

class Game {
    val board = Board()
    lateinit var player: Player
    lateinit var computer: Player
    var currentPlayer: Player? = null
    var outcome: String = ""
    var lastStompedColor: String? = null
        private set

    enum class MoveResult {
        VALID_MOVE, INVALID_MOVE, GAME_OVER
    }

    private val marbleInitials = mapOf(
        "Sky Blue" to "B",
        "Navy Blue" to "N",
        "Green" to "G",
        "Yellow" to "Y",
        "Black" to "K",
        "White" to "W",
        "Red" to "R"
    )
    private val stampColors = listOf("B", "N", "G", "Y", "K", "W") // Exclude 'R'

    fun setup() {
        // Assign stamp colors
        val availableColors = stampColors.shuffled().toMutableList()
        val playerStampColor = availableColors.removeAt(0)
        val computerStampColor = availableColors.removeAt(0)

        player = Player("Player", playerStampColor)
        computer = Player("Computer", computerStampColor)

        // Decide who goes first
        currentPlayer = computer // As per the game flow, computer starts
    }

    fun getPossibleMoves(player: Player): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        if (player.position == null) {
            // First turn, can stomp any edge marble
            for (row in 0 until board.size) {
                for (col in 0 until board.size) {
                    if (board.isEdgeCell(row, col)) {
                        val cell = board.getCell(row, col)
                        if (cell.marbleColor != null) {
                            moves.add(Pair(row, col))
                        }
                    }
                }
            }
        } else {
            val (row, col) = player.position!!
            val adjacents = board.getAdjacentCells(row, col)
            for ((r, c) in adjacents) {
                val cell = board.getCell(r, c)
                if (cell.marbleColor != null) {
                    moves.add(Pair(r, c))
                }
            }
            // Add any marble of player's stamp color
            for (r in 0 until board.size) {
                for (c in 0 until board.size) {
                    val cell = board.getCell(r, c)
                    if (cell.marbleColor == player.stampColor && !moves.contains(Pair(r, c))) {
                        moves.add(Pair(r, c))
                    }
                }
            }
        }
        return moves
    }

    fun getChainStompMoves(player: Player, chainColor: String): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        val (row, col) = player.position!!
        val adjacents = board.getAdjacentCells(row, col)
        for ((r, c) in adjacents) {
            val cell = board.getCell(r, c)
            if (cell.marbleColor == chainColor) {
                moves.add(Pair(r, c))
            }
        }
        return moves
    }

    fun performStomp(player: Player, row: Int, col: Int) {
        val cell = board.getCell(row, col)
        lastStompedColor = cell.marbleColor
        cell.marbleColor = null
        cell.stamp = player.name // 'Player' or 'Computer'
        if (player.position != null) {
            val (prevRow, prevCol) = player.position!!
            val prevCell = board.getCell(prevRow, prevCol)
            prevCell.stamp = null
        }
        player.position = Pair(row, col)
    }

    fun computerTurnSingleStep(): MoveResult {
        val possibleMoves = getPossibleMoves(computer)
        if (possibleMoves.isEmpty()) {
            outcome = "패배"
            return MoveResult.GAME_OVER
        }
        val bestMove = selectBestMoveForComputer(possibleMoves)
        performStomp(computer, bestMove.first, bestMove.second)
        return MoveResult.VALID_MOVE
    }

    private fun selectBestMoveForComputer(possibleMoves: List<Pair<Int, Int>>): Pair<Int, Int> {
        var minPlayerMoves: Int? = null
        var bestMove: Pair<Int, Int>? = null

        for (move in possibleMoves) {
            // Create a deep copy of the board to simulate the move
            val tempBoard = copyBoard()
            val tempComputer = Player("Computer", computer.stampColor).apply { position = computer.position }
            val tempPlayer = Player("Player", player.stampColor).apply { position = player.position }

            // Simulate the stomp on the temporary board
            val stompedColor = simulateStomp(tempBoard, tempComputer, move.first, move.second)
            simulateChainStomp(tempBoard, tempComputer, stompedColor)

            // Count potential moves for the player after the computer's move
            val playerMovesAfter = getPossibleMovesForBoard(tempBoard, tempPlayer).size

            // Update if this move minimizes the player's future options
            if (minPlayerMoves == null || playerMovesAfter < minPlayerMoves) {
                minPlayerMoves = playerMovesAfter
                bestMove = move
            }
        }

        return bestMove ?: possibleMoves.random()
    }

    private fun simulateChainStomp(board: Board, player: Player, chainColor: String?) {
        if (chainColor == null) return

        while (true) {
            val (row, col) = player.position!!
            val adjacentCells = board.getAdjacentCells(row, col)
            val sameColorCells = adjacentCells.filter { (r, c) ->
                board.getCell(r, c)?.marbleColor == chainColor
            }

            if (sameColorCells.isEmpty()) break

            val nextCell = sameColorCells.first() // Select the first adjacent cell of the same color
            simulateStomp(board, player, nextCell.first, nextCell.second)
        }
    }

    private fun simulateStomp(board: Board, player: Player, row: Int, col: Int): String? {
        val cell = board.getCell(row, col)
        val stompedColor = cell?.marbleColor
        cell?.marbleColor = null
        cell?.stamp = player.name // 'Player' or 'Computer'

        player.position?.let {
            val (prevRow, prevCol) = it
            val prevCell = board.getCell(prevRow, prevCol)
            prevCell?.stamp = null
        }
        player.position = Pair(row, col)

        return stompedColor
    }

    private fun copyBoard(): Board {
        val newBoard = Board()
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                val cell = board.getCell(row, col)
                newBoard.getCell(row, col)?.apply {
                    marbleColor = cell?.marbleColor
                    stamp = cell?.stamp
                }
            }
        }
        return newBoard
    }

    private fun getPossibleMovesForBoard(board: Board, player: Player): List<Pair<Int, Int>> {
        val moves = mutableListOf<Pair<Int, Int>>()
        if (player.position == null) {
            // First turn: choose any edge cell
            for (row in 0 until board.size) {
                for (col in 0 until board.size) {
                    if (board.isEdgeCell(row, col) && board.getCell(row, col)?.marbleColor != null) {
                        moves.add(Pair(row, col))
                    }
                }
            }
        } else {
            val (row, col) = player.position!!
            val adjacents = board.getAdjacentCells(row, col)
            adjacents.forEach { (r, c) ->
                val cell = board.getCell(r, c)
                if (cell?.marbleColor != null) {
                    moves.add(Pair(r, c))
                }
            }
            // Include any cell with the player’s color
            for (r in 0 until board.size) {
                for (c in 0 until board.size) {
                    if (board.getCell(r, c)?.marbleColor == player.stampColor) {
                        moves.add(Pair(r, c))
                    }
                }
            }
        }
        return moves
    }

    fun getScoreDetails(): String {
        val (redCount, otherCount) = countRemainingMarbles()
        val baseScore = 3
        val redScore = 3 * redCount
        val otherScore = otherCount
        var totalScore = baseScore + redScore + otherScore

        val builder = StringBuilder()
        builder.append("\n<점수 계산>\n")
        builder.append("기본 점수: 3 점\n")
        builder.append("남겨진 빨강색 구슬 수 당: 3 점 * $redCount = $redScore 점\n")
        builder.append("그 외의 남겨진 구슬 수 당: 1 점 * $otherCount = $otherScore 점\n")
        if (outcome == "패배") {
            totalScore *= -1
            builder.append("패배 여부: ×(-1)\n")
        }
        builder.append("합계: $totalScore 점")
        return builder.toString()
    }

    private fun countRemainingMarbles(): Pair<Int, Int> {
        var redCount = 0
        var otherCount = 0
        for (row in 0 until board.size) {
            for (col in 0 until board.size) {
                val cell = board.getCell(row, col)
                when (cell.marbleColor) {
                    "R" -> redCount++
                    null -> {}
                    else -> otherCount++
                }
            }
        }
        return Pair(redCount, otherCount)
    }

}

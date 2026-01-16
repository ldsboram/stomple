// GameActivity.kt
package com.example.stomple

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.stomple.models.*
import kotlin.concurrent.thread

class GameActivity : AppCompatActivity() {
    private lateinit var boardLayout: GridLayout
    private val boardSize = 7
    private lateinit var buttons: Array<Array<Button>>
    private lateinit var game: Game
    private lateinit var statusTextView: TextView
    private lateinit var handler: Handler
    private lateinit var playerColorEmoji: TextView
    private lateinit var computerColorEmoji: TextView

    private var isChainStomping = false
    private var chainColor: String? = null

    private val emojiMap = mapOf(
        "B" to "\uD83D\uDD35", // Blue Circle 🔵
        "N" to "\uD83D\uDFE3", // Purple Diamond 🟣
        "G" to "\uD83D\uDFE2", // Green Circle 🟢
        "Y" to "\uD83D\uDFE1", // Yellow Circle 🟡
        "K" to "\u26AB",       // Black Circle ⚫
        "W" to "\u26AA",       // White Circle ⚪
        "R" to "\uD83D\uDD34"  // Red Circle 🔴
    )

    private val stampMap = mapOf(
        "Player" to "\uD83E\uDDD1",   // Person 🧑
        "Computer" to "\uD83D\uDCBB" // Laptop 🖥️
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the action bar
        supportActionBar?.hide()
        setContentView(R.layout.activity_game)

        handler = Handler(mainLooper)

        boardLayout = findViewById(R.id.boardLayout)
        statusTextView = findViewById(R.id.statusTextView)
        playerColorEmoji = findViewById(R.id.playerColorEmoji)
        computerColorEmoji = findViewById(R.id.computerColorEmoji)

        boardLayout.columnCount = boardSize
        boardLayout.rowCount = boardSize
        buttons = Array(boardSize) { Array(boardSize) { Button(this) } }

        game = Game()
        game.setup()

        playerColorEmoji.text = emojiMap[game.player.stampColor]
        computerColorEmoji.text = emojiMap[game.computer.stampColor]

        createBoard()
        updateBoard()
        if (game.currentPlayer == game.computer) {
            statusTextView.text = "Computer's turn."
            computerTurn()
        } else {
            statusTextView.text = "Your turn."
            highlightValidMoves()
        }
    }

    private fun createBoard() {
        // Calculate button size with a slight margin for better fitting
        boardLayout.post {
            val totalWidth = boardLayout.width
            val buttonSize = (totalWidth / boardSize) * 0.95

            for (i in 0 until boardSize) {
                for (j in 0 until boardSize) {
                    val button = Button(this)
                    val params = GridLayout.LayoutParams()
                    params.width = buttonSize.toInt()
                    params.height = buttonSize.toInt()
                    params.setMargins(1, 1, 1, 1) // Small margins between buttons
                    button.layoutParams = params
                    button.setOnClickListener {
                        onCellClicked(i, j)
                    }
                    button.background = null
                    button.setBackgroundColor(Color.TRANSPARENT)
                    button.gravity = Gravity.CENTER
                    button.textSize = 24f // Emoji size
                    buttons[i][j] = button
                    boardLayout.addView(button)
                }
            }
            updateBoard() // Update the board display after creation
        }
    }



    private fun updateBoard() {
        clearHighlights()
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                val cell = game.board.getCell(i, j)
                val button = buttons[i][j]
                button.textSize = 24f
                button.setBackgroundColor(Color.TRANSPARENT)
                button.isEnabled = true
                button.setTextColor(Color.BLACK)
                button.background = null
                button.gravity = Gravity.CENTER

                when {
                    cell.stamp != null -> {
                        button.text = stampMap[cell.stamp]
                    }
                    cell.marbleColor != null -> {
                        button.text = emojiMap[cell.marbleColor]
                    }
                    else -> {
                        button.text = ""
                        button.isEnabled = false
                    }
                }
            }
        }
        if (isChainStomping) {
            highlightValidMoves()
        }
    }

    private fun onCellClicked(row: Int, col: Int) {
        if (game.currentPlayer != game.player) return

        if (isChainStomping) {
            val possibleChainMoves = game.getChainStompMoves(game.player, chainColor!!)
            if (!possibleChainMoves.contains(Pair(row, col))) {
                Toast.makeText(this, "You must stomp adjacent marbles of the same color!", Toast.LENGTH_SHORT).show()
                return
            }
            game.performStomp(game.player, row, col)
            updateBoard()
            checkChainStomping()
        } else {
            val possibleMoves = game.getPossibleMoves(game.player)
            if (!possibleMoves.contains(Pair(row, col))) {
                Toast.makeText(this, "Invalid move!", Toast.LENGTH_SHORT).show()
                return
            }
            game.performStomp(game.player, row, col)
            updateBoard()
            // 체인 스톰프 확인
            chainColor = game.lastStompedColor
            checkChainStomping()
        }
    }



    private fun checkChainStomping() {
        val possibleChainMoves = game.getChainStompMoves(game.player, chainColor!!)
        if (possibleChainMoves.isNotEmpty()) {
            isChainStomping = true
            statusTextView.text = "Stomp Chain needed!"
            highlightValidMoves()
        } else {
            isChainStomping = false
            chainColor = null
            // 컴퓨터의 가능한 이동 확인
            if (!game.computer.hasMove(game.board)) {
                navigateToResultActivity("승리")
            } else {
                game.currentPlayer = game.computer
                statusTextView.text = "Computer's turn."
                updateBoard()
                computerTurn()
            }
        }
    }


    private fun computerTurn() {
        thread {
            Thread.sleep(1000)
            handler.post {
                game.computerTurnSingleStep()
                updateBoard()
                performComputerChainStomp()
            }
        }
    }



    private fun performComputerChainStomp() {
        val chainMoves = game.getChainStompMoves(game.computer, game.lastStompedColor!!)
        if (chainMoves.isEmpty()) {
            // 플레이어의 가능한 이동 확인
            if (!game.player.hasMove(game.board)) {
                navigateToResultActivity("패배")
            } else {
                game.currentPlayer = game.player
                statusTextView.text = "Your turn."
                highlightValidMoves()
            }
        } else {
            val nextMove = chainMoves.first()
            handler.postDelayed({
                game.performStomp(game.computer, nextMove.first, nextMove.second)
                updateBoard()
                performComputerChainStomp()
            }, 500)
        }
    }


    private fun highlightValidMoves() {
        clearHighlights()
        val moves = if (isChainStomping) {
            game.getChainStompMoves(game.player, chainColor!!)
        } else {
            game.getPossibleMoves(game.player)
        }
        for (move in moves) {
            val button = buttons[move.first][move.second]
            button.setBackgroundColor(Color.parseColor("#80FFA500")) // Semi-transparent orange
        }
    }

    private fun clearHighlights() {
        for (i in 0 until boardSize) {
            for (j in 0 until boardSize) {
                val button = buttons[i][j]
                button.setBackgroundColor(Color.TRANSPARENT)
            }
        }
    }

    private fun navigateToResultActivity(outcome: String) {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra("outcome", outcome)
        game.outcome = outcome
        intent.putExtra("scoreDetails", game.getScoreDetails())
        startActivity(intent)
        finish()
    }
}

// models/Player.kt
package com.example.stomple.models

class Player(
    val name: String, // 'Player' or 'Computer'
    val stampColor: String // e.g., 'B', 'N', etc.
) {
    var position: Pair<Int, Int>? = null

    fun hasMove(board: Board): Boolean {
        if (position == null) {
            // Check if there's any edge marble for the first turn
            for (row in 0 until board.size) {
                for (col in 0 until board.size) {
                    if (board.isEdgeCell(row, col) && board.getCell(row, col)?.marbleColor != null) {
                        return true
                    }
                }
            }
        } else {
            // Check adjacent marbles for potential moves
            val (row, col) = position!!
            val adjacents = board.getAdjacentCells(row, col)
            adjacents.forEach { (r, c) ->
                if (board.getCell(r, c)?.marbleColor != null) {
                    return true
                }
            }
            // Check for any marble of the player's color across the board
            for (r in 0 until board.size) {
                for (c in 0 until board.size) {
                    if (board.getCell(r, c)?.marbleColor == stampColor) {
                        return true
                    }
                }
            }
        }
        // If no valid moves are found
        return false
    }

}

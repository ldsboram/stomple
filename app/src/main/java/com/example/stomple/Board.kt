// models/Board.kt
package com.example.stomple.models

import kotlin.random.Random

class Board {
    val size = 7
    val grid = Array(size) { Array(size) { Cell() } }

    init {
        initializeMarbles()
    }

    fun initializeMarbles() {
        val marbleColors = listOf("B", "N", "G", "Y", "K", "W", "R")
        val marbles = mutableListOf<String>()
        for (color in marbleColors) {
            repeat(7) {
                marbles.add(color)
            }
        }
        marbles.shuffle()
        var idx = 0
        for (row in 0 until size) {
            for (col in 0 until size) {
                grid[row][col].marbleColor = marbles[idx++]
            }
        }
    }

    fun getCell(row: Int, col: Int): Cell {
        return grid[row][col]
    }

    fun isEdgeCell(row: Int, col: Int): Boolean {
        return row == 0 || row == size - 1 || col == 0 || col == size - 1
    }

    fun getAdjacentCells(row: Int, col: Int): List<Pair<Int, Int>> {
        val adjacents = mutableListOf<Pair<Int, Int>>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val r = row + dr
                val c = col + dc
                if (r in 0 until size && c in 0 until size) {
                    adjacents.add(Pair(r, c))
                }
            }
        }
        return adjacents
    }
}

// models/Cell.kt
package com.example.stomple.models

data class Cell(
    var marbleColor: String? = null, // e.g., 'B', 'N', etc.
    var stamp: String? = null // 'P', 'C', or null
)

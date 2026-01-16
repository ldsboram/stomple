// InstructionsActivity.kt
package com.example.stomple

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class InstructionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
// Hide the action bar
        supportActionBar?.hide()
        setContentView(R.layout.activity_instructions)

        lateinit var mainMenuButton: Button
        mainMenuButton = findViewById(R.id.returnToMain)
        mainMenuButton.setOnClickListener {
            finish()
        }
    }
}

// MainActivity.kt
package com.example.stomple

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    private lateinit var startGameButton: Button
    private lateinit var instructionsButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the action bar
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        startGameButton = findViewById(R.id.startGameButton)
        instructionsButton = findViewById(R.id.instructionsButton)

        startGameButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
        }

        instructionsButton.setOnClickListener {
            val intent = Intent(this, InstructionsActivity::class.java)
            startActivity(intent)
        }
    }
}

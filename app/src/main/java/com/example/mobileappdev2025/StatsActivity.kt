package com.example.mobileappdev2025

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val receivedLstreak = intent.getStringExtra("lstreak") ?: "LONGEST STREAK NOT FOUND"
        Log.d("DEBUG", "Received Longest Streak in StatsActivity: $receivedLstreak")


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stats)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val score = intent.getStringExtra("score")?:"SCORE NOT FOUND";
        val totalCorrect = intent.getStringExtra("totalCorrect")?:"TOTAL CORRECT NOT FOUND";
        val totalWrong = intent.getStringExtra("totalWrong")?:"TOTAL WRONG NOT FOUND";
        val lstreak = intent.getStringExtra("lstreak")?: "LONGEST STREAK NOT FOUND"

        findViewById<TextView>(R.id.score_text).text = "Score : " + score;
        findViewById<TextView>(R.id.correct_text).text = "Correct Count : " + totalCorrect;
        findViewById<TextView>(R.id.wrong_text).text = "Incorrect Count : " + totalWrong;
        findViewById<TextView>(R.id.longest_streak_text).text = "Longest Streak : " + lstreak

        // finish(); // used the pop the activity stack
    }
}

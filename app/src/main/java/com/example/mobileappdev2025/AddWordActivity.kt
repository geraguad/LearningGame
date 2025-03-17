package com.example.mobileappdev2025

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class AddWordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_word)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    fun addWord(view : View){
        val word = findViewById<EditText>(R.id.word_edit_text).text.toString()
        val def = findViewById<EditText>(R.id.def_edit_text).text.toString()

        // error checking

        val myIntent = Intent()
        //getExtra is used to pass info between activities
        myIntent.putExtra("word", word)
        myIntent.putExtra("def", def)
        setResult(RESULT_OK, myIntent)
        finish()
    }
}
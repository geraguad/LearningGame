package com.example.mobileappdev2025

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileInputStream
import java.util.Random
import java.util.Scanner

data class WordDefinition(val word: String, val definition: String);

class MainActivity : AppCompatActivity() {
    private val ADD_WORD_CODE = 1234; // 1-65535
    private lateinit var myAdapter : ArrayAdapter<String>; // connect from data to gui
    private var dataDefList = ArrayList<String>(); // data
    private var wordDefinition = mutableListOf<WordDefinition>();
    private var score : Int = 0;
    private var streak: Int = 0;
    private var totalCorrect : Int = 0;
    private var totalWrong : Int = 0;
    private var lstreak: Int = 0
    private var correctDef: String = "";
    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        loadWordsFromDisk()

        pickNewWordAndLoadDataList();
        //
        setupList();
        //setScore(0)

        val defList = findViewById<ListView>(R.id.dynamic_def_list);
        defList.setOnItemClickListener { _, _, index, _ ->
            val selectedDef = dataDefList[index]
            if (selectedDef == correctDef) {
                setStreak (streak + 1)
                setScore (score + streak)
                totalCorrect ++
                longestStreak()
            } else {
                //longestStreak()
                setStreak (0)
                totalWrong ++
            }
            //longestStreak()
            pickNewWordAndLoadDataList()
            myAdapter.notifyDataSetChanged();
        };
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == ADD_WORD_CODE && resultCode == RESULT_OK && data != null){
            val word = data.getStringExtra("word")?:""
            val def = data.getStringExtra("def")?:""

            Log.d("MAD", word)
            Log.d("MAD", def)

            if ( word == "" || def == "")
                return

            wordDefinition.add(WordDefinition(word, def))
            pickNewWordAndLoadDataList()
            myAdapter.notifyDataSetChanged()
        }
    }

    private fun loadWordsFromDisk()
    {
        // user data
        val file = File(applicationContext.filesDir, "user_data.csv")

        if (file.exists()) {
            val readResult = FileInputStream(file)
            val scanner = Scanner(readResult)

            while(scanner.hasNextLine()){
                val line = scanner.nextLine()
                val wd = line.split("|")
                wordDefinition.add(WordDefinition(wd[0], wd[1]))
            }
        } else { // default data

            file.createNewFile()

            val reader = Scanner(resources.openRawResource(R.raw.default_words))
            while(reader.hasNextLine()){
                val line = reader.nextLine()
                val wd = line.split("|")
                wordDefinition.add(WordDefinition(wd[0], wd[1]))
                file.appendText("${wd[0]}|${wd[1]}\n")
            }
        }
    }

    private fun pickNewWordAndLoadDataList()
    {
        wordDefinition.shuffle();

        dataDefList.clear();
        correctDef = wordDefinition[0].definition

        val threedef = wordDefinition
            .filter { it.definition != correctDef }
            .shuffled()
            .take(3)

        dataDefList.add(correctDef)
        threedef.forEach {dataDefList.add(it.definition)}
        dataDefList.shuffle()
        findViewById<TextView>(R.id.word).text = wordDefinition[0].word
    }

    private fun setupList()
    {
        myAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataDefList);

        // connect to list
        val defList = findViewById<ListView>(R.id.dynamic_def_list);
        defList.adapter = myAdapter;
    }


    fun openStats(view : View)
    {
        Log.d("DEBUG", "Sending Longest Streak: $lstreak")
        var myIntent = Intent(this, StatsActivity::class.java);
        myIntent.putExtra("score", score.toString());
        myIntent.putExtra("totalCorrect", totalCorrect.toString());
        myIntent.putExtra("totalWrong", totalWrong.toString());
        myIntent.putExtra("lstreak", lstreak.toString());
        startActivity(myIntent)
    }

    fun openAddWord(view : View)
    {
        var myIntent = Intent(this, AddWordActivity::class.java);
        startActivityForResult(myIntent, ADD_WORD_CODE)
    }

    fun setScore(_score: Int)
    {
        score = _score
        findViewById<TextView>(R.id.cscore_text).text = "Score : " + score;
    }
    fun setStreak(_streak: Int)
    {
        streak = _streak
        findViewById<TextView>(R.id.Streak_Text).text = "Streak : " + streak;
    }
    fun longestStreak()
    {
        if (streak > lstreak){
            lstreak = streak;
        }
    }
}
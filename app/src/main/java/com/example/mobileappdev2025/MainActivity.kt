package com.example.mobileappdev2025

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Constraint
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileWriter
import java.io.IOException
import java.util.Random
import java.util.Scanner
import java.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration


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
        setupList();

        val defList = findViewById<ListView>(R.id.dynamic_def_list);
        defList.setOnItemClickListener { _, _, index, _ ->
            val selectedDef = dataDefList[index]
            if (selectedDef == correctDef) {
                setStreak (streak + 1)
                setScore (score + streak)
                totalCorrect ++
                longestStreak()
            } else {
                setStreak (0)
                totalWrong ++
                Toast.makeText(this, "wrong definition", Toast.LENGTH_LONG).show()

            }
            pickNewWordAndLoadDataList()
            myAdapter.notifyDataSetChanged();

            // toast popup
            //Toast.makeText(this, "hello", Toast.LENGTH_LONG).show()
        };
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val file = File(applicationContext.filesDir, "user_data.csv")

        if (requestCode == ADD_WORD_CODE && resultCode == RESULT_OK && data != null){
            val word = data.getStringExtra("word")?:""
            val def = data.getStringExtra("def")?:""

            Log.d("MAD", word)
            Log.d("MAD", def)

            if ( word == "" || def == "")
                return
            addToUser(file.absolutePath, word, def)
            //wordDefinition.add(WordDefinition(word, def))
            loadWordsFromDisk()
            pickNewWordAndLoadDataList()
            myAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MyActivity", "onDestroy called")

        // Delay the finishing of the activity
        Handler(Looper.getMainLooper()).postDelayed({
            // Create and show the file contents
            val file = File(applicationContext.filesDir, "user_stats.csv")
            saveToUserStats(file.absolutePath, score, totalCorrect, totalWrong, lstreak)
            displayStats(file.absolutePath)

            // After the delay, finish the activity (destroy it)
            finish() // This will close the activity after 5 seconds
        }, 5000) // Delay in milliseconds (5 seconds)
    }

    override fun onBackPressed() {
        Log.d("MyActivity", "Back button pressed")

        try {
            // Get the root layout
            val rootView = findViewById<ConstraintLayout>(R.id.main) // Update to your layout type
            val fileTextView = findViewById<TextView>(R.id.statsTextView) // Update to the actual TextView ID

            if (rootView != null) {
                for (i in 0 until rootView.childCount) {
                    val view = rootView.getChildAt(i)

                    // Hide everything EXCEPT the TextView where the file content is displayed
                    if (view.id != R.id.statsTextView) {
                        view.visibility = View.GONE
                    }
                }
            } else {
                Log.e("MyActivity", "Root view not found!")
            }

            // Wait 1 second before displaying the file content
            Handler(Looper.getMainLooper()).postDelayed({
                val file = File(applicationContext.filesDir, "user_stats.csv")
                saveToUserStats(file.absolutePath, score, totalCorrect, totalWrong, lstreak)
                displayStats(file.absolutePath) // This should now be visible

                // Ensure the TextView is VISIBLE so it can display the file content
                fileTextView.visibility = View.VISIBLE

                // Now delay the finish for 5 seconds (activity stays on screen)
                Handler(Looper.getMainLooper()).postDelayed({
                    super.onBackPressed()
                }, 5000) // 5-second delay to close app after showing file

            }, 100) // 1-second delay before displaying the file

        } catch (e: Exception) {
            Log.e("MyActivity", "Error during onBackPressed: ${e.message}")
            e.printStackTrace()
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

    private fun addToUser(filePath: String, word: String, def: String)
    {
        val file = File(filePath)
        try {
            BufferedWriter(FileWriter(file, true)).use { writer -> // 'true' for append mode
                writer.write(word)
                writer.write("|")
                writer.write(def)
                writer.newLine()
            }
        } catch (e: IOException)
        {
            return
        }
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
        var myIntent = Intent(this, StatsActivity::class.java);
        myIntent.putExtra("score", score.toString());
        myIntent.putExtra("totalCorrect", totalCorrect.toString());
        myIntent.putExtra("totalWrong", totalWrong.toString());
        myIntent.putExtra("lstreak", lstreak.toString());
        startActivity(myIntent)
    }

    private fun saveToUserStats(filePath: String, score: Int, correct: Int, incorrect: Int, streak: Int)
    {
        val file = File(filePath)
        try {
            BufferedWriter(FileWriter(file, false)).use { writer -> // 'true' for append mode
                writer.write("Final score: " + "$score\n\n")
                writer.write("Correct Answers: " + "$correct\n\n")
                writer.write("Incorrect Answers: " + "$incorrect\n\n")
                writer.write("Highest Streak: " + "$streak\n\n")
            }
        } catch (e: IOException)
        {
            return
        }
    }


    private fun displayStats(filePath: String) {
        val file = File(filePath)
        val statsTextView = findViewById<TextView>(R.id.statsTextView)

        if (file.exists()) {
            val stats = StringBuilder()
            file.forEachLine { line ->
                stats.append("$line\n")
            }
            statsTextView.text = stats.toString()
            Log.d("MyActivity", "Displaying file content from: $filePath")
        } else {
            statsTextView.text = "File does not exist."
        }
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
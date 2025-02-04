package com.example.tsiisware

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL

class EndQuizActivity : AppCompatActivity() {
    var correctQuestions: Int? = null
    var wrongQuestions: Int? = null
    var visitorsEmail: String? = null
    var visitorsName: String? = null
    private var btnSendEmail: Button? = null
    private var btnGoToStart: Button? = null

    private var numGoedTxt: TextView? = null
    private var numFoutTxt: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_quiz_end)
        numGoedTxt = findViewById(R.id.numGoedTxt)
        numFoutTxt = findViewById(R.id.numFoutTxt)

        val sharedPreferences = getSharedPreferences("quizData", MODE_PRIVATE)
        correctQuestions = sharedPreferences.getInt("correctQuestions", 0)
        wrongQuestions = sharedPreferences.getInt("wrongQuestions", 0)
        visitorsName = sharedPreferences.getString("name", "Unknown")
        this.numGoedTxt!!.text = correctQuestions.toString()
        with(numFoutTxt) { this?.setText(wrongQuestions.toString()) }

        Log.d("EndQuizActivity", correctQuestions.toString())
        Log.d("EndQuizActivity", wrongQuestions.toString())
        Log.d("EndQuizActivity", visitorsName.toString())
        btnSendEmail = findViewById(R.id.btnSendEmail)

        btnGoToStart = findViewById(R.id.btnToStart)
        btnGoToStart!!.setOnClickListener {
            startActivity(
                Intent(
                    this@EndQuizActivity,
                    UserMainActivity::class.java
                )
            )
        }
        btnSendEmail!!.setOnClickListener { sendEmail() }
    }

    private fun sendEmail() {
        visitorsEmail = findViewById<EditText>(R.id.visitorsEmailAddress).text.toString()
        Log.d("EndQuizActivity", visitorsEmail.toString())
        if (visitorsEmail == null || visitorsEmail!!.isEmpty()) {
            Toast.makeText(this, "Please enter an email address", Toast.LENGTH_SHORT).show()
            return
        }

        postWithFormData(
            "http://10.168.139.122:5000/quizsend",
            object : HashMap<String?, String?>() {
                init {
                    put("naam", visitorsName)
                    put("email", visitorsEmail)
                    put("correct_vragen", correctQuestions.toString())
                    put("aantal_vragen", (correctQuestions!! + wrongQuestions!!).toString())
                }
            })
    }

    private fun postWithFormData(url: String, params: HashMap<String?, String?>) {
        Thread {
            try {
                val urlObj = URL(url)
                val httpURLConnection = urlObj.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.setRequestProperty(
                    "Content-Type",
                    "application/x-www-form-urlencoded"
                )
                httpURLConnection.doOutput = true

                var formData = ""
                for ((key, value) in params) {
                    formData += "$key=$value&"
                }

                httpURLConnection.outputStream.use { os ->
                    val input = formData.toByteArray(charset("utf-8"))
                    os.write(input, 0, input.size)
                }
                val responseCode = httpURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Results sent successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    // clear visitorsEmail
                    visitorsEmail = ""
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Error sending results: $responseCode", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(
                        this,
                        "Error: " + e.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }
}

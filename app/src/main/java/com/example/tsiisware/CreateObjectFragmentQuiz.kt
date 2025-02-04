// CreateObjectFragment.kt
package com.example.tsiisware

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CreateObjectFragmentQuiz : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_create_object_quiz, container, false)

        val etObjectName = view.findViewById<EditText>(R.id.etObjectName)
        val etObjectVideoURL = view.findViewById<EditText>(R.id.etObjectVideoURL)
        val etQuestion = view.findViewById<EditText>(R.id.etQuestion)
        val etAnswer1 = view.findViewById<EditText>(R.id.etAnswer1)
        val etAnswer2 = view.findViewById<EditText>(R.id.etAnswer2)
        val etAnswer3 = view.findViewById<EditText>(R.id.etAnswer3)
        val etAnswer4 = view.findViewById<EditText>(R.id.etAnswer4)
        val etUitleg = view.findViewById<EditText>(R.id.etUitleg)
        val correctAnswer = view.findViewById<Spinner>(R.id.etCorrectAnswer)
        val btnCreateObject = view.findViewById<Button>(R.id.btnCreateObject)

        val answerFields = listOf(etAnswer1, etAnswer2, etAnswer3, etAnswer4) //List of answers
        val otherInputFields = listOf(etObjectName, etObjectVideoURL, etUitleg) //List of extra input fields

        val emptyFields = {
            //Goes through each list item
            answerFields.forEach {
                it.setText("")
            }

            otherInputFields.forEach {
                it.setText("")
            }
        }

        val updateSpinner = {
            val answers = answerFields.map { it.text.toString() }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, answers)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            correctAnswer.adapter = adapter
        }

        answerFields.forEach { editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    updateSpinner()
                }
                override fun afterTextChanged(s: Editable?) {}
            })
        }

        btnCreateObject.setOnClickListener {
            try {
                db.collection("quiz_objects").document(etObjectName.text.toString()).set(
                    hashMapOf(
                        "label" to etObjectName.text.toString(),
                        "video_url" to etObjectVideoURL.text.toString(),
                        "question" to etQuestion.text.toString(),
                        "explanation" to etUitleg.text.toString(),
                        "answers" to arrayListOf(
                            etAnswer1.text.toString(),
                            etAnswer2.text.toString(),
                            etAnswer3.text.toString(),
                            etAnswer4.text.toString()
                        ),
                        "correct_answer" to correctAnswer.selectedItem.toString()
                    )
                )

            Toast.makeText(activity, "Quiz object aangemaakt", Toast.LENGTH_SHORT).show() //Pop-Up
            emptyFields() //Function to empty the input fields
            val intent = Intent(activity, this::class.java)
            startActivity(intent)
            }
            catch (e: Exception)
            {
                System.out.println("Error occurred: $e")
            }
        }

//        Make QR code and print it out.

        return view
    }
}
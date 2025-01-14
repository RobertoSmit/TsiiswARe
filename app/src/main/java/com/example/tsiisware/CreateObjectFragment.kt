// CreateObjectFragment.kt
package com.example.tsiisware

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CreateObjectFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_create_object, container, false)

        val etObjectName = view.findViewById<EditText>(R.id.etObjectName)
        val etObjectVideoURL = view.findViewById<EditText>(R.id.etObjectVideoURL)
        val etObjectImageURL = view.findViewById<EditText>(R.id.etObjectImageURL)
        val etObjectDescription = view.findViewById<EditText>(R.id.etObjectDescription)
        val etUitlegVroeger = view.findViewById<EditText>(R.id.etVroeger)
        val etUitlegNu = view.findViewById<EditText>(R.id.etNu)
        val btnCreateObject = view.findViewById<Button>(R.id.btnCreateObject)
        val pastPresenCheck = view.findViewById<CheckBox>(R.id.PastPresentCheck)
        var selectStatus = false

        //List of all input fields
        val inputFields = listOf(etObjectName, etObjectDescription, etObjectVideoURL, etObjectImageURL, etUitlegVroeger, etUitlegNu)

        val setBool =
        {
            if (pastPresenCheck.isChecked()) {
                selectStatus = true
            } else {
                selectStatus = false
            }
        }

        //Empties the input fields
        val emptyFields = {
            //Goes through each list item
            inputFields.forEach {
                it.setText("")
            }
        }

        //Checks whether the checkbox is checked
        val ifChecked = {
            if (pastPresenCheck.isChecked()) {
                pastPresenCheck.setChecked(false)
            }
        }

        btnCreateObject.setOnClickListener {
            try {
                setBool()
                db.collection("video_objects").document(etObjectName.text.toString()).set(
                    hashMapOf(
                        "label" to etObjectName.text.toString(),
                        "video_url" to etObjectVideoURL.text.toString(),
                        "image_url" to etObjectImageURL.text.toString(),
                        "isPastPresent" to selectStatus,
                        "description" to etObjectDescription.text.toString(),
                        "description_past" to etUitlegVroeger.text.toString(),
                        "description_present" to etUitlegNu.text.toString(),
                    )
                )
                Toast.makeText(activity, "Video object aangemaakt", Toast.LENGTH_SHORT).show()
                emptyFields()
                ifChecked()
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
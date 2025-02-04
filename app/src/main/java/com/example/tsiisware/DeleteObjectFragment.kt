package com.example.tsiisware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class DeleteObjectFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var spinnerObjects: Spinner
    private lateinit var btnDeleteObject: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delete_object, container, false)

        db = FirebaseFirestore.getInstance()

        spinnerObjects = view.findViewById(R.id.spinnerObjects)
        btnDeleteObject = view.findViewById(R.id.btnDeleteObjects)

        loadObjectsIntoSpinner()

        btnDeleteObject.setOnClickListener {
            val selectedObject = spinnerObjects.selectedItem.toString()

            db.collection("video_objects").document(selectedObject).delete()
            db.collection("quiz_objects").document(selectedObject).delete()
            db.collection("objects").document(selectedObject).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Object successfully deleted", Toast.LENGTH_SHORT).show()
                    loadObjectsIntoSpinner()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error deleting object", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }

    private fun loadObjectsIntoSpinner() {
        db.collection("video_objects").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val objects = mutableListOf<String>()
                    for (document in task.result!!) {
                        objects.add(document.getString("label") ?: "")
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        objects
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerObjects.adapter = adapter
                } else {
                    Toast.makeText(context, "Error fetching objects", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
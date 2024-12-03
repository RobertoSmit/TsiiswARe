package com.example.tsiisware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class DeleteUserFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var spinnerUsers: Spinner
    private lateinit var btnDeleteUser: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delete_user, container, false)

        db = FirebaseFirestore.getInstance()

        spinnerUsers = view.findViewById(R.id.userSpinner)
        btnDeleteUser = view.findViewById(R.id.btnDeleteUser)

        loadUsersIntoSpinner()

        btnDeleteUser.setOnClickListener {
            val selectedUser = spinnerUsers.selectedItem.toString()


            db.collection("users").document(selectedUser).delete()
                .addOnSuccessListener {
                    Toast.makeText(context, "Gebruiker succesvol verwijderd", Toast.LENGTH_SHORT).show()
                    loadUsersIntoSpinner()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Fout bij het verwijderen van gebruiker", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }

    private fun loadUsersIntoSpinner() {
        db.collection("users").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val users = mutableListOf<String>()
                    for (document in task.result!!) {
                        users.add(document.getString("username") ?: "")
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        users
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerUsers.adapter = adapter
                } else {
                    Toast.makeText(context, "Fout bij het ophalen van gebruikers", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
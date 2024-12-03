package com.example.tsiisware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class CreateUserFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var spinnerRole: Spinner
    private lateinit var btnCreateUser: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_user, container, false)

        db = FirebaseFirestore.getInstance()

        etUsername = view.findViewById(R.id.usernameEditText)
        etPassword = view.findViewById(R.id.passwordEditText)
        btnCreateUser = view.findViewById(R.id.createUserButton)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.roles_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        btnCreateUser.setOnClickListener {
            val username = etUsername.text.toString()
            val password = etPassword.text.toString()

            val user = hashMapOf(
                "username" to username,
                "password" to password,
                "role" to "Admin"
            )

            db.collection("users").document(username).set(user)
                .addOnSuccessListener {
                    Toast.makeText(context, "Gebruiker succesvol aangemaakt", Toast.LENGTH_SHORT).show()
                    etUsername.text.clear()
                    etPassword.text.clear()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Fout bij het aanmaken van gebruiker", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}
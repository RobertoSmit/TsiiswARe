package com.example.tsiisware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import at.favre.lib.crypto.bcrypt.BCrypt

class CreateUserFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
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

        btnCreateUser.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Vul alle velden in", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val hashedPassword = BCrypt.withDefaults().hashToString(12, password.toCharArray())

            val user = hashMapOf(
                "username" to username,
                "password" to hashedPassword,
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

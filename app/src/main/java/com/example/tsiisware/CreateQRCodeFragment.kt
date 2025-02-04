package com.example.tsiisware

import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

/**
 * A simple [Fragment] subclass.
 * Use the [CreateQRCodeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateQRCodeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private lateinit var db: FirebaseFirestore
    private lateinit var btnCreateQRCode: Button
    private lateinit var spinnerObjects: Spinner
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_qr, container, false)
        val url = "http://10.168.139.122:5000/qr_create"
        var params = mapOf("label" to "")
        db = FirebaseFirestore.getInstance()

        btnCreateQRCode = view.findViewById(R.id.btnCreateQR)
        spinnerObjects = view.findViewById(R.id.spinnerObjectsQR)

        loadObjectsIntoSpinner()

        btnCreateQRCode.setOnClickListener {
            postWithFormData(url, params)
        }

        spinnerObjects.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                params = mapOf("label" to spinnerObjects.selectedItem.toString())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        // Return the inflated view
        return view
    }

    private fun postWithFormData(url: String, params: Map<String, String>) {
        thread {
            try {
                val urlObj = URL(url)
                val httpURLConnection = urlObj.openConnection() as HttpURLConnection
                httpURLConnection.requestMethod = "POST"
                httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                httpURLConnection.doOutput = true

                val formData = params.map { "${it.key}=${it.value}" }.joinToString("&")

                httpURLConnection.outputStream.use { os: OutputStream ->
                    val input = formData.toByteArray()
                    os.write(input, 0, input.size)
                }

                val responseCode = httpURLConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    httpURLConnection.inputStream.bufferedReader().use { reader ->
                        val response = reader.readText()
                        Log.d("CreateQRCodeFragment", "Response: $response")
                        // Handle the response here
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Request successful: $response", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("CreateQRCodeFragment", "Error in request: $responseCode")
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Error in request: $responseCode", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("CreateQRCodeFragment", "Error in request: ${e.message}")
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error in request: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun loadObjectsIntoSpinner() {
        val videoObjectsTask = db.collection("video_objects").get()
        val quizObjectsTask = db.collection("quiz_objects").get()

        Tasks.whenAllSuccess<QuerySnapshot>(videoObjectsTask, quizObjectsTask)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val objects = mutableSetOf<String>()
                    val videoObjects = task.result[0]
                    val quizObjects = task.result[1]

                    for (document in videoObjects) {
                        objects.add(document.getString("label") ?: "")
                    }
                    for (document in quizObjects) {
                        objects.add(document.getString("label") ?: "")
                    }

                    val adapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        objects.toList()
                    )
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerObjects.adapter = adapter
                } else {
                    Toast.makeText(context, "Error fetching objects", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
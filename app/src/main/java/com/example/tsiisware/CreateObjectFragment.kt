// CreateObjectFragment.kt
package com.example.tsiisware

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.text.set
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream

class CreateObjectFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_create_object, container, false)

        val etObjectName = view.findViewById<EditText>(R.id.etObjectName)
        val etObjectVideoURL = view.findViewById<EditText>(R.id.etObjectVideoURL)
        val etObtjectVideoURLPast = view.findViewById<EditText>(R.id.etObjectVideoURLPast)
        val etObtjectVideoURLPresent = view.findViewById<EditText>(R.id.etObjectVideoURLPresent)
        val etObjectImageURL = view.findViewById<EditText>(R.id.etObjectImageURL)
        val imageSelectBtn = view.findViewById<Button>(R.id.imageSelectButton)
        val etObjectDescription = view.findViewById<EditText>(R.id.etObjectDescription)
        val etUitlegVroeger = view.findViewById<EditText>(R.id.etVroeger)
        val etUitlegNu = view.findViewById<EditText>(R.id.etNu)
        val btnCreateObject = view.findViewById<Button>(R.id.btnCreateObject)
        val pastPresenCheck = view.findViewById<CheckBox>(R.id.PastPresentCheck)
        var selectStatus = false
        val SELECT_PICTURE = 200

        //List of all input fields
        val inputFields = listOf(etObjectName, etObjectDescription, etObjectVideoURL, etObjectImageURL, etUitlegVroeger, etUitlegNu)

        val setBool =
        {
            if (pastPresenCheck.isChecked()) {
                selectStatus = true //Checkbox is selected
            } else {
                selectStatus = false //Checkbox is not selected
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

        // Function to get the real file path from the content URI
        fun getRealPathFromURI(contentUri: Uri): String? {
            var filePath: String? = null
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context?.contentResolver?.query(contentUri, proj, null, null, null)

            cursor?.let {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                if (it.moveToFirst()) {
                    filePath = it.getString(columnIndex)
                }
                it.close()
            }
            return filePath
        }

        val changeImage = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){
            result->
            if (result.resultCode == Activity.RESULT_OK)
            {
                val context: Context
                val data = result.data //Retrieves all data of the Intent
                val imgUri = data?.data //Retrieve URI data
                imgUri?.let {
                    val imagePath = getRealPathFromURI(it)
                    etObjectImageURL.setText(imgUri.toString()) //Sets the image Uri as text in the ImageURL input field
                }
            }
        }

        val selectFromGallery = {
            //Setup the gallery
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            galleryIntent.type = "image/*"
            changeImage.launch(galleryIntent); //Launch the gallery
        }

        btnCreateObject.setOnClickListener {
            try {
                setBool()
                //Database setup
                db.collection("video_objects").document(etObjectName.text.toString()).set(
                    hashMapOf(
                        //Takes the form data and links it to the database variables.
                        "label" to etObjectName.text.toString(),
                        "video_url" to etObjectVideoURL.text.toString(),
                        "video_url_past" to etObtjectVideoURLPast.text.toString(),
                        "video_url_present" to etObtjectVideoURLPresent.text.toString(),
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

        imageSelectBtn.setOnClickListener {
            selectFromGallery()
        }



//        Make QR code and print it out.

        return view
    }

}
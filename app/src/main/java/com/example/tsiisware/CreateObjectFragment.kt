// CreateObjectFragment.kt
package com.example.tsiisware

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class CreateObjectFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val db = FirebaseFirestore.getInstance()
        val view = inflater.inflate(R.layout.fragment_create_object, container, false)

        val etObjectName = view.findViewById<EditText>(R.id.etObjectName)
        val etObjectVideoURLPast = view.findViewById<EditText>(R.id.etObjectVideoURLPast)
        val etObjectVideoURLPresent = view.findViewById<EditText>(R.id.etObjectVideoURLPresent)
        val etObjectImageURLPast = view.findViewById<EditText>(R.id.etObjectImageURLPast)
        val etObjectImageURLPresent = view.findViewById<EditText>(R.id.etObjectImageURLPresent)
        val imageSelectBtnPast = view.findViewById<Button>(R.id.imageSelectButtonPast)
        val imageSelectBtnPresent = view.findViewById<Button>(R.id.imageSelectButtonPresent)
        val etObjectDescription = view.findViewById<EditText>(R.id.etObjectDescription)
        val etUitlegVroeger = view.findViewById<EditText>(R.id.etVroeger)
        val etUitlegNu = view.findViewById<EditText>(R.id.etNu)
        val btnCreateObject = view.findViewById<Button>(R.id.btnCreateObject)
        val pastPresenCheck = view.findViewById<CheckBox>(R.id.PastPresentCheck)
        var selectStatus = false
        val SELECT_PICTURE = 200
        var currentEditText: EditText? = null

        //List of all input fields
        val inputFields = listOf(etObjectName, etObjectDescription, etObjectVideoURLPast, etObjectVideoURLPresent, etObjectImageURLPast, etObjectImageURLPresent, etUitlegVroeger, etUitlegNu)

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
            val appDir = File(context?.filesDir, "images")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val destfile = File(appDir, fileName)

            try {
                context?.contentResolver?.openInputStream(contentUri)?.use { InputStream ->
                    FileOutputStream(destfile).use { OutputStream ->
                        val buffer = ByteArray(1024)
                        var bytesRead: Int
                        while (InputStream.read(buffer).also { bytesRead = it } != -1) {
                            OutputStream.write(buffer, 0, bytesRead)
                        }
                    }
                }
                return destfile.absolutePath
            }
            catch (e: IOException)
            {
                e.printStackTrace()
            }
            return null
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
                    currentEditText?.setText(imagePath.toString()) //Sets the image Uri as text in the ImageURL input field
                }
            }
        }

        fun selectFromGallery(targetEditText: EditText) {
            //Setup the gallery
            currentEditText = targetEditText
            val galleryIntent = Intent(Intent.ACTION_GET_CONTENT)
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
                        "video_url_past" to etObjectVideoURLPast.text.toString(),
                        "video_url_present" to etObjectVideoURLPresent.text.toString(),
                        "image_url_past" to etObjectImageURLPast.text.toString(),
                        "image_url_present" to etObjectImageURLPresent.text.toString(),
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

        imageSelectBtnPast.setOnClickListener(){
            selectFromGallery(etObjectImageURLPast)
        }

        imageSelectBtnPresent.setOnClickListener(){
            selectFromGallery((etObjectImageURLPresent))
        }


//        Make QR code and print it out.

        return view
    }

}
package com.example.tsiisware

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.SurfaceTexture
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class QR_Activity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager
    private lateinit var handler: Handler
    private lateinit var category: String
    private lateinit var logoff: Button
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private var db: FirebaseFirestore? = null
    private lateinit var objects: CollectionReference
    private val scannedObjects = ArrayList<String>()
    private var popupVisible: Boolean = false
    private val dblabels = mutableListOf<String>()
    private var correctQuestions: Int = 0
    private var wrongQuestions: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ar_view)


        category = intent.getStringExtra("category")!!
        if (category == "quiz") {
            correctQuestions = intent.getIntExtra("correctQuestions", 0)
            wrongQuestions = intent.getIntExtra("wrongQuestions", 0)
            scannedObjects.addAll(intent.getStringArrayListExtra("scannedLabels")!!)
        }
        db = FirebaseFirestore.getInstance()
        objects = db!!.collection("objects")
        getAllDocumentNames()

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = surfaceTextureListener

        val handlerThread = HandlerThread("CameraThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        logoff = findViewById(R.id.btnLogoffAR)
        logoff.setOnClickListener {
            val intent = Intent(this, UserMainActivity::class.java)
            startActivity(intent)
        }
    }

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            if (!popupVisible) scanQRCode()
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                createCameraPreviewSession()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
            }
        }, handler)
    }

    private fun createCameraPreviewSession() {
        val surfaceTexture = textureView.surfaceTexture
        surfaceTexture?.setDefaultBufferSize(textureView.width, textureView.height)
        val surface = Surface(surfaceTexture)

        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(surface)

        cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                captureSession = session
                captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Toast.makeText(this@QR_Activity, "Camera configuration failed", Toast.LENGTH_SHORT).show()
            }
        }, handler)
    }

    private fun scanQRCode() {
        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        captureRequestBuilder.addTarget(Surface(textureView.surfaceTexture))
        val bitmap = textureView.bitmap ?: return
        val image = InputImage.fromBitmap(bitmap, 0)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val scanner = BarcodeScanning.getClient()

        imageView.setImageBitmap(bitmap)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                for (barcode in barcodes) {
                    if (barcode.valueType == Barcode.TYPE_TEXT) {
                        val label = barcode.displayValue ?: continue
                        Log.d("QR_Activity", "Scanne QR code: $label")
                        if (dblabels.contains(label.lowercase()) && !scannedObjects.contains(label.lowercase()) && !popupVisible) {
                            scannedObjects.add(label)
                            showPopup(label)
                            popupVisible = true
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QR_Activity", "QR code scanning failed", it)
            }
    }

    private fun showPopup(label: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_ar, null)

        val popupWindow = Dialog(this)
        popupWindow.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.setContentView(popupView)

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(popupWindow.window?.attributes)
        layoutParams.width = 800
        layoutParams.height = 500
        layoutParams.gravity = Gravity.TOP
        layoutParams.x = 0
        layoutParams.y = 350

        popupWindow.setCancelable(false)
        popupWindow.show()
        popupWindow.window?.attributes = layoutParams

        val popupText = popupView.findViewById<TextView>(R.id.popupTitle)
        popupText.text = popupText.text.toString() + label

        val popupClose = popupView.findViewById<Button>(R.id.btnClosePopup)
        val popupGo = popupView.findViewById<Button>(R.id.btnGoToInformationView)
        popupClose.setOnClickListener {
            popupWindow.dismiss()
            popupVisible = false
        }
        popupGo.setOnClickListener {
            if (!scannedObjects.contains(label)) {
                val sharedPreferences = getSharedPreferences("scanned_objects_prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putStringSet("scanned_objects", scannedObjects.toSet())
                editor.apply()
            }
            val intent = Intent(this, InformationActivity::class.java)
            intent.putExtra("label", label)
            intent.putExtra("category", category)
            if (category == "quiz") {
                intent.putStringArrayListExtra("gescandeObjecten", scannedObjects)
                intent.putExtra("label", label.lowercase())
                intent.putExtra("correctQuestions", correctQuestions)
                intent.putExtra("wrongQuestions", wrongQuestions)
                intent.putStringArrayListExtra("scannedLabels", ArrayList(scannedObjects))
            }
            startActivity(intent)
        }
    }

    private fun getAllDocumentNames() {
        objects.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentNames = task.result?.documents?.map { it.id }
                documentNames?.forEach { dblabels.add(it) }
            } else {
                Log.w("FirestoreError", "Error getting documents.", task.exception)
            }
        }
    }
}
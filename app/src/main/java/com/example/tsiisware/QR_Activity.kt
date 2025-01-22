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
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.barcode.Barcode
import android.os.Looper
import android.widget.ImageView

class QR_Activity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var cameraManager: CameraManager
    private lateinit var handler: Handler
    private lateinit var category: String
    private lateinit var logoff: Button
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var db: FirebaseFirestore? = null
    private lateinit var objects: CollectionReference
    private var scannedObjects = ArrayList<String>()
    private var popupVisible: Boolean = false
    private val dblabels = mutableListOf<String>()
    private var correctQuestions: Int = 0
    private var wrongQuestions: Int = 0
    private var scanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.qr_view)

        category = intent.getStringExtra("category")!!
        db = FirebaseFirestore.getInstance()
        scannedObjects = intent.getStringArrayListExtra("scannedLabels") ?: ArrayList()
        Log.d("ScannedObjects", scannedObjects.toString())
        if (category == "Quiz") {
            correctQuestions = intent.getIntExtra("correctQuestions", 0)
            wrongQuestions = intent.getIntExtra("wrongQuestions", 0)
            objects = db!!.collection("quiz_objects")
        } else {
            objects = db!!.collection("video_objects")
        }

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

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            closeCamera()
            return true
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            if (!popupVisible) scanQRCode()
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            cameraManager.openCamera(cameraManager.cameraIdList[0], object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    createCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    cameraDevice = null
                }
            }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun createCameraPreviewSession() {
        try {
            val surfaceTexture = textureView.surfaceTexture
            surfaceTexture?.setDefaultBufferSize(textureView.width, textureView.height)
            val surface = Surface(surfaceTexture)

            val captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            cameraDevice!!.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    captureSession!!.setRepeatingRequest(captureRequestBuilder.build(), null, handler)
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Toast.makeText(this@QR_Activity, "Camera configuration failed", Toast.LENGTH_SHORT).show()
                    captureSession?.close()
                    captureSession = null
                }
            }, handler)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
    }

    private fun scanQRCode() {
        if (scanning) return

        scanning = true
        Toast.makeText(this, "Scanning QR code", Toast.LENGTH_LONG).show()
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

                        if (!scannedObjects.contains(label) && !popupVisible) {
                            Log.d("Not_Scanned", "New QR code scanned: $label")
                            showPopup(label)
                            popupVisible = true
                        } else if (scannedObjects.contains(label) && !popupVisible) {
                            Log.d("Already_Scanned", "QR code already scanned: $label")
                            showPopupAlreadyScanned()
                            popupVisible = true
                        }
                    }
                }
            }
            .addOnFailureListener {
                Log.e("QR_Activity", "QR code scanning failed", it)
            }
        scanning = false
    }

    private fun showPopup(label: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_qr, null)

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
        popupText.text = getString(R.string.popup_text) + label  // Gebruik de string uit strings.xml

        val popupClose = popupView.findViewById<Button>(R.id.btnClosePopup)
        val popupGo = popupView.findViewById<Button>(R.id.btnGoToInformationView)

        //If the popup closes it needs to wait 2 seconds before it can be opened again
        popupClose.setOnClickListener {
            popupWindow.dismiss()
            popupVisible = false
            if (!textureView.isAvailable) {
                textureView.surfaceTextureListener = surfaceTextureListener
            }
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("Popup", "Popup can be opened again")
            }, 1000)
        }

        popupGo.setOnClickListener {
            if (!scannedObjects.contains(label)) {
                scannedObjects.add(label)
            }

            val intent = Intent(this, InformationActivity::class.java)
            intent.putExtra("label", label.lowercase())
            intent.putExtra("category", category)
            intent.putStringArrayListExtra("scannedLabels", scannedObjects)
            if (category == "Quiz") {
                intent.putExtra("correctQuestions", correctQuestions)
                intent.putExtra("wrongQuestions", wrongQuestions)
            }
            startActivity(intent)
        }
    }

    private fun showPopupAlreadyScanned() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_qr_already_scanned, null)

        val popupWindow = Dialog(this)
        popupWindow.window?.setBackgroundDrawable(ColorDrawable(Color.WHITE))
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

        val popupText = popupView.findViewById<TextView>(R.id.popupAlreadyScannedText)
        popupText.text = getString(R.string.already_scanned_text)  // Gebruik de string uit strings.xml

        val popupClose = popupView.findViewById<Button>(R.id.btnClosePopupAlreadyScanned)

        popupClose.setOnClickListener {
            popupWindow.dismiss()
            popupVisible = false
            if (!textureView.isAvailable) {
                textureView.surfaceTextureListener = surfaceTextureListener
            }
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d("Popup", "Popup can be opened again")
            }, 1000)
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

    override fun onPause() {
        super.onPause()
        closeCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        closeCamera()
    }
}
// AR_Activity.kt
package com.example.tsiisware

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.util.Range
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tsiisware.ml.SsdMobilenetV11Metadata1
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.io.ByteArrayOutputStream
import java.io.IOException

class AR_Activity : AppCompatActivity() {

    lateinit var labels: List<String>
    val paint = Paint()
    lateinit var imageProcessor: ImageProcessor
    lateinit var bitmap: Bitmap
    lateinit var imageView: ImageView
    lateinit var cameraDevice: CameraDevice
    lateinit var handler: Handler
    lateinit var cameraManager: CameraManager
    lateinit var textureView: TextureView
    lateinit var model: SsdMobilenetV11Metadata1
    lateinit var correctQuestions: Number
    lateinit var wrongQuestions: Number
    lateinit var scannedLabels: List<String>
    lateinit var objects: CollectionReference

    private val client = OkHttpClient()
    private var db: FirebaseFirestore? = null
    private val scannedObjects = mutableListOf<String>()
    private val interval: Long = 500 // 500 milliseconds
    private var category: String? = null
    private var currentDetectionIndex: Int = 0
    private var detectionCount: Int = 0
    private var popupVisible: Boolean = false
    private var dblabels = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ar_view)

        db = FirebaseFirestore.getInstance()
        //get all document names from the collection
        objects = db!!.collection("objects")

        getAllDocumentNames()

        category = intent.getStringExtra("category")

        }

    fun getAllDocumentNames() {
        objects.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val documentNames = task.result?.documents?.map { it.id }
                Log.d("FirestoreSuccess", "Document names: $documentNames")
                documentNames?.forEach { dblabels.add(it) }
            } else {
                Log.w("FirestoreError", "Error getting documents.", task.exception)
            }
        }

        if (category == "quiz") {
            correctQuestions = intent.getIntExtra("correctQuestions", 0)
            wrongQuestions = intent.getIntExtra("wrongQuestions", 0)
        }

        val btnLogoffAR = findViewById<Button>(R.id.btnLogoffAR)
        btnLogoffAR.setOnClickListener {
            val intent = Intent(this, UserMainActivity::class.java)
            startActivity(intent)
        }

        labels = FileUtil.loadLabels(this, "labels.txt")
        imageProcessor = ImageProcessor.Builder().add(ResizeOp(300, 300, ResizeOp.ResizeMethod.BILINEAR)).build()
        model = SsdMobilenetV11Metadata1.newInstance(this)
        val handlerThread = HandlerThread("videoThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        imageView = findViewById(R.id.imageView)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(p0: SurfaceTexture, p1: Int, p2: Int) {
                open_camera()
                startImageUpload() // Start uploading images
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, p1: Int, p2: Int) {}
            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                return false
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
                handler.post { detectAndDraw() } // Run detection on a background thread
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        // Retrieve scanned objects from SharedPreferences
        val sharedPreferences = getSharedPreferences("quizData", MODE_PRIVATE)
        val scannedObjectsSet = sharedPreferences.getStringSet("scanned_objects", emptySet()) ?: emptySet()
        scannedObjects.addAll(scannedObjectsSet)

        // Schedule detection switching
        val mainHandler = Handler(Looper.getMainLooper())
        mainHandler.post(object : Runnable {
            override fun run() {
                if (detectionCount > 0) {
                    currentDetectionIndex = (currentDetectionIndex + 1) % detectionCount
                }
                mainHandler.postDelayed(this, 2000)
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        model.close()
    }

    @SuppressLint("MissingPermission")
    fun open_camera(){
        cameraManager.openCamera(cameraManager.cameraIdList[0], object:CameraDevice.StateCallback(){
            override fun onOpened(p0: CameraDevice) {
                cameraDevice = p0

                val surfaceTexture = textureView.surfaceTexture
                val surface = Surface(surfaceTexture)

                val captureRequest = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                captureRequest.addTarget(surface)
                captureRequest.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, Range(30, 30))

                cameraDevice.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(p0: CameraCaptureSession) {
                        p0.setRepeatingRequest(captureRequest.build(), null, null)
                    }

                    override fun onConfigureFailed(p0: CameraCaptureSession) {}
                }, handler)
            }

            override fun onDisconnected(p0: CameraDevice) {}
            override fun onError(p0: CameraDevice, p1: Int) {}
        }, handler)
    }

    private fun showPopup(label: String) {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.popup_ar, null)
        val popupWidth = 800
        val popupHeight = 400

        val popupWindow = PopupWindow(popupView, popupWidth, popupHeight, true)
        popupWindow.showAtLocation(findViewById(R.id.arView), Gravity.CENTER, 0, 0)

        val popupText = popupView.findViewById<TextView>(R.id.popupTitle)
        popupText.text = popupText.text.toString() + " " + label

        val popupClose = popupView.findViewById<Button>(R.id.btnClosePopup)
        val popupGo = popupView.findViewById<Button>(R.id.btnGoToInformationView)
        popupClose.setOnClickListener {
            popupWindow.dismiss()
            popupVisible = false
        }
        popupGo.setOnClickListener {
            if (!scannedObjects.contains(label)) {
                // Save scanned objects to SharedPreferences
                val sharedPreferences = getSharedPreferences("scanned_objects_prefs", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putStringSet("scanned_objects", scannedObjects.toSet())
                editor.apply()
            }
            val intent = Intent(this, InformationActivity::class.java)
            intent.putExtra("label", label)
            intent.putExtra("category", category)
            if (category == "quiz") {
                intent.putExtra("gescandeObjecten", label)
                intent.putExtra("correctQuestions", correctQuestions)
                intent.putExtra("wrongQuestions", wrongQuestions)
                intent.putStringArrayListExtra("scannedLabels", ArrayList(scannedObjects))
            }
            startActivity(intent)
        }
    }

    private fun startImageUpload() {
        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (::bitmap.isInitialized) {
                    sendImage(bitmap)
                }
                handler.postDelayed(this, interval)
            }
        }
        handler.post(runnable)
    }

    private fun sendImage(bitmap: Bitmap) {
        val url = "http://172.20.10.2:8008/img-detect-tsiisware"

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image", "image.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), bitmapToByteArray(bitmap))
            )
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // Delay in milliseconds
        val delayMillis: Long = 2000 // 2 seconds

        Handler(Looper.getMainLooper()).postDelayed({
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    if (response.isSuccessful) {
                        println("Image upload successful")
                    } else {
                        println("Image upload failed")
                    }
                }
            })
        }, delayMillis)
    }

    private fun detectAndDraw() {
        bitmap = textureView.bitmap!!
        var image = TensorImage.fromBitmap(bitmap)
        image = imageProcessor.process(image)

        val outputs = model.process(image)
        val locations = outputs.locationsAsTensorBuffer.floatArray
        val classes = outputs.classesAsTensorBuffer.floatArray
        val scores = outputs.scoresAsTensorBuffer.floatArray
        val numberOfDetections = outputs.numberOfDetectionsAsTensorBuffer.floatArray

        var mutable = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutable)

        val h = mutable.height
        val w = mutable.width
        paint.textSize = h / 20f
        paint.strokeWidth = h / 100f

        // Ensure the index is within bounds
        val index = currentDetectionIndex % scores.size
        val x = index * 4


        if (scores[index] > 0.5) {
            val detectedLabel = labels[classes[index].toInt()]
            val isInDatabase = dblabels.contains(detectedLabel)
            Log.d("detectedLabel", detectedLabel)
            Log.d("Objects", dblabels.toString())
            Log.d("isInDatabase", isInDatabase.toString())
            if (isInDatabase) {
                if (!scannedObjects.contains(detectedLabel)) {
                    paint.color = Color.YELLOW
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(
                        RectF(
                            locations[x + 1] * w,
                            locations[x] * h,
                            locations[x + 3] * w,
                            locations[x + 2] * h
                        ), paint
                    )
                    paint.style = Paint.Style.FILL
                    canvas.drawText(
                        labels[classes[index].toInt()],
                        locations[x + 1] * w,
                        locations[x] * h,
                        paint
                    )
                    if (!popupVisible) {
                        runOnUiThread {
                            showPopup(labels[classes[index].toInt()])
                        }
                        popupVisible = true
                    }
                } else {
//                Add a checkmark in the box to indicate that the object has already been scanned
                    paint.color = Color.GREEN
                    paint.style = Paint.Style.STROKE
                    canvas.drawRect(
                        RectF(
                            locations[x + 1] * w,
                            locations[x] * h,
                            locations[x + 3] * w,
                            locations[x + 2] * h
                        ), paint
                    )
                    paint.style = Paint.Style.FILL
                    canvas.drawText(
                        labels[classes[index].toInt()],
                        locations[x + 1] * w,
                        locations[x] * h,
                        paint
                    )
                }
            }

            runOnUiThread {
                imageView.setImageBitmap(mutable)
            }
        }
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        // Reduce image size before compressing
        val options = BitmapFactory.Options()
        options.inSampleSize = 2 // Downsample by a factor of 2
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, bitmap.width / 2, bitmap.height / 2, false)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream) // Adjust quality as needed
        val byteArray = stream.toByteArray()
        resizedBitmap.recycle() // Recycle the resized bitmap
        return byteArray
    }
}
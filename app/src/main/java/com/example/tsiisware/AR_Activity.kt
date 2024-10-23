package com.example.tsiisware

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.tsiisware.ml.SsdMobilenetV11Metadata1
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
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

    private val client = OkHttpClient()

    private val interval: Long = 500 // 500 milliseconds
    private var currentDetectionIndex: Int = 0
    private var detectionCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.ar_view)
        get_permission()

        val btnLogoffAR = findViewById<Button>(R.id.btnLogoffAR)
        btnLogoffAR.setOnClickListener(View.OnClickListener {
                setContentView(R.layout.activity_main_user)
        });

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
                detectAndDraw()
            }
        }

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

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

    fun get_permission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 101)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            get_permission()
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
        paint.strokeWidth = h / 100f

        // Ensure the index is within bounds
        val index = currentDetectionIndex % scores.size
        val x = index * 4

        if (scores[index] > 0.5) {
            paint.color = Color.YELLOW
            paint.style = Paint.Style.STROKE // Set the paint style to STROKE to draw only the borders
            // Draw bounding box without labels
            canvas.drawRect(
                locations[x + 1] * w,
                locations[x] * h,
                locations[x + 3] * w,
                locations[x + 2] * h,
                paint
            )
        }

        imageView.setImageBitmap(mutable)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}


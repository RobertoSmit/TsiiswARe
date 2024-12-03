package com.example.tsiisware

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class CameraCreateObjectActivity : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private lateinit var imageView: ImageView
    private lateinit var cameraManager: CameraManager
    private lateinit var handler: Handler
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var btnCapture: Button
    private lateinit var btnBack: Button
    private lateinit var bitmap: Bitmap
    private lateinit var frames: Number
    private lateinit var objectLabel: String
    private val client = OkHttpClient()
    private var isCameraReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_crud_objects_camera)

        objectLabel = intent.getStringExtra("label")!!
        frames = findViewById<TextView>(R.id.frameNumber).text.toString().split(": ")[1].toInt()
        btnCapture = findViewById(R.id.btnCapture)
        btnBack = findViewById(R.id.btnGoBackCreate)
        imageView = findViewById(R.id.imageViewCreate)
        textureView = findViewById(R.id.textureViewCreate)
        textureView.surfaceTextureListener = surfaceTextureListener

        val handlerThread = HandlerThread("CameraThread")
        handlerThread.start()
        handler = Handler(handlerThread.looper)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        btnBack.setOnClickListener {
            val intent = Intent(this, CrudMainActivityObjects::class.java)
            startActivity(intent)
        }

        btnCapture.setOnClickListener {
            cameraDevice.close()
            val intent = Intent(this, CrudMainActivityObjects::class.java)
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
            captureImage()
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
                isCameraReady = true
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                isCameraReady = false
            }
        }, handler)
    }

    private fun captureImage() {
        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
        val surface = Surface(textureView.surfaceTexture)
        captureRequestBuilder.addTarget(surface)

        captureSession.capture(captureRequestBuilder.build(), object : CameraCaptureSession.CaptureCallback() {
            override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                bitmap = textureView.bitmap!!
                runOnUiThread {
                    imageView.setImageBitmap(bitmap)
                }
                sendImageToAPI()
            }
        }, handler)
    }

    private fun sendImageToAPI() {
        try {
            val url = "http://192.168.1.101:8008/get_images"

            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val byteArray = stream.toByteArray()
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "image", "$objectLabel.jpg",
                    byteArray.toRequestBody("image/*".toMediaTypeOrNull())
                )
                .addFormDataPart("label", objectLabel)
                .build()

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute()
            runOnUiThread {
                Toast.makeText(this, "Object successfully created", Toast.LENGTH_SHORT).show()
            }
            frames = frames.toInt() + 1

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CameraCreateObject", "Error creating object", e)
            runOnUiThread {
                Toast.makeText(this, "Error creating object", Toast.LENGTH_SHORT).show()
            }
        } finally {
            runOnUiThread {
                findViewById<TextView>(R.id.frameNumber).text = "Aantal scans: $frames"
            }
        }
    }
}
package com.aperotechnologies.googlevisionapidemo.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.constraint.solver.widgets.Rectangle
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.Toast
import com.aperotechnologies.googlevisionapidemo.R
import com.aperotechnologies.googlevisionapidemo.graphics_utils.MyGraphic
import com.aperotechnologies.googlevisionapidemo.utils.BaseActivity
import com.aperotechnologies.googlevisionapidemo.utils.PermissionUtils
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.common.FirebaseVisionPoint
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark
import com.google.firebase.ml.vision.label.FirebaseVisionLabel
import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector
import com.google.firebase.ml.vision.text.FirebaseVisionText
import kotlinx.android.synthetic.main.activity_chooser.*
import java.io.IOException


class MainActivity : BaseActivity() {

    val TAG = "MainActivity"
    var imageBitmap: Bitmap? = null

    lateinit var faceSquare: Paint
    lateinit var mouthColor: Paint
    lateinit var cheekColor: Paint
    lateinit var noseColor: Paint
    lateinit var eyeColor: Paint

    companion object {
        lateinit var userId: String; set get;
        var userList: HashMap<Int, String> = HashMap(); set get
    }

    private fun initPaintColors() {
        noseColor = Paint()
        noseColor.color = Color.BLUE
        noseColor.strokeWidth = 5.0f

        cheekColor = Paint()
        cheekColor.color = Color.RED
        cheekColor.strokeWidth = 5.0f

        mouthColor = Paint()
        mouthColor.color = Color.YELLOW
        mouthColor.strokeWidth = 5.0f

        eyeColor = Paint()
        eyeColor.color = Color.GREEN
        eyeColor.strokeWidth = 5.0f

        faceSquare = Paint()
        faceSquare.color = Color.BLUE
        faceSquare.style = Paint.Style.STROKE
        faceSquare.strokeWidth = 2.0f

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chooser)
        initPaintColors()
        btn_text_detection.setOnClickListener { view ->
            if (imageBitmap != null) {
                textDetection(imageBitmap!!)
            } else {
                Toast.makeText(this, resources.getText(R.string.select_image), Toast.LENGTH_LONG).show()
            }
        }
        btn_object_detection.setOnClickListener { view ->
            if (imageBitmap != null) {
                findObjectInImg(imageBitmap!!)
            } else {
                Toast.makeText(this, resources.getText(R.string.select_image), Toast.LENGTH_LONG).show()
            }
        }
        btn_face_detection.setOnClickListener { view ->
            if (imageBitmap != null) {
                faceDetectionFun(imageBitmap!!)
            } else {
                Toast.makeText(this, resources.getText(R.string.select_image), Toast.LENGTH_LONG).show()
            }
        }
        btn_barcode_scan.setOnClickListener { view ->
            if (imageBitmap != null) {
                barcodeScanFun(imageBitmap!!)
            } else {
                Toast.makeText(this, resources.getText(R.string.select_image), Toast.LENGTH_LONG).show()
            }
        }
        fab.setOnClickListener { view -> selectImage(this) }
    }

    private fun barcodeScanFun(imageBitmap: Bitmap) {
        val option = FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                .build()
        val imgae = FirebaseVisionImage.fromBitmap(imageBitmap);
        val detector = FirebaseVision.getInstance().getVisionBarcodeDetector(option)
        detector.detectInImage(imgae)
                .addOnSuccessListener { barcodes ->
                    Log.e(TAG, "Barcode size: ${barcodes.size}")
                    text_title_msg.text = "Got barcode"
                    var displayVal = ""
                    for (barcode: FirebaseVisionBarcode in barcodes) {
                        displayVal += barcode.displayValue.toString() + "\n";
                        val moreinfo = when (barcode.valueType) {
                            FirebaseVisionBarcode.TYPE_URL -> barcode.url.toString()
                            FirebaseVisionBarcode.TYPE_CONTACT_INFO -> barcode.contactInfo.toString()
                            FirebaseVisionBarcode.TYPE_PHONE -> barcode.phone.toString()
                            FirebaseVisionBarcode.TYPE_CALENDAR_EVENT -> barcode.calendarEvent.toString()
                            FirebaseVisionBarcode.TYPE_DRIVER_LICENSE -> barcode.driverLicense.toString()
                            FirebaseVisionBarcode.TYPE_EMAIL -> barcode.email.toString()
                            FirebaseVisionBarcode.TYPE_GEO -> "lat-" + barcode.geoPoint!!.lat.toString() + "lng-" + barcode.geoPoint!!.lng.toShort()
                            FirebaseVisionBarcode.TYPE_SMS -> barcode.sms.toString()
                            else -> ""
                        }
                        if (!displayVal.isEmpty()) {
                            displayVal += moreinfo
                        }
                    }
                    text_title_msg.text = displayVal;
                }
                .addOnFailureListener { e ->

                }

    }


    private fun faceDetectionFun(imageBitmap: Bitmap) {

        val firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap)

        val highAccuracyOpt = FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .setMinFaceSize(0.15f)
                .build()
        val realTimeOpt = FirebaseVisionFaceDetectorOptions.Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build()

        val detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(highAccuracyOpt)

        detector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener { faces: List<FirebaseVisionFace> ->
                    println("faces: ${faces.size}")
                    text_title_msg.text = if (faces.isNotEmpty()) "faces detected" else "faces not found, add high resolution image"
                    for (face in faces) {
                        var leftEyeOpenProbability: Float
                        var rightEyeOpenProbability: Float
                        var smilingProbability: Float
                        val bounds = face.boundingBox
                        val rotY = face.headEulerAngleY //  head rotate at y with degree
                        val rotZ = face.headEulerAngleZ  //
                        val canvas = Canvas(imageBitmap)
                        drawRect(bounds, faceSquare)

                        var trackingId: Int? = -1
                        if (face.trackingId != FirebaseVisionFace.INVALID_ID) {
                            trackingId = face.trackingId;
                            var userName: String? = "user_${trackingId}"
                            if (!userList.containsKey(trackingId)) {
                                userList.put(trackingId!!, userName!!)
                            } else {
                                userName = userList.get(trackingId!!);
                            }
                            println("User name: $userName")
                        }

                        if (face.leftEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            leftEyeOpenProbability = face.leftEyeOpenProbability
                        }
                        if (face.rightEyeOpenProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            rightEyeOpenProbability = face.rightEyeOpenProbability
                        }

                        if (face.smilingProbability != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                            smilingProbability = face.smilingProbability
                            Log.e(TAG, "Smiling Probability : " + smilingProbability + "Of user: " + trackingId.toString())
                        }
                        val leftMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_LEFT)
                        if (leftMouth != null) {
                            val leftMouthPos = leftMouth.position
                            drawPoint(imageBitmap, leftMouthPos, mouthColor)

                        }

                        val rightMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_RIGHT)
                        if (rightMouth != null) {
                            val rightMouthPos = rightMouth.position
                            drawPoint(imageBitmap, rightMouthPos, mouthColor)
                        }

                        val bottomMouth = face.getLandmark(FirebaseVisionFaceLandmark.MOUTH_BOTTOM)
                        if (bottomMouth != null) {
                            val bottomMouthPos = bottomMouth.position
                            drawPoint(imageBitmap, bottomMouthPos, mouthColor)
                        }

                        val rightCheek = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK)
                        if (rightCheek != null) {
                            val rightCheekPos = rightCheek.position
                            drawPoint(imageBitmap, rightCheekPos, cheekColor)
                        }

                        val leftCheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK)
                        if (leftCheek != null) {
                            val leftCheekPos = leftCheek.position
                            drawPoint(imageBitmap, leftCheekPos, cheekColor)
                        }
                        val noseBase = face.getLandmark(FirebaseVisionFaceLandmark.NOSE_BASE)
                        if (noseBase != null) {
                            val noseBasePos = noseBase.position
                            drawPoint(imageBitmap, noseBasePos, noseColor)
                        }

                        val rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE)
                        if (rightEye != null) {
                            val rightCheekPos = rightEye.position
                            drawPoint(imageBitmap, rightCheekPos, eyeColor)
                        }

                        val leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE)
                        if (leftEye != null) {
                            val leftCheekPos = leftEye.position
                            drawPoint(imageBitmap, leftCheekPos, eyeColor)
                        }
                    }
                }
                .addOnFailureListener {

                }
    }

    fun drawRect(rect: Rect, paint: Paint) {
        val drawrect = MyGraphic(graphic_overlay, Rectangle(), rect, paint)
        graphic_overlay.add(drawrect)
    }

    fun drawPoint(bitmap: Bitmap, fbvPoint: FirebaseVisionPoint, paint: Paint) {
        val drawPoint = MyGraphic(graphic_overlay, Point(), fbvPoint, paint)
        graphic_overlay.add(drawPoint)
    }


    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            BaseActivity.CAMERA_PERMISSIONS_REQUEST -> if (PermissionUtils.permissionGranted(requestCode, BaseActivity.CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                startCamera()
            }
            BaseActivity.GALLERY_PERMISSIONS_REQUEST -> if (PermissionUtils.permissionGranted(requestCode, BaseActivity.GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                startGalleryChooser()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == BaseActivity.GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            uploadImage(data.data)

        } else if (requestCode == BaseActivity.CAMERA_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val photoUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", cameraFile)
            uploadImage(photoUri)
        }
    }

    @SuppressLint("SetTextI18n")
    fun uploadImage(uri: Uri?) {
        uri.let {
            try {
                text_title_msg.text = "Use floating action button to detect"
                graphic_overlay.clear()
                // scale the image to save on bandwidth
                val bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(contentResolver, uri), BaseActivity.MAX_DIMENSION)
                bitmap?.let {
                    main_image.setImageBitmap(bitmap)
                    imageBitmap = bitmap
                }
            } catch (e: IOException) {
                Log.d(TAG, "Image picking failed because " + e.message)
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
            }
        }
    }

    fun textDetection(bitmap: Bitmap) {
        val image = FirebaseVisionImage.fromBitmap(bitmap)
        val textRecognizer = FirebaseVision.getInstance().onDeviceTextRecognizer
        textRecognizer.processImage(image)
                .addOnSuccessListener { result ->

                    if (result.textBlocks.size > 0) {
                        val resultText = result.text
                        processTextRecognitionResult(result, bitmap)
                        text_title_msg.text = resultText
                    } else {
                        text_title_msg.text = getString(R.string.no_text_detected)
                    }
                }
                .addOnFailureListener { e -> Log.e(TAG, "uploadFirebaseImage: ", e) }
    }

    private fun processTextRecognitionResult(result: FirebaseVisionText?, bitmap: Bitmap?) {
        try {
            val blocks = result!!.textBlocks
            if (blocks.size == 0) {
                Log.e("TAG", "No text found")
                return
            }
            val canvas = Canvas(bitmap!!)
            val rectPaint = Paint()
            rectPaint.color = Color.BLUE
            rectPaint.style = Paint.Style.STROKE
            rectPaint.strokeWidth = 2.0f

            val textPaint = Paint()
            textPaint.color = Color.RED
            textPaint.textSize = 54.0f
            for (i in blocks.indices) {
                val lines = blocks.get(i).lines
                for (j in lines.indices) {
                    val elements = lines.get(j).elements
                    for (k in elements.indices) {
                        val element = elements.get(k)
                        val rect = element.boundingBox
                        if (rect != null) {
                            canvas.drawRect(rect, rectPaint)
                            canvas.drawText(element.text, rect.left.toFloat(), rect.bottom.toFloat(), textPaint)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "processTextRecognitionResult: ", e)
        }
    }


    private fun findObjectInImg(bitmap: Bitmap) {
        val firebaseVisionImage: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap)
        val detection: FirebaseVisionLabelDetector =
                FirebaseVision.getInstance().visionLabelDetector
        detection.detectInImage(firebaseVisionImage).addOnSuccessListener {
            val stringBuilder = StringBuilder()
            for (firbasevisionlabel: FirebaseVisionLabel in it) {
                stringBuilder.append("Label : ${firbasevisionlabel.label} , Accuracy: ${(firbasevisionlabel.confidence * 100).toInt()}%\n")
                println(firbasevisionlabel.entityId)
            }
            text_title_msg.text = stringBuilder.toString()
        }.addOnFailureListener {
            Log.e(TAG, "", it)

        }
    }
}
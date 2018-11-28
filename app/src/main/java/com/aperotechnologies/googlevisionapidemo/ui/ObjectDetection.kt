//package com.aperotechnologies.googlevisionapidemo.ui
//
//import android.app.Activity
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.support.v4.content.FileProvider
//import android.util.Log
//import android.widget.Toast
//import com.aperotechnologies.googlevisionapidemo.R
//import com.aperotechnologies.googlevisionapidemo.utils.BaseActivity
//import com.aperotechnologies.googlevisionapidemo.utils.PermissionUtils
//import com.google.firebase.ml.vision.FirebaseVision
//import com.google.firebase.ml.vision.common.FirebaseVisionImage
//import com.google.firebase.ml.vision.label.FirebaseVisionLabel
//import com.google.firebase.ml.vision.label.FirebaseVisionLabelDetector
//import kotlinx.android.synthetic.main.activity_object_detection.*
//import kotlinx.android.synthetic.main.content_main.*
//import java.io.IOException
//
//class ObjectDetection : BaseActivity() {
//
//    val TAG = "ObjectDetection"
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_object_detection)
//
//        fab.setOnClickListener { view ->
//            selectImage(this)
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            BaseActivity.CAMERA_PERMISSIONS_REQUEST ->
//                if (PermissionUtils.permissionGranted(requestCode, BaseActivity.CAMERA_PERMISSIONS_REQUEST, grantResults)) {
//                    startCamera()
//                }
//            BaseActivity.GALLERY_PERMISSIONS_REQUEST ->
//                if (PermissionUtils.permissionGranted(requestCode, BaseActivity.GALLERY_PERMISSIONS_REQUEST, grantResults)) {
//                    startGalleryChooser()
//                }
//        }
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == BaseActivity.GALLERY_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
//            uploadImage(data.data)
//
//        } else if (requestCode == BaseActivity.CAMERA_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
//            val photoUri = FileProvider.getUriForFile(this, applicationContext.packageName + ".provider", cameraFile)
//            uploadImage(photoUri)
//        }
//    }
//
//    private fun uploadImage(uri: Uri?) {
//        uri?.let {
//        }
//        if (uri != null) {
//            try {
//                // scale the image to save on bandwidth
//                val bitmap = scaleBitmapDown(MediaStore.Images.Media.getBitmap(contentResolver, uri), BaseActivity.MAX_DIMENSION)
//                main_image.setImageBitmap(bitmap)
//                bitmap?.let {
//                    findObjectInImg(it)
//                }
//            } catch (e: IOException) {
//                Log.d(TAG, "Image picking failed because " + e.message)
//                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
//            }
//
//        } else {
//            Log.d(TAG, "Image picker gave us a null image.")
//            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show()
//        }
//    }
//
//    private fun findObjectInImg(bitmap: Bitmap?) {
//        val firebaseVisionImage: FirebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap!!);
//        val detection: FirebaseVisionLabelDetector =
//                FirebaseVision.getInstance().visionLabelDetector;
//        detection.detectInImage(firebaseVisionImage).addOnSuccessListener {
//            val stringBuilder = StringBuilder();
//            for (firbasevisionlabel: FirebaseVisionLabel in it) {
//                stringBuilder.append("Label : ${firbasevisionlabel.label} , Accuracy: ${(firbasevisionlabel.confidence * 100).toInt()}%\n")
//                println(firbasevisionlabel.entityId)
//            }
//            text_title_msg.text = stringBuilder.toString()
//        }.addOnFailureListener {
//            Log.e(TAG, "", it)
//
//        }
//    }
//
//
//}

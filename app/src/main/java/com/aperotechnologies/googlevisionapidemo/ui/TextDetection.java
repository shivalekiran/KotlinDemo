package com.aperotechnologies.googlevisionapidemo.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.aperotechnologies.googlevisionapidemo.utils.BaseActivity;
import com.aperotechnologies.googlevisionapidemo.utils.PermissionUtils;
import com.aperotechnologies.googlevisionapidemo.R;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.IOException;
import java.util.List;

public class TextDetection extends BaseActivity {

    private static final String TAG = "TextDetection";

    private TextView mTitleMsg;
    private ImageView mMainImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_layout);
        mMainImage = findViewById(R.id.main_image);
        mTitleMsg = findViewById(R.id.text_title_msg);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            selectImage(this);
        });
    }


    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, CAMERA_PERMISSIONS_REQUEST, grantResults)) {
                    startCamera();
                }
                break;
            case GALLERY_PERMISSIONS_REQUEST:
                if (PermissionUtils.permissionGranted(requestCode, GALLERY_PERMISSIONS_REQUEST, grantResults)) {
                    startGalleryChooser();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            uploadImage(data.getData());

        } else if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri photoUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", getCameraFile());
            uploadImage(photoUri);
        }
    }

    public void uploadImage(Uri uri) {
        if (uri != null) {
            try {
                // scale the image to save on bandwidth
                Bitmap bitmap =
                        scaleBitmapDown(MediaStore.Images.Media.getBitmap(getContentResolver(), uri), MAX_DIMENSION);

                mMainImage.setImageBitmap(bitmap);
                if (bitmap != null) {
                    uploadFirebaseImage(bitmap);
                }
            } catch (IOException e) {
                Log.d(TAG, "Image picking failed because " + e.getMessage());
                Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "Image picker gave us a null image.");
            Toast.makeText(this, R.string.image_picker_error, Toast.LENGTH_LONG).show();
        }
    }

    private void uploadFirebaseImage(Bitmap bitmap) {
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        textRecognizer.processImage(image)
                .addOnSuccessListener(result -> {

                    if (result.getTextBlocks().size() > 0) {
                        String resultText = result.getText();
                        processTextRecognitionResult(result, bitmap);
                        mTitleMsg.setText(resultText);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "uploadFirebaseImage: ", e);
                })
        ;
    }

    private void processTextRecognitionResult(FirebaseVisionText texts, Bitmap bitmap) {
        try {
            List<FirebaseVisionText.TextBlock> blocks = texts.getTextBlocks();
            if (blocks.size() == 0) {
                Log.e("TAG", "No text found");
                return;
            }
            Canvas canvas = new Canvas(bitmap);
            Paint rectPaint = new Paint();
            rectPaint.setColor(Color.BLUE);
            rectPaint.setStyle(Paint.Style.STROKE);
            rectPaint.setStrokeWidth(2.0F);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.RED);
            textPaint.setTextSize(54.0f);
            for (int i = 0; i < blocks.size(); i++) {
                List<FirebaseVisionText.Line> lines = blocks.get(i).getLines();
                for (int j = 0; j < lines.size(); j++) {
                    List<FirebaseVisionText.Element> elements = lines.get(j).getElements();
                    for (int k = 0; k < elements.size(); k++) {
                        FirebaseVisionText.Element element = elements.get(k);
                        Rect rect = element.getBoundingBox();
                        if (rect != null) {
                            canvas.drawRect(rect, rectPaint);
                            canvas.drawText(element.getText(), rect.left, rect.bottom, textPaint);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "processTextRecognitionResult: ", e);
        }
    }


}

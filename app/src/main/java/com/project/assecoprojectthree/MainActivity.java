package com.project.assecoprojectthree;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    private ImageView ivBluredPhoto;
    private EditText etxtBlurValue;
    private Button btnInsertImage, btnTakeAPhoto, btnCheckBlur;
    private Uri uri;
    private static String TAG = "MainActivity";

    static {
        if (OpenCVLoader.initDebug()) {
            Log.d(TAG, "OpenCV is Configured or Connected Successfully");
        } else {
            Log.d(TAG, "OpenCV not Working or Loaded");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivBluredPhoto = findViewById(R.id.iv_blured_image);

        btnInsertImage = findViewById(R.id.btn_insert_image);
        btnInsertImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, 1);
        });

        btnCheckBlur = findViewById(R.id.btn_check_blur);

        btnCheckBlur.setOnClickListener(view -> {
            Toast.makeText(MainActivity.this, "Load image", Toast.LENGTH_SHORT).show();
        });

        btnTakeAPhoto = findViewById(R.id.btn_make_photo);

        btnTakeAPhoto.setOnClickListener(view -> {
            Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, 2);
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 1) {
            uri = data.getData();
            ivBluredPhoto.setImageURI(uri);
        } else if (requestCode == 2) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ivBluredPhoto.setImageBitmap(imageBitmap);
        }

        etxtBlurValue = findViewById(R.id.etxt_blur_value);
        String txtBlurValue = etxtBlurValue.getText().toString().trim();

        if (ivBluredPhoto.getDrawable() != null && !txtBlurValue.equals("")) {
            int blurValue = Integer.parseInt(txtBlurValue);
            blurValue = -167772 * blurValue;
            int finalBlurValue = blurValue;
            btnCheckBlur.setOnClickListener(view -> {
                checkBlur(ivBluredPhoto, finalBlurValue);
            });
        } else {
            Toast.makeText(MainActivity.this, "Load image or blur value", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkBlur(ImageView bluredPhoto, int blurValue) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inDither = true;
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;

        bluredPhoto.invalidate();
        BitmapDrawable drawable = (BitmapDrawable) bluredPhoto.getDrawable();

        Bitmap image = drawable.getBitmap();

        int l = CvType.CV_8UC1; //8-bit grey scale image

        Mat matImage = new Mat();
        Utils.bitmapToMat(image, matImage);
        Mat matImageGrey = new Mat();
        Imgproc.cvtColor(matImage, matImageGrey, Imgproc.COLOR_BGR2GRAY);

        Bitmap imageBitmap;
        imageBitmap = Bitmap.createBitmap(image);
        Mat mat = new Mat();
        Utils.bitmapToMat(imageBitmap, mat);
        Mat laplacianImageMat = new Mat();
        mat.convertTo(laplacianImageMat, l);
        Imgproc.Laplacian(matImageGrey, laplacianImageMat, CvType.CV_8U);
        Mat laplacianImage8bitMat = new Mat();
        laplacianImageMat.convertTo(laplacianImage8bitMat, l);

        Bitmap bitmap = Bitmap.createBitmap(laplacianImage8bitMat.cols(),
                laplacianImage8bitMat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(laplacianImage8bitMat, bitmap);
        int[] pixels = new int[bitmap.getHeight() * bitmap.getWidth()];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(),
                bitmap.getHeight());

        int maxLap = -16777216;

        for (int i = 0; i < pixels.length; i++) {
            if (pixels[i] > maxLap)
                maxLap = pixels[i];
        }

        btnCheckBlur = findViewById(R.id.btn_check_blur);
        int finalMaxLap = maxLap;
        int numberBlurValue = blurValue;

        if (finalMaxLap <= numberBlurValue) {
            Toast.makeText(MainActivity.this, "Blur image", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(MainActivity.this, "not Blur image", Toast.LENGTH_SHORT).show();
        }
    }
}

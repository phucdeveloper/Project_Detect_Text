package com.philipstudio.projectdetecttext.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.philipstudio.projectdetecttext.R;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,
        Camera.PictureCallback {

    JavaCameraView javaCameraView;
    Button btnTakePhoto;
    ImageButton imgButtonClose;
    ImageView imgOpenGallery, imgListFilePDF;
    Spinner spinnerLanguage;
    //   CropImageView cropImageView;

    Camera camera;
    Mat imgGrey, mRgba, imgCanny, mByte;
    Bitmap bitmap;

    String[] languages = {"Vietnamese", "English", "Chinese", "Japanese", "Korean", "Russian", "Malaysian"};
    String itemSelected, currentDateAndTime, nameFileImage;
    ArrayList<String> listDataLanguage = new ArrayList<>();
    static final int REQUEST_CODE = 123;


    FirebaseStorage firebaseStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        initView();

        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        loadOpenCV();

        getDataLanguageFromFirebaseStorage();

        setUpSpinnerLanguage(languages);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (languages[i]) {
                    case "English":
                        itemSelected = "eng";
                        break;
                    case "Vietnamese":
                        itemSelected = "vie";
                        break;
                    case "Chinese":
                        itemSelected = "chi_tra";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        imgButtonClose.setOnClickListener(view -> finish());

        btnTakePhoto.setOnClickListener(view -> captureImage());

        imgOpenGallery.setOnClickListener(view -> requestPermission());

        new Thread(() -> runOnUiThread(() -> imgListFilePDF.setOnClickListener(view -> {
            Intent intent = new Intent(CameraActivity.this, ListFilePdfActivity.class);
            startActivity(intent);
        }))).start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(CameraActivity.this, "Problem in OpenCv", Toast.LENGTH_SHORT).show();
        } else {
            javaCameraView.enableView();
            javaCameraView.updateMatrix();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        imgGrey = new Mat(height, width, CvType.CV_8UC4);
        mByte = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        imgCanny = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        new Thread(() -> {
            bitmap = Bitmap.createBitmap(mByte.cols(), mByte.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mRgba, bitmap);
            //   runOnUiThread(() -> cropImageView.setImageBitmap(bitmap));
        }).start();
        Imgproc.cvtColor(mRgba, mByte, Imgproc.COLOR_BGR2RGB);
        return mRgba;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        }
    }

    private void loadOpenCV() {
        if (OpenCVLoader.initDebug()) {
            javaCameraView.enableView();
            javaCameraView.updateMatrix();
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, new LoaderCallbackInterface() {
                @Override
                public void onManagerConnected(int status) {
                    if (status == LoaderCallbackInterface.SUCCESS) {
                        javaCameraView.enableView();
                        javaCameraView.updateMatrix();
                    }
                }

                @Override
                public void onPackageInstall(int operation, InstallCallbackInterface callback) {

                }
            });
        }
    }

    private void captureImage() {
        Bitmap bitmap;
        try {
            bitmap = Bitmap.createBitmap(mByte.cols(), mByte.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mByte, bitmap);

            sendData(mByte, itemSelected);
        } catch (CvException e) {
            Log.d("Exception", Objects.requireNonNull(e.getMessage()));
        }
    }

    private void sendData(Mat mat, String language) {
        String fileName = Environment.getExternalStorageDirectory() + "/project/" + currentDateAndTime + ".jpg";
        Imgcodecs.imwrite(fileName, mat);
        Intent intent = new Intent(CameraActivity.this, TextEditorActivity.class);
        intent.putExtra("nameFile", currentDateAndTime);
        intent.putExtra("language", language);
        startActivity(intent);

    }

    private void setUpSpinnerLanguage(String[] strings) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.item_language, strings);
        spinnerLanguage.setAdapter(arrayAdapter);
    }

    private void requestPermission() {
        Dexter.withContext(getApplicationContext()).withPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(new MultiplePermissionsListener() {
            @Override
            public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                if (multiplePermissionsReport.areAllPermissionsGranted()) {
                    openGallery();
                }
            }

            @Override
            public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {

            }
        }).check();
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE && data != null) {
            Uri uri = data.getData();
        }
    }

    private void getDataLanguageFromFirebaseStorage() {
        firebaseStorage = FirebaseStorage.getInstance();
        StorageReference stoRef = firebaseStorage.getReference().child("assets");
        stoRef.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference storageReference : listResult.getItems()) {
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String link = uri.toString();
                    listDataLanguage.add(link);
                    Log.d("size", listDataLanguage.size() + " ");
                });
            }
        });
    }

    private void initView() {
        javaCameraView = findViewById(R.id.java_camera_view);
        spinnerLanguage = findViewById(R.id.spinner_language);
        imgButtonClose = findViewById(R.id.imagebutton_close);
        imgOpenGallery = findViewById(R.id.image_view_open_gallery);
        btnTakePhoto = findViewById(R.id.button_take_a_photo);
        //    cropImageView = findViewById(R.id.crop_image_view);
        imgListFilePDF = findViewById(R.id.image_view_list_file_pdf);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH_mm_ss");
        currentDateAndTime = sdf.format(new Date());
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        this.camera = camera;
        camera.startPreview();
        camera.setPreviewCallback((Camera.PreviewCallback) this);
        String name = String.valueOf(System.currentTimeMillis());
        try {
            FileOutputStream outputStream = new FileOutputStream(name);

            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
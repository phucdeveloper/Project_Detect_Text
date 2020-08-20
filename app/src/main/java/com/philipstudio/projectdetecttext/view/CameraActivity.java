package com.philipstudio.projectdetecttext.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.philipstudio.projectdetecttext.R;

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
import java.util.Date;
import java.util.Objects;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,
                Camera.PictureCallback {

    JavaCameraView javaCameraView;
    ImageButton imgButtonTakeAPhoto, imgButtonClose, imgButtonSavePhoto;
    Spinner spinnerLanguage;

    Camera camera;
    Mat imgGrey, mRgba, imgCanny, mByte;

    String[] languages = {"Tiếng Việt", "Tiếng Anh", "Tiếng Trung"};
    Bitmap bitmap;
    String itemSelected, currentDateAndTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        initView();

        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);
        loadOpenCV();

        setUpSpinnerLanguage(languages);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (languages[i].equals("Tiếng Anh")) {
                    itemSelected = "eng";
                } else if (languages[i].equals("Tiếng Việt")) {
                    itemSelected = "vie";
                }
                else if (languages[i].equals("Tiếng Trung")){
                    itemSelected = "chi_tra";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        imgButtonClose.setOnClickListener(view -> finish());

        imgButtonTakeAPhoto.setOnClickListener(view -> {
            Toast.makeText(CameraActivity.this, "Taked a photo", Toast.LENGTH_SHORT).show();
            captureImage();
        });

        imgButtonSavePhoto.setOnClickListener(view -> showDialog(CameraActivity.this));
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
        Imgproc.cvtColor(mRgba, mByte, Imgproc.COLOR_BGR2RGB);
        return mRgba;
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

    private void showDialog(Context context) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_dialog);

        dialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;

        EditText edtNhapTenFile;
        Button btnOk, btnHuy;

        edtNhapTenFile = dialog.findViewById(R.id.edittext_nhaptenfile);
        btnOk = dialog.findViewById(R.id.button_ok);
        btnHuy = dialog.findViewById(R.id.button_huy);

        btnOk.setOnClickListener(view -> {
            String nameFile = edtNhapTenFile.getText().toString();
            String path = Environment.getExternalStorageDirectory() + "/project/" + currentDateAndTime + ".jpg";
            Mat mat = Imgcodecs.imread(path);
            saveImage(nameFile, mat);
            Toast.makeText(CameraActivity.this, "Saved image!", Toast.LENGTH_SHORT).show();
        });

        btnHuy.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
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

    private void saveImage(String name, Mat mat) {
        String fileName = Environment.getExternalStorageDirectory().getPath() + "/" + name + ".jpg";
        Imgcodecs.imwrite(fileName, mat);
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
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, strings);
        spinnerLanguage.setAdapter(arrayAdapter);
    }

    private void initView() {
        javaCameraView = findViewById(R.id.java_camera_view);
        imgButtonTakeAPhoto = findViewById(R.id.imagebutton_take_a_photo);
        spinnerLanguage = findViewById(R.id.spinner_language);
        imgButtonClose = findViewById(R.id.imagebutton_close);
        imgButtonSavePhoto = findViewById(R.id.imagebutton_save_photo);

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
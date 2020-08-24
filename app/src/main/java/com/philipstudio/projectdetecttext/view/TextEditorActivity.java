package com.philipstudio.projectdetecttext.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.philipstudio.projectdetecttext.callback.OnSendDataImageListener;
import com.philipstudio.projectdetecttext.util.MyTessOCR;
import com.philipstudio.projectdetecttext.R;
import com.philipstudio.projectdetecttext.util.ProcessImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TextEditorActivity extends AppCompatActivity implements OnSendDataImageListener {

    private static final int REQUEST_CODE = 123;
    ProgressDialog progressDialog;
    Button btnRender;
    EditText editInput;
    Spinner spinnerFont, spinnerSize;


    String language, nameFile;
    Bitmap convertBitmap;
    ProcessImage processImage;
    boolean isImageBlur = false;
    String[] font = {"Calibri", "Sanna", "Yessica", "Hio"};
    String[] size = {"10f", "15.f", "17f", "20", "18"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        initView();

        Intent intent = getIntent();
        if (intent != null) {
            nameFile = intent.getStringExtra("nameFile");
            String pathDir = Environment.getExternalStorageDirectory() + "/project/" + nameFile + ".jpg";
            Mat mat = Imgcodecs.imread(pathDir);

            convertBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(mat, convertBitmap);
            language = intent.getStringExtra("language");
            Log.d("phuc", language);
            isImageBlur = processImage.detectImageBlur(convertBitmap);

            if (isImageBlur) {
                showDialogNotificationImageBlur(TextEditorActivity.this);
            } else {
                showProgressDialogExtractingTextFromImage();
                doOCR(convertBitmap, language);
            }

            setUpSpinnerFont(spinnerFont, font);

            setUpSpinnerSize(spinnerSize);

        }
    }

    private void showProgressDialogExtractingTextFromImage() {
        progressDialog.setMessage("Extracting text from image...");
        progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }

    private void doOCR(Bitmap bitmap, String language) {
        new Thread(() -> {
            boolean isDetectDark = processImage.detectImageDark(bitmap);
            boolean isDetectBlur = processImage.detectImageBlur(bitmap);
            Log.d("phuc", isDetectBlur + " ");
            if (isDetectBlur) {

            }
            if (isDetectDark) {
                convertBitmap = processImage.scaleBitmapToImage(bitmap);
            } else {
                convertBitmap = processImage.toGrayscale(bitmap);
            }
            MyTessOCR myTessOCR = new MyTessOCR(TextEditorActivity.this, language);
            String textResult = myTessOCR.getTextOCRResult(convertBitmap);
            runOnUiThread(() -> {
                if (textResult != null) {
                    progressDialog.dismiss();
                    Log.d("phuc", textResult);
                    editInput.setText(textResult);

                    btnRender.setOnClickListener(view -> {
                        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                                    PackageManager.PERMISSION_GRANTED){
                                showDialogCreateFilePDF(TextEditorActivity.this, textResult);
                            }
                            else{
                                String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                                requestPermissions(permission, REQUEST_CODE);
                            }
                        }
                        else{
                            showDialogCreateFilePDF(TextEditorActivity.this, textResult);
                        }
                    });
                }
            });
            myTessOCR.onDestroy();
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    String text = editInput.getText().toString();
                    showDialogCreateFilePDF(TextEditorActivity.this, text);
                }
                else{
                    Toast.makeText(TextEditorActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showDialogCreateFilePDF(Context context, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Create New File PDF");
        builder.setMessage("Do you want to create new PDF ?");

        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            try {
                createFileNewPDF(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        alertDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        alertDialog.show();
    }

    private void showDialogNotificationImageBlur(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage("Your photo is too blurry. Do you want to continue the detect process?");

        builder.setPositiveButton("Ok, continue", (dialogInterface, i) -> {
            Toast.makeText(context, "Ok, we processing image for detect text", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        alertDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        alertDialog.show();
    }

    private void createFileNewPDF(String content){
        Document document = new Document();
        String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mFilePath = Environment.getExternalStorageDirectory().getPath() + "/" + mFileName + ".pdf";

        try{
            PdfWriter.getInstance(document, new FileOutputStream(mFilePath));
            document.open();
            document.addLanguage("vie");
            document.add(new Paragraph(content));
            Toast.makeText(TextEditorActivity.this, mFileName + " created at " + mFilePath, Toast.LENGTH_SHORT).show();
            document.close();
        }
        catch (Exception e){
            Toast.makeText(TextEditorActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpSpinnerFont(Spinner spinner, String[] name){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(TextEditorActivity.this, android.R.layout.simple_list_item_1, name);
        spinner.setAdapter(arrayAdapter);
    }

    private void setUpSpinnerSize(Spinner spinner){
        String[] size = {{"10.5", "13.6", "10", "1.2", "1.6"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(TextEditorActivity.this, android.R.layout.simple_list_item_1, size);
        spinner.setAdapter(arrayAdapter);
    }

    private void initView() {
        progressDialog = new ProgressDialog(this);
        btnRender = findViewById(R.id.button_render);
        processImage = new ProcessImage();
        editInput = findViewById(R.id.edit_text);
        spinnerFont = findViewById(R.id.spinner_font);
        spinnerSize = findViewById(R.id.spinner_size);
    }
}
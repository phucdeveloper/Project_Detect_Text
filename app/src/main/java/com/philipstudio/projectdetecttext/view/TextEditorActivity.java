package com.philipstudio.projectdetecttext.view;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.philipstudio.projectdetecttext.util.MyTessOCR;
import com.philipstudio.projectdetecttext.R;
import com.philipstudio.projectdetecttext.util.ProcessImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TextEditorActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    Button btnRender;
    TextView txtDisplay;
    ImageView imgImage;


    String language, nameFile;
    Bitmap convertBitmap;
    ProcessImage processImage;
    boolean isImageBlur = false;

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
                imgImage.setImageBitmap(convertBitmap);
                showProgressDialogExtractingTextFromImage();
                doOCR(convertBitmap, language);
            }
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
                LightingColorFilter lightingColorFilter = new LightingColorFilter(0x77777777, 0x77777777);
                imgImage.setColorFilter(lightingColorFilter);
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
                    txtDisplay.setText(textResult);

                    btnRender.setOnClickListener(view -> {
                        showDialogCreateFilePDF(TextEditorActivity.this, textResult);
                    });
                }
            });
            myTessOCR.onDestroy();
        }).start();
    }

    private void showDialogCreateFilePDF(Context context, String content) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Extract File PDF");
        builder.setView(R.layout.layout_dialog_create_file_pdf);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        alertDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            String name = System.currentTimeMillis() + ".pdf";
            try {
                createFilePDF(name, content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

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

    private void createFilePDF(String nameFile, String content) throws IOException {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument
                .PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        int x = 10, y = 25;
        Paint paint = new Paint();
        page.getCanvas().drawText(content, x, y, paint);
        pdfDocument.finishPage(page);

        String pathFile = Environment.getExternalStorageDirectory().getPath() + "/MyFilePDF/" + nameFile + ".pdf";
        File file = new File(pathFile);
        pdfDocument.writeTo(new FileOutputStream(file));
        pdfDocument.close();
    }

    private void initView() {
        progressDialog = new ProgressDialog(this);
        btnRender = findViewById(R.id.button_render);
        processImage = new ProcessImage();
        txtDisplay = findViewById(R.id.text_view);
        imgImage = findViewById(R.id.image_view);
    }
}
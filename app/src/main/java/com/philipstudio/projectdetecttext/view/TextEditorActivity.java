package com.philipstudio.projectdetecttext.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.widget.TextView;

import com.philipstudio.projectdetecttext.util.MyTessOCR;
import com.philipstudio.projectdetecttext.R;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class TextEditorActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    TextView txtDisplay;

    String language, nameFile;
    Bitmap convertBitmap;

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
            doOCR(convertBitmap, language);
        }
    }


    private void showProgressDialogExtractingTextFromImage() {
        progressDialog.setContentView(R.layout.layout_progress_dialog);
        progressDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        progressDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT);
        progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        progressDialog.show();
    }

    private void doOCR(Bitmap bitmap, String language) {
        new Thread(() -> {
            showProgressDialogExtractingTextFromImage();
            convertBitmap = scaleBitmapToImage(bitmap);
            MyTessOCR myTessOCR = new MyTessOCR(TextEditorActivity.this, language);
            String textResult = myTessOCR.getTextOCRResult(convertBitmap);
            runOnUiThread(() -> {
                if (textResult != null){
                    progressDialog.dismiss();
                    txtDisplay.setText(textResult);
                }
            });
            myTessOCR.onDestroy();
        }).start();
    }

    private Bitmap scaleBitmapToImage(Bitmap bitmap) {
        Bitmap bitmap1;
        int bitmapByteCount = bitmap.getByteCount();
        if (bitmapByteCount > 3000000) {
            bitmap1 = grayBitmapToImage(bitmap);
        } else {
            bitmap1 = Bitmap.createScaledBitmap(bitmap, 8 * bitmap.getWidth(), 8 * bitmap.getHeight(), true);
        }
        return bitmap1;
    }

    private Bitmap grayBitmapToImage(Bitmap bitmap) {
        double RED = 0.299;
        double GREEN = 0.587;
        double BLUE = 0.114;
        int red, green, blue, pixel, alpha;

        Bitmap bitmap1 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                pixel = bitmap.getPixel(i, j);
                alpha = Color.alpha(pixel);
                red = Color.red(pixel);
                green = Color.green(pixel);
                blue = Color.blue(pixel);

                int gray = (int) (RED * red + GREEN * green + BLUE * blue);
                bitmap1.setPixel(i, j, Color.argb(alpha, gray, green, gray));
            }
        }

        return bitmap1;
    }

    private void initView() {
        txtDisplay = findViewById(R.id.textview_display);

        progressDialog = new ProgressDialog(this);
    }
}
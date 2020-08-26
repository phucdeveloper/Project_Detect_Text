package com.philipstudio.projectdetecttext.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.philipstudio.projectdetecttext.util.MyTessOCR;
import com.philipstudio.projectdetecttext.R;
import com.philipstudio.projectdetecttext.util.ProcessImage;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import top.defaults.colorpicker.ColorPickerView;

public class TextEditorActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 123;
    ProgressDialog progressDialog;
    Button btnRender, btnSize;
    EditText editInput;
    LinearLayout linearLayout;
    BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    ImageView imgLayout, imgUndo, imgRedo, imgStyleBold, imgBold, imgStyleItalic,
            imgItalic, imgUnderline, imgStyleUnderline, imgOpenGallery, imgImage;
    Spinner spinnerFont;
    ColorPickerView colorPickerView;


    String language, nameFile, textResult;
    Bitmap convertBitmap;
    ProcessImage processImage;
    boolean isImageBlur = false, isClick = false;
    MyTessOCR myTessOCR;
    private static final int INITIAL_COLOR = 0xFF000000;
    float size = 0.0f;

    String[] arrayFont = {"Normal", "Monospace", "Sans", "Serif"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_editor);

        initView();

        Intent intent = getIntent();
        if (intent != null) {
            String data = intent.getStringExtra("data");
            if (!TextUtils.isEmpty(data)) {
                try {
                    Uri uri = Uri.parse(data);
                    imgImage.setImageURI(uri);
                    convertBitmap = Bitmap.createBitmap(1920, 1080, Bitmap.Config.ARGB_8888);
                    convertBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    convertBitmap = processImage.scaleBitmapToImage(convertBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                nameFile = intent.getStringExtra("nameFile");
                if (!TextUtils.isEmpty(nameFile)) {
                    String pathDir = Environment.getExternalStorageDirectory() + "/project/" + nameFile + ".jpg";
                    Mat mat = Imgcodecs.imread(pathDir);
                    convertBitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(mat, convertBitmap);
                }

                language = intent.getStringExtra("language");
                Log.d("phuc", language);
            }

            if (convertBitmap != null) {
                isImageBlur = processImage.detectImageBlur(convertBitmap);
                if (isImageBlur) {
                    showDialogNotificationImageBlur(TextEditorActivity.this, convertBitmap);
                } else {
                    showProgressDialogExtractingTextFromImage();
                    doOCR(convertBitmap, language);
                }
            }
        }

        setUpSpinnerFont(arrayFont);

        setUpColorPicker();

        imgLayout.setOnClickListener(listener);
        imgRedo.setOnClickListener(listener);
        imgUndo.setOnClickListener(listener);
        btnSize.setOnClickListener(listener);
        imgStyleBold.setOnClickListener(listener);
        imgStyleItalic.setOnClickListener(listener);
        imgBold.setOnClickListener(listener);
        imgItalic.setOnClickListener(listener);
        imgOpenGallery.setOnClickListener(listener);
        imgStyleUnderline.setOnClickListener(listener);
        imgUnderline.setOnClickListener(listener);

        btnRender.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
                    showDialogCreateFilePDF(TextEditorActivity.this, textResult);
                } else {
                    String[] permission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permission, REQUEST_CODE);
                }
            } else {
                showDialogCreateFilePDF(TextEditorActivity.this, textResult);
            }
        });
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.image_view_layout:
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    break;
                case R.id.image_view_redo:
                    Toast.makeText(TextEditorActivity.this, "Redo!!!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.image_view_undo:
                    Toast.makeText(TextEditorActivity.this, "Undo!!!", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.image_view_style_bold:
                case R.id.image_view_bold:
                    editInput.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
                    break;
                case R.id.image_view_style_italic:
                case R.id.image_view_italic:
                    editInput.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                    break;
                case R.id.image_view_style_underline:
                case R.id.image_view_underline:
                    editInput.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
                    break;
                case R.id.image_view_open_gallery:
                    openGallery();
                    break;
                case R.id.button_size:
                    showDialogChooseSize(TextEditorActivity.this, editInput);
                    break;
            }
        }
    };

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
            Toast.makeText(TextEditorActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();

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
            if (!TextUtils.isEmpty(language)){
                myTessOCR = new MyTessOCR(TextEditorActivity.this, language);
            }
            else{
                myTessOCR = new MyTessOCR(TextEditorActivity.this);
            }

            textResult = myTessOCR.getTextOCRResult(convertBitmap);
            runOnUiThread(() -> {
                if (textResult != null) {
                    progressDialog.dismiss();
                    Log.d("phuc", textResult);
                    editInput.setText(textResult);
                }
            });
            myTessOCR.onDestroy();
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String text = editInput.getText().toString();
                    showDialogCreateFilePDF(TextEditorActivity.this, text);
                } else {
                    Toast.makeText(TextEditorActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void showDialogCreateFilePDF(Context context, String text) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Create New File PDF");
        builder.setMessage("Do you want to create new PDF ?");

        builder.setPositiveButton("Create", (dialogInterface, i) -> {
            try {
                createFileNewPDF(text);
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

    private void showDialogNotificationImageBlur(Context context, Bitmap bitmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.app_name);
        builder.setMessage("Your photo is too blurry. Do you want to continue the detect process?");

        builder.setPositiveButton("Ok, continue", (dialogInterface, i) -> {
            showProgressDialogExtractingTextFromImage();
            doOCR(bitmap, null);
        });

        builder.setNegativeButton("Cancel", (dialogInterface, i) -> {
            dialogInterface.dismiss();
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        alertDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);

        alertDialog.show();
    }

    private void showDialogChooseSize(Context context, EditText editText) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_choose_size);


        RadioButton rBSize12, rBSize14, rBSize16, rBSize18, rBSize20, rBSize22, rBSize24;
        Button btnOk, btnCancel;
        EditText edtInputSize;

        rBSize12 = dialog.findViewById(R.id.radio_button12);
        rBSize14 = dialog.findViewById(R.id.radio_button14);
        rBSize16 = dialog.findViewById(R.id.radio_button16);
        rBSize18 = dialog.findViewById(R.id.radio_button18);
        rBSize20 = dialog.findViewById(R.id.radio_button20);
        rBSize22 = dialog.findViewById(R.id.radio_button22);
        rBSize24 = dialog.findViewById(R.id.radio_button24);
        btnOk = dialog.findViewById(R.id.button_ok);
        btnCancel = dialog.findViewById(R.id.button_cancel);
        edtInputSize = dialog.findViewById(R.id.edit_text);


        rBSize12.setOnClickListener(view -> {
            isClick = true;
            size = 12.0f;
        });

        rBSize14.setOnClickListener(view -> {
            isClick = true;
            size = 14.0f;
        });

        rBSize16.setOnClickListener(view -> {
            isClick = true;
            size = 16.0f;
        });

        rBSize18.setOnClickListener(view -> {
            isClick = true;
            size = 18.0f;
        });

        rBSize20.setOnClickListener(view -> {
            isClick = true;
            size = 20.0f;
        });

        rBSize22.setOnClickListener(view -> {
            isClick = true;
            size = 22.0f;
        });

        rBSize24.setOnClickListener(view -> {
            isClick = true;
            size = 24.0f;
        });

        btnOk.setOnClickListener(view -> {
            if (isClick) {
                editText.setTextSize(size);
            } else {
                String sizeInput = edtInputSize.getText().toString();
                float size = Float.parseFloat(sizeInput);
                editText.setTextSize(size);
            }
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        dialog.show();
    }

    private void setUpColorPicker() {
        colorPickerView.subscribe((color, fromUser, shouldPropagate) -> editInput.setTextColor(color));

        colorPickerView.setInitialColor(INITIAL_COLOR);
    }

    private void createFileNewPDF(String content) {
        Document document = new Document();
        String mFileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String mFilePath = Environment.getExternalStorageDirectory().getPath() + "/MyFilePDF";
        try {
            File dir = new File(mFilePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, mFileName + ".pdf");
            FileOutputStream outputStream = new FileOutputStream(file);
            PdfWriter.getInstance(document, outputStream);

            document.open();

            Paragraph paragraph = new Paragraph(content);
            document.add(paragraph);
            Toast.makeText(TextEditorActivity.this, mFileName + " created at " + mFilePath, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(TextEditorActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            document.close();
        }
    }

    private void setUpSpinnerFont(String[] strings) {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(TextEditorActivity.this, android.R.layout.simple_list_item_1, strings);
        spinnerFont.setAdapter(arrayAdapter);
    }

    private void initView() {
        progressDialog = new ProgressDialog(this);
        btnRender = findViewById(R.id.button_render);
        processImage = new ProcessImage();
        editInput = findViewById(R.id.edit_text);
        linearLayout = findViewById(R.id.layout_bottom_sheet);
        imgLayout = findViewById(R.id.image_view_layout);
        imgUndo = findViewById(R.id.image_view_undo);
        imgRedo = findViewById(R.id.image_view_redo);
        spinnerFont = findViewById(R.id.spinner_font);
        imgStyleBold = findViewById(R.id.image_view_style_bold);
        imgStyleItalic = findViewById(R.id.image_view_style_italic);
        btnSize = findViewById(R.id.button_size);
        colorPickerView = findViewById(R.id.color_picker_view);
        imgOpenGallery = findViewById(R.id.image_view_open_gallery);
        imgBold = findViewById(R.id.image_view_bold);
        imgItalic = findViewById(R.id.image_view_italic);
        imgStyleUnderline = findViewById(R.id.image_view_style_underline);
        imgUnderline = findViewById(R.id.image_view_underline);
        imgImage = findViewById(R.id.image_view_image);

        bottomSheetBehavior = BottomSheetBehavior.from(linearLayout);
    }
}
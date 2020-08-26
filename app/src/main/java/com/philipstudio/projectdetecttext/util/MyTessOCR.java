package com.philipstudio.projectdetecttext.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MyTessOCR {
    private TessBaseAPI tessBaseAPI;
    private String dataPath;

    public MyTessOCR(Context context, String lang) {
        dataPath = Environment.getExternalStorageDirectory() + "/project/";
        File dir = new File(dataPath + "/tessdata/");
        if (!dir.exists()) {
            Log.d("phuc", "in file doesn't exist");
            dir.mkdirs();
            copyFile(context, lang);
        }

        tessBaseAPI = new TessBaseAPI();
        if (!TextUtils.isEmpty(lang)) {
            tessBaseAPI.init(dataPath, lang);
        } else {
            tessBaseAPI.init(dataPath, "eng");
        }
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY);
    }

    public MyTessOCR(Context context) {
        dataPath = Environment.getExternalStorageDirectory() + "/project/";
        File dir = new File(dataPath + "/tessdata/");
        if (!dir.exists()) {
            Log.d("phuc", "in file doesn't exist");
            dir.mkdirs();
            copyFile(context, "eng");
        }

        tessBaseAPI = new TessBaseAPI();
        tessBaseAPI.init(dataPath, "eng");
        tessBaseAPI.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY);
    }

    public void stopRecongize() {
        tessBaseAPI.stop();
    }

    public String getTextOCRResult(Bitmap bitmap) {
        tessBaseAPI.setImage(bitmap);
        return tessBaseAPI.getUTF8Text();
    }

    public void onDestroy() {
        if (tessBaseAPI != null)
            tessBaseAPI.end();
    }

    public void copyFile(Context context, String lang) {
        AssetManager manager = context.getAssets();
        if (lang.equals("eng")) {
            try {
                InputStream inputStream = manager.open("eng.traineddata");
                OutputStream outputStream = new FileOutputStream(dataPath + "/tessdata/" + "eng.traineddata");
                byte[] bytes = new byte[1024];
                int read = inputStream.read(bytes);

                while (read != -1) {
                    outputStream.write(bytes, 0, read);
                    read = inputStream.read(bytes);
                }

            } catch (IOException e) {
                Log.d("phuc", "couldn't copy with the following error : " + e.toString());
            }
        } else if (lang.equals("vie")) {
            try {
                InputStream inputStream = manager.open("vie.traineddata");
                OutputStream outputStream = new FileOutputStream(dataPath + "/tessdata/" + "vie.traineddata");
                byte[] bytes = new byte[1024];
                int read = inputStream.read(bytes);

                while (read != -1) {
                    outputStream.write(bytes, 0, read);
                    read = inputStream.read(bytes);
                }

            } catch (IOException e) {
                Log.d("phuc", "couldn't copy with the following error : " + e.toString());
            }
        } else if (lang.equals("chi_tra")) {
            try {
                InputStream inputStream = manager.open("chi_tra.traineddata");
                OutputStream outputStream = new FileOutputStream(dataPath + "/tessdata/" + "chi_tra.traineddata");
                byte[] bytes = new byte[1024];
                int read = inputStream.read(bytes);

                while (read != -1) {
                    outputStream.write(bytes, 0, read);
                    read = inputStream.read(bytes);
                }

            } catch (IOException e) {
                Log.d("phuc", "couldn't copy with the following error : " + e.toString());
            }
        }

    }
}

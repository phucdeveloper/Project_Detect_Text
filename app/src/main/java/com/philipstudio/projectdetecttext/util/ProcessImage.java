package com.philipstudio.projectdetecttext.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;

public class ProcessImage {

    public boolean detectImageDark(Bitmap a) {
        boolean isDark = false;
        int[] histogram = new int[256];
        for (int i = 0; i < 256; i++) {
            histogram[i] = 0;
        }

        for (int x = 0; x < a.getWidth(); x++) {
            for (int y = 0; y < a.getHeight(); y++) {
                int pixel = a.getPixel(x, y);

                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int brightness = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
                histogram[brightness]++;
            }
        }

        int allPixelsCount = a.getWidth() * a.getHeight();
        int darkPixelCount = 0;
        for (int i = 0; i < 10; i++) {
            darkPixelCount += histogram[i];
        }

        if (darkPixelCount > allPixelsCount * 0.25)
            isDark = true;

        return isDark;
    }

    public boolean detectImageBlur(Bitmap bitmap) {
        boolean isBlur = false;
        Mat matSrc = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Utils.bitmapToMat(bitmap, matSrc);
        Mat matGrey = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Imgproc.cvtColor(matSrc, matGrey, Imgproc.COLOR_BGR2GRAY);
        Mat matDestination = new Mat(bitmap.getHeight(), bitmap.getWidth(), CvType.CV_8UC1);
        Imgproc.Laplacian(matGrey, matDestination, 3);
        MatOfDouble median = new MatOfDouble();
        MatOfDouble std = new MatOfDouble();
        Core.meanStdDev(matDestination, median, std);
        String result = new DecimalFormat("0.00").format(Math.pow(std.get(0, 0)[0], 2.0));
        Log.d("phuc", result);
        String[] data = result.split(",");
        double number = Double.parseDouble(data[0]);
        if (number < 30){
            isBlur = true;
        }
        return isBlur;
    }

    public Bitmap scaleBitmapToImage(Bitmap bitmap) {
        int bitmapByteCount = bitmap.getByteCount();
        if (bitmapByteCount > 3000000) {
            return bitmap;
        }
        return Bitmap.createScaledBitmap(bitmap, 8 * bitmap.getWidth(), 8 * bitmap.getHeight(), true);
    }

    public Bitmap toGrayscale(Bitmap srcImage) {

        Bitmap bmpGrayscale = Bitmap.createBitmap(srcImage.getWidth(), srcImage.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bmpGrayscale);
        Paint paint = new Paint();

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        canvas.drawBitmap(srcImage, 0, 0, paint);

        return bmpGrayscale;
    }
}

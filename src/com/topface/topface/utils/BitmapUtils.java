package com.topface.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class BitmapUtils {

    public static Bitmap getBitmap(Context context, Uri uri) {
        Bitmap result = null;
        BufferedInputStream bis = null;
        try
        {
            bis = new BufferedInputStream(getInputStream(context,uri), 8192);
            result = BitmapFactory.decodeStream(bis);
            bis.close();
            bis = null;
        } catch(Exception ex) {
            Debug.error(ex);
        }

        return result;
    }

    public static InputStream getInputStream(Context context, Uri uri) throws IOException {
        InputStream stream;

        if (uri == null) {
            stream = null;
        } else if (isInternetUri(uri)) {
            stream = new URL(uri.toString()).openStream();
        } else {
            stream = context.getContentResolver().openInputStream(uri);
        }

        return stream;
    }

    /**
     * Проверяет, ссылается ли данный Uri на ресурс в интернете
     *
     * @param uri ресурса
     * @return является ресурс ссылкой на файл в интернете
     */
    private static boolean isInternetUri(Uri uri) {
        return Arrays.asList(
                "http",
                "https",
                "ftp"
        ).contains(uri.getScheme());
    }

    public static Bitmap getScaledBitmap(final Bitmap rawBitmap,final float dstWidth,final float dstHeight) {
        // Исходный размер загруженного изображения
        final int srcWidth = rawBitmap.getWidth();
        final int srcHeight = rawBitmap.getHeight();

        // коффициент сжатия фотографии
        float ratio = Math.max(dstWidth / srcWidth, dstHeight / srcHeight);

        // на получение оригинального размера по ширине или высоте
        if (ratio <= 0) ratio = 1;

        // матрица сжатия
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);

        Bitmap result = Bitmap.createBitmap(rawBitmap, 0, 0, srcWidth, srcHeight, matrix, true);
        rawBitmap.recycle();
        // сжатие изображения
        return result;
    }
}

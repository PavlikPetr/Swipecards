package com.topface.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class BitmapUtils {

    public static Bitmap getBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        try {
            int orientation = resolveBitmapOrientation(uri);
            bitmap = decodeSampledBitmap(context, uri, reqWidth, reqHeight, orientation);
            bitmap = applyOrientation(bitmap,orientation);
        } catch (Exception e) {
            Debug.error(e);
        }

        return bitmap;
    }

    public static BitmapFactory.Options readImageFileOptions(Context context, Uri uri) {
        BufferedInputStream bis = null;
        InputStream is = null;
        BitmapFactory.Options options = null;
        try {
            is = getInputStream(context, uri);
            bis = new BufferedInputStream(is, 8192);
            // First decode with inJustDecodeBounds=true to check dimensions
            options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bis,null,options);
        } catch (Exception ex) {
            Debug.error(ex);
        } finally {
            try {
                if (bis != null) bis.close();
                if (is != null) is.close();
            } catch (Exception e) {
                Debug.error(e);
            }
        }

        return options;
    }

    public static Bitmap decodeSampledBitmap(Context context, Uri uri, int reqWidth, int reqHeight,int orientation) {
        Bitmap bitmap = null;
        BufferedInputStream bis = null;
        InputStream is = null;
        try {
            BitmapFactory.Options options = readImageFileOptions(context, uri);
            // Calculate inSampleSize
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                    orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                options.inSampleSize = calculateInSampleSize(options, reqHeight, reqWidth);
            } else {
                options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            }

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            is = getInputStream(context, uri);
            bis = new BufferedInputStream(is, 8192);
            bitmap = BitmapFactory.decodeStream(bis, null, options);
        } catch (Exception ex) {
            Debug.error(ex);
        } finally {
            try {
                if (bis != null) bis.close();
                if (is != null) is.close();
            } catch (Exception e) {
                Debug.error(e);
            }
        }

        return bitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
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

    public static Bitmap getScaledBitmap(final Bitmap rawBitmap, final float dstWidth, final float dstHeight) {
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

    public static Bitmap applyOrientation(Bitmap bitmap, int orientation) {
        int rotate;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            default:
                return bitmap;
        }

        return getRotatedBitmap(bitmap, rotate);
    }

    private static Bitmap getRotatedBitmap(Bitmap bitmap, int rotate) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
        bitmap.recycle();
        return result;
    }

    public static int resolveBitmapOrientation(Uri bitmapUri) throws IOException {
        ExifInterface exif = new ExifInterface(bitmapUri.getPath());
        return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
    }
}

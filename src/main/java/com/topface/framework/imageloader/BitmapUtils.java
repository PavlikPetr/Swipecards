package com.topface.framework.imageloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.net.Uri;

import com.topface.framework.utils.Debug;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

public class BitmapUtils {

    public static final int RADIUS_OUT = 0;
    public static final int RADIUS_IN = 1;

    public static Bitmap getBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        Bitmap bitmap = null;
        try {
            int orientation = resolveBitmapOrientation(uri);
            bitmap = decodeSampledBitmap(context, uri, reqWidth, reqHeight, orientation);
            bitmap = applyOrientation(bitmap, orientation);
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
            BitmapFactory.decodeStream(bis, null, options);
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

    public static Bitmap decodeSampledBitmap(Context context, Uri uri, int reqWidth, int reqHeight, int orientation) {
        Bitmap bitmap = null;
        BufferedInputStream bis = null;
        InputStream is = null;
        try {
            BitmapFactory.Options options = readImageFileOptions(context, uri);
            // Calculate inSampleSize
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                    orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                //noinspection SuspiciousNameCombination
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
        if (rawBitmap.isRecycled()) {
            Debug.error("Bitmap is already recycled");
            return rawBitmap;
        }
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
        if (result != rawBitmap) {
            rawBitmap.recycle();
        }
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
        if (bitmap.isRecycled()) {
            Debug.error("Bitmap is already recycled");
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
        if (result != bitmap) {
            bitmap.recycle();
        }
        return result;
    }

    public static int resolveBitmapOrientation(Uri bitmapUri) throws IOException {
        int attribute = ExifInterface.ORIENTATION_UNDEFINED;
        try {
            ExifInterface exif = new ExifInterface(bitmapUri.getPath());
            attribute = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (Exception e) {
            Debug.error(e);
        }
        return attribute;
    }

    public static Bitmap clipBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0)
            return null;

        // Исходный размер загруженного изображения
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // длинная фото или высокая
        Bitmap clippedBitmap;
        if (width >= height) {  // горизонтальная, вырезаем по центру
            int offset_x = (width - height) / 2;
            //noinspection SuspiciousNameCombination
            clippedBitmap = Bitmap.createBitmap(bitmap, offset_x, 0, height, height, null, false);
        } else {                // вертикальная, вырезаем сверху
            //noinspection SuspiciousNameCombination
            clippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, width, null, false);
        }

        return clippedBitmap;
    }

    public static Bitmap clipAndScaleBitmap(Bitmap rawBitmap, int dstWidth, int dstHeight) {
        if (rawBitmap == null
                || rawBitmap.getWidth() <= 0 || rawBitmap.getHeight() <= 0
                || dstWidth <= 0 || dstHeight <= 0)
            return null;

        Bitmap clippedBitmap = null;
        try {
            // Исходный размер загруженного изображения
            int srcWidth = rawBitmap.getWidth();
            int srcHeight = rawBitmap.getHeight();

            // буль, длинная фото или высокая
            boolean LAND = false;
            if (srcWidth >= srcHeight)
                LAND = true;

            // коффициент сжатия фотографии
            float ratio = Math.max(((float) dstWidth) / srcWidth, ((float) dstHeight) / srcHeight);

            // на получение оригинального размера по ширине или высоте
            if (ratio <= 0) ratio = 1;

            // матрица сжатия
            Matrix matrix = new Matrix();
            matrix.postScale(ratio, ratio);

            // сжатие изображения
            Bitmap scaledBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, srcWidth, srcHeight, matrix, true);

            if (scaledBitmap != null) {
                // вырезаем необходимый размер
                if (LAND) {
                    // у горизонтальной, вырезаем по центру
                    int offset_x = (scaledBitmap.getWidth() - dstWidth) / 2;
                    clippedBitmap = Bitmap.createBitmap(scaledBitmap, offset_x, 0, dstWidth, dstHeight, null, false);
                } else {
                    // у вертикальной режим с верху
                    clippedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, dstWidth, dstHeight, null, false);
                }
            }

        } catch (OutOfMemoryError e) {
            Debug.error("ClipANdScaleImage:: " + e.toString());
        }

        return clippedBitmap;
    }

    public static Bitmap getRoundedCornerBitmapByMask(Bitmap bitmap, Bitmap mask, Bitmap border) {
        int width = mask.getWidth();
        int height = mask.getHeight();

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap clippedBitmap = clipAndScaleBitmap(bitmap, width, height);

        if (clippedBitmap == null) {
            return null;
        }

        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        canvas.drawARGB(0, 0, 0, 0);
//
        canvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(clippedBitmap, 0, 0, paint);
        if (border != null) {
            paint.setXfermode(null);
            canvas.drawBitmap(border, 0, 0, paint);
        }
        clippedBitmap.recycle();

        return output;
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap, int dstWidth, int dstHeight) {
        if (bitmap == null)
            return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (dstWidth < dstHeight)
            //noinspection SuspiciousNameCombination
            dstHeight = dstWidth;
        else
            //noinspection SuspiciousNameCombination
            dstWidth = dstHeight;

        Bitmap output = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);

        Bitmap clippedBitmap;
        if (width == dstWidth && height == dstHeight) {
            clippedBitmap = clipBitmap(bitmap);
        } else {
            //noinspection SuspiciousNameCombination
            clippedBitmap = clipAndScaleBitmap(bitmap, dstWidth, dstWidth);
        }

        Canvas canvas = new Canvas(output);

        @SuppressWarnings("SuspiciousNameCombination")
        Rect rect = new Rect(0, 0, dstWidth, dstWidth);

        Paint paint = new Paint();


        paint.setAntiAlias(true);
        paint.setColor(0xff424242);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(dstWidth / 2, dstWidth / 2, dstWidth / 2, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(clippedBitmap, rect, rect, paint);

        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        } else {
            Debug.error("Bitmap is already recycled");
        }

        clippedBitmap.recycle();

        return output;
    }

    public static Bitmap getScaleAndRoundBitmapOut(Bitmap bitmap, final int width, final int height, float radiusMult) {
        return getScaleAndRoundBitmap(RADIUS_OUT, bitmap, width, height, radiusMult);
    }

    public static Bitmap getScaleAndRoundBitmapIn(Bitmap bitmap, final int width, final int height, float radiusMult) {
        return getScaleAndRoundBitmap(RADIUS_IN, bitmap, width, height, radiusMult);
    }

    private static Bitmap getScaleAndRoundBitmap(int type, Bitmap bitmap, final int width, final int height, float radiusMult) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        int multWidth;
        if (type == RADIUS_OUT)
            multWidth = (int) (((bitmapWidth > bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);
        else
            multWidth = (int) (((bitmapWidth < bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);


        @SuppressWarnings("SuspiciousNameCombination")
        Bitmap output = Bitmap.createBitmap(multWidth, multWidth, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final Rect src = new Rect(0, 0, bitmapWidth, bitmapHeight);
        final Rect dst = new Rect((multWidth - bitmapWidth) / 2, (multWidth - bitmapHeight) / 2, (multWidth + bitmapWidth) / 2, (multWidth - bitmapHeight) / 2 + bitmapHeight);

        Paint circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.WHITE);

        Paint canvasPaint = new Paint();
        canvasPaint.setAntiAlias(true);
        canvasPaint.setColor(0xff424242);

        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(multWidth / 2, multWidth / 2, multWidth / 2, circlePaint);
        canvasPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, canvasPaint);

        Bitmap scaledBitmap;

        if (multWidth != width)
            scaledBitmap = Bitmap.createScaledBitmap(output, width, height, true);
        else {
            scaledBitmap = output;
        }

        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        } else {
            Debug.error("Bitmap is already recycled");
        }

        return scaledBitmap;
    }
}

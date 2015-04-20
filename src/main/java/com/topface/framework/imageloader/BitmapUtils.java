package com.topface.framework.imageloader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;

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
            bitmap = applyOrientation(bitmap, orientation);
        } catch (Exception e) {
            Debug.error(e);
        }

        return bitmap;
    }

    public static int getOrientationPhotoFromGallery(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        int result = cursor.getInt(0);
        cursor.close();
        return result;
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
            int width = options.outWidth;
            int height = options.outHeight;
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            is = getInputStream(context, uri);
            bis = new BufferedInputStream(is, 8192);
            bitmap = BitmapFactory.decodeStream(bis, null, options);
            if (orientation == 0) {
                int galleryPhotoOrientation = getOrientationPhotoFromGallery(context, uri);
                //на некоторых телефонах ширина с высотой не меняются местами в зависимости от ориентации
                if (height < width && (galleryPhotoOrientation == 90 || galleryPhotoOrientation == 270)) {
                    bitmap = getRotatedBitmap(bitmap, galleryPhotoOrientation);
                }
            }
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
            if (srcWidth >= srcHeight) {
                LAND = true;
            }

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

    public static Bitmap getScaleAndRoundBitmapIn(Bitmap bitmap, float radiusMult) {
        return getScaleAndRoundBitmap(bitmap, radiusMult);
    }

    private static Bitmap getScaleAndRoundBitmap(Bitmap bitmap, float radiusMult) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        int resSize = ((bitmapWidth < bitmapHeight) ? bitmapWidth : bitmapHeight);

        Bitmap output = Bitmap.createBitmap(resSize, resSize, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        Path clipPath = new Path();
        RectF rect = new RectF(0, 0, resSize, resSize);
        clipPath.addRoundRect(rect, radiusMult, radiusMult, Path.Direction.CW);
        canvas.clipPath(clipPath);

        final Rect src = new Rect(0, 0, resSize, resSize);
        canvas.drawBitmap(bitmap, src, src, new Paint());
        return output;
    }

    public static Bitmap getRoundBitmap(Bitmap bitmap, float radiusMult) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        int multWidth = (int) (((bitmapWidth > bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);
        @SuppressWarnings("SuspiciousNameCombination")
        Bitmap output = Bitmap.createBitmap(multWidth, multWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Rect src = new Rect(0, 0, bitmapWidth, bitmapHeight);
        final Rect dst = new Rect((multWidth - bitmapWidth) / 2, (multWidth - bitmapHeight) / 2, (multWidth + bitmapWidth) / 2, (multWidth - bitmapHeight) / 2 + bitmapHeight);
        Paint circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(App.getContext().getResources().getColor(R.color.bg_white));
        Paint canvasPaint = new Paint();
        canvasPaint.setAntiAlias(true);
        canvas.drawCircle(multWidth / 2, multWidth / 2, multWidth / 2, circlePaint);
        canvasPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, src, dst, canvasPaint);
        return Bitmap.createScaledBitmap(output, bitmap.getWidth(), bitmap.getWidth(), true);
    }

    public static Bitmap squareBitmap(Bitmap bitmap, int width) {
        return getScaledBitmapInsideSquare(bitmap, width, 1.0f);
    }

    private static Bitmap getScaledBitmapInsideSquare(Bitmap bitmap, final int destSize, float radiusMult) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        int size = (int) (((bitmapWidth > bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Rect src = new Rect(0, 0, bitmapWidth, bitmapHeight);
        final Rect dst = new Rect((size - bitmapWidth) / 2, (size - bitmapHeight) / 2, (size + bitmapWidth) / 2, (size - bitmapHeight) / 2 + bitmapHeight);
        Paint canvasPaint = new Paint();
        canvasPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawBitmap(bitmap, src, dst, canvasPaint);
        Bitmap scaledBitmap;
        if (size != destSize) {
            scaledBitmap = Bitmap.createScaledBitmap(output, destSize, destSize, true);
            if (!output.isRecycled()) {
                output.recycle();
            }
        } else {
            scaledBitmap = output;
        }
        return scaledBitmap;
    }

    public static Bitmap squareCrop(Bitmap bitmap) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        int size = srcHeight > srcWidth ? srcWidth : srcHeight;
        return Bitmap.createBitmap(bitmap, 0, 0, size, size);
    }
}

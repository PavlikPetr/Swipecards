package com.topface.framework.imageloader;

import android.content.Context;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
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

import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import static com.topface.topface.ui.views.ImageViewRemote.DEFAULT_BORDER_COLOR;

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

    public static Bitmap fastBlur(Bitmap bitmap, int radius) {

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int dv[] = new int[256 * divsum];
        for (i = 0; i < 256 * divsum; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
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

    public static Bitmap getRoundBitmap(Bitmap bitmap, float radiusMult, float borderWidth, @Nullable ColorStateList borderColor) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        int whiteColor = Color.WHITE;
        borderColor = borderColor != null ? borderColor : DEFAULT_BORDER_COLOR;

        Bitmap mask = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        Canvas canvasMask = new Canvas(mask);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(whiteColor);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvasMask.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvasMask.drawCircle(bitmapWidth / 2, bitmapHeight / 2, bitmapWidth / 2 - borderWidth, paint);

        Bitmap output = mask.copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(output);
        int borderSize = (int) borderWidth;
        paint.reset();
        paint.setAntiAlias(true);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        int scaledImageHeight = (int) ((bitmapHeight - borderSize) / radiusMult);
        int scaledImageWidth = (int) ((bitmapWidth - borderSize) / radiusMult);
        int scaledImagePaddingHorizontal = (bitmapWidth - scaledImageWidth) / 2;
        int scaledImagePaddingVertical = (bitmapHeight - scaledImageWidth) / 2;
        canvas.drawBitmap(Bitmap.createScaledBitmap(bitmap, scaledImageWidth, scaledImageHeight, true), scaledImagePaddingHorizontal, scaledImagePaddingVertical, paint);
        paint.reset();
        paint.setAntiAlias(true);
        paint.setColor(borderColor.getDefaultColor());
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(borderWidth);
        canvas.drawCircle(bitmapWidth / 2, bitmapHeight / 2, bitmapWidth / 2 - borderWidth / 2, paint);
        return output;
    }

    @Nullable
    public static Bitmap getRoundAvatarBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        int multWidth = (bitmapWidth > bitmapHeight) ? bitmapWidth : bitmapHeight;
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

    public static Bitmap getInscribedCircleBitmap(Bitmap bitmap, int width, int height, float borderWidth, @Nullable ColorStateList borderColor) {
        if (borderWidth == 0 || borderColor == DEFAULT_BORDER_COLOR) {
            return getInscribedCircleBitmap(bitmap, width, height);
        } else {
            return getRoundBitmap(getInscribedCircleBitmap(bitmap, width, height), 1f, borderWidth, borderColor);
        }
    }

    private static Bitmap getInscribedCircleBitmap(Bitmap bitmap, int width, int height) {
        int bWidth = bitmap.getWidth();
        int bHeight = bitmap.getHeight();
        float viewRation = (float) width / (float) height;
        float bitmapRatio = (float) bWidth / (float) bHeight;
        int newWidth;
        int newHeight;
        if (bitmapRatio > viewRation) {
            newHeight = height;
            newWidth = (int) (height * bitmapRatio);
        } else {
            newWidth = width;
            newHeight = (int) (width / bitmapRatio);
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        int multWidth = (bitmapWidth > bitmapHeight) ? bitmapHeight : bitmapWidth;
        @SuppressWarnings("SuspiciousNameCombination")
        Bitmap output = Bitmap.createBitmap(multWidth, multWidth, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Rect src = new Rect(0, 0, multWidth, multWidth);
        Paint circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(App.getContext().getResources().getColor(R.color.bg_white));
        Paint canvasPaint = new Paint();
        canvasPaint.setAntiAlias(true);
        canvas.drawCircle(multWidth / 2, multWidth / 2, multWidth / 2, circlePaint);
        canvasPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
        canvas.drawBitmap(bitmap, src, src, canvasPaint);
        return Bitmap.createScaledBitmap(output, bitmap.getWidth(), bitmap.getWidth(), true);
    }

    public static Bitmap getBluredBitmap(Bitmap bitmap, int radius) {
        int crop = 15;
        if (bitmap != null) {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            if (width > 0 && height > 0) {
                bitmap = fastBlur(Bitmap.createScaledBitmap(bitmap, width / crop, height / crop, false), radius);
            }
        }
        return bitmap;
    }

    public static Bitmap squareBitmap(Bitmap bitmap, int width) {
        return getScaledBitmapInsideSquare(bitmap, width);
    }

    private static Bitmap getScaledBitmapInsideSquare(Bitmap bitmap, final int destSize) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(destSize, destSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint canvasPaint = new Paint();
        canvasPaint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        float aspectRatio;
        int scaledSize;
        int left;
        int top;
        int bottom;
        int right;
        if (bitmapWidth > bitmapHeight) {
            aspectRatio = (float) destSize / (float) bitmapWidth;
            left = 0;
            right = destSize;
            scaledSize = (int) (bitmapHeight * aspectRatio);
            top = (destSize - scaledSize) / 2;
            bottom = scaledSize + top;
        } else {
            aspectRatio = (float) destSize / (float) bitmapHeight;
            top = 0;
            bottom = destSize;
            scaledSize = (int) (bitmapWidth * aspectRatio);
            left = (destSize - scaledSize) / 2;
            right = scaledSize + left;
        }
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmapWidth, bitmapHeight), new Rect(left, top, right, bottom), canvasPaint);
        return output;
    }

    public static Bitmap squareCrop(Bitmap bitmap) {
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        int size = srcHeight > srcWidth ? srcWidth : srcHeight;
        return Bitmap.createBitmap(bitmap, 0, 0, size, size);
    }
}

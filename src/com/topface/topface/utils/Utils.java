package com.topface.topface.utils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Calendar;

import android.graphics.*;
import android.net.Uri;
import com.topface.topface.App;
import com.topface.topface.R;
import android.content.Context;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.TextView;

public class Utils {
    //---------------------------------------------------------------------------
    public static int unixtime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }
    //---------------------------------------------------------------------------
    public static String md5(String value) {
        if (value == null)
            return null;
        try {
            StringBuffer hexString = new StringBuffer();
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(value.getBytes());
            byte[] bytes = digester.digest();
            for (int i = 0; i < bytes.length; i++)
                hexString.append(Integer.toHexString(0xFF & bytes[i]));
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }
    //---------------------------------------------------------------------------    
    public static Bitmap clipping(Bitmap rawBitmap, int bitmapWidth, int bitmapHeight) {
        if (rawBitmap == null || bitmapWidth <= 0 || bitmapHeight <= 0)
            return null;

        // Исходный размер загруженного изображения
        int width = rawBitmap.getWidth();
        int height = rawBitmap.getHeight();

        // буль, длинная фото или высокая
        boolean LEG = false;

        if (width >= height)
            LEG = true;

        // коффициент сжатия фотографии
        float ratio = Math.max(((float) bitmapWidth) / width, ((float) bitmapHeight) / height);

        // на получение оригинального размера по ширине или высоте
        if (ratio == 0) ratio = 1;

        // матрица сжатия
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);

        // сжатие изображения
        Bitmap scaledBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, width, height, matrix, true);

        // вырезаем необходимый размер
        Bitmap clippedBitmap;
        if (LEG) {
            // у горизонтальной, вырезаем по центру
            int offset_x = (scaledBitmap.getWidth() - bitmapWidth) / 2;
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, offset_x, 0, bitmapWidth, bitmapHeight, null, false);
        } else
            // у вертикальной режим с верху
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, bitmapWidth, bitmapHeight, null, false);

        //rawBitmap.recycle();
        //rawBitmap = null;

        //scaledBitmap.recycle();
        //scaledBitmap = null;

        return clippedBitmap;
    }
    //---------------------------------------------------------------------------
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int width, int height, int roundPx) {
        if (width < height)
            height = width;
        else
            width = height;

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        Bitmap clippedBitmap = clipping(bitmap, width, height);

        Canvas canvas = new Canvas(output);

        final Rect rect = new Rect(0, 0, width, height);
        //final RectF rectF = new RectF(rect);
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setColor(0xff424242);
        canvas.drawARGB(0, 0, 0, 0);
        //canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        int r = width/2;
        canvas.drawCircle(r, r, r-2, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(clippedBitmap, rect, rect, paint);

        //bitmap.recycle();
        bitmap = null;

        return output;
    }
    //---------------------------------------------------------------------------
    public static void formatTime(TextView tv, long time) {
        Context context = tv.getContext();
        String text;
        long now = System.currentTimeMillis() / 1000;
        long full_time = time * 1000;
        long t = now - time;
        if ((time > now) || t < 60)
            text = context.getString(R.string.time_now);
        else if (t < 3600)
            text = formatMinute(context, t / 60);
        else if (t < 6 * 3600)
            text = formatHour(context, t / 3600);
        else if (DateUtils.isToday(full_time))
            text = context.getString(R.string.time_today) + DateFormat.format(" kk:mm", full_time).toString();
        else {
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            if (full_time > (now - (now - cal.getTimeInMillis()) - (24 * 60 * 60 * 1000)))
                text = context.getString(R.string.time_yesterday) + DateFormat.format(" kk:mm", full_time).toString();
            else
                text = DateFormat.format("dd.MM.yyyy kk:mm", full_time).toString();
        }
        tv.setText(text);
    }
    //---------------------------------------------------------------------------
    public static String formatHour(Context context, long hours) {
        byte caseValue = 0;
        if ((hours < 11) || (hours > 19)) {
            if (hours % 10 == 1)
                caseValue = 1;
            if ((hours % 10 == 2) || (hours % 10 == 3) || (hours % 10 == 4))
                caseValue = 2;
        }
        switch (caseValue) {
            case 1:
                return String.format(context.getString(R.string.time_hour_0), hours);
            case 2:
                return String.format(context.getString(R.string.time_hour_1), hours);
            default:
                return String.format(context.getString(R.string.time_hours), hours);
        }
    }
    //---------------------------------------------------------------------------
    public static String formatMinute(Context context, long minutes) {
        byte caseValue = 0;
        if ((minutes < 11) || (minutes > 19)) {
            if (minutes % 10 == 1)
                caseValue = 1;
            if ((minutes % 10 == 2) || (minutes % 10 == 3) || (minutes % 10 == 4))
                caseValue = 2;
        }
        switch (caseValue) {
            case 1:
                return String.format(context.getString(R.string.time_minute_0), minutes);
            case 2:
                return String.format(context.getString(R.string.time_minute_1), minutes);
            default:
                return String.format(context.getString(R.string.time_minutes), minutes);
        }
    }
    //---------------------------------------------------------------------------
    public static int getBatteryResource(int power) {
        int n = 50 * CacheProfile.power / 100;
        switch (n) {
            case 0:
                return R.drawable.battery_00;
            case 1:
                return R.drawable.battery_01;
            case 2:
                return R.drawable.battery_02;
            case 3:
                return R.drawable.battery_03;
            case 4:
                return R.drawable.battery_04;
            case 5:
                return R.drawable.battery_05;
            case 6:
                return R.drawable.battery_06;
            case 7:
                return R.drawable.battery_07;
            case 8:
                return R.drawable.battery_08;
            case 9:
                return R.drawable.battery_09;
            case 10:
                return R.drawable.battery_10;
            case 11:
                return R.drawable.battery_11;
            case 12:
                return R.drawable.battery_12;
            case 13:
                return R.drawable.battery_13;
            case 14:
                return R.drawable.battery_14;
            case 15:
                return R.drawable.battery_15;
            case 16:
                return R.drawable.battery_16;
            case 17:
                return R.drawable.battery_17;
            case 18:
                return R.drawable.battery_18;
            case 19:
                return R.drawable.battery_19;
            case 20:
                return R.drawable.battery_20;
            case 21:
                return R.drawable.battery_21;
            case 22:
                return R.drawable.battery_22;
            case 23:
                return R.drawable.battery_23;
            case 24:
                return R.drawable.battery_24;
            case 25:
                return R.drawable.battery_25;
            case 26:
                return R.drawable.battery_26;
            case 27:
                return R.drawable.battery_27;
            case 28:
                return R.drawable.battery_28;
            case 29:
                return R.drawable.battery_29;
            case 30:
                return R.drawable.battery_30;
            case 31:
                return R.drawable.battery_31;
            case 32:
                return R.drawable.battery_32;
            case 33:
                return R.drawable.battery_33;
            case 34:
                return R.drawable.battery_34;
            case 35:
                return R.drawable.battery_35;
            case 36:
                return R.drawable.battery_36;
            case 37:
                return R.drawable.battery_37;
            case 38:
                return R.drawable.battery_38;
            case 39:
                return R.drawable.battery_39;
            case 40:
                return R.drawable.battery_40;
            case 41:
                return R.drawable.battery_41;
            case 42:
                return R.drawable.battery_42;
            case 43:
                return R.drawable.battery_43;
            case 44:
                return R.drawable.battery_44;
            case 45:
                return R.drawable.battery_45;
            case 46:
                return R.drawable.battery_46;
            case 47:
                return R.drawable.battery_47;
            case 48:
                return R.drawable.battery_48;
            case 49:
                return R.drawable.battery_49;
            case 50:
                return R.drawable.battery_50;
            default:
                return R.drawable.battery_50;
        }
    }
    //---------------------------------------------------------------------------
    /**
     * Возвращает делитель, во сколько раз уменьшить размер изображения при создании битмапа
     *
     * @param in   InputStrem к изображению, для того, что бы получить его размеры, не загружая его в память
     * @param size размер до которого нужно уменьшить
     * @return делитель размера битмапа
     * @throws java.io.FileNotFoundException
     */
    public static int getBitmapScale(InputStream in, int size) throws FileNotFoundException {
        //1 по умолчанию, значит что битмап нет необходимости уменьшать
        int scale = 1;
        BitmapFactory.Options options = new BitmapFactory.Options();
        //Опция, сообщающая что не нужно грузить изображение в память
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(in, null, options);

        //Определяем во сколько раз нужно уменьшить изображение для создания битмапа
        if (options.outHeight > size || options.outWidth > size) {
            scale = (int) Math.pow(2,
                    (int) Math.round(
                            Math.log(
                                    size /
                                    (double) Math.max(options.outHeight, options.outWidth)) /
                                    Math.log(0.5)
                    )
            );
        }

        return scale;
    }
    //---------------------------------------------------------------------------
    public static Bitmap getMemorySafeBitmap(Uri uri, int size, Context ctx) {
        Bitmap bitmap = null;
        //Decode with inSampleSize
        try {
            InputStream in = ctx.getContentResolver().openInputStream(uri);
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = getBitmapScale(in, size);
            in.close();

            in = ctx.getContentResolver().openInputStream(uri);
            bitmap = BitmapFactory.decodeStream(in, null, o2);
            in.close();
        } catch (Exception e) {
            android.util.Log.w(App.TAG, "Can't get memory safe bitmap", e);
        }

        return bitmap;
    }
    //---------------------------------------------------------------------------
}

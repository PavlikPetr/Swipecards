package com.topface.topface.utils;

import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.text.ClipboardManager;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.i18n.plurals.PluralResources;
import com.topface.topface.App;
import com.topface.topface.Data;
import com.topface.topface.R;

import java.util.Calendar;

public class Utils {
    private static PluralResources mPluralResources;

    public static int unixtime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    public static Bitmap clippingBitmap(Bitmap bitmap) {
        if (bitmap == null || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0)
            return null;

        // Исходный размер загруженного изображения
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // длинная фото или высокая
        Bitmap clippedBitmap = null;
        if (width >= height) {  // горизонтальная, вырезаем по центру
            int offset_x = (width - height) / 2;
            clippedBitmap = Bitmap.createBitmap(bitmap, offset_x, 0, height, height, null, false);
        } else {                // вертикальная, вырезаем сверху
            clippedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, width, null, false);
        }

        return clippedBitmap;
    }

    public static Bitmap clipAndScaleBitmap(Bitmap rawBitmap, int dstWidth, int dstHeight) {
        if (rawBitmap == null || rawBitmap.getWidth() <= 0 || rawBitmap.getHeight() <= 0 || dstWidth <= 0 || dstHeight <= 0)
            return null;

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

        // вырезаем необходимый размер
        Bitmap clippedBitmap;
        if (LAND) {
            // у горизонтальной, вырезаем по центру
            int offset_x = (scaledBitmap.getWidth() - dstWidth) / 2;
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, offset_x, 0, dstWidth, dstHeight, null, false);
        } else
            // у вертикальной режим с верху
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, dstWidth, dstHeight, null, false);


        return clippedBitmap;
    }

    public static Bitmap getRoundedCornerBitmapByMask(Bitmap bitmap, Bitmap mask) {
        int width = mask.getWidth();
        int height = mask.getHeight();

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Bitmap clippedBitmap = clipAndScaleBitmap(bitmap, width, height);

        if (clippedBitmap == null)
            return null;

        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(clippedBitmap, 0, 0, paint);

        return output;
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        return getRoundedBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight());
    }

    public static Bitmap getRoundedBitmap(Bitmap bitmap, int dstWidth, int dstHeight) {
        if (bitmap == null)
            return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (dstWidth < dstHeight)
            dstHeight = dstWidth;
        else
            dstWidth = dstHeight;

        Bitmap output = Bitmap.createBitmap(dstWidth, dstHeight, Config.ARGB_8888);

        Bitmap clippedBitmap = null;
        if (width == dstWidth && height == dstHeight)
            clippedBitmap = clippingBitmap(bitmap);
        else
            clippedBitmap = clipAndScaleBitmap(bitmap, dstWidth, dstWidth);


        Canvas canvas = new Canvas(output);

        Rect rect = new Rect(0, 0, dstWidth, dstWidth);
        Paint paint = new Paint();


        paint.setAntiAlias(true);
        paint.setColor(0xff424242);
        canvas.drawARGB(0, 0, 0, 0);

        canvas.drawCircle(dstWidth / 2, dstWidth / 2, dstWidth / 2, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(clippedBitmap, rect, rect, paint);

        bitmap = null;

        return output;
    }

    public static final int RADIUS_OUT = 0;
    public static final int RADIUS_IN = 1;

    public static Bitmap getScaleAndRoundBitmapOut(Bitmap bitmap, final int width, final int height, float radiusMult) {
        return getScaleAndRoundBitmap(RADIUS_OUT, bitmap, width, height, radiusMult);
    }

    public static Bitmap getScaleAndRoundBitmapIn(Bitmap bitmap, final int width, final int height, float radiusMult) {
        return getScaleAndRoundBitmap(RADIUS_IN, bitmap, width, height, radiusMult);
    }

    private static Bitmap getScaleAndRoundBitmap(int type, Bitmap bitmap, final int width, final int height, float radiusMult) {
        final int bitmapWidth = bitmap.getWidth();
        final int bitmapHeight = bitmap.getHeight();

        int multWidth = 0;
        if (type == RADIUS_OUT)
            multWidth = (int) (((bitmapWidth > bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);
        else
            multWidth = (int) (((bitmapWidth < bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);


        Bitmap output = Bitmap.createBitmap(multWidth, multWidth, Config.ARGB_8888);

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
        canvasPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, canvasPaint);

        Bitmap scaledBitmap = null;

        if (multWidth != width)
            scaledBitmap = Bitmap.createScaledBitmap(output, width, height, true);
        else
            scaledBitmap = output;

        bitmap = null;
        output = null;

        return scaledBitmap;
    }

    public static String formatTime(Context context, long time) {
        String text;

        long day = 1000 * 60 * 60 * 24;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);

        if (time > Data.midnight)
            text = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        else if (time > Data.midnight - day * 5)
            text = formatDayOfWeek(context, cal.get(Calendar.DAY_OF_WEEK));
        else
            text = cal.get(Calendar.DAY_OF_MONTH) + " " + formatMonth(context, cal.get(Calendar.MONTH));

        return text;
    }

    public static String formatMinute(Context context, long minutes) {
        return Utils.getQuantityString(R.plurals.time_minute, (int) minutes, (int) minutes);
    }

    public static String formatDayOfWeek(Context context, int dayOfWeek) {
        int resurseId = 0;
        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                resurseId = R.string.time_sunday;
                break;
            case Calendar.MONDAY:
                resurseId = R.string.time_monday;
                break;
            case Calendar.TUESDAY:
                resurseId = R.string.time_tuesday;
                break;
            case Calendar.WEDNESDAY:
                resurseId = R.string.time_wednesday;
                break;
            case Calendar.THURSDAY:
                resurseId = R.string.time_thursday;
                break;
            case Calendar.FRIDAY:
                resurseId = R.string.time_friday;
                break;
            case Calendar.SATURDAY:
                resurseId = R.string.time_saturday;
                break;
        }
        return context.getString(resurseId);
    }

    public static String formatMonth(Context context, int month) {
        int resurseId = 0;
        switch (month) {
            case Calendar.JANUARY:
                resurseId = R.string.time_january;
                break;
            case Calendar.FEBRUARY:
                resurseId = R.string.time_february;
                break;
            case Calendar.MARCH:
                resurseId = R.string.time_march;
                break;
            case Calendar.APRIL:
                resurseId = R.string.time_april;
                break;
            case Calendar.MAY:
                resurseId = R.string.time_may;
                break;
            case Calendar.JUNE:
                resurseId = R.string.time_june;
                break;
            case Calendar.JULY:
                resurseId = R.string.time_july;
                break;
            case Calendar.AUGUST:
                resurseId = R.string.time_august;
                break;
            case Calendar.SEPTEMBER:
                resurseId = R.string.time_september;
                break;
            case Calendar.OCTOBER:
                resurseId = R.string.time_october;
                break;
            case Calendar.NOVEMBER:
                resurseId = R.string.time_november;
                break;
            case Calendar.DECEMBER:
                resurseId = R.string.time_december;
                break;
        }
        return context.getString(resurseId);
    }

    public static void formatTimeOld(TextView tv, long time) {
        Context context = tv.getContext();
        String text;
    }


    public static String formatHour(long hours) {
        return Utils.getQuantityString(R.plurals.time_hour, (int) hours, (int) hours);
    }


    public static String formatMinute(long minutes) {
        return Utils.getQuantityString(R.plurals.time_minute, (int) minutes, (int) minutes);
    }

    public static String formatPhotoQuantity(int quantity) {
        return Utils.getQuantityString(R.plurals.photo, (int) quantity, (int) quantity);
    }

    public static String formatFormMatchesQuantity(int quantity) {
        return Utils.getQuantityString(R.plurals.form_matches, (int) quantity, (int) quantity);
    }

    public static int getBatteryResource(int power) {
        int n = 50 * power / 100;
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

    public static void copyTextToClipboard(String text, Context context) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
    }

    public static String getQuantityString(int id, int quantity, Object... formatArgs) {
        try {
            mPluralResources = new PluralResources(App.getContext().getResources());
        } catch (Exception e) {
            Debug.error("Plural resources error", e);
        }
        return mPluralResources.getQuantityString(id, quantity, formatArgs);
    }

    public static void showErrorMessage(Context context) {
        Toast.makeText(
                context,
                context.getString(R.string.general_data_error),
                Toast.LENGTH_SHORT
        ).show();
    }
}
package com.topface.topface.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.text.ClipboardManager;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.i18n.plurals.PluralResources;
import com.topface.topface.App;
import com.topface.topface.R;

import java.security.MessageDigest;
import java.util.Calendar;

public class Utils {

    private static PluralResources mPluralResources;

    public static int unixtime() {
        return (int) (System.currentTimeMillis() / 1000L);
    }


    public static String md5(String value) {
        if (value == null)
            return null;
        try {
            StringBuilder hexString = new StringBuilder();
            MessageDigest digester = MessageDigest.getInstance("MD5");
            digester.update(value.getBytes());
            byte[] bytes = digester.digest();
            for (byte aByte : bytes) hexString.append(Integer.toHexString(0xFF & aByte));
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }


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
        try {
            if (LEG) {
                // у горизонтальной, вырезаем по центру
                int offset_x = (scaledBitmap.getWidth() - bitmapWidth) / 2;
                clippedBitmap = Bitmap.createBitmap(scaledBitmap, offset_x, 0, bitmapWidth, bitmapHeight, null, false);
            } else {
                // у вертикальной режим с верху
                clippedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, bitmapWidth, bitmapHeight, null, false);
            }
        } catch (Exception e) {
            Debug.error("Bitmap clip error", e);
            clippedBitmap = null;
        }


        return clippedBitmap;
    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int width, int height, int roundPx) {
        if (width < height) {
            //noinspection SuspiciousNameCombination
            height = width;
        } else {
            //noinspection SuspiciousNameCombination
            width = height;
        }

        Bitmap rounder = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap = clipping(bitmap, width, height);
        Canvas canvas = new Canvas(rounder);

        Paint xferPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        xferPaint.setColor(Color.RED);
        canvas.drawRoundRect(new RectF(0, 0, width, height), roundPx, roundPx, xferPaint);

        xferPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas resultCanvas = new Canvas(result);
        resultCanvas.drawBitmap(bitmap, 0, 0, null);
        resultCanvas.drawBitmap(rounder, 0, 0, xferPaint);

        return result;
    }


    public static void formatTime(TextView tv, long time) {
        Context context = tv.getContext();
        String text;
        long now = unixtime();
        long full_time = time * 1000;
        long t = now - time;
        if ((time > now) || t < 60)
            text = context.getString(R.string.time_now);
        else if (t < 3600)
            text = formatMinute(t / 60);
        else if (t < 6 * 3600)
            text = formatHour(t / 3600);
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


    public static String formatHour(long hours) {
        return Utils.getQuantityString(R.plurals.time_hour, (int) hours, (int) hours);
    }


    public static String formatMinute(long minutes) {
        return Utils.getQuantityString(R.plurals.time_minute, (int) minutes, (int) minutes);
    }

    @SuppressWarnings("ConstantConditions")
    public static int getBatteryResource() {
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

    public static boolean isDebugMode(Application application) {
        boolean debug = false;
        PackageInfo packageInfo = null;
        try {
            packageInfo = application.getPackageManager().getPackageInfo(application.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            int flags = packageInfo.applicationInfo.flags;
            debug = (flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
        }
        return debug;
    }
}

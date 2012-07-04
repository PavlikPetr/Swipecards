package com.topface.topface.utils;

import java.security.MessageDigest;
import java.util.Calendar;
import com.topface.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.TextView;

public class Utils {
    //---------------------------------------------------------------------------
    public static int unixtime() {
        return (int)(System.currentTimeMillis() / 1000L);
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
        } catch(Exception e) {
            return null;
        }
    }
    //---------------------------------------------------------------------------
    public static Bitmap clipping(Bitmap rawBitmap,int bitmapWidth,int bitmapHeight) {
        if (rawBitmap == null || bitmapWidth <= 0 || bitmapHeight <= 0)
            return null;

        // Р�СЃС…РѕРґРЅС‹Р№ СЂР°Р·РјРµСЂ Р·Р°РіСЂСѓР¶РµРЅРЅРѕРіРѕ РёР·РѕР±СЂР°Р¶РµРЅРёСЏ
        int width = rawBitmap.getWidth();
        int height = rawBitmap.getHeight();

        // Р±СѓР»СЊ, РґР»РёРЅРЅР°СЏ С„РѕС‚Рѕ РёР»Рё РІС‹СЃРѕРєР°СЏ
        boolean LEG = false;

        if (width >= height)
            LEG = true;

        // РєРѕС„С„РёС†РёРµРЅС‚ СЃР¶Р°С‚РёСЏ С„РѕС‚РѕРіСЂР°С„РёРё
        float ratio = Math.max(((float)bitmapWidth) / width, ((float)bitmapHeight) / height);

        // РЅР° РїРѕР»СѓС‡РµРЅРёРµ РѕСЂРёРіРёРЅР°Р»СЊРЅРѕРіРѕ СЂР°Р·РјРµСЂР° РїРѕ С€РёСЂРёРЅРµ РёР»Рё РІС‹СЃРѕС‚Рµ
        if (ratio == 0)
            ratio = 1;

        // РјР°С‚СЂРёС†Р° СЃР¶Р°С‚РёСЏ
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);

        // СЃР¶Р°С‚РёРµ РёР·РѕР±СЂР°Р¶РµРЅРёСЏ
        Bitmap scaledBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, width, height, matrix, true);

        // РІС‹СЂРµР·Р°РµРј РЅРµРѕР±С…РѕРґРёРјС‹Р№ СЂР°Р·РјРµСЂ
        Bitmap clippedBitmap;
        if (LEG) {
            // Сѓ РіРѕСЂРёР·РѕРЅС‚Р°Р»СЊРЅРѕР№, РІС‹СЂРµР·Р°РµРј РїРѕ С†РµРЅС‚СЂСѓ
            int offset_x = (scaledBitmap.getWidth() - bitmapWidth) / 2;
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, offset_x, 0, bitmapWidth, bitmapHeight, null, false);
        } else
            // Сѓ РІРµСЂС‚РёРєР°Р»СЊРЅРѕР№ СЂРµР¶РёРј СЃ РІРµСЂС…Сѓ
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, bitmapWidth, bitmapHeight, null, false);

        //rawBitmap.recycle();
        //rawBitmap = null;

        //scaledBitmap.recycle();
        //scaledBitmap = null;

        return clippedBitmap;
    }
    //---------------------------------------------------------------------------
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap,int width,int height,int roundPx) {
        if (width < height)
            height = width;
        else
            width = height;

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        Bitmap clippedBitmap = clipping(bitmap, width, height);

        Canvas canvas = new Canvas(output);

        final Rect rect = new Rect(0, 0, width, height);
        final RectF rectF = new RectF(rect);
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setColor(0xff424242);
        canvas.drawARGB(0, 0, 0, 0);

        // Mask
        //canvas.drawRoundRect(rectF, roundPx, roundPx, paint); //  Р·Р°РєСЂСѓРіР»РµРЅРЅС‹Рµ СѓРіР»С‹
        canvas.drawCircle(width / 2, height / 2, width / 2 - 2, paint); //  РєСЂСѓРіР»С‹Р№ Р°РІР°С‚Р°СЂ

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(clippedBitmap, rect, rect, paint);

        //bitmap.recycle();
        bitmap = null;

        return output;
    }
    //---------------------------------------------------------------------------
    public static Bitmap getRoundBitmap(Bitmap bitmap,int width,int height,float radiusMult) {
        int bitmapWidth  = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        int multWidth = (int) (bitmapWidth * radiusMult);
        
        Bitmap output = Bitmap.createBitmap(multWidth, multWidth, Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final Rect src = new Rect(0, 0, bitmapWidth, bitmapHeight);
        final Rect dst = new Rect((multWidth - bitmapWidth)/2, (multWidth - bitmapHeight)/2, (multWidth + bitmapWidth)/2, (multWidth - bitmapHeight)/2 + bitmapHeight);        
        
        final Paint circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.WHITE);
        
        final Paint canvasPaint = new Paint();
        canvasPaint.setAntiAlias(true);
        canvasPaint.setColor(0xff424242);
        
        canvas.drawARGB(0, 0, 0, 0);
        
        // Mask
        //canvas.drawRoundRect(rectF, roundPx, roundPx, paint); //  Р·Р°РєСЂСѓРіР»РµРЅРЅС‹Рµ СѓРіР»С‹
        canvas.drawCircle(multWidth / 2, multWidth / 2, multWidth / 2, circlePaint); //  РєСЂСѓРіР»С‹Р№ Р°РІР°С‚Р°СЂ

        canvasPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, canvasPaint);

        bitmap.recycle();
        bitmap = null;
        
        return Bitmap.createScaledBitmap(output, width, height, true);
    }
    //---------------------------------------------------------------------------
    public static void formatTime(TextView tv,long time) {
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
    public static String formatHour(Context context,long hours) {
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
    public static String formatMinute(Context context,long minutes) {
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
}

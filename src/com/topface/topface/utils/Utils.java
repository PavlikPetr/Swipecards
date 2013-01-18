package com.topface.topface.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.net.Uri;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;
import com.topface.i18n.plurals.PluralResources;
import com.topface.topface.App;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.ui.NavigationActivity;

import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final long WEEK = 604800L;
    public static final long DAY = 86400L;

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
        if (rawBitmap == null || rawBitmap.getWidth() <= 0 || rawBitmap.getHeight() <= 0 || dstWidth <= 0 || dstHeight <= 0)
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

            // вырезаем необходимый размер

            if (LAND) {
                // у горизонтальной, вырезаем по центру
                int offset_x = (scaledBitmap.getWidth() - dstWidth) / 2;
                clippedBitmap = Bitmap.createBitmap(scaledBitmap, offset_x, 0, dstWidth, dstHeight, null, false);
            } else
                // у вертикальной режим с верху
                clippedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, dstWidth, dstHeight, null, false);
        } catch (OutOfMemoryError e) {
            Debug.error("ClipANdScaleImage:: " + e.toString());
        }

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
            //noinspection SuspiciousNameCombination
            dstHeight = dstWidth;
        else
            //noinspection SuspiciousNameCombination
            dstWidth = dstHeight;

        Bitmap output = Bitmap.createBitmap(dstWidth, dstHeight, Config.ARGB_8888);

        Bitmap clippedBitmap;
        if (width == dstWidth && height == dstHeight) {
            clippedBitmap = clippingBitmap(bitmap);
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

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(clippedBitmap, rect, rect, paint);

        //noinspection UnusedAssignment
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

        int multWidth;
        if (type == RADIUS_OUT)
            multWidth = (int) (((bitmapWidth > bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);
        else
            multWidth = (int) (((bitmapWidth < bitmapHeight) ? bitmapWidth : bitmapHeight) * radiusMult);


        @SuppressWarnings("SuspiciousNameCombination")
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

        Bitmap scaledBitmap;

        if (multWidth != width)
            scaledBitmap = Bitmap.createScaledBitmap(output, width, height, true);
        else
            scaledBitmap = output;

        //noinspection UnusedAssignment
        output = bitmap = null;

        return scaledBitmap;
    }

    public static String formatTime(Context context, long time) {
        String text;

        long day = 1000 * 60 * 60 * 24;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        Calendar cal2 = Calendar.getInstance();
        int currentYear = cal2.get(Calendar.YEAR);
        cal2.set(currentYear, Calendar.JANUARY, 1);

        if (time > Data.midnight)
            text = cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE);
        else if (time > Data.midnight - day * 5)
            text = formatDayOfWeek(context, cal.get(Calendar.DAY_OF_WEEK));

        else if (time > cal2.getTimeInMillis())
            text = cal.get(Calendar.DAY_OF_MONTH) + " " + formatMonth(context, cal.get(Calendar.MONTH));

        else
            text = cal.get(Calendar.DAY_OF_MONTH) + " " + formatMonth(context, cal.get(Calendar.MONTH)) + " " + cal.get(Calendar.YEAR);

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

    public static String formatHour(long hours) {
        return Utils.getQuantityString(R.plurals.time_hour, (int) hours, (int) hours);
    }


    public static String formatMinute(long minutes) {
        return Utils.getQuantityString(R.plurals.time_minute, (int) minutes, (int) minutes);
    }

    public static String formatPhotoQuantity(int quantity) {
        return Utils.getQuantityString(R.plurals.photo, quantity, (int) quantity);
    }

    public static String formatFormMatchesQuantity(int quantity) {
        return Utils.getQuantityString(R.plurals.form_matches, quantity, (int) quantity);
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
        if (context != null) {
            Toast.makeText(
                    context,
                    context.getString(R.string.general_data_error),
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    @SuppressWarnings("deprecation")
    public static Point getSrceenSize(Context context) {
        Point size;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT < 13) {
            size = new Point(display.getWidth(), display.getHeight());
        } else {
            size = new Point();
            display.getSize(size);
        }
        return size;
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static boolean isThereNavigationActivity(Activity activity) {
        ActivityManager mngr = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = mngr.getRunningTasks(10);
        if (taskList != null) {
            if (taskList.size() > 1) {
                if (
                        taskList.get(0).baseActivity.getClassName().equals(NavigationActivity.class.getName())
                                || taskList.get(1).topActivity.getClassName().equals(NavigationActivity.class.getName())
                        ) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return флаг наличия API Google карт
     */
    public static boolean isGoogleMapsAvailable() {
        Class mapClass;
        try {
            mapClass = Class.forName("com.google.android.maps.MapActivity");
        } catch (ClassNotFoundException e) {
            mapClass = null;
        } catch (Exception e) {
            mapClass = null;
            Debug.error(e);
        }
        return mapClass != null;
    }

    private final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\._%\\-\\+]{1,256}@" +
                    "" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    public static boolean isValidEmail(CharSequence email) {
        return email != null && EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    public static void goToMarket(Context context) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.default_market_link))));
    }

    public static String getBuildType() {
        String type;
        Context context = App.getContext();

        try {
            //Получаем мета данные из информации приложения
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA
            );
            //Получаем тип сборки
            type = info.metaData.getString(
                    context.getString(R.string.build_type_key)
            );
        } catch (PackageManager.NameNotFoundException e) {
            Debug.error("BuildType error", e);
            type = context.getString(R.string.build_default);
        }

        return type;
    }

    public static String getClientVersion(Context context) {
        String version;
        context = context != null ? context : App.getContext();
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (Exception e) {
            Debug.error(e);
            version = AuthRequest.FALLBACK_CLIENT_VERSION;
        }
        return version;
    }
}
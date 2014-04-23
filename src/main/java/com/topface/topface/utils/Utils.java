package com.topface.topface.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Display;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.topface.i18n.plurals.PluralResources;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.utils.social.AuthToken;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final long DAY = 86400000;
    public static final long WEEK_IN_SECONDS = 604800;
    public static final int RADIUS_OUT = 0;
    public static final int RADIUS_IN = 1;
    // from android.util.Patterns.EMAIL_ADDRESS
    private final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\._%\\-\\+]{1,256}@" +
                    "" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );
    private static PluralResources mPluralResources;
    private static String mClientVersion;
    private static float mDensity = App.getContext().getResources().getDisplayMetrics().density;

    public static int unixtimeInSeconds() {
        return (int) (System.currentTimeMillis() / 1000L);
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

        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Bitmap clippedBitmap = clipAndScaleBitmap(bitmap, width, height);

        if (clippedBitmap == null) {
            return null;
        }

        Canvas canvas = new Canvas(output);

        Paint paint = new Paint();
        canvas.drawARGB(0, 0, 0, 0);
//
        canvas.drawBitmap(mask, 0, 0, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
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

        Bitmap output = Bitmap.createBitmap(dstWidth, dstHeight, Config.ARGB_8888);

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

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
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

    public static String formatPhotoQuantity(int quantity) {
        return Utils.getQuantityString(R.plurals.photo, quantity, (int) quantity);
    }

    public static String getQuantityString(int id, int quantity, Object... formatArgs) {
        try {
            mPluralResources = new PluralResources(App.getContext().getResources());
        } catch (Exception e) {
            Debug.error("Plural resources error", e);
        }
        return mPluralResources.getQuantityString(id, quantity, formatArgs);
    }

    public static void showErrorMessage() {
        Context context = App.getContext();
        if (context != null) {
            Toast.makeText(
                    context,
                    R.string.general_data_error,
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
        if (packageManager != null) {
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } else {
            return false;
        }
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void goToUrl(Context context, String url) {
        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    public static void goToMarket(Context context) {
        context.startActivity(getMarketIntent(context));
    }

    public static Intent getMarketIntent(Context context) {
        String link;
        //Для амазона делаем специальную ссылку, иначе он ругается, хотя и работает
        switch (BuildConfig.BILLING_TYPE) {
            case AMAZON:
                link = context.getString(R.string.amazon_market_link);
                break;
            default:
                link = context.getString(R.string.default_market_link);
                break;
        }

        return new Intent(Intent.ACTION_VIEW, Uri.parse(link));
    }

    public static String getClientVersion() {
        if (mClientVersion == null) {
            try {
                Context context = App.getContext();
                PackageManager pManager = context.getPackageManager();
                if (pManager != null) {
                    mClientVersion = pManager.getPackageInfo(context.getPackageName(), 0).versionName;
                }
            } catch (Exception e) {
                Debug.error(e);
                mClientVersion = AuthRequest.FALLBACK_CLIENT_VERSION;
            }
        }
        return mClientVersion;
    }

    public static String getClientDeviceName() {
        return Build.MANUFACTURER + " " + Build.MODEL + " " + Build.PRODUCT;

    }

    public static String getClientOsVersion() {
        return "Android " + Build.VERSION.RELEASE + "; Build/" + Build.PRODUCT;
    }

    public static void hideSoftKeyboard(Context context, EditText... edTexts) {
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            for (EditText edText : edTexts) {
                if (edText != null) {
                    imm.hideSoftInputFromWindow(edText.getWindowToken(), 0);
                }
            }
        }
    }

    public static void showSoftKeyboard(Context context, EditText editText) {
        editText.requestFocus();
        InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static int getPxFromDp(int pixels) {
        return (int) (mDensity * pixels);
    }

    public static String getSocialNetworkLink(String socialNetwork, String socialId) {
        String socialNetworkLink = "";
        if (TextUtils.equals(socialNetwork, AuthToken.SN_VKONTAKTE)) {
            socialNetworkLink = "https://vk.com/id" + socialId;
        } else if (TextUtils.equals(socialNetwork, AuthToken.SN_FACEBOOK)) {
            socialNetworkLink = "https://www.facebook.com/" + socialId;
        } else if (TextUtils.equals(socialNetwork, AuthToken.SN_TOPFACE)) {
            socialNetworkLink = "http://topface.com/profile/" + socialId + "/";
        }
        return socialNetworkLink;
    }

    @SuppressWarnings("unchecked")
    public static ArrayList sparsArrayToArrayList(SparseArray array) {
        ArrayList list = new ArrayList();
        for (int i = 0; i < array.size(); i++) {
            int key = array.keyAt(i);
            list.add(key, array.get(key));
        }
        return list;
    }

    public static String capitalize(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    public static String getText(EditText editText) {
        if (editText == null) {
            return Static.EMPTY;
        }
        Editable text = editText.getText();
        if (text == null) {
            return Static.EMPTY;
        }
        return text.toString();
    }

    public static int getConnectivityType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null) {
            return info.getType();
        }
        return -1;
    }
}
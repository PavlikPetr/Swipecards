package com.topface.topface.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
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

import com.topface.framework.utils.Debug;
import com.topface.i18n.plurals.PluralResources;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.social.AuthToken;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final long DAY = 86400000;
    public static final long WEEK_IN_SECONDS = 604800;
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
    private static float mDensity = App.getContext().getResources().getDisplayMetrics().density;

    public static int unixtimeInSeconds() {
        return (int) (System.currentTimeMillis() / 1000L);
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

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @SuppressWarnings("deprecation")
    public static Point getSrceenSize(Context context) {
        Point size;
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
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
        Intent marketIntent = getMarketIntent(context);
        if (isCallableIntent(marketIntent, context)) {
            context.startActivity(marketIntent);
        } else {
            Toast.makeText(context, R.string.open_market_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isCallableIntent(Intent intent, Context context) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
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

}
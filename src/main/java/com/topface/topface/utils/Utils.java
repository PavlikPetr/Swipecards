package com.topface.topface.utils;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.i18n.plurals.PluralResources;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.IDialogListener;
import com.topface.topface.ui.dialogs.OldVersionDialog;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.debug.HockeySender;
import com.topface.topface.utils.social.AuthToken;

import org.acra.sender.ReportSenderException;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final long DAY = 86400000;
    public static final long WEEK_IN_SECONDS = 604800;
    private static final String DASH_SYMBOL = "-";
    private static final String HYPHEN_SYMBOL = "&#8209;";
    // from android.util.Patterns.EMAIL_ADDRESS
    private final static Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\._%\\-\\+]{1,256}@" +
                    "" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{1,25}" +
                    ")+"
    );
    private static PluralResources mPluralResources;
    private static float mDensity = App.getContext().getResources().getDisplayMetrics().density;
    private static String mCarrier;

    public static int unixtimeInSeconds() {
        return (int) (System.currentTimeMillis() / 1000L);
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
            Utils.showToastNotification(R.string.general_data_error, Toast.LENGTH_SHORT);
        }
    }

    public static void showToastNotification(int stringId, int duration) {
        Context context = App.getContext();
        if (context != null && (duration == 0 || duration == 1)) {
            Toast.makeText(
                    context,
                    stringId,
                    duration
            ).show();
        }
    }

    public static void showCantSetPhotoAsMainToast(IApiResponse response) {
        try {
            Utils.showToastNotification(response.getJsonResult().getString("userMessage"), Toast.LENGTH_SHORT);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void showToastNotification(String text, int duration) {
        Context context = App.getContext();
        if (context != null && (duration == 0 || duration == 1)) {
            Toast.makeText(
                    context,
                    text,
                    duration
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

    public static Integer getGooglePlayServicesVersion() {
        try {
            return App.getContext().getPackageManager().getPackageInfo("com.google.android.gms", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Debug.error("Can't obtain google play services version, no gcm available");
        } catch (Exception e) {
            Debug.error("Can't obtain google play services version", e);
        }
        return null;
    }

    public static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    public static void goToUrl(Context context, String url) {
        Intent i = Utils.getIntentToOpenUrl(url);
        if (i != null) {
            context.startActivity(i);
        }
    }

    public static Intent getIntentToOpenUrl(String url) {
        if (!TextUtils.isEmpty(url)) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            return i;
        }
        return null;
    }

    public static void startOldVersionPopup(final Activity activity) {
        startOldVersionPopup(activity, true);
    }

    public static void startOldVersionPopup(final Activity activity, boolean cancelable) {
        OldVersionDialog oldVersionDialog = OldVersionDialog.newInstance(cancelable);
        oldVersionDialog.setDialogInterface(new IDialogListener() {
            @Override
            public void onPositiveButtonClick() {
                Utils.goToMarket(activity);
            }

            @Override
            public void onNegativeButtonClick() {
            }

            @Override
            public void onDismissListener() {
            }
        });
        oldVersionDialog.show(((FragmentActivity) activity).getSupportFragmentManager(), OldVersionDialog.class.getName());
    }

    public static void goToMarket(Activity context) {
        goToMarket(context, null);
    }

    public static void goToMarket(Activity context, Integer requestCode) {
        Intent marketIntent = getMarketIntent();
        if (isCallableIntent(marketIntent, context)) {
            if (requestCode == null) {
                context.startActivity(marketIntent);
            } else {
                context.startActivityForResult(marketIntent, requestCode);
            }
        } else {
            Toast.makeText(context, R.string.open_market_error, Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isCallableIntent(Intent intent, Context context) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static Intent getMarketIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(CacheProfile.getOptions().updateUrl));
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

    public static void hideSoftKeyboard(Context context, IBinder windowToken) {
        if (context != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void showSoftKeyboard(Context context, EditText editText) {
        InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editText == null) {
            keyboard.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        } else {
            editText.requestFocus();
            keyboard.showSoftInput(editText, InputMethodManager.SHOW_FORCED);
        }
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

    /**
     * Method to pass activity results to nested fragments.
     */
    public static void activityResultToNestedFragments(FragmentManager fm, int requestCode, int resultCode, Intent data) {
        if (fm != null) {
            List<Fragment> bodyFragments = fm.getFragments();
            if (bodyFragments != null) {
                for (Fragment fragment : bodyFragments) {
                    if (fragment != null && !fragment.isDetached() && !fragment.isRemoving()) {
                        fragment.onActivityResult(requestCode, resultCode, data);
                    }
                }
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void enableLayoutChangingTransition(ViewGroup viewGroup) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewGroup.setLayoutTransition(new LayoutTransition());
            LayoutTransition transition = viewGroup.getLayoutTransition();
            transition.enableTransitionType(LayoutTransition.CHANGING);
        }
    }

    public static String getCarrierName() {
        if (!TextUtils.isEmpty(mCarrier)) {
            return mCarrier;
        }
        TelephonyManager telephonyManager = (TelephonyManager) App.getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        if (telephonyManager == null) {
            return null;
        }
        mCarrier = telephonyManager.getSimOperatorName();
        return mCarrier;
    }

    public static String getNameAndAge(String userName, int userAge) {
        String name = TextUtils.isEmpty(userName) ? "" : userName;
        String divider = TextUtils.isEmpty(name) ? "" : ", ";
        String age = userAge > 0 ? Integer.toString(userAge) : "";
        divider = TextUtils.isEmpty(age) ? "" : divider;
        return name.concat(divider).concat(age);
    }

    public static String optString(JSONObject json, String key) {
        if (json.isNull(key))
            return null;
        else
            return json.optString(key, null);
    }

    /**
     * Устанавливает фон для ImageView если передать -1 будет установлен null
     *
     * @param imageView ImageView в котором нужно поменять фон
     * @param res       цвет фона
     */
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void setBackground(ImageView imageView, @DrawableRes int res) {
        Drawable background = null;
        if (res != -1) {
            background = App.getContext().getResources().getDrawable(res);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setBackgroundDrawable(background);
        } else {
            imageView.setBackground(background);
        }
    }

    public static ConnectionChangeReceiver.ConnectionType getConnectionType() {
        AppConfig config = App.getAppConfig();
        if (config.getDebugConnectionChecked()) {
            return ConnectionChangeReceiver.ConnectionType.valueOf(config.getDebugConnection());
        }
        return ConnectionChangeReceiver.getConnectionType();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void addOnGlobalLayoutListener(final View view, final ViewTreeObserver.OnGlobalLayoutListener listener) {
        ViewTreeObserver vto = view.getViewTreeObserver();
        if (vto != null && vto.isAlive()) {
            view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    listener.onGlobalLayout();
                    ViewTreeObserver vto = view.getViewTreeObserver();
                    if (vto != null && vto.isAlive()) {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            //noinspection deprecation
                            vto.removeGlobalOnLayoutListener(this);
                        } else {
                            vto.removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }

    }

    public static int getColorPrimaryDark(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            TypedArray a = context.getTheme().obtainStyledAttributes(R.style.Theme_Topface, new int[]{android.R.attr.colorPrimaryDark});
            return a.getResourceId(0, 0);
        }
        return R.color.light_theme_color_primary_dark;
    }

    public static String replaceDashWithHyphen(String text) {
        return Html.fromHtml(text.replaceAll(DASH_SYMBOL, HYPHEN_SYMBOL)).toString();
    }

    public static void sendHockeyMessage(final Context context, final String message) {
        new BackgroundThread() {
            @Override
            public void execute() {
                HockeySender hockeySender = new HockeySender();
                try {
                    hockeySender.send(context, hockeySender.createLocalReport(context, new Exception(message)));
                } catch (ReportSenderException e) {
                    e.printStackTrace();
                }
            }
        };
    }

}

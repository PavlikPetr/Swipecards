package com.topface.topface.utils;

import android.animation.LayoutTransition;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Display;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.topface.framework.imageloader.IPhoto;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.i18n.plurals.PluralResources;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.IEmailConfirmationListener;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.debug.HockeySender;
import com.topface.topface.utils.exception.OurTestException;
import com.topface.topface.utils.social.AuthToken;

import org.acra.sender.ReportSenderException;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class Utils {
    public static final long DAY = 86400000;
    public static final long WEEK_IN_SECONDS = 604800;
    public static final String EMPTY = "";
    public static final String AMPERSAND = "&";
    public static final String SEMICOLON = ":";
    public static final String LOCAL_RES = "drawable://%d";
    public static final String PLATFORM = "Android";
    private static final String DASH_SYMBOL = "-";
    private static final String HYPHEN_SYMBOL = "&#8209;";
    private static final String EMPTY_JSON = "{}";
    public static String RU_LOCALE = "ru";
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
    public static final String USER_ID = "{userId}";
    public static final String SECRET_KEY = "{secretKey}";

    public static int unixtimeInSeconds() {
        return (int) (System.currentTimeMillis() / 1000L);
    }

    public static String getLocalResUrl(@DrawableRes int res) {
        return String.format(App.getCurrentLocale(), Utils.LOCAL_RES, res);
    }

    public static String getQuantityString(int id, int quantity, Object... formatArgs) {
        try {
            mPluralResources = new PluralResources(App.getContext().getResources());
        } catch (Exception e) {
            Debug.error("Plural resources error", e);
        }
        return mPluralResources.getQuantityString(id, quantity, formatArgs);
    }

    public static Locale getRussianLocale() {
        return new Locale(RU_LOCALE);
    }

    public static void showErrorMessage() {
        Context context = App.getContext();
        if (context != null) {
            Utils.showToastNotification(R.string.general_data_error, Toast.LENGTH_SHORT);
        }
    }

    public static IPhoto getUserPhotoGag(final String emptyPhoto) {
        return new IPhoto() {
            @Override
            public boolean isFake() {
                return false;
            }

            @Override
            public String getSuitableLink(int height, int width) {
                return emptyPhoto;
            }

            @Override
            public String getSuitableLink(String sizeString) {
                return emptyPhoto;
            }

            @Override
            public String getDefaultLink() {
                return emptyPhoto;
            }
        };
    }

    public static void showToastNotification(int stringId, int duration) {
        Context context = App.getContext();
        if (context != null && (duration == 0 || duration == 1)) {
            Toast toast;
            try {
                /*
                краш при инфлейте тоста на соньках
                https://rink.hockeyapp.net/manage/apps/26531/app_versions/343/crash_reasons/110273859?scope=devices&type=statistics
                 */
                toast = Toast.makeText(
                        context,
                        stringId,
                        duration
                );
            } catch (InflateException e) {
                e.printStackTrace();
                return;
            }
            if (toast != null) {
                /*
                нет, я не сошел с ума. На некоторых девайсах Toast.makeText может возвращать null.
                Такие дела
                 */
                toast.show();
            }
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

    public static boolean isEmptyJson(JSONObject object) {
        return object.toString().equals(EMPTY_JSON);
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

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        if (packageManager != null) {
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            return list.size() > 0;
        } else {
            return false;
        }
    }

    public static boolean isIntentAvailable(Context context, String action) {
        return isIntentAvailable(context, new Intent(action));
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
            FlurryManager.getInstance().sendExternalUrlEvent(url);
            context.startActivity(i);
        }
    }

    public static void goToUrl(IActivityDelegate iActivityDelegate, String url) {
        if (iActivityDelegate != null) {
            Intent i = Utils.getIntentToOpenUrl(url);
            if (i != null) {
                FlurryManager.getInstance().sendExternalUrlEvent(url);
                iActivityDelegate.startActivity(i);
            }
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

    public static String getClassName(String className) {
        return removeModulesName(className, "Fragment", "Dialog", "Popup");
    }

    public static String removeModulesName(String className, String... modulesName) {
        for (String module : modulesName) {
            className = className.replace(module, EMPTY);
        }
        return className;
    }


    public static void showCustomToast(int text) {
        Context context = App.getContext();
        if (context != null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.custom_toast, null, false);

            ImageView image = (ImageView) layout.findViewById(R.id.image);
            image.setImageResource(R.drawable.ic_not_enough_data);
            ((TextView) layout.findViewById(R.id.text)).setText(text);

            Toast toast = new Toast(context);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            toast.show();
        }
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
            showToastNotification(R.string.open_market_error, Toast.LENGTH_SHORT);
        }
    }

    public static void goToMarket(IActivityDelegate activityDelegate, Integer requestCode) {
        Intent marketIntent = getMarketIntent();
        if (isCallableIntent(marketIntent, activityDelegate)) {
            if (requestCode == null) {
                activityDelegate.startActivity(marketIntent);
            } else {
                activityDelegate.startActivityForResult(marketIntent, requestCode);
            }
        } else {
            showToastNotification(R.string.open_market_error, Toast.LENGTH_SHORT);
        }
    }

    public static boolean isCallableIntent(Intent intent, Context context) {
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private static boolean isCallableIntent(Intent intent, IActivityDelegate activityDelegate) {
        if (intent == null) {
            return false;
        }
        List<ResolveInfo> list = activityDelegate.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static Intent getMarketIntent() {
        return new Intent(Intent.ACTION_VIEW, Uri.parse(App.get().getOptions().updateUrl));
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

    public static void hideSoftKeyboard(Context context, @Nullable IBinder windowToken) {
        if (context != null && windowToken != null) {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public static void showSoftKeyboard(Context context, EditText editText) {
        InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (editText != null) {
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
            return EMPTY;
        }
        Editable text = editText.getText();
        if (text == null) {
            return EMPTY;
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

    public static void sendHockeyMessage(final String message) {
        final Context context = App.getContext();
        new BackgroundThread() {
            @Override
            public void execute() {
                HockeySender hockeySender = new HockeySender();
                try {
                    hockeySender.send(context, hockeySender.createLocalReport(context, new OurTestException(message)));
                } catch (ReportSenderException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static String getUnlockButtonText(int sec) {
        int minutes = (int) Math.ceil((float) sec / (float) DateUtils.MINUTE_IN_SECONDS);
        return String.format(App.getContext().getString(R.string.unlock_by_viewed_ad_video_button_text),
                Utils.getQuantityString(R.plurals.free_minutes, minutes, minutes));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static class ActivityLifecycleCallbacksAdapter implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }
    }

    public static boolean checkPlayServices(Context context) {
        return GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
    }

    public static void checkEmailConfirmation(final IEmailConfirmationListener emailConfirmationListener, final boolean isNeedShowToast) {
        final boolean isEmailConfirmedCurrentValue = App.get().getProfile().emailConfirmed;
        new ProfileRequest(App.getContext()).callback(new DataApiHandler<Profile>() {
            @Override
            protected void success(Profile data, IApiResponse response) {
                if (emailConfirmationListener != null) {
                    emailConfirmationListener.onSuccess(data, response);
                }
                if (data != null) {
                    boolean isConfirmed = data.emailConfirmed;
                    if (emailConfirmationListener != null) {
                        emailConfirmationListener.onEmailConfirmed(isConfirmed);
                    }
                    if (isNeedShowToast) {
                        Utils.showToastNotification(isConfirmed ? R.string.general_email_success_confirmed : R.string.general_email_not_confirmed, Toast.LENGTH_LONG);
                    } else {
                        if (isEmailConfirmedCurrentValue != isConfirmed && isConfirmed) {
                            Utils.showToastNotification(R.string.general_email_success_confirmed, Toast.LENGTH_LONG);
                        }
                    }
                    if (isConfirmed) {
                        App.getUserOptionsRequest().exec();
                    }
                }

            }

            @Override
            protected Profile parseResponse(ApiResponse response) {
                return new Profile(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (emailConfirmationListener != null) {
                    emailConfirmationListener.fail(codeError, response);
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (emailConfirmationListener != null) {
                    emailConfirmationListener.always(response);
                }
            }
        }).exec();
    }
}

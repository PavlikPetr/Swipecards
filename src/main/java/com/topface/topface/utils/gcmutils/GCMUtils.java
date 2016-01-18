package com.topface.topface.utils.gcmutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SerializableToJson;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.requests.RegistrationTokenRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.fragments.feed.LikesFragment;
import com.topface.topface.ui.fragments.feed.MutualFragment;
import com.topface.topface.ui.fragments.feed.TabbedFeedFragment;
import com.topface.topface.ui.fragments.feed.VisitorsFragment;
import com.topface.topface.ui.fragments.profile.UserFormFragment;
import com.topface.topface.ui.fragments.profile.UserPhotoFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.notifications.MessageStack;
import com.topface.topface.utils.notifications.UserNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.GEO;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_LIKES;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.TABBED_VISITORS;

public class GCMUtils {
    public static final String GCM_NOTIFICATION = "com.topface.topface.action.NOTIFICATION";

    private static final String SENDER_ID = "932206034265";
    private Context mContext;
    private GoogleCloudMessaging mGcmObject;
    private String mRegId;

    public static final int GCM_TYPE_UNKNOWN = -1;
    public static final int GCM_TYPE_MESSAGE = 0;
    public static final int GCM_TYPE_MUTUAL = 1;
    public static final int GCM_TYPE_LIKE = 2;
    public static final int GCM_TYPE_GUESTS = 4;
    public static final int GCM_TYPE_UPDATE = 5;
    public static final int GCM_TYPE_PROMO = 6;
    public static final int GCM_TYPE_GIFT = 7;
    public static final int GCM_TYPE_DIALOGS = 8;
    public static final int GCM_TYPE_PEOPLE_NEARBY = 9;
    public static final int GCM_TYPE_UPDATE_COUNTERS_BALANCE = 10;
    public static final int GCM_TYPE_FAN_UPDATE_PROFILE = 11;
    public static final int GCM_TYPE_FAN_ADD_PHOTO = 12;
    public static final int GCM_TYPE_FAN_ONLINE = 13;

    /**
     * параметры геометрической прогрессии для расчета задержки между запросами на регистрацию в GCM
     * все значения в сек.
     * <p/>
     * при начальном значении = 2, знаменателе = 2 и последнем значении прогрессии = 128, суммарное
     * время, в течении которого будем пытаться зарегистрироваться в GCM, составит 254 сек
     */
    public static final int TIME_GEOMETRIC_PROGRESSION_DENOMINATOR = 2;
    public static final int TIME_GEOMETRIC_PROGRESSION_FIRST_VALUE = 2;
    public static final int TIME_GEOMETRIC_PROGRESSION_LAST_VALUE = 128;


    public static final String NEXT_INTENT = "com.topface.topface_next";

    public static final String GCM_DIALOGS_UPDATE = "com.topface.topface.action.GCM_DIALOGS_UPDATE";
    public static final String GCM_MUTUAL_UPDATE = "com.topface.topface.action.GCM_MUTUAL_UPDATE";
    public static final String GCM_LIKE_UPDATE = "com.topface.topface.action.GCM_LIKE_UPDATE";
    public static final String GCM_GUESTS_UPDATE = "com.topface.topface.action.GCM_GUESTS_UPDATE";
    public static final String GCM_PEOPLE_NEARBY_UPDATE = "com.topface.topface.action.GCM_PEOPLE_NEARBY_UPDATE";

    public static final String USER_ID_EXTRA = "id";

    public static final int NOTIFICATION_CANCEL_DELAY = 2000;

    public static int lastNotificationType = GCM_TYPE_UNKNOWN;

    private static boolean showMessage = false;
    private static boolean showLikes = false;
    private static boolean showSympathy = false;
    private static boolean showVisitors = false;
    public static final String NOTIFICATION_INTENT = "GCM";
    /**
     * Extras key for gcm type.
     */
    public static final String GCM_TYPE = "GCM_TYPE";
    /**
     * Extras key for additon gcm message label.
     */
    public static final String GCM_LABEL = "GCM_LABEL";

    public GCMUtils(Context context) {
        mContext = context;
    }

    public boolean registerGCM(String serverToken) {
        if (App.isGmsEnabled()) {
            mGcmObject = GoogleCloudMessaging.getInstance(mContext);
            mRegId = getRegistrationId();
            if (mRegId.isEmpty()) {
                register(serverToken, 0);
                return true;
            } else if (!mRegId.equals(serverToken)) {
                sendRegistrationIdToBackend();
            }
        }
        return false;
    }

    private void saveGcmToken(String regId, String serverToken) {
        if (!TextUtils.isEmpty(regId)) {
            mRegId = regId;
            storeRegistrationId();
            if (!regId.equals(serverToken)) {
                sendRegistrationIdToBackend();
            }
        } else {
            Debug.log("Registration id is empty");
        }
    }

    private void register(final String serverToken, final int time) {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                String regId = null;
                try {
                    if (mGcmObject == null) {
                        mGcmObject = GoogleCloudMessaging.getInstance(mContext);
                    }
                    regId = mGcmObject.register(SENDER_ID);
                } catch (IOException ex) {
                    Debug.error(ex);
                    if (isDelayTimeCorrect(time)) {
                        register(serverToken, getTimerDelay(time));
                    } else {
                        Debug.log("Unable to register in GCM, all attempts have been exhausted");
                    }
                } catch (SecurityException e) {
                    Debug.log("Can't register in GCM. No com.google.android.c2dm.permission.RECEIVE permission.");
                }
                if (regId != null) {
                    saveGcmToken(regId, serverToken);
                }
            }
        }, time * 1000);
    }

    private int getTimerDelay(int time) {
        return time == 0 ? TIME_GEOMETRIC_PROGRESSION_FIRST_VALUE : time * TIME_GEOMETRIC_PROGRESSION_DENOMINATOR;
    }

    private boolean isDelayTimeCorrect(int time) {
        return getTimerDelay(time) <= TIME_GEOMETRIC_PROGRESSION_LAST_VALUE;
    }

    @SuppressWarnings("unused")
    public void unregister() {
        new BackgroundThread() {
            @Override
            public void execute() {
                try {
                    if (mGcmObject == null) {
                        mGcmObject = GoogleCloudMessaging.getInstance(mContext);
                    }
                    mGcmObject.unregister();
                } catch (IOException e) {
                    Debug.error("Can't unregister gcm", e);
                }
            }
        };

    }

    private void sendRegistrationIdToBackend() {
        Debug.log("GCM: Try send regId to server: ", mRegId);
        new RegistrationTokenRequest(mRegId, mContext).exec();
    }

    private void storeRegistrationId() {
        AppConfig config = App.getAppConfig();
        config.setGcmRegId(mRegId);
        config.saveLastAppVersion();
        config.saveConfig();
    }

    private String getRegistrationId() {
        String registrationId = App.getAppConfig().getGcmRegId();
        if (registrationId.isEmpty()) {
            Debug.log("No reg id");
            return "";
        }
        int registeredVersion = App.getAppConfig().getLastAppVersion();
        if (registeredVersion != BuildConfig.VERSION_CODE) {
            Debug.log("App version changed.");
            return "";
        }
        return registrationId;
    }


    public static boolean showNotificationIfNeed(final Intent extra, Context context) {
        //Проверяем, не отключены ли уведомления
        if (!App.getUserConfig().isNotificationEnabled()) {
            Debug.log("GCM: notification is disabled");
            return false;
        } else if (extra == null) {
            Debug.log("GCM: intent is null");
            return false;
        } else if (getType(extra) == GCM_TYPE_UPDATE_COUNTERS_BALANCE) {
            setCounters(extra, context);
            return false;
        }
        try {
            return showNotification(extra, context);
        } catch (Exception e) {
            Debug.error("GCM: Notifcation error", e);
        }

        return false;
    }

    private static boolean showNotification(final Intent extra, Context context) {
        if (!CacheProfile.isLoaded()) {
            Debug.log("GCM: wait for profile load to show notification");
            BroadcastReceiver profileLoadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    showNotification(extra, context);
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                }
            };
            LocalBroadcastManager.getInstance(context).registerReceiver(profileLoadReceiver,
                    new IntentFilter(CacheProfile.ACTION_PROFILE_LOAD));
            return false;
        }
        String uid = Integer.toString(CacheProfile.uid);
        String targetUserId = extra.getStringExtra("receiver");
        targetUserId = targetUserId != null ? targetUserId : uid;

        //Проверяем id адресата GCM, что бы не показывать уведомления, предназначенные
        //другому пользователю. Такое может произойти, если не было нормального разлогина,
        //например если удалить приложения будучи залогиненым
        if (!TextUtils.equals(targetUserId, uid)) {
            Debug.error("GCM: target id # " + targetUserId + " dont equal current user id " + CacheProfile.uid);
            return false;
        }

        final String data = extra.getStringExtra("text");
        if (data != null) {
            loadNotificationSettings();
            setCounters(extra, context);
            int type = getType(extra);
            final User user = getUser(extra);
            String title = getTitle(context, extra.getStringExtra("title"));
            Intent intent = getIntentByType(context, type, user);

            if (intent != null) {
                intent.putExtra(GCMUtils.NOTIFICATION_INTENT, true);
                intent.putExtra(GCM_TYPE, type);
                intent.putExtra(GCM_LABEL, getLabel(extra));
                showNotificationByType(extra, data, type, user, title, intent);
                return true;
            }
        }
        return false;
    }

    private static void showNotificationByType(Intent extra, String data, int type, User user, String title, Intent intent) {
        final UserNotificationManager notificationManager = UserNotificationManager.getInstance();
        if (!Ssid.isLoaded()) {
            if (type == GCM_TYPE_UPDATE || type == GCM_TYPE_PROMO) {
                notificationManager.showNotification(
                        type,
                        title,
                        data,
                        true, null,
                        getUnread(extra),
                        intent,
                        false,
                        null);
            }
        } else if (user != null && !TextUtils.isEmpty(user.photoUrl)) {
            notificationManager.showNotificationAsync(
                    type,
                    title,
                    data,
                    user,
                    true,
                    user.photoUrl,
                    getUnread(extra),
                    intent,
                    false
            );
        } else {
            notificationManager.showNotification(
                    type,
                    title,
                    data,
                    true, null,
                    getUnread(extra),
                    intent,
                    false,
                    user);
        }
    }

    public static int getType(Intent extra) {
        if (extra == null) {
            return GCM_TYPE_UNKNOWN;
        }
        String typeString = extra.getStringExtra("type");
        try {
            return typeString != null ? Integer.parseInt(typeString) : GCM_TYPE_UNKNOWN;
        } catch (NumberFormatException exc) {
            Debug.error(exc);
            return GCM_TYPE_UNKNOWN;
        }
    }

    private static void setCounters(Intent extra, Context context) {
        CountersManager counterManager = CountersManager.getInstance(context);
        counterManager.setLastRequestMethod(CountersManager.CHANGED_BY_GCM);
        try {
            String countersStr = extra.getStringExtra("counters");
            if (countersStr != null) {
                JSONObject countersJson = new JSONObject(countersStr);
                // on Api version 8 unread counter will have the same keys as common requests
                counterManager.setEntitiesCounters(countersJson);
            }
            String balanceStr = extra.getStringExtra("balance");
            if (balanceStr != null) {
                JSONObject balanceJson = new JSONObject(balanceStr);
                counterManager.setBalanceCounters(balanceJson);
            }
        } catch (JSONException e) {
            Debug.error(e);
        }
    }

    private static User getUser(Intent extra) {
        final User user = new User();
        String userJson = extra.getStringExtra("user");
        if (userJson != null) {
            user.json2User(userJson);
        }
        return user;
    }

    private static void loadNotificationSettings() {
        if (CacheProfile.notifications != null) {
            if (CacheProfile.notifications.size() > 0) {
                showMessage = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).apns;
                showLikes = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).apns;
                showSympathy = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).apns;
                showVisitors = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).apns;
            }
        }
    }

    private static String getTitle(Context context, String title) {
        if (TextUtils.isEmpty(title)) {
            title = context.getString(R.string.app_name);
        }
        return title;
    }

    private static int getUnread(Intent extra) {
        String unreadExtra = extra.getStringExtra("unread");
        int unread = 0;
        try {
            unread = unreadExtra != null ? Integer.parseInt(unreadExtra) : 0;
        } catch (NumberFormatException e) {
            Debug.error("GCM: Wrong unread format: " + unreadExtra, e);
        }
        return unread;
    }

    private static int getUsersCountInMessageStack(User user) {
        UserConfig config = App.getUserConfig();
        MessageStack messagesStack = config.getNotificationMessagesStack();
        Set<Integer> uniqueIds = new HashSet<>();
        for (SerializableToJson item : messagesStack) {
            MessageStack.Message message = (MessageStack.Message) item;
            if (message.mUserId != MessageStack.EMPTY_USER_ID) {
                uniqueIds.add(message.mUserId);
            }
        }
        // add last received gcm user id
        uniqueIds.add(user.id);
        return uniqueIds.size();
    }

    private static Intent openChat(Context context, User user, int type) {
        if (showMessage) {
            Intent i;
            if (user.id != 0) {
                lastNotificationType = type;
                if (getUsersCountInMessageStack(user) > 1) {
                    // create intent to open Dialogs
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(GCMUtils.NEXT_INTENT, BaseFragment.FragmentId.TABBED_DIALOGS);
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, DialogsFragment.class.getName());
                    // add the same request code like Chat intent
                    i.putExtra(App.INTENT_REQUEST_KEY, ChatActivity.REQUEST_CHAT);
                } else {
                    return ChatActivity.createIntent(user.id, user.getNameAndAge(), user.city, null, null, true, GCMUtils.class.getSimpleName());
                }
                return i;
            }
        }
        return null;
    }

    private static Intent getIntentByType(Context context, int type, User user) {
        Intent i = null;
        switch (type) {
            case GCM_TYPE_MESSAGE:
            case GCM_TYPE_GIFT:
                i = openChat(context, user, GCM_TYPE_MESSAGE);
                break;
            case GCM_TYPE_MUTUAL:
                if (showSympathy) {
                    lastNotificationType = GCM_TYPE_MUTUAL;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(NEXT_INTENT, TABBED_LIKES);
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, MutualFragment.class.getName());
                }
                break;

            case GCM_TYPE_LIKE:
                if (showLikes) {
                    lastNotificationType = GCM_TYPE_LIKE;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(NEXT_INTENT, TABBED_LIKES);
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, LikesFragment.class.getName());
                }
                break;

            case GCM_TYPE_GUESTS:
                if (showVisitors) {
                    lastNotificationType = GCM_TYPE_GUESTS;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, VisitorsFragment.class.getName());
                    i.putExtra(NEXT_INTENT, TABBED_VISITORS);
                }
                break;
            case GCM_TYPE_PEOPLE_NEARBY:
                lastNotificationType = GCM_TYPE_PEOPLE_NEARBY;
                i = new Intent(context, NavigationActivity.class);
                i.putExtra(NEXT_INTENT, GEO);
                break;
            case GCM_TYPE_UPDATE:
                i = Utils.getMarketIntent(context);
                //Есть шанс что ссылка на маркет не будет поддерживаться
                if (!Utils.isCallableIntent(i, context)) {
                    i = new Intent(context, NavigationActivity.class);
                }
                break;
            case GCM_TYPE_DIALOGS:
                lastNotificationType = GCM_TYPE_DIALOGS;
                i = new Intent(context, NavigationActivity.class);
                FeedScreensIntent.equipMessageAllIntent(i);
                break;
            case GCM_TYPE_FAN_UPDATE_PROFILE:
                lastNotificationType = GCM_TYPE_FAN_UPDATE_PROFILE;
                i = UserProfileActivity.createIntent(null, null, user.id, null, true, true, Utils.getNameAndAge(user.name, user.age), user.city);
                i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, UserFormFragment.class.getName());
                break;
            case GCM_TYPE_FAN_ADD_PHOTO:
                lastNotificationType = GCM_TYPE_FAN_ADD_PHOTO;
                i = UserProfileActivity.createIntent(null, null, user.id, null, true, true, Utils.getNameAndAge(user.name, user.age), user.city);
                i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, UserPhotoFragment.class.getName());
                break;
            case GCM_TYPE_FAN_ONLINE:
                i = openChat(context, user, GCM_TYPE_FAN_ONLINE);
                break;
            case GCM_TYPE_PROMO:
            default:
                i = new Intent(context, NavigationActivity.class);
        }
        return i;
    }

    public static void cancelNotification(final Context context, final int type) {
        if (context == null) {
            return;
        }
        //Отменяем уведомления с небольшой задержкой,
        //что бы на ICS успело доиграть уведомление (длинные не успеют. но не страшно. все стандартные - короткие)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (type == lastNotificationType) {
                    int id;
                    switch (type) {
                        case GCM_TYPE_MESSAGE:
                        case GCM_TYPE_DIALOGS:
                            id = UserNotificationManager.MESSAGES_ID;
                            break;
                        default:
                            id = UserNotificationManager.NOTIFICATION_ID;
                    }
                    UserNotificationManager.getInstance().cancelNotification(id);
                }
            }
        }, NOTIFICATION_CANCEL_DELAY);

    }

    public static String getLabel(Intent intent) {
        return intent.getStringExtra("label");
    }


    public static class User {
        public int id;
        public String name;
        public String photoUrl;
        public int sex;
        public int age;
        public String city;

        public User() {
            id = 0;
            age = 0;
        }

        public void json2User(String json) {
            try {
                JSONObject obj = new JSONObject(json);
                id = obj.optInt("id");
                name = obj.optString("name");
                sex = obj.optInt("sex", Profile.BOY);
                JSONObject photo = obj.optJSONObject("photo");
                if (photo != null && photo.has(Photo.SIZE_128)) {
                    photoUrl = obj.optJSONObject("photo").optString(Photo.SIZE_128);
                }
                age = obj.optInt("age");
                city = obj.optString("city");
            } catch (Exception e) {
                Debug.error(e);
            }

        }

        public String getNameAndAge() {
            String result;
            if (name != null && name.length() > 0 && age > 0) {
                result = name + ", " + age;
            } else {
                result = name;
            }
            return result;
        }
    }
}

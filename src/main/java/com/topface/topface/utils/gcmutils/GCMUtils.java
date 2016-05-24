package com.topface.topface.utils.gcmutils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SerializableToJson;
import com.topface.topface.data.experiments.FeedScreensIntent;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RegistrationTokenRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.UserProfileActivity;
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

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static com.topface.topface.data.leftMenu.FragmentIdData.*;

public class GCMUtils {
    public static final String GCM_NOTIFICATION = "com.topface.topface.action.NOTIFICATION";

    private Context mContext;

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

    public void registerGcmToken(String token) {
        String oldToken = getGcmToken();
        if (!token.equals(oldToken)) {
            sendTokenToBackend(token);
        }
    }

    private void sendTokenToBackend(final String token) {
        Debug.log("GCM: Try send token to server: ", token);
        new RegistrationTokenRequest(token, mContext).callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                Debug.log("GCM: OK send token ");
                storeToken(token);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.log("GCM: fail send token to server: ");
            }
        }).exec();
    }

    private void storeToken(String token) {
        AppConfig config = App.getAppConfig();
        config.setGcmRegId(token);
        config.saveLastAppVersion();
        config.saveConfig();
    }

    private String getGcmToken() {
        String token = App.getAppConfig().getGcmRegId();
        if (token.isEmpty()) {
            Debug.log("No reg id");
            return "";
        }
        int registeredVersion = App.getAppConfig().getLastAppVersion();
        if (registeredVersion != BuildConfig.VERSION_CODE) {
            Debug.log("App version changed.");
            return "";
        }
        return token;
    }

    private static void getEmailConfirmationState(Bundle data) {
        if (data != null) {
            Handler mHandler = new Handler(App.get().getMainLooper());
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    Utils.checkEmailConfirmation(null, false);
                }
            });
        }
    }

    public static boolean showNotificationIfNeed(final Bundle data, Context context, String updateUrl) {
        getEmailConfirmationState(data);
        //Проверяем, не отключены ли уведомления
        if (!App.getUserConfig().isNotificationEnabled()) {
            Debug.log("GCM: notification is disabled");
            return false;
        } else if (data == null) {
            Debug.log("GCM: intent is null");
            return false;
        } else if (getType(data) == GCM_TYPE_UPDATE_COUNTERS_BALANCE) {
            setCounters(data, context);
            return false;
        }
        try {
            return showNotification(data, context, updateUrl);
        } catch (Exception e) {
            Debug.error("GCM: Notifcation error", e);
        }

        return false;
    }

    private static boolean showNotification(final Bundle data, Context context, final String updateUrl) {
        if (!CacheProfile.isLoaded()) {
            Debug.log("GCM: wait for profile load to show notification");
            BroadcastReceiver profileLoadReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    showNotification(data, context, updateUrl);
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                }
            };
            LocalBroadcastManager.getInstance(context).registerReceiver(profileLoadReceiver,
                    new IntentFilter(CacheProfile.ACTION_PROFILE_LOAD));
            return false;
        }
        Profile profile = App.from(context).getProfile();
        String uid = Integer.toString(profile.uid);
        String targetUserId = data.getString("receiver");
        targetUserId = targetUserId != null ? targetUserId : uid;

        //Проверяем id адресата GCM, что бы не показывать уведомления, предназначенные
        //другому пользователю. Такое может произойти, если не было нормального разлогина,
        //например если удалить приложения будучи залогиненым
        if (!TextUtils.equals(targetUserId, uid)) {
            Debug.error("GCM: target id # " + targetUserId + " dont equal current user id " + profile.uid);
            return false;
        }

        final String text = data.getString("text");
        if (text != null) {
            loadNotificationSettings(context);
            setCounters(data, context);
            int type = getType(data);
            final User user = getUser(data);
            String title = getTitle(context, data.getString("title"));
            Intent intent = getIntentByType(context, type, user, updateUrl);

            if (intent != null) {
                intent.putExtra(GCMUtils.NOTIFICATION_INTENT, true);
                intent.putExtra(GCM_TYPE, type);
                intent.putExtra(GCM_LABEL, getLabel(data));
                showNotificationByType(data, text, type, user, title, intent);
                return true;
            }
        }
        return false;
    }

    private static void showNotificationByType(Bundle data, String text, int type, User user, String title, Intent intent) {
        final UserNotificationManager notificationManager = UserNotificationManager.getInstance();
        if (!Ssid.isLoaded()) {
            if (type == GCM_TYPE_UPDATE || type == GCM_TYPE_PROMO) {
                notificationManager.showNotification(
                        type,
                        title,
                        text,
                        true, null,
                        getUnread(data),
                        intent,
                        false,
                        null);
            }
        } else if (user != null && !TextUtils.isEmpty(user.photoUrl)) {
            notificationManager.showNotificationAsync(
                    type,
                    title,
                    text,
                    user,
                    true,
                    user.photoUrl,
                    getUnread(data),
                    intent,
                    false
            );
        } else {
            notificationManager.showNotification(
                    type,
                    title,
                    text,
                    true, null,
                    getUnread(data),
                    intent,
                    false,
                    user);
        }
    }

    public static int getType(Bundle bundle) {
        if (bundle == null) {
            return GCM_TYPE_UNKNOWN;
        }
        String typeString = bundle.getString("type");
        try {
            return typeString != null ? Integer.parseInt(typeString) : GCM_TYPE_UNKNOWN;
        } catch (NumberFormatException exc) {
            Debug.error(exc);
            return GCM_TYPE_UNKNOWN;
        }
    }

    private static void setCounters(Bundle data, Context context) {
        CountersManager counterManager = CountersManager.getInstance(context);
        counterManager.setLastRequestMethod(CountersManager.CHANGED_BY_GCM);
        try {
            String countersStr = data.getString("counters");
            if (countersStr != null) {
                JSONObject countersJson = new JSONObject(countersStr);
                // on Api version 8 unread counter will have the same keys as common requests
                counterManager.setEntitiesCounters(countersJson);
            }
            String balanceStr = data.getString("balance");
            if (balanceStr != null) {
                JSONObject balanceJson = new JSONObject(balanceStr);
                counterManager.setBalanceCounters(balanceJson);
            }
        } catch (JSONException e) {
            Debug.error(e);
        }
    }

    private static User getUser(Bundle data) {
        final User user = new User();
        String userJson = data.getString("user");
        if (userJson != null) {
            user.json2User(userJson);
        }
        return user;
    }

    private static void loadNotificationSettings(Context context) {
        Profile profile = App.from(context).getProfile();
        if (profile.notifications != null) {
            if (profile.notifications.size() > 0) {
                showMessage = profile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).apns;
                showLikes = profile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).apns;
                showSympathy = profile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).apns;
                showVisitors = profile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).apns;
            }
        }
    }

    private static String getTitle(Context context, String title) {
        if (TextUtils.isEmpty(title)) {
            title = context.getString(R.string.app_name);
        }
        return title;
    }

    private static int getUnread(Bundle data) {
        String unreadExtra = data.getString("unread");
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
                    i.putExtra(GCMUtils.NEXT_INTENT, new LeftMenuSettingsData(TABBED_DIALOGS));
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, DialogsFragment.class.getName());
                    // add the same request code like Chat intent
                    i.putExtra(App.INTENT_REQUEST_KEY, ChatActivity.REQUEST_CHAT);
                } else {
                    return ChatActivity.createIntent(user.id, user.sex,user.getNameAndAge(), user.city,
                            null, null, true, null, false);
                }
                return i;
            }
        }
        return null;
    }

    private static Intent getIntentByType(Context context, int type, User user, String ё) {
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
                    i.putExtra(NEXT_INTENT, new LeftMenuSettingsData(TABBED_LIKES));
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, MutualFragment.class.getName());
                }
                break;

            case GCM_TYPE_LIKE:
                if (showLikes) {
                    lastNotificationType = GCM_TYPE_LIKE;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(NEXT_INTENT, new LeftMenuSettingsData(TABBED_LIKES));
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, LikesFragment.class.getName());
                }
                break;

            case GCM_TYPE_GUESTS:
                if (showVisitors) {
                    lastNotificationType = GCM_TYPE_GUESTS;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(TabbedFeedFragment.EXTRA_OPEN_PAGE, VisitorsFragment.class.getName());
                    i.putExtra(NEXT_INTENT, new LeftMenuSettingsData(TABBED_VISITORS));
                }
                break;
            case GCM_TYPE_PEOPLE_NEARBY:
                lastNotificationType = GCM_TYPE_PEOPLE_NEARBY;
                i = new Intent(context, NavigationActivity.class);
                i.putExtra(NEXT_INTENT, new LeftMenuSettingsData(GEO));
                break;
            case GCM_TYPE_UPDATE:
                i = Utils.getMarketIntent();
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

    public static String getLabel(Bundle bundle) {
        return bundle.getString("label");
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
            return name != null && name.length() > 0 && age > 0 ? name + ", " + age : name;
        }
    }
}

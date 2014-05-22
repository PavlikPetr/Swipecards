package com.topface.topface;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.text.TextUtils;

import com.google.android.gcm.GCMRegistrar;
import com.topface.framework.utils.BackgroundThread;
import com.topface.framework.utils.Debug;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RegistrationTokenRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.notifications.UserNotificationManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.F_DIALOGS;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.F_GEO;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.F_LIKES;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.F_MUTUAL;
import static com.topface.topface.ui.fragments.BaseFragment.FragmentId.F_VISITORS;

public class GCMUtils {
    public static final String GCM_NOTIFICATION = "com.topface.topface.action.NOTIFICATION";

    /**
     * Типы уведомлений с сервера. У разных типов - разные действия
     */

    public static final int GCM_TYPE_UNKNOWN = -1;
    public static final int GCM_TYPE_MESSAGE = 0;
    public static final int GCM_TYPE_MUTUAL = 1;
    public static final int GCM_TYPE_LIKE = 2;
    public static final int GCM_TYPE_GUESTS = 4;
    public static final int GCM_TYPE_UPDATE = 5;
    public static final int GCM_TYPE_PROMO = 6;
    public static final int GCM_TYPE_GIFT = 7;
    public static final int GCM_TYPE_DIALOGS = 8;
    public static final int GCM_TYPE_GEO = 9;


    public static final String NEXT_INTENT = "com.topface.topface_next";

    public static final int NOTIFICATION_CANCEL_DELAY = 2000;

    public static int lastNotificationType = GCM_TYPE_UNKNOWN;

    public static int lastUserId = -1;

    private static boolean showMessage = false;
    private static boolean showLikes = false;
    private static boolean showSympathy = false;
    private static boolean showVisitors = false;
    public static final String NOTIFICATION_INTENT = "GCM";
    public static boolean GCM_SUPPORTED = true;

    public static void init(final Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            try {
                GCMRegistrar.checkDevice(context);
                GCMRegistrar.checkManifest(context);
                new BackgroundThread() {
                    @Override
                    public void execute() {
                        if (GCMRegistrar.isRegistered(context)) {
                            final String regId = GCMRegistrar.getRegistrationId(context);
                            Debug.log("GCM: Already registered, regID is " + regId);

                            //Если на сервере не зарегистрированы, отправляем запрос
                            if (!GCMRegistrar.isRegisteredOnServer(context)) {
                                Looper.prepare();
                                sendRegId(context, regId);
                                Looper.loop();
                            }

                        } else {
                            GCMRegistrar.register(context, GCMIntentService.SENDER_ID);
                            Debug.log("GCM: Registered: " + GCMRegistrar.getRegistrationId(context));
                        }
                    }
                };

            } catch (Exception ex) {
                GCM_SUPPORTED = false;
                Debug.error("GCM: GCM not supported", ex);
            }
        }
    }

    /**
     * Метод для тестирования GCM сообщений
     *
     * @param context контекст приложения
     */
    public static boolean showNotificationIfNeed(final Intent extra, Context context) {
        //Проверяем, не отключены ли уведомления
        if (!Settings.getInstance().isNotificationEnabled()) {
            Debug.log("GCM: notification is disabled");
            return false;
        } else if (extra == null) {
            Debug.log("GCM: intent is null");
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
                if (!TextUtils.equals(intent.getComponent().getClassName(), ContainerActivity.class.getName())) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                showNotificationByType(extra, context, data, type, user, title, intent);
                return true;
            }
        }
        return false;
    }

    private static void showNotificationByType(Intent extra, Context context, String data, int type, User user, String title, Intent intent) {
        final UserNotificationManager notificationManager = UserNotificationManager.getInstance(context);
        if (!Ssid.isLoaded()) {
            if (type == GCM_TYPE_UPDATE || type == GCM_TYPE_PROMO) {
                notificationManager.showNotification(
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
                    title,
                    data,
                    true, null,
                    getUnread(extra),
                    intent,
                    false,
                    null);
        }
    }

    private static int getType(Intent extra) {
        String typeString = extra.getStringExtra("type");
        return typeString != null ? Integer.parseInt(typeString) : GCM_TYPE_UNKNOWN;
    }

    private static void setCounters(Intent extra, Context context) {
        CountersManager counterManager = CountersManager.getInstance(context)
                .setMethod(CountersManager.CHANGED_BY_GCM);
        try {
            String countersStr = extra.getStringExtra("counters");
            if (countersStr != null) {
                JSONObject countersJson = new JSONObject(countersStr);
                // on Api version 8 unread counter will have the same keys as common requests
                counterManager.setEntitiesCounters(
                        countersJson.optInt("unread_likes"),
                        countersJson.optInt("unread_sympaties"),
                        countersJson.optInt("unread_messages"),
                        countersJson.optInt("unread_visitors"),
                        countersJson.optInt("unread_fans"),
                        countersJson.optInt("unread_admirations"),
                        countersJson.optInt("unread_people_nearby")
                );
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
        if (title == null || title.equals("")) {
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

    private static Intent getIntentByType(Context context, int type, User user) {
        Intent i = null;
        switch (type) {
            case GCM_TYPE_MESSAGE:
            case GCM_TYPE_GIFT:
                if (showMessage) {
                    if (user.id != 0) {
                        lastNotificationType = GCM_TYPE_MESSAGE;
                        i = new Intent(context, ContainerActivity.class);
                        i.putExtra(ChatFragment.INTENT_USER_ID, user.id);
                        i.putExtra(ChatFragment.INTENT_USER_NAME, user.name);
                        i.putExtra(ChatFragment.INTENT_USER_SEX, user.sex);
                        i.putExtra(ChatFragment.INTENT_USER_AGE, user.age);
                        i.putExtra(ChatFragment.INTENT_USER_CITY, user.city);
                        i.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, GCM_NOTIFICATION);
                        i.putExtra(Static.INTENT_REQUEST_KEY, ContainerActivity.INTENT_CHAT_FRAGMENT);
                    } else {
                        i = new Intent(context, NavigationActivity.class);
                    }
                }
                break;


            case GCM_TYPE_MUTUAL:
                if (showSympathy) {
                    lastNotificationType = GCM_TYPE_MUTUAL;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(NEXT_INTENT, F_MUTUAL);
                }
                break;

            case GCM_TYPE_LIKE:
                if (showLikes) {
                    lastNotificationType = GCM_TYPE_LIKE;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(NEXT_INTENT, F_LIKES);
                }
                break;

            case GCM_TYPE_GUESTS:
                if (showVisitors) {
                    lastNotificationType = GCM_TYPE_GUESTS;
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(NEXT_INTENT, F_VISITORS);
                }
                break;
            case GCM_TYPE_GEO:
                lastNotificationType = GCM_TYPE_GEO;
                i = new Intent(context, NavigationActivity.class);
                i.putExtra(NEXT_INTENT, F_GEO);
                break;
            case GCM_TYPE_UPDATE:
                i = Utils.getMarketIntent(context);
                break;
            case GCM_TYPE_DIALOGS:
                i = new Intent(context, NavigationActivity.class);
                i.putExtra(NEXT_INTENT, F_DIALOGS);
                break;
            case GCM_TYPE_PROMO:
            default:
                i = new Intent(context, NavigationActivity.class);

        }
        return i;
    }

    public static void cancelNotification(final Context context, final int type) {
        //Отменяем уведомления с небольшой задержкой,
        //что бы на ICS успело доиграть уведомление (длинные не успеют. но не страшно. все стандартные - короткие)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (type == lastNotificationType) {
                    if (context != null) {
                        int id = type == GCM_TYPE_MESSAGE ? UserNotificationManager.MESSAGES_ID : UserNotificationManager.NOTIFICATION_ID;
                        UserNotificationManager.getInstance(context).cancelNotification(id);
                    }
                }
            }
        }, NOTIFICATION_CANCEL_DELAY);

    }

    public static void sendRegId(final Context context, final String registrationId) {
        Debug.log("GCM: Try send regId to server: ", registrationId);

        //Ебаный стыд. Но ничего, мы это поправим, когда сделаем поддержку коллбэков без handler
        new RegistrationTokenRequest(registrationId, context).callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                new BackgroundThread() {
                    @Override
                    public void execute() {
                        GCMRegistrar.setRegisteredOnServer(context, true);
                    }
                };
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                Debug.error(String.format("GCM: RegistrationRequest fail: #%d %s", codeError, response));
                new BackgroundThread() {
                    @Override
                    public void execute() {
                        GCMRegistrar.setRegisteredOnServer(context, false);
                    }
                };
            }
        }).exec();
    }


    public static class User {
        public int id;
        public String name;
        public String photoUrl;
        public int sex;
        public int age;
        @SuppressWarnings("unused")
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
                sex = obj.optInt("sex", Static.BOY);
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

    }
}
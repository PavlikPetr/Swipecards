package com.topface.topface;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.ListView;
import com.google.android.gcm.GCMRegistrar;
import com.topface.topface.data.Options;
import com.topface.topface.data.Photo;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.RegistrationTokenRequest;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.TopfaceNotificationManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;


public class GCMUtils {
    public static final String GCM_REGISTERED = "gcmRegistered";
    public static final String GCM_NOTIFICATION = "com.topface.topface.action.NOTIFICATION";

    public static final int GCM_TYPE_UNKNOWN = -1;
    public static final int GCM_TYPE_MESSAGE = 0;
    public static final int GCM_TYPE_SYMPATHY = 1;
    public static final int GCM_TYPE_LIKE = 2;
    public static final int GCM_TYPE_GUESTS = 3;
    public static final int GCM_NO_NOTIFICATION = -2;
    public static final String GCM_UPDATE_COUNTERS = "com.topface.topface.action.UPDATE_COUNTERS";

    public static final String NEXT_INTENT = "next";

    public static final int NOTIFICATION_CANCEL_DELAY = 2000;

    public static int  lastNotificationType = GCM_NO_NOTIFICATION;

    public static int lastUserId = -1;

    public static void init(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            try {
                GCMRegistrar.checkDevice(context);
                GCMRegistrar.checkManifest(context);
                final String regId = GCMRegistrar.getRegistrationId(context);
                if (regId.equals("")) {
                    GCMRegistrar.register(context, GCMIntentService.SENDER_ID);
                    Debug.log("Registered: " + regId);
                } else {
                    sendRegId(context, regId);
                    Debug.log("Already registered, regID is " + regId);
                }
            } catch (Exception ex) {
                Debug.error(ex);
            }    
        }
    }

    /**
     * Метод для тестирования GCM сообщений
     *
     * @param context контекст приложения
     */
    public static void generateFakeNotification(Context context) {
        Intent intent = new Intent();
        intent.putExtra("text", "asd");
        intent.putExtra("title", "da");
        intent.putExtra("type", "0");
        intent.putExtra("unread", "1");
        intent.putExtra("counters", "788");
        try {
            intent.putExtra("user", new JSONObject().put("id", "43945394").put("photo", new JSONObject().put("c128x128", "http://imgs.topface.com/u43945394/c128x128/nnf6g6.jpg")).put("name", "Ilya").put("age", "21").toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        showNotification(intent, context);
    }

    public static void setRegisteredFlag(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(GCM_REGISTERED, true);
        editor.commit();
    }

    public static void showNotification(final Intent extra, Context context) {
        final String data = extra.getStringExtra("text");
        if (data != null) {
            Intent i = null;

            String typeString = extra.getStringExtra("type");
            int type = typeString != null ? Integer.parseInt(typeString) : GCM_TYPE_UNKNOWN;

            final User user = new User();
            user.json2User(extra.getStringExtra("user"));
            String title = extra.getStringExtra("title");
            if (title == null || title.equals("")) {
                title = context.getString(R.string.app_name);
            }

            Options options = CacheProfile.getOptions();

            String countersString = extra.getStringExtra("counters");
            if (countersString != null)
                setCounters(countersString, context);

            final TopfaceNotificationManager mNotificationManager = TopfaceNotificationManager.getInstance(context);

            switch (type) {
                case GCM_TYPE_MESSAGE:
                    if (options.notifications.get(Options.NOTIFICATIONS_MESSAGE).apns) {
                        if (user.id != 0) {
                            lastNotificationType = GCM_TYPE_MESSAGE;
                            i = new Intent(context, ChatActivity.class);

                            i.putExtra(ChatActivity.INTENT_USER_ID, user.id);
                            i.putExtra(ChatActivity.INTENT_USER_NAME, user.name);
                            i.putExtra(ChatActivity.INTENT_USER_AVATAR, user.photoUrl);
                            i.putExtra(ChatActivity.INTENT_USER_AGE, user.age);
                            i.putExtra(ChatActivity.INTENT_USER_CITY, user.city);
                        } else {
                            i = new Intent(context, NavigationActivity.class);
                        }
                    }
                    break;


                case GCM_TYPE_SYMPATHY:
                    if (options.notifications.get(Options.NOTIFICATIONS_SYMPATHY).apns) {
                        lastNotificationType = GCM_TYPE_SYMPATHY;
                        i = new Intent(context, NavigationActivity.class);
                        i.putExtra(NEXT_INTENT, BaseFragment.F_MUTUAL);
                    }
                    break;

                case GCM_TYPE_LIKE:
                    if (options.notifications.get(Options.NOTIFICATIONS_LIKES).apns) {
                        lastNotificationType = GCM_TYPE_LIKE;
                        i = new Intent(context, NavigationActivity.class);
                        i.putExtra(NEXT_INTENT, BaseFragment.F_LIKES);
                    }
                    break;

                case GCM_TYPE_GUESTS:
                    if (options.notifications.get(Options.NOTIFICATIONS_VISITOR).apns) {
                        lastNotificationType = GCM_TYPE_GUESTS;
                        i = new Intent(context, NavigationActivity.class);
                        i.putExtra(NEXT_INTENT, BaseFragment.F_VISITORS);
                    }
                    break;
                default:
                    i = new Intent(context, AuthActivity.class);

            }

            if (i != null) {
                i.putExtra("C2DM", true);
                final TempImageViewRemote fakeImageView = new TempImageViewRemote(context);
                fakeImageView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));
                final Intent newI = i;
                newI.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                final String finalTitle = title;
                fakeImageView.setRemoteSrc(user.photoUrl, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if(user.id != lastUserId) {
                            mNotificationManager.showNotification(user.id, finalTitle, data, fakeImageView.getImageBitmap(), Integer.parseInt(extra.getStringExtra("unread")), newI);
                        }
                    }
                });
            }
        }
    }

    private static void setCounters(String counters, Context context) {
        try {
            JSONObject countersJson = new JSONObject(counters);
            CacheProfile.unread_likes = countersJson.optInt("unread_likes");
            CacheProfile.unread_messages = countersJson.optInt("unread_messages");
            CacheProfile.unread_mutual = countersJson.optInt("unread_sympaties");
            CacheProfile.unread_visitors = countersJson.optInt("unread_visitors");
            sendBroadcastUpdateCounters(context);
        } catch (JSONException e) {
            Debug.error(e);
        }

    }

    private static void sendBroadcastUpdateCounters(Context context) {
        Intent intent = new Intent();
        intent.setAction(GCM_UPDATE_COUNTERS);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void cancelNotification(final Context context, final int type) {
        //Отменяем уведомления с небольшой задержкой,
        //что бы на ICS успело доиграть уведомление (длинные не успеют. но не страшно. все стандартные - короткие)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if(type == lastNotificationType) {
                    NotificationManager notificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(TopfaceNotificationManager.id);
                }
            }
        }, NOTIFICATION_CANCEL_DELAY);

    }

    public static void sendRegId(final Context context, final String registrationId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                Debug.log("GCM onRegistered", registrationId);

                RegistrationTokenRequest registrationRequest = new RegistrationTokenRequest(context);
                registrationRequest.token = registrationId;
                registrationRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        GCMUtils.setRegisteredFlag(context);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        Debug.error(String.format("RegistrationRequest fail: #%d %s", codeError, response));
                    }
                }).exec();
                Looper.loop();
            }
        }).start();
    }

    private static class TempImageViewRemote extends ImageViewRemote {
        private Bitmap mImageBitmap;

        public TempImageViewRemote(Context context) {
            super(context);
        }

        @Override
        public void setImageBitmap(Bitmap bm) {
            super.setImageBitmap(bm);
            mImageBitmap = bm;
        }

        public Bitmap getImageBitmap() {
            return mImageBitmap;
        }
    }

    private static class User {
        public int id;
        public String name;
        public String photoUrl;
        public int age;
        @SuppressWarnings("unused")
        public String city;

        public User() {
        }

        public void json2User(String json) {
            try {
                JSONObject obj = new JSONObject(json);
                id = obj.optInt("id");
                name = obj.optString("name");
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
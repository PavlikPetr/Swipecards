package com.topface.topface;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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
import com.topface.topface.utils.CountersManager;
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
    public static final int GCM_TYPE_DIALOGS = 4;

    public static final int GCM_TYPE_UPDATE = 5;
    public static final int GCM_TYPE_NOTIFICATION = 6;
    public static final int GCM_TYPE_INTENT = 7;

    public static final String NEXT_INTENT = "next";

    public static final int NOTIFICATION_CANCEL_DELAY = 2000;

    public static int  lastNotificationType = GCM_TYPE_DIALOGS;

    public static int lastUserId = -1;

    private static boolean showMessage = true;
    private static boolean showLikes = true;
    private static boolean showSympathy = true;
    private static boolean showVisitors = true;

    public static void init(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            try {
                GCMRegistrar.checkDevice(context);
                GCMRegistrar.checkManifest(context);

                if (GCMRegistrar.isRegistered(context)) {
                    final String regId = GCMRegistrar.getRegistrationId(context);
                    Debug.log("Already registered, regID is " + regId);

                    //Если на сервере не зарегистрированы, отправляем запрос
                    if (!GCMRegistrar.isRegisteredOnServer(context)) {
                        sendRegId(context, regId);
                    }

                } else {
                    GCMRegistrar.register(context, GCMIntentService.SENDER_ID);
                    Debug.log("Registered: " + GCMRegistrar.getRegistrationId(context));
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

    public static void showNotification(final Intent extra, Context context) {
        final String data = extra.getStringExtra("text");
        if (data != null) {
            Intent i = null;

            String typeString = extra.getStringExtra("type");
            int type = typeString != null ? Integer.parseInt(typeString) : GCM_TYPE_UNKNOWN;

            final User user = new User();
            String userJSON = extra.getStringExtra("user");
            if (userJSON != null) {
                user.json2User(extra.getStringExtra("user"));
            }
            String title = extra.getStringExtra("title");
            if (title == null || title.equals("")) {
                title = context.getString(R.string.app_name);
            }

            Options options = CacheProfile.getOptions();
            if(options.notifications != null) {
                if(!options.notifications.isEmpty()) {
                    showMessage = options.notifications.get(Options.NOTIFICATIONS_MESSAGE).apns;
                    showLikes =  options.notifications.get(Options.NOTIFICATIONS_LIKES).apns;
                    showSympathy = options.notifications.get(Options.NOTIFICATIONS_SYMPATHY).apns;
                    showVisitors = options.notifications.get(Options.NOTIFICATIONS_VISITOR).apns;
                }
            }
            String countersString = extra.getStringExtra("counters");
            if (countersString != null)
                setCounters(countersString, context);

            final TopfaceNotificationManager mNotificationManager = TopfaceNotificationManager.getInstance(context);

            switch (type) {
                case GCM_TYPE_MESSAGE:
                    if (showMessage) {
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
                    if (showSympathy) {
                        lastNotificationType = GCM_TYPE_SYMPATHY;
                        i = new Intent(context, NavigationActivity.class);
                        i.putExtra(NEXT_INTENT, BaseFragment.F_MUTUAL);
                    }
                    break;

                case GCM_TYPE_LIKE:
                    if (showLikes) {
                        lastNotificationType = GCM_TYPE_LIKE;
                        i = new Intent(context, NavigationActivity.class);
                        i.putExtra(NEXT_INTENT, BaseFragment.F_LIKES);
                    }
                    break;

                case GCM_TYPE_GUESTS:
                    if (showVisitors)
                    break;
                case GCM_TYPE_UPDATE:
                    i = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.topface.topface"));
                    break;

                case GCM_TYPE_NOTIFICATION:
                    i = new Intent(context, NavigationActivity.class);
                    break;

                case GCM_TYPE_INTENT:

                    break;

                default:
                    i = new Intent(context, AuthActivity.class);

            }

            if (i != null) {
                i.putExtra("C2DM", true);
                final TempImageViewRemote fakeImageView = new TempImageViewRemote(context);
                fakeImageView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));
                final Intent newI = i;
//                newI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            CountersManager.getInstance(context).setMethod(CountersManager.CHANGED_BY_GCM);
            CountersManager.getInstance(context).setAllCounters(countersJson.optInt("unread_likes"),
                    countersJson.optInt("unread_sympaties"),
                    countersJson.optInt("unread_messages"),
                    countersJson.optInt("unread_visitors"));
        } catch (JSONException e) {
            Debug.error(e);
        }

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

                Debug.log("Try send GCM regId to server: ", registrationId);

                RegistrationTokenRequest registrationRequest = new RegistrationTokenRequest(context);
                registrationRequest.token = registrationId;
                registrationRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        GCMRegistrar.setRegisteredOnServer(context, true);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        Debug.error(String.format("RegistrationRequest fail: #%d %s", codeError, response));
                        GCMRegistrar.setRegisteredOnServer(context, false);
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
            id = 0;
            age = 0;
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
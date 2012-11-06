package com.topface.topface;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import com.google.android.gcm.GCMRegistrar;
import com.topface.topface.ui.AuthActivity;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.TopfaceNotificationManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created with IntelliJ IDEA.
 * User: Ilya Vorobiev
 * Date: 31.10.12
 * Time: 16:37
 * To change this template use File | Settings | File Templates.
 */
public class GCMUtils {
    public static final String GCM_REGISTERED = "gcmRegistered";
    public static final String GCM_NOTIFICATION = "com.topface.topface.action.NOTIFICATION";

    public static final int GCM_TYPE_UNKNOWN = -1;
    public static final int GCM_TYPE_MESSAGE = 0;
    public static final int GCM_TYPE_SYMPATHY = 1;
    public static final int GCM_TYPE_LIKE = 2;
    public static final int GCM_TYPE_GUESTS = 3;

    public static final String NEXT_INTENT = "next";

    public static final int NOTIFICATION_CANCEL_DELAY = 2000;

    public static void init(Context context) {
        GCMRegistrar.checkDevice(context);
        GCMRegistrar.checkManifest(context);
        final String regId = GCMRegistrar.getRegistrationId(context);
        if (regId.equals("")) {
            GCMRegistrar.register(context, GCMIntentService.SENDER_ID);
            Debug.log("Registered: "+regId);
        } else {
            Debug.log("Already registered, regID is "+regId);
        }
    }

    public static void generateFakeNotification(Context context) {
        Intent intent = new Intent();
        intent.putExtra("text","asd");
        intent.putExtra("title","da");
        intent.putExtra("type","0");
        intent.putExtra("unread","1");
        intent.putExtra("counters","788");
        try{
            intent.putExtra("user",new JSONObject().put("id","43945394").put("photo", new JSONObject().put("c128x128", "http://imgs.topface.com/u43945394/c128x128/nnf6g6.jpg")).put("name","Ilya").put("age","21").toString());

        } catch(Exception e) {
            e.printStackTrace();
        }
        showNotification(intent,context);
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

            User user = new User();
            user.json2User(extra.getStringExtra("user"));
            String title = extra.getStringExtra("title");
            if(title==null || title.equals("")) {
                title = context.getString(R.string.default_notification_title);
            }

            String countersString = extra.getStringExtra("counters");
            if(countersString != null)
                setCounters(countersString);

            final TopfaceNotificationManager mNotificationManager = TopfaceNotificationManager.getInstance(context);


            switch (type) {
                case GCM_TYPE_MESSAGE:
                    if(user.id !=0){
                        i = new Intent(context, ChatActivity.class);

                        i.putExtra(
                                ChatActivity.INTENT_USER_ID,
                                user.id
                        );
                        i.putExtra(ChatActivity.INTENT_USER_NAME, user.name);
                        i.putExtra(ChatActivity.INTENT_USER_AVATAR,user.photoUrl );
                        i.putExtra(ChatActivity.INTENT_USER_AGE,user.age);
                    } else {
                        i = new Intent(context,NavigationActivity.class);
                    }
                    break;


                case GCM_TYPE_SYMPATHY:
                    if (Settings.getInstance().getSetting(Settings.SETTINGS_C2DM_MUTUAL_PHONE)) {
                        i = new Intent(context, NavigationActivity.class);
                        i.putExtra(NEXT_INTENT, BaseFragment.F_MUTUAL);
                    }
                    break;

                case GCM_TYPE_LIKE:
                    if (Settings.getInstance().getSetting(Settings.SETTINGS_C2DM_LIKES_PHONE)) {
                        i = new Intent(context, NavigationActivity.class);
                        i.putExtra(NEXT_INTENT, BaseFragment.F_LIKES);
                    }
                    break;

                case GCM_TYPE_GUESTS:
                    i = new Intent(context, NavigationActivity.class);
                    i.putExtra(NEXT_INTENT, BaseFragment.F_VISITORS);
                    break;
                default:
                    i = new Intent(context, AuthActivity.class);

            }

            if (i != null) {
                i.putExtra("C2DM", true);
                final TempImageViewRemote fakeImageView = new TempImageViewRemote(context);
                fakeImageView.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT, ListView.LayoutParams.MATCH_PARENT));
                final Intent newI = i;//new Intent(context,ChatActivity.class);
                final String finalTitle = title;
                fakeImageView.setRemoteSrc(user.photoUrl, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        mNotificationManager.showNotification(finalTitle, data, fakeImageView.getImageBitmap(), Integer.parseInt(extra.getStringExtra("unread")), newI);
                    }
                });
            }
        }
    }

    private static void setCounters(String counters) {
        try {
            JSONObject countersJson = new JSONObject(counters);
            CacheProfile.unread_likes = countersJson.optInt("unread_likes");
            CacheProfile.unread_messages = countersJson.optInt("unread_messages");
            CacheProfile.unread_mutual = countersJson.optInt("unread_sympaties");
            CacheProfile.unread_visitors = countersJson.optInt("unread_visitors");
        } catch (JSONException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void cancelNotification(final Context context) {
        //Отменяем уведомления с небольшой задержкой,
        //что бы на ICS успело доиграть уведомление (длинные не успеют. но не страшно. все стандартные - короткие)
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(TopfaceNotificationManager.id);
            }
        }, NOTIFICATION_CANCEL_DELAY);

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
        public String city;

        public User(){}

        public void json2User(String json) {
            try{
                JSONObject obj = new JSONObject(json);
                id = obj.optInt("id");
                name = obj.optString("name");
                photoUrl = obj.optJSONObject("photo").optString("c128x128");
                age = obj.optInt("age");
                city = obj.optString("city");
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }
}
package com.topface.topface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import com.topface.topface.utils.Debug;

public class Recycle {
    // Data
    public static Bitmap s_People;
    public static Bitmap s_Online;
    public static Bitmap s_Offline;
    public static Bitmap s_ChatFrame;
    public static Bitmap s_InboxFrame;
    public static Bitmap s_Heart;
    public static Bitmap s_Money;
    public static Bitmap s_ProfilePhotoFrame;
    public static Bitmap s_ProfileGalleryFrame;
    public static Bitmap s_ProfileEroInfo;
    public static Bitmap s_DatingInformer;
    public static Bitmap s_RateHighPressed;
    public static Bitmap s_RateHigh;
    public static Bitmap s_RateTopPressed;
    public static Bitmap s_RateTop;
    public static Bitmap s_RateLowPressed;
    public static Bitmap s_RateLow;
    public static Bitmap s_RateAveragePressed;
    public static Bitmap s_RateAverage;

    // Animations
    public static AnimationDrawable s_Loader;
    public static Drawable s_Loader_0;

    public static boolean init(Context context) {
        try {
            s_People = BitmapFactory.decodeResource(context.getResources(), R.drawable.im_people);
            s_Online = BitmapFactory.decodeResource(context.getResources(), R.drawable.im_online);
            s_Offline = BitmapFactory.decodeResource(context.getResources(), R.drawable.im_offline);
            s_ChatFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.chat_avatar_frame);
            s_InboxFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.im_avatar_list_frame);
            s_Heart = BitmapFactory.decodeResource(context.getResources(), R.drawable.tops_heart);
            s_Money = BitmapFactory.decodeResource(context.getResources(), R.drawable.dating_money);
            s_ProfilePhotoFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_frame_photo);
            s_ProfileGalleryFrame = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_frame_gallery);
            s_ProfileEroInfo = BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_ero_info);

            s_Loader = (AnimationDrawable) context.getResources().getDrawable(R.drawable.loader);
            s_Loader_0 = context.getResources().getDrawable(R.drawable.loader0);

            //      s_DatingInformer = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_informer);
            //      s_RateHighPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_high_pressed);
            //      s_RateHigh = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_high);
            //      s_RateTopPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_top_pressed);
            //      s_RateTop = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_top);
            //      s_RateLowPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_low_pressed);
            //      s_RateLow = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_low);
            //      s_RateAveragePressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_avarage_pressed);
            //      s_RateAverage = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_avarage);
        } catch (Exception e) {
            Debug.log("Recycle", "init exception:" + e);
            return false;
        }
        return true;
    }

    public static void release() {
        if (s_People == null)
            return;
        /*s_People.recycle();
         * s_People=null;
         * s_Online.recycle();
         * s_Online=null;
         * s_Offline.recycle();
         * s_Offline=null;
         * s_ChatFrame.recycle();
         * s_ChatFrame=null;
         * s_InboxFrame.recycle();
         * s_InboxFrame=null;
         * s_Heart.recycle();
         * s_Heart=null;
         * s_Money.recycle();
         * s_Money=null;
         * s_ProfilePhotoFrame.recycle();
         * s_ProfilePhotoFrame=null;
         * s_ProfileGalleryFrame.recycle();
         * s_ProfileGalleryFrame=null;
         * s_ProfileEroInfo.recycle();
         * s_ProfileEroInfo=null;
         * s_StarPopupBG.recycle();
         * s_StarPopupBG=null;
         * s_StarBluePressed.recycle();
         * s_StarBluePressed=null;
         * s_StarBlue.recycle();
         * s_StarBlue=null;
         * s_StarGreyPressed.recycle();
         * s_StarGreyPressed=null;
         * s_StarGrey.recycle();
         * s_StarGrey=null;
         * s_StarYellowPressed.recycle();
         * s_StarYellowPressed=null;
         * s_StarYellow.recycle();
         * s_StarYellow=null;
         * s_Star10Pressed.recycle();
         * s_Star10Pressed=null;
         * s_Star10.recycle();
         * s_Star10=null; */
    }
}

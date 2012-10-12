package com.topface.topface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
  public static Bitmap s_DatingInformer;
  public static Bitmap s_RateHighPressed;
  public static Bitmap s_RateHigh;
  public static Bitmap s_RateTopPressed;
  public static Bitmap s_RateTop;
  public static Bitmap s_RateLowPressed;
  public static Bitmap s_RateLow;
  public static Bitmap s_RateAveragePressed;
  public static Bitmap s_RateAverage;
  //---------------------------------------------------------------------------
  public static boolean init(Context context) {
    try {
      s_People = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_people);
      s_Online = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_online);
      s_Offline = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_offline);
      s_ChatFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.chat_frame_photo);
      s_InboxFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.inbox_frame_photo);
      s_Heart = BitmapFactory.decodeResource(context.getResources(),R.drawable.tops_heart);
      s_Money = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_money);
      s_ProfilePhotoFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.profile_frame_photo);
      s_ProfileGalleryFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.profile_frame_gallery);
      s_DatingInformer = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_informer);
      s_RateHighPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_high_pressed);
      s_RateHigh = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_high);
      s_RateTopPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_top_pressed);
      s_RateTop = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_top);
      s_RateLowPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_low_pressed);
      s_RateLow = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_low);
      s_RateAveragePressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_avarage_pressed);
      s_RateAverage = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_rate_avarage);
    } catch (Exception e) {
      Debug.log("Recycle","init exception:" + e);
      return false;
    }
    return true;
  }
}

package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.utils.Debug;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

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
  public static Bitmap s_StarPopupBG;
  public static Bitmap s_StarBluePressed;
  public static Bitmap s_StarBlue;
  public static Bitmap s_StarGreyPressed;
  public static Bitmap s_StarGrey;
  public static Bitmap s_StarYellowPressed;
  public static Bitmap s_StarYellow;
  public static Bitmap s_Star10Pressed;
  public static Bitmap s_Star10;
  //---------------------------------------------------------------------------
  public static boolean init(Context context) {
    try {
      s_People = BitmapFactory.decodeResource(context.getResources(),R.drawable.icon_people);
      s_Online = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_online);
      s_Offline = BitmapFactory.decodeResource(context.getResources(),R.drawable.im_offline);
      s_ChatFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.chat_frame_photo);
      s_InboxFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.inbox_frame_photo);
      s_Heart = BitmapFactory.decodeResource(context.getResources(),R.drawable.tops_heart);
      s_Money = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_money);
      s_ProfilePhotoFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.profile_frame_photo);
      s_ProfileGalleryFrame = BitmapFactory.decodeResource(context.getResources(),R.drawable.profile_frame_gallery);
      s_ProfileEroInfo = BitmapFactory.decodeResource(context.getResources(),R.drawable.profile_ero_info);
      s_StarPopupBG = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_popup);
      s_StarBluePressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_blue_pressed);
      s_StarBlue = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_blue);
      s_StarGreyPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_grey_pressed);
      s_StarGrey = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_grey);
      s_StarYellowPressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_yellow_pressed);
      s_StarYellow = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_yellow);
      s_Star10Pressed = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_10_pressed);
      s_Star10 = BitmapFactory.decodeResource(context.getResources(),R.drawable.dating_star_10);
    } catch (Exception e) {
      Debug.log("Recycle","init exception:" + e);
      return false;
    }
    return true;
  }
  //---------------------------------------------------------------------------
  public static void release() {
    if(s_People==null)
      return;
    
    s_People.recycle();
    s_People=null;
    s_Online.recycle();
    s_Online=null;
    s_Offline.recycle();
    s_Offline=null;
    s_ChatFrame.recycle();
    s_ChatFrame=null;
    s_InboxFrame.recycle();
    s_InboxFrame=null;
    s_Heart.recycle();
    s_Heart=null;
    s_Money.recycle();
    s_Money=null;
    s_ProfilePhotoFrame.recycle();
    s_ProfilePhotoFrame=null;
    s_ProfileGalleryFrame.recycle();
    s_ProfileGalleryFrame=null;
    s_ProfileEroInfo.recycle();
    s_ProfileEroInfo=null;
    s_StarPopupBG.recycle();
    s_StarPopupBG=null;
    s_StarBluePressed.recycle();
    s_StarBluePressed=null;
    s_StarBlue.recycle();
    s_StarBlue=null;
    s_StarGreyPressed.recycle();
    s_StarGreyPressed=null;
    s_StarGrey.recycle();
    s_StarGrey=null;
    s_StarYellowPressed.recycle();
    s_StarYellowPressed=null;
    s_StarYellow.recycle();
    s_StarYellow=null;
    s_Star10Pressed.recycle();
    s_Star10Pressed=null;
    s_Star10.recycle();
    s_Star10=null;
  }
  //---------------------------------------------------------------------------
}

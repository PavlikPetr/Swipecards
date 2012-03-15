package com.sonetica.topface.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import com.sonetica.topface.Data;

public class Imager {
  //---------------------------------------------------------------------------
  public static void avatarOwnerPreloading(Context context) {
    if(Data.s_OwnerDrw!=null)
      return;
    Bitmap ava = Http.bitmapLoader(Data.s_Profile.avatar_small);
    ava = Utils.getRoundedCornerBitmap(ava,ava.getWidth(),ava.getHeight(),12);
    Data.s_OwnerDrw = new BitmapDrawable(context.getResources(),ava);
  }
  //---------------------------------------------------------------------------
  public static void avatarUserPreloading(Context context,String url) {
    Bitmap ava = Http.bitmapLoader(url);
    ava = Utils.getRoundedCornerBitmap(ava,ava.getWidth(),ava.getHeight(),12);
    Data.s_UserDrw = new BitmapDrawable(context.getResources(),ava);
  }
}

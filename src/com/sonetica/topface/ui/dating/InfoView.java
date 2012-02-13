package com.sonetica.topface.ui.dating;

import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.widget.ImageView;

class InfoView extends ImageView {
  // Data
  public  int age;
  public  String name;
  public  String city;
  public  String status;
  public  boolean online;
  // Bitmaps
  private static Bitmap mOnlineBmp;
  private static Bitmap mOfflineBmp;
  private static Bitmap mShadowTopBmp;
  private static Bitmap mShadowBottomBmp;
  // Constants
  private static final Paint paint      = new Paint();
  private static final Paint paintName   = new Paint();
  private static final Paint paintCity    = new Paint();
  private static final Paint paintStatus   = new Paint();
  //----------------------------------
  public InfoView(Context context) {
    super(context);
    
    setBackgroundColor(Color.TRANSPARENT);
    
    paint.setColor(Color.RED);
    paint.setDither(true);
    
    // city
    paintCity.setColor(Color.WHITE);
    paintCity.setTextSize(getResources().getDimension(R.dimen.city_font_size));
    paintCity.setAntiAlias(true);
    
    // status
    paintStatus.setColor(Color.WHITE);
    paintStatus.setTextSize(getResources().getDimension(R.dimen.status_font_size));
    paintStatus.setAntiAlias(true);
    paintStatus.setSubpixelText(true);
    
    // name age online
    paintName.setColor(Color.WHITE);
    paintName.setTextSize(getResources().getDimension(R.dimen.name_font_size));
    paintName.setTypeface(Typeface.DEFAULT_BOLD);
    paintName.setAntiAlias(true);

    mOnlineBmp       = BitmapFactory.decodeResource(getResources(),R.drawable.im_online);
    mOfflineBmp      = BitmapFactory.decodeResource(getResources(),R.drawable.im_offline);
    mShadowTopBmp    = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_top);
    mShadowBottomBmp = BitmapFactory.decodeResource(getResources(),R.drawable.dating_shadow_bottom);
  }

}

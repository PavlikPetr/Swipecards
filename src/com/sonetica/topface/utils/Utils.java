package com.sonetica.topface.utils;

import java.security.MessageDigest;
import java.util.Calendar;
import java.util.HashMap;
import com.sonetica.topface.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.widget.TextView;

/*
 *  Набор вспомагательных функций
 */
public class Utils {
  //---------------------------------------------------------------------------
  public static int unixtime(){
    return (int)(System.currentTimeMillis() / 1000L);
  }
  //---------------------------------------------------------------------------
  public static String md5(String value) { 
    if(value==null) 
      return null; 
    try { 
      StringBuffer hexString = new StringBuffer();
      MessageDigest digester = MessageDigest.getInstance("MD5"); 
      digester.update(value.getBytes()); 
      byte[] bytes = digester.digest();
      for(int i=0; i<bytes.length; i++)
        hexString.append(Integer.toHexString(0xFF & bytes[i]));
      return hexString.toString();
    } catch (Exception e) { 
      return null; 
    } 
  } 
  //---------------------------------------------------------------------------
  public static HashMap<String, String> parseQueryString(String query) {
    String[] params = query.split("&");
    HashMap<String, String> map = new HashMap<String, String>();
    for(String param : params) {
      String name  = param.split("=")[0];
      String value = param.split("=")[1];
      map.put(name, value);
    }
    return map;
  }
  //---------------------------------------------------------------------------
  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int width, int height, int roundPx) {
    Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    Canvas canvas = new Canvas(output);

    final Rect rect = new Rect(0, 0, width, height);
    final RectF rectF = new RectF(rect);
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    canvas.drawARGB(0, 0, 0, 0);
    paint.setColor(0xff424242);
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    canvas.drawBitmap(bitmap, rect, rect, paint);

    return output;
  }
  //---------------------------------------------------------------------------
  public static Bitmap clipping(Bitmap rawBitmap,int bitmapWidth,int bitmapHeight) {
    if(rawBitmap==null || bitmapWidth<=0 || bitmapHeight<=0)
      return null;
    
    // Исходный размер загруженного изображения
    int width  = rawBitmap.getWidth();
    int height = rawBitmap.getHeight();
    
    // буль, длиная фото или высокая
    boolean LEG = false;

    if(width >= height) 
      LEG = true;
    
    // коффициент сжатия фотографии
    float ratio = Math.max(((float)bitmapWidth)/width,((float) bitmapHeight)/height);
    
    // на получение оригинального размера по ширине или высоте
    if(ratio==0) ratio=1;
    
    // матрица сжатия
    Matrix matrix = new Matrix();
    matrix.postScale(ratio,ratio);
    
    // сжатие изображения
    Bitmap scaledBitmap = Bitmap.createBitmap(rawBitmap,0,0,width,height,matrix,true);
    
    // вырезаем необходимый размер
    Bitmap clippedBitmap;
    if(LEG) {
      // у горизонтальной, вырезаем по центру
      int offset_x = (scaledBitmap.getWidth()-bitmapWidth)/2;
      clippedBitmap = Bitmap.createBitmap(scaledBitmap,offset_x,0,bitmapWidth,bitmapHeight,null,false);
    } else
      // у вертикальной режим с верху
      clippedBitmap = Bitmap.createBitmap(scaledBitmap,0,0,bitmapWidth,bitmapHeight,null,false);
      
    return clippedBitmap;
  }
  //---------------------------------------------------------------------------
  public static void formatTime(TextView tv,long time) {
    Context context = tv.getContext();
    long now = System.currentTimeMillis();
    /*
    String text = null;
    long t = now - time;
    if((time > now) || t < 60)
      text = context.getString(R.string.time_now);
    else if(t < 36000)
      text = formatMinute(context,t/600);
    else if(t < 6*36000)
      text = formatHour(context,t/3600);
    else if(DateUtils.isToday(time))
      text = DateFormat.format("kk:mm",time).toString();
    else { 
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.HOUR_OF_DAY,0);
      cal.set(Calendar.MINUTE,0);
      if(time > (now-(now-cal.getTimeInMillis())-(24*60*60*1000)))
        text = DateFormat.format(context.getString(R.string.time_yesterday)+" kk:mm",time).toString();
      else
        text = DateFormat.format("dd.MM.yyyy kk:mm",time).toString();
    }
    */
    tv.setText("time");
  }
  //---------------------------------------------------------------------------
  private static String formatHour(Context context,long hours) {
    byte caseValue = 0;
    if((hours < 11) || (hours > 19)) {
      if(hours%10 == 1)
        caseValue = 1;
      if((hours%10 == 2) || (hours%10 == 3) || (hours%10 == 4))
        caseValue = 2;
    }
    /*
    switch(caseValue) {
     case 1:  return String.format(context.getString(R.string.time_hour_0),hours);
     case 2:  return String.format(context.getString(R.string.time_hour_1),hours);
     default: return String.format(context.getString(R.string.time_hours),hours);
    }
    */
    return null;
  }
  //---------------------------------------------------------------------------
  private static String formatMinute(Context context,long minutes) {
    byte caseValue = 0;
    if((minutes < 11) || (minutes > 19)) {
      if(minutes%10 == 1)
        caseValue = 1;
      if((minutes%10 == 2) || (minutes%10 == 3) || (minutes%10 == 4))
        caseValue = 2;
    }
    /*
    switch(caseValue) {
     case 1:  return String.format(context.getString(R.string.time_minute_0),minutes);
     case 2:  return String.format(context.getString(R.string.time_minute_1),minutes);
     default: return String.format(context.getString(R.string.time_minutes),minutes);
    }
    */
    return null;
  }
  //---------------------------------------------------------------------------
}

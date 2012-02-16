package com.sonetica.topface.utils;

import java.security.MessageDigest;
import java.util.HashMap;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;

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
    final Bitmap clippedBitmap;
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
}

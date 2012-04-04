package com.sonetica.topface.utils;

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
import android.graphics.drawable.BitmapDrawable;
import com.sonetica.topface.Data;

public class Imager {
  //---------------------------------------------------------------------------
  public static void avatarOwnerPreloading(Context context) {
    if(Data.s_OwnerDrw!=null)
      return;
    Bitmap ava = Http.bitmapLoader(Data.s_Profile.avatar_small);
    ava = getRoundedCornerBitmap(ava,ava.getWidth(),ava.getHeight(),12);
    Data.s_OwnerDrw = new BitmapDrawable(context.getResources(),ava);
  }
  //---------------------------------------------------------------------------
  public static void avatarUserPreloading(Context context,String url) {
    Bitmap ava = Http.bitmapLoader(url);
    ava = getRoundedCornerBitmap(ava,ava.getWidth(),ava.getHeight(),12);
    Data.s_UserDrw = new BitmapDrawable(context.getResources(),ava);
  }
  //---------------------------------------------------------------------------
  public static Bitmap clipping(Bitmap rawBitmap,int bitmapWidth,int bitmapHeight) {
    if(rawBitmap==null || bitmapWidth<=0 || bitmapHeight<=0)
      return null;
    
    // Исходный размер загруженного изображения
    int width  = rawBitmap.getWidth();
    int height = rawBitmap.getHeight();
    
    // буль, длинная фото или высокая
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
    
    //rawBitmap.recycle();
    //rawBitmap = null;
      
    //scaledBitmap.recycle();
    //scaledBitmap = null;
    
    return clippedBitmap;
  }
  //---------------------------------------------------------------------------
  public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int width, int height, int roundPx) {
    if(width < height)
      height = width;
    else
      width = height;
    
    Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    
    Bitmap clippedBitmap = clipping(bitmap,width,height);

    Canvas canvas = new Canvas(output);

    final Rect  rect  = new Rect(0, 0, width, height);
    final RectF rectF = new RectF(rect);
    final Paint paint = new Paint();
    
    paint.setAntiAlias(true);
    paint.setColor(0xff424242);
    canvas.drawARGB(0, 0, 0, 0);
    canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

    paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
    canvas.drawBitmap(clippedBitmap, rect, rect, paint);
    
    //bitmap.recycle();
    bitmap = null;

    return output;
  }
  //---------------------------------------------------------------------------
}

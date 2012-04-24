package com.topface.topface.social;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import com.topface.topface.Global;
import com.topface.topface.utils.Http;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;

/*
 *  Класс для запросов к Facebook
 */
public class FbApi extends SnApi {
  // Data
  private static final String mApiUrl = "https://graph.facebook.com/";
  //---------------------------------------------------------------------------
  public FbApi(Context context,AuthToken.Token token) {
    super(context,token);
  }
  //---------------------------------------------------------------------------
  @Override
  public void getProfile() {
    
  }
  //---------------------------------------------------------------------------
  @Override
  public String[] uploadPhoto(Uri uri) {
    Context context = getContext();
    AuthToken.Token token = getAuthToken();
    String[] result = new String[3];
    try {
      StringBuilder request = new StringBuilder("https://graph.facebook.com/me/photos?access_token=");
      request.append("&access_token=" + token.getTokenKey());
    

      // отправка
      // получаем размеры изображения 
      InputStream is = context.getContentResolver().openInputStream(uri);
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inJustDecodeBounds = true;
      BitmapFactory.decodeStream(is,null,options); // чтение размеров
      is.close();
      int width = options.outWidth, height = options.outHeight;
      int scale = 1;
      if(width>Global.PHOTO_WIDTH && height>Global.PHOTO_HEIGHT) // определили степень уменьшения
        while (true) {
          if((width/2)<Global.PHOTO_WIDTH && (height/2)<Global.PHOTO_HEIGHT)
            break;
          width  /= 2;
          height /= 2;
          scale  *= 2;
        }
      options =  new BitmapFactory.Options();
      options.inSampleSize  = scale;
      options.inTempStorage = new byte[64 * 1024];
      options.inPurgeable   = true;
      // подгрузка изображения
      is = context.getContentResolver().openInputStream(uri);
      Bitmap bitmap = BitmapFactory.decodeStream(is,null,options);
      // если горизонтальная - переварачиваем
      int or = getOrientation(context,uri);
      if(or>0) {
        Matrix matrix = new Matrix();
        matrix.postRotate(or);
        bitmap = Bitmap.createBitmap(bitmap,0,0,width,height,matrix,true);
      }

      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bitmap.compress(CompressFormat.JPEG,100,bos);
      byte[] data = bos.toByteArray();
      
      // загрузка фото
      //String response = Http.httpPostDataRequest(request.toString(),null,mContext.getContentResolver().openInputStream(uri));
      String response = Http.httpPostDataRequest(request.toString(),null,data);
    
      JSONObject jsonResult = new JSONObject(response);
      long id = jsonResult.getLong("id");
      
      request = new StringBuilder("https://graph.facebook.com/");
      request.append(id + "?");
      request.append("access_token=" + token.getTokenKey());
      
      response = Http.httpPostRequest(request.toString(),null);
      jsonResult = new JSONObject(response);
      JSONArray images = jsonResult.getJSONArray("images");
      
      if(images.length()<2)
        return null;
      
      result[0] = images.getJSONObject(1).getString("source");
      result[1] = images.getJSONObject(2).getString("source");
      result[2] = images.getJSONObject(3).getString("source");
      
    } catch(Exception e) {
      e.printStackTrace();
    }
    return result;
  }
  //---------------------------------------------------------------------------
  @Override
  protected String getApiUrl() {
    return mApiUrl;
  }
  //---------------------------------------------------------------------------
}//VkApi

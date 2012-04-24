package com.topface.topface.social;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import com.topface.topface.Global;
import com.topface.topface.R;
import com.topface.topface.utils.Http;

/*
 *  Класс для работы с Vkontakte
 */
public class VkApi extends SnApi {
  // Data
  // Constants
  private static final String mApiUrl = "https://api.vkontakte.ru/method/";
  //---------------------------------------------------------------------------
  public VkApi(Context context,AuthToken.Token token) {
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
      StringBuilder request = new StringBuilder("https://api.vk.com/method/photos.getAlbums?");
      request.append("uid=" + token.getUserId());
      request.append("&access_token=" + token.getTokenKey());
      
      int albumId=0;
      String albumName = context.getString(R.string.vk_album_name);      
      // запрос альбомов
      String response = Http.httpPostRequest(request.toString(),null);
      JSONObject jsonResult = null;
      jsonResult = new JSONObject(response);
      // получили список альбомов
      JSONArray albumsList = jsonResult.getJSONArray("response");
      if(albumsList!=null && albumsList.length()>0) {
        for(int i = 0; i<albumsList.length();i++) {
          JSONObject obj = albumsList.getJSONObject(i); 
          if(obj.getString("title").equals(albumName)) {
            albumId = obj.getInt("aid"); // нашли нужный
            break;
          }
        }
      }
      
      // создаем новый
      if(albumId == 0) {
        request = new StringBuilder("https://api.vk.com/method/photos.createAlbum?");
        request.append("title=" + URLEncoder.encode(albumName));
        request.append("&access_token=" + token.getTokenKey());
        response = Http.httpPostRequest(request.toString(),null);
        jsonResult = new JSONObject(response);
        JSONObject obj = jsonResult.getJSONObject("response");
        albumId = obj.getInt("aid");
      }
      
      // uploading
      request = new StringBuilder("https://api.vk.com/method/photos.getUploadServer?");
      request.append("aid=" + albumId);
      request.append("&access_token=" + token.getTokenKey());
      response = Http.httpPostRequest(request.toString(),null);
      jsonResult = new JSONObject(response);
      JSONObject obj = jsonResult.getJSONObject("response");
      String url =  obj.getString("upload_url");
     
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
        while(true) {
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
      //response = Http.httpPostDataRequest(url,null,mContext.getContentResolver().openInputStream(uri));
      response = Http.httpPostDataRequest(url,null,data);

      jsonResult = new JSONObject(response);
      
      String photosList = jsonResult.getString("photos_list");
      String hash       = jsonResult.getString("hash");
      String server     = jsonResult.getString("server");
      
      request = new StringBuilder("https://api.vk.com/method/photos.save?");
      request.append("aid=" + albumId);
      request.append("&server=" + server);
      request.append("&photos_list=" + photosList);
      request.append("&hash=" + hash);
      request.append("&access_token=" + token.getTokenKey());
      
      response = Http.httpPostRequest(request.toString(),null);
      jsonResult = new JSONObject(response);
      JSONArray links = jsonResult.getJSONArray("response");
      JSONObject link = links.getJSONObject(0);   // повесить проверки 

      result[0] = link.getString("src_big"); 
      result[1] = link.getString("src");
      result[2] = link.getString("src_small");
      
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


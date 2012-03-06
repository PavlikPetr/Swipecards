package com.sonetica.topface.social;

import java.io.FileNotFoundException;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.net.Http;
import android.content.Context;
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
    String[] result = new String[3];
    try {
      StringBuilder request = new StringBuilder("https://graph.facebook.com/me/photos?access_token=");
      request.append("&access_token=" + mToken.getTokenKey());
    
      /*
      // отправка по урлу данные
      //Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.icon_people);
      Bitmap bitmap = BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri));
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      bitmap.compress(CompressFormat.JPEG,75,bos);
      byte[] data = bos.toByteArray();
      */
      
      // загрузка фото
      String response = Http.httpPostDataRequest(request.toString(),null,mContext.getContentResolver().openInputStream(uri));
    
      JSONObject jsonResult=null;
    
      jsonResult = new JSONObject(response);
      long id = jsonResult.getLong("id");
      
      request = new StringBuilder("https://graph.facebook.com/");
      request.append(id + "?");
      request.append("access_token=" + mToken.getTokenKey());
      
      response = Http.httpPostRequest(request.toString(),null);
      
      /*
      result[0]
      result[1]
      result[2]
      */
      
    } catch(JSONException e) {
      e.printStackTrace();
    } catch(FileNotFoundException e) {
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

package com.topface.topface.social;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import com.topface.topface.utils.Http;

/*
 *  Интерфейс api для запросов к социальным сетям
 */
public abstract class SnApi {
  // Data
  private Context mContext;
  private AuthToken.Token mToken;
  //---------------------------------------------------------------------------
  public SnApi(Context context,AuthToken.Token token) {
    mContext = context;
    mToken = token;
  }
  //---------------------------------------------------------------------------
  protected Context getContext() {
    return mContext;
  }
  //---------------------------------------------------------------------------
  protected AuthToken.Token getAuthToken() {
    return mToken;
  } 
  //---------------------------------------------------------------------------
  abstract public void getProfile();
  //---------------------------------------------------------------------------
  abstract public String[] uploadPhoto(Uri uri);
  //---------------------------------------------------------------------------
  abstract protected String getApiUrl();
  //---------------------------------------------------------------------------
  protected JSONObject sendRequest(SnRequest request) {
    request.setParam("access_token",mToken.getTokenKey());
    JSONObject jsonResult = null;
    try {
      String response = Http.httpGetRequest(getApiUrl()+request.toString());
      jsonResult = new JSONObject(response);
    } catch(JSONException e) {
      e.printStackTrace();
    } catch(Exception e) {
      e.printStackTrace();
    }
    return jsonResult;
  }
  //---------------------------------------------------------------------------
  public static int getOrientation(Context context, Uri photoUri) {
    Cursor cursor = context.getContentResolver().query(photoUri,
            new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);
    if(cursor.getCount() != 1)
      return -1;
    cursor.moveToFirst();
    
    return cursor.getInt(0);
  }
  /*
  //---------------------------------------------------------------------------
  // class SnRequestTask
  //---------------------------------------------------------------------------
  public class SnRequestTask extends AsyncTask<String, Void, snResponse> {
    // Data
    private SnRequest mRequest;
    private Handler   mHandler;
    //-----------------------------------------------------
    public SnRequestTask(SnRequest request, Handler handler) {
      mRequest = request;
      mHandler = handler;
    }
    //-----------------------------------------------------
    @Override
    protected snResponse doInBackground(String... params) {
      return sendRequest(mRequest);
    }
    //-----------------------------------------------------
    @Override
    protected void onPostExecute(SnResponse response) {
      Message message = new Message();
      if(response != null) {
        Utils.log("VkApi: answer " + response.toString());
        message.arg1 = RESPONSE_STATUS_OK;
        message.obj = response;
      } else {
        Utils.log("VkApi: error");
        message.arg1 = RESPONSE_STATUS_ERROR;
      }
      mHandler.sendMessage(message);
    }
  }//VkRequestTask
  */
  //---------------------------------------------------------------------------
}

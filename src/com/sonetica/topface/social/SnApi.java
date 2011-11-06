package com.sonetica.topface.social;

import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import com.sonetica.topface.net.Http;

/*
 *  Интерфейс api для запросов к социальным сетям
 */
public abstract class SnApi {
  // Data
  protected AuthToken.Token mToken;
  //---------------------------------------------------------------------------
  public SnApi(AuthToken.Token token) {
    mToken = token;
  }
  //---------------------------------------------------------------------------
  abstract public void getProfile();
  //---------------------------------------------------------------------------
  abstract public Bitmap getAvatar();
  //---------------------------------------------------------------------------
  abstract protected String getApiUrl();
  //---------------------------------------------------------------------------
  protected JSONObject sendRequest(SnRequest request) {
    JSONObject result = null;
    request.setParam("access_token",mToken.getTokenKey());
    try {
      result = new JSONObject(Http.httpGetRequest(getApiUrl()+request.toString()));
    } catch(JSONException e) {
      e.printStackTrace();
    }
    return result;
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

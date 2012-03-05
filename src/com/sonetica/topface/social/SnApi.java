package com.sonetica.topface.social;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.net.Uri;
import com.sonetica.topface.net.Http;

/*
 *  Интерфейс api для запросов к социальным сетям
 */
public abstract class SnApi {
  // Data
  protected Context mContext;
  protected AuthToken.Token mToken;
  //---------------------------------------------------------------------------
  public SnApi(Context context,AuthToken.Token token) {
    mContext = context;
    mToken = token;
  }
  //---------------------------------------------------------------------------
  abstract public void getProfile();
  //---------------------------------------------------------------------------
  abstract public void uploadPhoto(Uri uri);
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

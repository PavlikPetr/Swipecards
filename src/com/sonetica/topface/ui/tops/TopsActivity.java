package com.sonetica.topface.ui.tops;

import com.sonetica.topface.App;
import com.sonetica.topface.R;
import com.sonetica.topface.data.User;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.Requester;
import com.sonetica.topface.net.Top;
import com.sonetica.topface.social.SocialWebActivity;
import com.sonetica.topface.utils.GalleryCachedManager;
import com.sonetica.topface.utils.IFrame;
import com.sonetica.topface.utils.Utils;
import com.sonetica.topface.utils.GalleryCachedManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*  
 *  Класс активити для просмотра списка топ пользователей   "топы"
 */
public class TopsActivity extends Activity {
  // Data
  private GridView mGallary;
  private TopsGridAdapter mGridAdapter;
  private ProgressDialog mProgressDialog;
  private UsersLoaderTask mAsyncTask;
  private GalleryCachedManager mGalleryCachedManager;
  private String ssid;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Utils.log(this,"+onCreate");
    
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    mGallary = (GridView)findViewById(R.id.grdTopsGallary);
    mAsyncTask = new UsersLoaderTask();
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    mGallary = (GridView)findViewById(R.id.grdTopsGallary);
    mGallary.setAnimationCacheEnabled(false);
    mGallary.setScrollingCacheEnabled(false);
    mGallary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startActivity(new Intent(TopsActivity.this,RateitActivity.class));
      }
    });
    
    SharedPreferences preferences = getSharedPreferences(App.SHARED_PREFERENCES_TAG, Context.MODE_PRIVATE);
    ssid = preferences.getString(getString(R.string.ssid),"");
    
    Top top = new Top();
    top.sex  = 0;
    top.city = 2;
    top.ssid = ssid;
    Requester.sendTop(top,
      new Handler() {
        @Override
        public void handleMessage(Message msg) {
          super.handleMessage(msg);
          if(msg.arg1==Requester.OK && msg.obj!=null) {
            
            int i = 0;
            
          }
        }
      }
    );
    
    //update(null);
  }
  //---------------------------------------------------------------------------
  private void update(String params) {
    mAsyncTask.execute(params);
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    //mGalleryCachedManager.release();
    Utils.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private class UsersLoaderTask extends AsyncTask<String, Void, ArrayList<User>> {
    //---------------------------------
    @Override
    protected void onPreExecute(){
      mProgressDialog.show();
    }
    //---------------------------------
    @Override
    protected ArrayList<User> doInBackground(String... params) {
      ArrayList<User> userList = new ArrayList<User>();
      
      Top top = new Top();
      top.sex  = 0;
      top.city = 2;
      top.ssid = ssid;
      Requester.sendTop(top,
        new Handler() {
          @Override
          public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.arg1==Requester.OK && msg.obj!=null) {
              
              int i = 0;
              
            }
          }
        }
      );
      /*
      ArrayList<User> userList = new ArrayList<User>();
      
      JSONObject obj = null;
      JSONArray  arr = null;
      try {
        obj = new JSONObject(response);
        arr = new JSONArray(obj.getString("covers"));
        for(int i=0; i<arr.length(); ++i) {
          JSONObject o = (JSONObject)arr.get(i);
          User user = new User();
          user.link = o.getString("cover");
          user.name = o.getString("artist");
          userList.add(user);
        }
      } catch(JSONException e) {
        e.printStackTrace();
      }
      */
      
      mProgressDialog.cancel();
      return userList;
    }
    //---------------------------------
    @Override
    protected void onPostExecute(ArrayList<User> userList) {
      mGalleryCachedManager = new GalleryCachedManager(TopsActivity.this,IFrame.TOPS,userList);
      mGallary.setOnScrollListener(mGalleryCachedManager);
      mGridAdapter = new TopsGridAdapter(TopsActivity.this,mGalleryCachedManager);
      mGallary.setAdapter(mGridAdapter);
      mProgressDialog.cancel();
    }
  }// UsersLoaderTask
  //---------------------------------------------------------------------------  
}

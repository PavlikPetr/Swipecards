package com.sonetica.topface.ui.tops;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.R;
import com.sonetica.topface.data.User;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.GalleryManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

public class RateitActivity extends Activity {
  // Data
  private RateitGallery mGallery;
  private RateitGalleryAdapter mGalleryAdapter;
  private ProgressDialog mProgressDialog;
  private GalleryManager mGalleryManager; 
  private ArrayList<String> mUrlList = new ArrayList<String>();
  // Constants
  public static final String INTENT_USER_ID = "user_id";
  String url = "http://www.mssoft.org/data/big.json";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_rateit2);
    
    // getIntent.getString id user profile (url link)
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.rateit_header_title));
    
    // Progress Dialog
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    // Bitmat manager
    mGalleryManager = new GalleryManager(this,mUrlList,1);
    
    // Gallery
    mGallery = (RateitGallery)findViewById(R.id.galleryRateit2);
    
    update(null);
  }
  //---------------------------------------------------------------------------
  private void update(String params) {
    new UsersLoaderTask().execute(params);
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    mGalleryManager.stop();
    mGalleryManager.release();
    super.onDestroy();  
  }
  //---------------------------------------------------------------------------
  // class UsersLoaderTask
  private class UsersLoaderTask extends AsyncTask<String, Void, Boolean> {
    //---------------------------------
    @Override
    protected void onPreExecute(){
      mProgressDialog.show();
    }
    //---------------------------------
    @Override
    protected Boolean doInBackground(String... params) {
      String s = null;
      try {
        s = Http.httpGetRequest(url);
      } catch(Exception ex) { ex.printStackTrace(); } 
        finally { if(s == null) return false; }
      
      JSONObject obj = null;
      JSONArray  arr = null;
      try {
        obj = new JSONObject(s);
        arr = new JSONArray(obj.getString("covers"));
        for(int i=0; i<arr.length(); ++i) {
          JSONObject o = (JSONObject)arr.get(i);
          User user = new User("","","0");
          user.photo = o.getString("cover");
          user.name = o.getString("artist");
          mUrlList.add(user.photo);
        }
      } catch(JSONException e) {
        e.printStackTrace();
      }
      return true;
    }
    //---------------------------------
    @Override
    protected void onPostExecute(Boolean result) {
      mGalleryAdapter = new RateitGalleryAdapter(RateitActivity.this,mGalleryManager);
      mGallery.setAdapter(mGalleryAdapter);
      mGalleryAdapter.notifyDataSetChanged();
      mProgressDialog.cancel();
    }
  }// UsersLoaderTask
  //---------------------------------------------------------------------------
}

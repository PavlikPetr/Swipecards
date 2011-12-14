package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.data.TopUser;
import com.sonetica.topface.net.*;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.GalleryCachedManager;
import com.sonetica.topface.utils.IFrame;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

/*  
 *  Класс активити для просмотра списка топ пользователей   "топы"
 */
public class TopsActivity extends Activity {
  // Data
  private GridView mGallery;
  private TopsGridAdapter mGridAdapter;
  private ProgressDialog  mProgressDialog;
  private GalleryCachedManager mGalleryCachedManager;
  private ArrayList<TopUser> mUserList;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Debug.log(this,"+onCreate");

    // Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    
    // Girl Button
    Button btnGirls = (Button)findViewById(R.id.btnBarGirls);
    btnGirls.setText(getString(R.string.tops_btn_girls));
    btnGirls.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        update(0,2,true);
      }
    });
    
    // Boy Button
    Button btnBoys  = (Button)findViewById(R.id.btnBarBoys);
    btnBoys.setText(getString(R.string.tops_btn_boys));
    btnBoys.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        update(1,2,true);
      }
    });
    
    // Boy Button
    Button btnCity  = (Button)findViewById(R.id.btnBarCity);
    btnCity.setText(getString(R.string.tops_btn_city));
    btnCity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        update(0,1,true);
      }
    });
    
    // Gallery
    mGallery = (GridView)findViewById(R.id.grdTopsGallary);
    mGallery.setAnimationCacheEnabled(false);
    mGallery.setScrollingCacheEnabled(false);
    mGallery.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(TopsActivity.this,RateitActivity.class);
        intent.putExtra(RateitActivity.INTENT_USER_ID,mUserList.get(position).uid);
        startActivity(intent);
      }
    });
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    update(0,2,false);
  }
  //---------------------------------------------------------------------------
  private void update(int sex, int city, boolean isRefreshed) {
    TopRequest topRequest = new TopRequest();
    topRequest.sex  = sex;
    topRequest.city = city;
    ConnectionService.sendRequest(topRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        mUserList = resp.getUsers(); 
        if(mUserList != null) {
          mGalleryCachedManager = new GalleryCachedManager(TopsActivity.this,IFrame.TOPS,mUserList);
          mGridAdapter = new TopsGridAdapter(TopsActivity.this,mGalleryCachedManager);
          mGallery.setAdapter(mGridAdapter);
        }
        mProgressDialog.cancel();
      }
    });
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    
    if(mGalleryCachedManager!=null) {
      mGalleryCachedManager.release();
      mGalleryCachedManager=null;
    }
    if(mGridAdapter!=null) mGridAdapter=null;
    if(mGallery!=null)     mGallery=null;
    
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  /*
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
      * /


      
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
   */
}
//Toast.makeText(TopsActivity.this,mUserList.get(position).uid,Toast.LENGTH_LONG).show();
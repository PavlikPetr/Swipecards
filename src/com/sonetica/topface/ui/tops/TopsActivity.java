package com.sonetica.topface.ui.tops;

import com.sonetica.topface.R;
import com.sonetica.topface.data.User;
import com.sonetica.topface.net.*;
import com.sonetica.topface.services.ConnectionService;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
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
  private GridView mGallary;
  private TopsGridAdapter mGridAdapter;
  private ProgressDialog mProgressDialog;
  private ArrayList<User> mUserList;
  //private UsersLoaderTask mAsyncTask;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_tops);
    Debug.log(this,"+onCreate");
    
    mUserList = new ArrayList<User>();
    
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.tops_header_title));
    mGallary = (GridView)findViewById(R.id.grdTopsGallary);
    //mAsyncTask = new UsersLoaderTask();
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    mProgressDialog.show();
    
    // Girl Button
    Button btnGirls = (Button)findViewById(R.id.btnBarGirls);
    btnGirls.setText(getString(R.string.tops_btn_girls));
    btnGirls.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(TopsActivity.this,"girls",Toast.LENGTH_LONG).show();
        update(0,2,true);
      }
    });
    // Boy Button
    Button btnBoys  = (Button)findViewById(R.id.btnBarBoys);
    btnBoys.setText(getString(R.string.tops_btn_boys));
    btnBoys.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(TopsActivity.this,"boys",Toast.LENGTH_LONG).show();
        update(1,2,true);
      }
    });
    // Boy Button
    Button btnCity  = (Button)findViewById(R.id.btnBarCity);
    btnCity.setText(getString(R.string.tops_btn_city));
    btnCity.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Toast.makeText(TopsActivity.this,"city",Toast.LENGTH_LONG).show();
        update(0,1,true);
      }
    });
    // Gallery
    mGallary = (GridView)findViewById(R.id.grdTopsGallary);
    mGallary.setAnimationCacheEnabled(false);
    mGallary.setScrollingCacheEnabled(false);
    mGallary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(TopsActivity.this,mUserList.get(position).uid,Toast.LENGTH_LONG).show();
        //startActivity(new Intent(TopsActivity.this,RateitActivity.class));
      }
    });
    
    update(0,2,false);
  }
  //---------------------------------------------------------------------------
  private void update(int sex, int city, boolean isRefreshed) {
    //mAsyncTask.execute(params);
    TopRequest topRequest = new TopRequest();
    topRequest.sex  = sex;
    topRequest.city = city;
    ConnectionService.sendRequest(topRequest,new Handler() {
      @Override
      public void handleMessage(Message msg) {
        super.handleMessage(msg);
        Response resp = (Response)msg.obj;
        // do it
        mUserList = resp.getUsers(); 
        if(mUserList != null) {
          mGridAdapter = new TopsGridAdapter(TopsActivity.this,mUserList);
          mGallary.setAdapter(mGridAdapter);

        }
        mProgressDialog.cancel();
      }
    });
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onDestroy() {
    //mGalleryCachedManager.release();
    Debug.log(this,"-onDestroy");
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

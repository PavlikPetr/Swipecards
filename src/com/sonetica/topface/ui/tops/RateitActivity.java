package com.sonetica.topface.ui.tops;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.sonetica.topface.R;
import com.sonetica.topface.data.User;
import com.sonetica.topface.utils.Http;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

public class RateitActivity extends FragmentActivity {
  // Data
  private PagerAdapter mPagerAdapter;
  private ProgressDialog mProgressDialog;
  private ArrayList<Fragment> mFragments = new ArrayList<Fragment>();
  private ViewPager mPager;
  // Constants
  public static final String INTENT_USER_ID = "user_id";
  String url = "http://www.mssoft.org/data/big.json";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_rateit);
    
    // Start progress dialog    
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    update(null);    
    
    mPagerAdapter = new RateitPagerAdapter(getSupportFragmentManager(), mFragments);
    mPager = (ViewPager)findViewById(R.id.viewpager);
    mPager.setAdapter(mPagerAdapter);

  }
  //---------------------------------------------------------------------------
  private void update(String params) {
    new UsersLoaderTask().execute(params);
  }
  //---------------------------------------------------------------------------
  // class UsersLoaderTask
  private class UsersLoaderTask extends AsyncTask<String, Void, Boolean> {
    //---------------------------------
    @Override
    protected void onPreExecute(){
      mProgressDialog.show();
      mFragments = new ArrayList<Fragment>();
    }
    //---------------------------------
    // @params параметры для получения списка линков
    @Override
    protected Boolean doInBackground(String... params) {
      // получить массив ссылок на изображения с сервера
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
          User user = new User();
          user.link = o.getString("cover");
          user.name = o.getString("artist");
          RateitFragment fragment = (RateitFragment)Fragment.instantiate(RateitActivity.this, RateitFragment.class.getName());
          fragment.setUrl(user.link);
          mFragments.add(fragment);
        }
      } catch(JSONException e) {
        e.printStackTrace();
      }
      return true;
    }
    //---------------------------------
    @Override
    protected void onPostExecute(Boolean result) {
      if(result == false)
        return;
      mPagerAdapter.notifyDataSetChanged();
      mPager.invalidate();
      mProgressDialog.cancel();
    }
  }// UsersLoaderTask
  //---------------------------------------------------------------------------
}

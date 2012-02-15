package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/*
 *      "Профиль"
 */
public class ProfileActivity extends Activity {
  // Data
  private LayoutInflater mInflater;
  private ViewPager awesomePager;
  private AwesomePagerAdapter awesomeAdapter;
  private View mProfileTop;
  private View mProfileBottom;
  private static int NUM_AWESOME_VIEWS = 2;
  //Constants
  public static final String INTENT_USER_ID = "user_id";
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_profile);
    Debug.log(this,"+onCreate");
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.profile_header_title));
    
    // свой - чужой профиль
    final int userId = getIntent().getIntExtra(INTENT_USER_ID,-1);
    
    mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mProfileTop = mInflater.inflate(R.layout.profile_form_top, null, false);
    mProfileBottom = mInflater.inflate(R.layout.profile_form_bottom, null, false);

    awesomeAdapter = new AwesomePagerAdapter();
    awesomePager = (ViewPager) findViewById(R.id.awesomepager);
    awesomePager.setAdapter(awesomeAdapter);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private class AwesomePagerAdapter extends PagerAdapter{
    @Override
    public int getCount() {
      return NUM_AWESOME_VIEWS;
    }
    @Override
    public Object instantiateItem(View collection, int position) {
      View view;
      
      if(position == 0)
        ((ViewPager)collection).addView(view=mProfileTop,0);
      else
        ((ViewPager)collection).addView(view=mProfileBottom,0);
      
      return view;
    }
    @Override
    public void destroyItem(View collection, int position, Object view) {
      ((ViewPager)collection).removeView((TextView)view);
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view==((TextView)object);
    }
    @Override
    public void finishUpdate(View arg0) {
      
    }
    @Override
    public void restoreState(Parcelable arg0, ClassLoader arg1) {
      
    }
    @Override
    public Parcelable saveState() {
      return null;
    }
    @Override
    public void startUpdate(View arg0) {
      
    }
  }
  //---------------------------------------------------------------------------
}


/*
if(userId==-1) {
  ProfileRequest profileRequest = new ProfileRequest(this,false);
  profileRequest.callback(new ApiHandler() {
    @Override
    public void success(final Response response) {
      ProfileActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          Profile profile = Profile.parse(response,false);
        }
      });
    }
    @Override
    public void fail(int codeError) {
    }
  }).exec();
} else {
  ProfilesRequest profilesRequest = new ProfilesRequest(this);
  profilesRequest.uids.add(userId);
  profilesRequest.callback(new ApiHandler() {
    @Override
    public void success(final Response response) {
      ProfileActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          LinkedList<ProfileUser> profile = ProfileUser.parse(userId,response);
        }
      });
    }
    @Override
    public void fail(int codeError) {
    }
  }).exec();
}
*/
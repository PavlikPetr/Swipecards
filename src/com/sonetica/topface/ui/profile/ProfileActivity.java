package com.sonetica.topface.ui.profile;

import com.sonetica.topface.R;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.data.ProfileUser;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.net.ProfilesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.FrameImageView;
import com.sonetica.topface.ui.HorizontalListView;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/*
 *      "Профиль"
 */
public class ProfileActivity extends Activity {
  // Data
  private LayoutInflater mInflater;
  //private ViewPager mViewPager;
  //private ProfilePagerAdapter mPageAdapter;
  private ProgressDialog mProgressDialog;
  private TextView mName;
  private TextView mCity;
  private FrameImageView mFramePhoto;
  //private GridView mGallary;
  private View mProfileTop;
  private View mProfileBottom;
  // Info
  private TextView mHeight;
  private TextView mWeight;
  private TextView mEducation;
  private TextView mCommunication;
  private TextView mCharacter;
  private TextView mAlcohol;
  private TextView mFitness;
  private TextView mJob;
  private TextView mMarriage;
  private TextView mFinances;
  private TextView mSmoking;
  //private TextView mStatus;  // дана отмашка на отключение статуса
  
  //Constants
  private static final int PROFILE_TOP = 0;
  private static final int PROFILE_BOTTOM = 1;
  private static final int NUM_VIEWS = 2;
  public  static final String INTENT_USER_ID = "user_id";
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
    
    /*
    mInflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    mProfileTop = mInflater.inflate(R.layout.profile_form_top, null, false);
    mProfileBottom = mInflater.inflate(R.layout.profile_form_bottom, null, false);

    mPageAdapter = new ProfilePagerAdapter();
    mViewPager = (ViewPager)findViewById(R.id.viewPagerProfile);
    mViewPager.setAdapter(mPageAdapter);
    */
    
    // Name
    mName = (TextView)this.findViewById(R.id.tvProfileName);
    // City
    mCity = (TextView)this.findViewById(R.id.tvProfileCity);
    // Photo
    mFramePhoto = (FrameImageView)this.findViewById(R.id.ivProfileFramePhoto);
    // Gallary
    HorizontalListView gridview = (HorizontalListView)findViewById(R.id.QQ);
    gridview.setAdapter(new ImageAdapter(this));
    
    //Gallery gridview2 = (Gallery)findViewById(R.id.QQ2);
    //gridview2.
    //gridview2.setAdapter(new ImageAdapter(this));

    mHeight = (TextView)this.findViewById(R.id.tvProfileHeight);
    mWeight = (TextView)this.findViewById(R.id.tvProfileWeight);
    mEducation = (TextView)this.findViewById(R.id.tvProfileEducation);
    mCommunication = (TextView)this.findViewById(R.id.tvProfileCommutability);
    mCharacter = (TextView)this.findViewById(R.id.tvProfileCharacter);
    mAlcohol = (TextView)this.findViewById(R.id.tvProfileAlcohol);
    mFitness = (TextView)this.findViewById(R.id.tvProfileFitness);
    mJob = (TextView)this.findViewById(R.id.tvProfileJob);
    mMarriage = (TextView)this.findViewById(R.id.tvProfileMarriage);
    mFinances = (TextView)this.findViewById(R.id.tvProfileFinances);
    mSmoking = (TextView)this.findViewById(R.id.tvProfileSmoking);
    //mStatus = (TextView)mProfileBottom.findViewById(R.id.tvProfileStatus);
    
    if(userId==-1) {
      Button btnEdit = (Button)this.findViewById(R.id.btnProfileEdit);
      btnEdit.setVisibility(View.VISIBLE);
      btnEdit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
      });
    } else {
      Button btnChat = (Button)this.findViewById(R.id.btnProfileChat);
      btnChat.setVisibility(View.VISIBLE);
      btnChat.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(ProfileActivity.this,ChatActivity.class);
          intent.putExtra(ChatActivity.INTENT_USER_ID,userId);
          startActivityForResult(intent,0);
        }
      });
    }
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));

    update(userId);
  }
  //---------------------------------------------------------------------------
  public void update(int userId) {
    mProgressDialog.show();
    
    if(userId==-1)
      getProfile();
    else
      getProfile(userId);
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  private void getProfile() {
    ProfileRequest profileRequest = new ProfileRequest(this,false);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {
        Profile profile = Profile.parse(response,false);
        mName.setText(profile.first_name);
        mCity.setText(profile.age+", "+profile.city_name);
        mHeight.setText(""+profile.questionary_height);
        mWeight.setText(""+profile.questionary_weight);
        
        FormInfo formInfo = new FormInfo(ProfileActivity.this,profile.sex);
        mEducation.setText(formInfo.getEducation(profile.questionary_education_id));
        mCommunication.setText(formInfo.getCommunication(profile.questionary_communication_id));
        mCharacter.setText(formInfo.getCharacter(profile.questionary_character_id));
        mAlcohol.setText(formInfo.getAlcohol(profile.questionary_alcohol_id));
        mFitness.setText(formInfo.getFitness(profile.questionary_fitness_id));
        mJob.setText(formInfo.getJob(profile.questionary_job_id));
        mMarriage.setText(formInfo.getMarriage(profile.questionary_marriage_id));
        mFinances.setText(formInfo.getFinances(profile.questionary_finances_id));
        mSmoking.setText(formInfo.getSmoking(profile.questionary_smoking_id));
        //mStatus.setText(""+profile.questionary_status);
        
        mFramePhoto.mOnlineState = true;
        Http.imageLoader(profile.photo_url,mFramePhoto);
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void getProfile(final int userId) {
    ProfilesRequest profileRequest = new ProfilesRequest(this);
    profileRequest.uids.add(userId);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {
        ProfileUser profile = ProfileUser.parse(userId,response);
        mName.setText(profile.first_name);
        mCity.setText(profile.age+", "+profile.city_name);
        mHeight.setText(""+profile.questionary_height);
        mWeight.setText(""+profile.questionary_weight);
        
        FormInfo formInfo = new FormInfo(ProfileActivity.this,profile.sex);
        mEducation.setText(formInfo.getEducation(profile.questionary_education_id));
        mCommunication.setText(formInfo.getCommunication(profile.questionary_communication_id));
        mCharacter.setText(formInfo.getCharacter(profile.questionary_character_id));
        mAlcohol.setText(formInfo.getAlcohol(profile.questionary_alcohol_id));
        mFitness.setText(formInfo.getFitness(profile.questionary_fitness_id));
        mJob.setText(formInfo.getJob(profile.questionary_job_id));
        mMarriage.setText(formInfo.getMarriage(profile.questionary_marriage_id));
        mFinances.setText(formInfo.getFinances(profile.questionary_finances_id));
        mSmoking.setText(formInfo.getSmoking(profile.questionary_smoking_id));
        //mStatus.setText(""+profile.questionary_status);
        
        mFramePhoto.mOnlineState = profile.online;
        Http.imageLoader(profile.getBigLink(),mFramePhoto);
        mProgressDialog.cancel();
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  // class ProfilePagerAdapter
  //---------------------------------------------------------------------------
  private class ProfilePagerAdapter extends PagerAdapter{
    @Override
    public int getCount() {
      return NUM_VIEWS;
    }
    @Override
    public Object instantiateItem(View collection, int position) {
      View view=null;
      
      if(position==PROFILE_TOP)
        ((ViewPager)collection).addView(view=mProfileTop,0);
      else if(position==PROFILE_BOTTOM)
        ((ViewPager)collection).addView(view=mProfileBottom,0);
      
      return view;
    }
    @Override
    public void destroyItem(View collection, int position, Object view) {
      ((ViewPager)collection).removeView((View)view);
    }
    @Override
    public boolean isViewFromObject(View view, Object object) {
      return view==((View)object);
    }
    @Override public void finishUpdate(View arg0) {}
    @Override public void restoreState(Parcelable arg0, ClassLoader arg1) {}
    @Override public Parcelable saveState() {return null;}
    @Override public void startUpdate(View arg0) {}
  }
  //---------------------------------------------------------------------------
  // class ImageAdapter
  //---------------------------------------------------------------------------
  public class ImageAdapter extends BaseAdapter {    
  private Context mContext;    
  public ImageAdapter(Context c) {        mContext = c;    }    
  public int getCount() {        return mThumbIds.length;    }    
  public Object getItem(int position) {        return null;    }    
  public long getItemId(int position) {        return 0;    }    // create a new ImageView for each item referenced by the Adapter    
  public View getView(int position, View convertView, ViewGroup parent) {        
    ImageView imageView;        if (convertView == null) {  // if it's not recycled, initialize some attributes            
    imageView = new ImageView(mContext);            
    imageView.setLayoutParams(new Gallery.LayoutParams(85, 85));            
    imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);            
    imageView.setPadding(8, 8, 8, 8);        } else {            
      imageView = (ImageView) convertView;        }        
    imageView.setImageResource(mThumbIds[position]);        
    return imageView;    }
  
    private Integer[] mThumbIds  = {
            R.drawable.ic_launcher,
            R.drawable.im_red_informer,
            R.drawable.ic_launcher,
            R.drawable.im_red_informer,
            R.drawable.ic_launcher,
            R.drawable.im_red_informer,
            R.drawable.ic_launcher,
            R.drawable.im_red_informer
  };
  }
  //---------------------------------------------------------------------------
  
}

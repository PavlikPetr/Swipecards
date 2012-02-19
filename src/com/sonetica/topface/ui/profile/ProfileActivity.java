package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.data.ProfileUser;
import com.sonetica.topface.net.AlbumRequest;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.ProfileRequest;
import com.sonetica.topface.net.ProfilesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.FrameImageView;
import com.sonetica.topface.ui.album.AlbumActivity;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/*
 *      "Профиль"
 */
public class ProfileActivity extends Activity {
  // Data
  private TextView mName;
  private TextView mCity;
  private TextView mEroTitle;
  private FrameImageView mFramePhoto;
  private HorizontalListView mListView;
  private HorizontalListView mListEroView;
  private PhotoGalleryAdapter mListAdapter;
  private PhotoGalleryAdapter mListEroAdapter;
  private ProgressDialog mProgressDialog;
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
    
    // Name
    mName = (TextView)this.findViewById(R.id.tvProfileName);
    // City
    mCity = (TextView)this.findViewById(R.id.tvProfileCity);
    // Photo
    mFramePhoto = (FrameImageView)this.findViewById(R.id.ivProfileFramePhoto);
    // Gallary and Adapter
    mListAdapter = new PhotoGalleryAdapter(ProfileActivity.this);
    mListView = (HorizontalListView)findViewById(R.id.lvAlbumPreview);
    mListView.setBackgroundResource(R.drawable.profile_bg_gallery);
    mListView.setAdapter(mListAdapter);
    mListView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0,View arg1,int arg2,long arg3) {
        Toast.makeText(ProfileActivity.this,"p:"+arg2,Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this,AlbumActivity.class);
        intent.putExtra(AlbumActivity.INTENT_USER_ID,userId);
        startActivityForResult(intent,0);
      }
    });
    // Ero Gallary and Adapter
    mListEroAdapter = new PhotoGalleryAdapter(ProfileActivity.this);
    mListEroView = (HorizontalListView)findViewById(R.id.lvEroAlbumPreview);
    mListEroView.setBackgroundColor(Color.WHITE);
    mListEroView.setAdapter(mListEroAdapter);
    mEroTitle = (TextView)this.findViewById(R.id.tvEroTitle);
    // Info
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
    
    // Buttons
    if(userId==-1) {  // редактировать
      Button btnEdit = (Button)this.findViewById(R.id.btnProfileEdit);
      btnEdit.setVisibility(View.VISIBLE);
      btnEdit.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
      });
    } else {  // поболтать
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
  // свой профиль
  private void getProfile() {
    ProfileRequest profileRequest = new ProfileRequest(this,false);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {
        Profile profile = Profile.parse(response,false);
        
        // грузим галерею
        getAlbum(profile.id);
        
        // основная информация
        mName.setText(profile.first_name);
        mCity.setText(profile.age+", "+profile.city_name);
        mHeight.setText(""+profile.questionary_height);
        mWeight.setText(""+profile.questionary_weight);
        
        // анкета
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
        
        // avatar
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
  // чужой профиль
  private void getProfile(final int userId) {
    ProfilesRequest profileRequest = new ProfilesRequest(this);
    profileRequest.uids.add(userId);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {
        ProfileUser profile = ProfileUser.parse(userId,response);
        
        // грузим галерею
        getAlbum(userId);

        // основная информация
        mName.setText(profile.first_name);
        mCity.setText(profile.age+", "+profile.city_name);
        mHeight.setText(""+profile.questionary_height);
        mWeight.setText(""+profile.questionary_weight);
        
        // анкета
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
        
        // avatar
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
  private void getAlbum(int uid) {
    AlbumRequest albumRequest = new AlbumRequest(this);
    albumRequest.uid  = uid;
    albumRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        LinkedList<Album> photoList = new LinkedList<Album>(); 
        LinkedList<Album> eroList   = new LinkedList<Album>();
        
        // сортируем эро и не эро
        LinkedList<Album> albumList = Album.parse(response);        
        for(Album album : albumList)
          if(album.ero)
            eroList.add(album);
          else
            photoList.add(album);
        
        // обнавляем галереи
        if(photoList.size()>0) {
          mListAdapter.setDataList(photoList);
          mListAdapter.notifyDataSetChanged();
        }

        if(eroList.size()>0) {
          mEroTitle.setVisibility(View.VISIBLE);
          mListEroView.setVisibility(View.VISIBLE);
          mListEroAdapter.setDataList(eroList);
          mListEroAdapter.notifyDataSetChanged();
        }
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  public void release() {
    mName = null;
    mCity = null;
    mFramePhoto = null;
    mListView = null;
    mListAdapter = null;
    mProgressDialog = null;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    release();
    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------  
}

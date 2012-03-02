package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.data.ProfileUser;
import com.sonetica.topface.net.AlbumRequest;
import com.sonetica.topface.net.ApiHandler;
import com.sonetica.topface.net.Http;
import com.sonetica.topface.net.ProfilesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.ui.BuyingActivity;
import com.sonetica.topface.ui.album.AlbumActivity;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

/*
 *      "Профиль"
 */
public class ProfileActivity extends Activity implements SwapView.OnSwapListener, View.OnClickListener, AdapterView.OnItemClickListener {
  // Data
  private int mUserId;
  private boolean mOwner;
  private SwapView mSwapView;
  private Button mProfileButton;
  private Button mBuyingButton;
  private ViewGroup mEroViewGroup;
  private FrameImageView mFramePhoto;
  private HorizontalListView mListView;
  private HorizontalListView mListEroView;
  private PhotoGalleryAdapter mListAdapter;
  private PhotoEroGalleryAdapter mListEroAdapter;
  private ProgressDialog mProgressDialog;
  private LinkedList<Album> mPhotoList; 
  private LinkedList<Album> mEroList;
  // Info
  private TextView mName;
  private TextView mCity;
  private TextView mEroTitle;
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
  private TextView mStatus;
  boolean swap = true;  // проверить на оптимизацию 
  //Constants
  public static final String INTENT_USER_ID = "user_id";
  public static final int FORM_TOP = 0;
  public static final int FORM_BOTTOM = 1;
  public static final int GALLARY_IMAGE_ACTIVITY_REQUEST_CODE = 100;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_profile);
    Debug.log(this,"+onCreate");
    
    // Title Header
    ((TextView)findViewById(R.id.tvHeaderTitle)).setText(getString(R.string.profile_header_title));
    // Swap
    mSwapView = ((SwapView)findViewById(R.id.swapFormView));
    mSwapView.setOnSwapListener(this);
    // Profile Header Button 
    mProfileButton = ((Button)findViewById(R.id.btnHeader));
    mProfileButton.setOnClickListener(this);
    // свой - чужой профиль
    mUserId = getIntent().getIntExtra(INTENT_USER_ID,-1);
    // Buttons
    if(mUserId==-1) {  
      mOwner  = true;           // СВОЙ ПРОФИЛЬ
      mUserId = Data.s_Profile.uid;
      // редактировать
      Button btnEdit = (Button)this.findViewById(R.id.btnProfileEdit);
      btnEdit.setVisibility(View.VISIBLE);
      btnEdit.setOnClickListener(this);
    } else {  
      // поболтать
      Button btnChat = (Button)this.findViewById(R.id.btnProfileChat);
      btnChat.setVisibility(View.VISIBLE);
      btnChat.setOnClickListener(this);
    }
    
    // Buying Button
    if(mOwner) {
      ((ViewGroup)findViewById(R.id.lvProfileBuying)).setVisibility(View.VISIBLE);
      mBuyingButton = ((Button)findViewById(R.id.btnProfileBuying));
      mBuyingButton.setOnClickListener(this);
    }
    
    // Gallary and Adapter
    mListAdapter = new PhotoGalleryAdapter(ProfileActivity.this,mOwner);
    mListView = (HorizontalListView)findViewById(R.id.lvAlbumPreview);
    mListView.setBackgroundResource(R.drawable.profile_bg_gallery);
    mListView.setAdapter(mListAdapter);
    mListView.setOnItemClickListener(this);
    
    // Ero Gallary and Adapter
    mEroTitle = (TextView)this.findViewById(R.id.tvEroTitle);
    mEroViewGroup = (ViewGroup)findViewById(R.id.loEroAlbum);
    mListEroAdapter = new PhotoEroGalleryAdapter(ProfileActivity.this,mOwner);
    mListEroView = (HorizontalListView)findViewById(R.id.lvEroAlbumPreview);
    mListEroView.setBackgroundResource(R.drawable.profile_bg_gallery);
    mListEroView.setAdapter(mListEroAdapter);
    mListEroView.setOnItemClickListener(this);
    
    // Info
    mName = (TextView)this.findViewById(R.id.tvProfileName);
    mCity = (TextView)this.findViewById(R.id.tvProfileCity);
    mFramePhoto = (FrameImageView)this.findViewById(R.id.ivProfileFramePhoto);
    mHeight = (TextView)this.findViewById(R.id.tvProfileHeight);
    mWeight = (TextView)this.findViewById(R.id.tvProfileWeight);
    mEducation = (TextView)this.findViewById(R.id.tvProfileEducation);
    mCommunication = (TextView)this.findViewById(R.id.tvProfileCommutability);
    mCharacter = (TextView)this.findViewById(R.id.tvProfileCharacter);
    mAlcohol = (TextView)this.findViewById(R.id.tvProfileAlcohol);
    mFitness = (TextView)this.findViewById(R.id.tvProfileFitness);
    mJob = (TextView)this.findViewById(R.id.tvProfileJob);              // ЧЁЗА ???
    mMarriage = (TextView)this.findViewById(R.id.tvProfileMarriage);
    mFinances = (TextView)this.findViewById(R.id.tvProfileFinances);
    mSmoking = (TextView)this.findViewById(R.id.tvProfileSmoking);
    mStatus = (TextView)this.findViewById(R.id.tvProfileStatus);
    
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    
    // Albums
    mPhotoList = new LinkedList<Album>(); 
    mEroList   = new LinkedList<Album>();

    if(!mOwner)
      getUserProfile(mUserId);
    else 
      getAlbum();  // грузим галерею
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onStart() {
    super.onStart();
    
    if(mOwner)
      getProfile();
  }  
  //---------------------------------------------------------------------------
  // свой профиль
  private void getProfile() {
    Profile profile = Data.s_Profile;
    
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
    mStatus.setText(profile.questionary_status);
    
    // avatar
    mFramePhoto.mOnlineState = true;
    Http.imageLoader(profile.getBigLink(),mFramePhoto);
  }
  //---------------------------------------------------------------------------
  // чужой профиль
  private void getUserProfile(final int userId) {
    // включаем прогресс
    mProgressDialog.show();
    ProfilesRequest profileRequest = new ProfilesRequest(this);
    profileRequest.uids.add(userId);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {        
        ProfileUser profile = ProfileUser.parse(userId,response);
        
        // грузим галерею
        getUserAlbum(userId);
        
        // отключаем прогресс
        mProgressDialog.cancel();

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
        mStatus.setText(profile.questionary_status);
        
        // avatar
        mFramePhoto.mOnlineState = profile.online;
        Http.imageLoader(profile.getBigLink(),mFramePhoto);
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void getAlbum() {
    // кнопки добавления
    mPhotoList.add(new Album()); 
    mEroList.add(new Album());

    // сортируем эро и не эро
    LinkedList<Album> albumList = Data.s_Profile.albums;
    for(Album album : albumList)
      if(album.ero)
        mEroList.add(album);
      else
        mPhotoList.add(album);
    
    // обнавляем галереи
    if(mPhotoList.size()>0) {
      mListAdapter.setDataList(mPhotoList);
      mListAdapter.notifyDataSetChanged();
    }

    if(mEroList.size()>0) {
      mListEroAdapter.setDataList(mEroList);
      mListEroAdapter.notifyDataSetChanged();
      mEroTitle.setVisibility(View.VISIBLE);
      mEroViewGroup.setVisibility(View.VISIBLE);
    }
  }
  //---------------------------------------------------------------------------
  private void getUserAlbum(int uid) {
    AlbumRequest albumRequest = new AlbumRequest(this);
    albumRequest.uid  = uid;
    albumRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        // сортируем эро и не эро
        LinkedList<Album> albumList = Album.parse(response);        
        for(Album album : albumList)
          if(album.ero)
            mEroList.add(album);
          else
            mPhotoList.add(album);
        
        // обнавляем галереи
        if(mPhotoList.size()>0) {
          mListAdapter.setDataList(mPhotoList);
          mListAdapter.notifyDataSetChanged();
        }

        if(mEroList.size()>0) {
          mListEroAdapter.setDataList(mEroList);
          mListEroAdapter.notifyDataSetChanged();
          mEroTitle.setVisibility(View.VISIBLE);
          mEroViewGroup.setVisibility(View.VISIBLE);
        }
      }
      @Override
      public void fail(int codeError) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  @Override
  public void onSwap() {
    swap=!swap; // костыль на скорую руку
  }
  //---------------------------------------------------------------------------
  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.btnProfileChat: {
        Intent intent = new Intent(ProfileActivity.this,ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mUserId);
        startActivity(intent);
      } break;
      case R.id.btnProfileEdit: {
        startActivity(new Intent(ProfileActivity.this,EditProfileActivity.class));
      } break;
      case R.id.btnProfileBuying: {
        startActivity(new Intent(ProfileActivity.this,BuyingActivity.class));
      } break;
      case R.id.btnHeader: {
        // mCurrForm = mCurrForm == FORM_TOP ? FORM_BOTTOM : FORM_TOP;
        mSwapView.snapToScreen(swap?FORM_BOTTOM:FORM_TOP);
        if(!swap) {
          mProfileButton.setText(R.string.profile_header_title);
        } else {
          mProfileButton.setText(R.string.profile_btn_form);
        }
      } break;
    }
  }
  //---------------------------------------------------------------------------
  @Override
  public void onItemClick(AdapterView<?> parent,View arg1,int position,long arg3) {
    switch(parent.getId()) {
      case R.id.lvAlbumPreview: {                // ALBUM
        if(position==0 && mOwner==true) { 
          addPhoto(false);
        } else {
          Intent intent = new Intent(ProfileActivity.this,AlbumActivity.class);
          if(mOwner==true) {
            --position;
            intent.putExtra(AlbumActivity.INTENT_OWNER,true);            
          }
          intent.putExtra(AlbumActivity.INTENT_USER_ID,mUserId);
          intent.putExtra(AlbumActivity.INTENT_ALBUM_POS,position);

          startActivity(intent);
        }
      } break;
      case R.id.lvEroAlbumPreview: {            // ERO ALBUM
        if(position==0 && mOwner==true) {
          addPhoto(true);
        } else {
          Intent intent = null;
          if(mOwner==true) {
            --position;
            intent = new Intent(ProfileActivity.this,AlbumActivity.class);
            intent.putExtra(AlbumActivity.INTENT_OWNER,true);
          } else
            intent = new Intent(ProfileActivity.this,EroAlbumActivity.class);
          intent.putExtra(EroAlbumActivity.INTENT_USER_ID,mUserId);
          intent.putExtra(EroAlbumActivity.INTENT_ALBUM_POS,position);

          startActivity(intent);
        }        
      } break;
    }
  }
  //---------------------------------------------------------------------------
  private void addPhoto(boolean isEro) {
    
    /*
    Intent intent = new Intent();
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
    */
    startActivity(new Intent(this,AddPhotoActivity.class));
  }
  //---------------------------------------------------------------------------
  // получение фото из галереи и отправка на сервер
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    /*
    super.onActivityResult(requestCode,resultCode,data);
    Uri imageUri = data != null ? data.getData() : null;
    if (requestCode == GALLARY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && imageUri != null) {
      Toast.makeText(ProfileActivity.this.getApplicationContext(),"yes", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(ProfileActivity.this.getApplicationContext(),"no", Toast.LENGTH_SHORT).show();
    }
    */
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

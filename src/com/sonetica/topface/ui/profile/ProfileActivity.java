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
import com.sonetica.topface.net.PhotoAddRequest;
import com.sonetica.topface.net.ProfilesRequest;
import com.sonetica.topface.net.Response;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.social.Socium;
import com.sonetica.topface.social.Socium.AuthException;
import com.sonetica.topface.ui.BuyingActivity;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.utils.Debug;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
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
  private boolean mAddEroState;
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
  private AlertDialog mAddPhotoDialog;
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
  private TextView mMarriage;
  private TextView mFinances;
  private TextView mSmoking;
  //private TextView mStatus;
  //private TextView mJob;
  private TextView mAbout;
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
      mOwner = true;           // СВОЙ ПРОФИЛЬ
      mUserId = Data.s_Profile.uid;
      
      // Edit button
      Button btnEdit = (Button)findViewById(R.id.btnProfileEdit);
      btnEdit.setVisibility(View.VISIBLE);
      btnEdit.setOnClickListener(this);
      
      // Exit button
      Button btnChat = (Button)findViewById(R.id.btnProfileExit);
      btnChat.setVisibility(View.VISIBLE);
      btnChat.setOnClickListener(this);
      
      // Buying Button
      ((ViewGroup)findViewById(R.id.lvProfileBuying)).setVisibility(View.VISIBLE);
      mBuyingButton = ((Button)findViewById(R.id.btnProfileBuying));
      mBuyingButton.setOnClickListener(this);
    } else {
      // Chat button
      Button btnChat = (Button)findViewById(R.id.btnProfileChat);
      btnChat.setVisibility(View.VISIBLE);
      btnChat.setOnClickListener(this);
    }

    // Gallary and Adapter
    mListAdapter = new PhotoGalleryAdapter(getApplicationContext(),mOwner);
    mListView = (HorizontalListView)findViewById(R.id.lvAlbumPreview);
    mListView.setBackgroundResource(R.drawable.profile_bg_gallery);
    mListView.setAdapter(mListAdapter);
    mListView.setOnItemClickListener(this);
    
    // Ero Gallary and Adapter
    mEroTitle = (TextView)findViewById(R.id.tvEroTitle);
    mEroViewGroup = (ViewGroup)findViewById(R.id.loEroAlbum);
    mListEroAdapter = new PhotoEroGalleryAdapter(getApplicationContext(),mOwner);
    mListEroView = (HorizontalListView)findViewById(R.id.lvEroAlbumPreview);
    mListEroView.setBackgroundResource(R.drawable.profile_bg_gallery);
    mListEroView.setAdapter(mListEroAdapter);
    mListEroView.setOnItemClickListener(this);
    
    // Info
    mName = (TextView)findViewById(R.id.tvProfileName);
    mCity = (TextView)findViewById(R.id.tvProfileCity);
    mFramePhoto = (FrameImageView)findViewById(R.id.ivProfileFramePhoto);
    mHeight = (TextView)findViewById(R.id.tvProfileHeight);
    mWeight = (TextView)findViewById(R.id.tvProfileWeight);
    mEducation = (TextView)findViewById(R.id.tvProfileEducation);
    mCommunication = (TextView)findViewById(R.id.tvProfileCommutability);
    mCharacter = (TextView)findViewById(R.id.tvProfileCharacter);
    mAlcohol = (TextView)findViewById(R.id.tvProfileAlcohol);
    mFitness = (TextView)findViewById(R.id.tvProfileFitness);
    mMarriage = (TextView)findViewById(R.id.tvProfileMarriage);
    mFinances = (TextView)findViewById(R.id.tvProfileFinances);
    mSmoking = (TextView)findViewById(R.id.tvProfileSmoking);
    //mJob = (TextView)findViewById(R.id.tvProfileJob);
    //mStatus = (TextView)findViewById(R.id.tvProfileStatus);
    mAbout = (TextView)findViewById(R.id.tvProfileAbout);
    
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
    FormInfo formInfo = new FormInfo(getApplicationContext(),profile.sex);
    mEducation.setText(formInfo.getEducation(profile.questionary_education_id));
    mCommunication.setText(formInfo.getCommunication(profile.questionary_communication_id));
    mCharacter.setText(formInfo.getCharacter(profile.questionary_character_id));
    mAlcohol.setText(formInfo.getAlcohol(profile.questionary_alcohol_id));
    mFitness.setText(formInfo.getFitness(profile.questionary_fitness_id));
    mMarriage.setText(formInfo.getMarriage(profile.questionary_marriage_id));
    mFinances.setText(formInfo.getFinances(profile.questionary_finances_id));
    mSmoking.setText(formInfo.getSmoking(profile.questionary_smoking_id));
    //mStatus.setText(profile.status);
    //mJob.setText(formInfo.getJob(profile.questionary_job_id));
    mAbout.setText(profile.status);
    
    // avatar
    mFramePhoto.mOnlineState = true;
    Http.imageLoader(profile.getBigLink(),mFramePhoto);
  }
  //---------------------------------------------------------------------------
  // чужой профиль
  private void getUserProfile(final int userId) {
    // включаем прогресс
    mProgressDialog.show();
    ProfilesRequest profileRequest = new ProfilesRequest(getApplicationContext());
    profileRequest.uids.add(userId);
    profileRequest.callback(new ApiHandler() {
      @Override
      public void success(final Response response) {
        if(ProfileActivity.this==null)
          return;
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
        FormInfo formInfo = new FormInfo(ProfileActivity.this.getApplicationContext(),profile.sex);
        mEducation.setText(formInfo.getEducation(profile.questionary_education_id));
        mCommunication.setText(formInfo.getCommunication(profile.questionary_communication_id));
        mCharacter.setText(formInfo.getCharacter(profile.questionary_character_id));
        mAlcohol.setText(formInfo.getAlcohol(profile.questionary_alcohol_id));
        mFitness.setText(formInfo.getFitness(profile.questionary_fitness_id));
        mMarriage.setText(formInfo.getMarriage(profile.questionary_marriage_id));
        mFinances.setText(formInfo.getFinances(profile.questionary_finances_id));
        mSmoking.setText(formInfo.getSmoking(profile.questionary_smoking_id));
        //mStatus.setText(profile.status);
        //mJob.setText(formInfo.getJob(profile.questionary_job_id));
        mAbout.setText(profile.status);
        
        // avatar
        mFramePhoto.mOnlineState = profile.online;
        Http.imageLoader(profile.getBigLink(),mFramePhoto);
      }
      @Override
      public void fail(int codeError,Response response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void getAlbum() {
    // кнопки добавления
    mPhotoList.add(new Album()); // добавление элемента кнопки загрузки новых сообщений
    mEroList.add(new Album());   // кнопка

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
    AlbumRequest albumRequest = new AlbumRequest(getApplicationContext());
    albumRequest.uid  = uid;
    albumRequest.callback(new ApiHandler() {
      @Override
      public void success(Response response) {
        if(ProfileActivity.this==null)
          return;
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
      public void fail(int codeError,Response response) {
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void addPhoto(boolean bEro) {
    mAddEroState = bEro;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.album_add_photo_title));
    View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.profile_add_photo,null);
    view.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(this);
    view.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(this);
    builder.setView(view);
    mAddPhotoDialog = builder.create();
    mAddPhotoDialog.show();   
  }
  //---------------------------------------------------------------------------
  @Override
  public void onSwap() {
    swap=!swap; // костыль на скорую руку
  }
  //---------------------------------------------------------------------------
  // обработчик нажатия на кнопки
  @Override
  public void onClick(View view) {
    switch(view.getId()) {
      case R.id.btnProfileChat: {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_USER_ID,mUserId);
        startActivity(intent);
      } break;
      case R.id.btnProfileEdit: {
        startActivity(new Intent(getApplicationContext(),EditProfileActivity.class));
      } break;
      case R.id.btnProfileExit: {
        Data.removeSSID(getApplicationContext());
        Intent intent = new Intent(this, SocialActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        finish();
      } break;
      case R.id.btnProfileBuying: {
        startActivity(new Intent(getApplicationContext(),BuyingActivity.class));
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
      // popup
      case R.id.btnAddPhotoAlbum: {
        Intent intent = new Intent();
        intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
        mAddPhotoDialog.cancel();
      } break;
      case R.id.btnAddPhotoCamera: {
        Intent intent = new Intent();
        intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
        mAddPhotoDialog.cancel();
      } break;
    }
  }
  //---------------------------------------------------------------------------
  // обработчик нажатия на итем галереи
  @Override
  public void onItemClick(AdapterView<?> parent,View arg1,int position,long arg3) {
    switch(parent.getId()) {
      case R.id.lvAlbumPreview: {        // ALBUM
        if(position==0 && mOwner==true)  // нажатие на добавление фотки в своем альбоме
          addPhoto(false);
        else {
          Intent intent = new Intent(getApplicationContext(),PhotoAlbumActivity.class);
          if(mOwner==true) {
            --position;
            Data.s_PhotoAlbum = new LinkedList<Album>();  // ммм, передумать реализацию проброса массива линков
            Data.s_PhotoAlbum.addAll(mPhotoList);
            Data.s_PhotoAlbum.removeFirst();
            intent.putExtra(PhotoAlbumActivity.INTENT_OWNER,true);
          } else {
            Data.s_PhotoAlbum = mPhotoList;
          }
          intent.putExtra(PhotoAlbumActivity.INTENT_USER_ID,mUserId);
          intent.putExtra(PhotoAlbumActivity.INTENT_ALBUM_POS,position);

          startActivity(intent);
        }
      } break;
      case R.id.lvEroAlbumPreview: {     // ERO ALBUM
        if(position==0 && mOwner==true)  // нажатие на добавление эро фотки в своем альбоме
          addPhoto(true);
        else {
          Intent intent = null;
          if(mOwner==true) {
            --position;
            Data.s_PhotoAlbum = new LinkedList<Album>();  // ммм, передумать реализацию проброса массива линков
            Data.s_PhotoAlbum.addAll(mEroList);
            Data.s_PhotoAlbum.removeFirst();
            intent = new Intent(getApplicationContext(),PhotoAlbumActivity.class);
            intent.putExtra(PhotoAlbumActivity.INTENT_OWNER,true);
          } else {
            Data.s_PhotoAlbum = mEroList;
            intent = new Intent(getApplicationContext(),EroAlbumActivity.class);
          }
          intent.putExtra(EroAlbumActivity.INTENT_USER_ID,mUserId);
          intent.putExtra(EroAlbumActivity.INTENT_ALBUM_POS,position);

          startActivity(intent);
        }        
      } break;
    }
  }
  //---------------------------------------------------------------------------
  // получение фото из галереи и отправка на сервер
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    if(requestCode == GALLARY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
      Uri imageUri = data != null ? data.getData() : null;
      if(imageUri==null)
        return;
      new AsyncTaskUploader().execute(imageUri);
    }
  }
  //---------------------------------------------------------------------------
  public void release() {
    mName=null;
    mCity=null;
    mEroTitle=null;
    mHeight=null;
    mWeight=null;
    mEducation=null;
    mCommunication=null;
    mCharacter=null;
    mAlcohol=null;
    mFitness=null;
    mMarriage=null;
    mFinances=null;
    mSmoking=null;
    mSwapView=null;
    mProfileButton=null;
    mBuyingButton=null;
    mEroViewGroup=null;
    
    if(mFramePhoto!=null)
      mFramePhoto.release();
    mFramePhoto=null;
    
    mListView=null;
    mListEroView=null;

    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter=null;
    
    if(mListEroAdapter!=null)
      mListEroAdapter.release();
    mListEroAdapter=null;
    
    mProgressDialog=null;
    
    if(mPhotoList!=null)
      mPhotoList.clear();
    mPhotoList=null;
    
    if(mEroList!=null)
      mEroList.clear();
    mEroList=null;
    
    if(Data.s_PhotoAlbum!=null)
      Data.s_PhotoAlbum.clear();
    Data.s_PhotoAlbum=null;
    
    mAddPhotoDialog=null;
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    release();

    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // class AsyncTaskUploader
  //---------------------------------------------------------------------------
  class AsyncTaskUploader extends AsyncTask<Uri, Void, String[]> {
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog.show();
    }
    @Override
    protected String[] doInBackground(Uri... uri) {
      try {
        Socium soc = new Socium(ProfileActivity.this.getApplicationContext());
        // сжатие и поворот
        return soc.uploadPhoto(uri[0]);
      } catch(AuthException e) {
        e.printStackTrace();
      }
      return null;
    }
    @Override
    protected void onPostExecute(final String[] result) {
      super.onPostExecute(result);
      mProgressDialog.cancel();  

      if(mAddEroState) {
        // попап с выбором цены эро фотографии
        final CharSequence[] items = {getString(R.string.profile_coin_1), 
                                      getString(R.string.profile_coin_2),
                                      getString(R.string.profile_coin_3)};
        new AlertDialog.Builder(ProfileActivity.this)
        .setTitle(getString(R.string.profile_ero_price))
        .setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
              sendAddRequest(result,item+1);
            }
        }).create().show();
      } else
        sendAddRequest(result,0);
    }
    private void sendAddRequest(final String[] result,final int price) {
      ProfileActivity.this.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          PhotoAddRequest addPhotoRequest = new PhotoAddRequest(ProfileActivity.this.getApplicationContext());
          addPhotoRequest.big    = result[0];
          addPhotoRequest.medium = result[1];
          addPhotoRequest.small  = result[2];
          addPhotoRequest.ero = mAddEroState;
          if(mAddEroState)
            addPhotoRequest.cost=price;
          addPhotoRequest.callback(new ApiHandler() {
            @Override
            public void success(Response response) {
              //PhotoAdd add = PhotoAdd.parse(response);
            }
            @Override
            public void fail(int codeError,Response response) {
            }
          }).exec();
        }
      });//runOnUiThread
    }
  }
  //---------------------------------------------------------------------------
}

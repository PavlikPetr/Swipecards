package com.sonetica.topface.ui.profile;

import java.util.LinkedList;
import com.sonetica.topface.Data;
import com.sonetica.topface.R;
import com.sonetica.topface.billing.BuyingActivity;
import com.sonetica.topface.data.Album;
import com.sonetica.topface.data.PhotoAdd;
import com.sonetica.topface.data.Profile;
import com.sonetica.topface.data.ProfileUser;
import com.sonetica.topface.requests.AlbumRequest;
import com.sonetica.topface.requests.ApiHandler;
import com.sonetica.topface.requests.ApiResponse;
import com.sonetica.topface.requests.MessageRequest;
import com.sonetica.topface.requests.PhotoAddRequest;
import com.sonetica.topface.requests.ProfilesRequest;
import com.sonetica.topface.social.SocialActivity;
import com.sonetica.topface.social.Socium;
import com.sonetica.topface.social.Socium.AuthException;
import com.sonetica.topface.ui.dating.ResourcesView;
import com.sonetica.topface.ui.inbox.ChatActivity;
import com.sonetica.topface.ui.profile.album.PhotoEroAlbumActivity;
import com.sonetica.topface.ui.profile.album.PhotoAlbumActivity;
import com.sonetica.topface.ui.profile.gallery.HorizontalListView;
import com.sonetica.topface.ui.profile.gallery.PhotoEroGalleryAdapter;
import com.sonetica.topface.ui.profile.gallery.PhotoGalleryAdapter;
import com.sonetica.topface.utils.Debug;
import com.sonetica.topface.utils.FormInfo;
import com.sonetica.topface.utils.Http;
import com.sonetica.topface.utils.Imager;
import com.sonetica.topface.utils.LeaksManager;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/*
 *      "Профиль"
 */
public class ProfileActivity extends Activity{
  // Data
  private int mUserId;
  private boolean mOwner;
  private boolean mAddEroState;
  private boolean mChatInvoke;
  private Button mProfileButton;
  private TextView mHeaderTitle;
  private ViewGroup mEroViewGroup;
  private ResourcesView mResources;
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
  private String mUserAvatarUrl;
  // Arrows
  private ImageView mGR;
  private ImageView mGL;
  private ImageView mEGL;
  private ImageView mEGR;
  //Constants
  public static final String INTENT_USER_ID = "user_id";
  public static final String INTENT_USER_NAME = "user_name";
  public static final String INTENT_CHAT_INVOKE = "chat_invoke";
  public static final int FORM_TOP = 0;
  public static final int FORM_BOTTOM = 1;
  public static final int GALLARY_IMAGE_ACTIVITY_REQUEST_CODE = 100;
  public static final int ALBUM_ACTIVITY_REQUEST_CODE = 101;
  //---------------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.ac_profile);
    Debug.log(this,"+onCreate");

    System.gc();
    LeaksManager.getInstance().monitorObject(this);
    
    // Title Header
    mHeaderTitle = (TextView)findViewById(R.id.tvHeaderTitle);
    // Albums
    mPhotoList = new LinkedList<Album>();
    mEroList   = new LinkedList<Album>();
    // Avatar
    mFramePhoto = (FrameImageView)findViewById(R.id.ivProfileFramePhoto);
    // Profile Header Button 
    mProfileButton = ((Button)findViewById(R.id.btnHeader));
    mProfileButton.setOnClickListener(mOnClickListener);
    // Resources
    mResources = (ResourcesView)findViewById(R.id.datingRes);
    // Progress Bar
    mProgressDialog = new ProgressDialog(this);
    mProgressDialog.setMessage(getString(R.string.dialog_loading));
    // Arrows
    mGR  = (ImageView)findViewById(R.id.ivProfileArrowGL);
    mGL  = (ImageView)findViewById(R.id.ivProfileArrowGR);
    mEGR = (ImageView)findViewById(R.id.ivProfileArrowEGR);
    mEGL = (ImageView)findViewById(R.id.ivProfileArrowEGL);
    
    // пришли из чата
    mChatInvoke = getIntent().getBooleanExtra(INTENT_CHAT_INVOKE,false);    
    // свой - чужой профиль
    mUserId = getIntent().getIntExtra(INTENT_USER_ID,-1);
    // name
    String name = getIntent().getStringExtra(INTENT_USER_NAME);

    if(name!=null)
      mHeaderTitle.setText(name);  // пришли из likes, rates, chat
    else if(name==null && mUserId>0)
      mHeaderTitle.setText("");    // пришли из tops
    else
      mHeaderTitle.setText(getString(R.string.profile_header_title)); // свой профиль
    
    // Buttons
    if(mUserId==-1) {  // СВОЙ ПРОФИЛЬ
      mOwner = true;   
      mUserId = Data.s_Profile.uid;
      
      // Edit button
      View btnEdit = findViewById(R.id.btnProfileEdit);
      btnEdit.setVisibility(View.VISIBLE);
      btnEdit.setOnClickListener(mOnClickListener);
      
      // Exit button
      View btnExit = findViewById(R.id.btnProfileExit);
      btnExit.setVisibility(View.VISIBLE);
      btnExit.setOnClickListener(mOnClickListener);
      
      // Buying Button
      findViewById(R.id.loProfileBuying).setVisibility(View.VISIBLE);
      View btnBuying = findViewById(R.id.btnProfileBuying);
      btnBuying.setOnClickListener(mOnClickListener);
      
    } else {  // ЧУЖОЙ ПРОФИЛЬ
      // Chat button
      View btnChat = findViewById(R.id.btnProfileChat);
      btnChat.setVisibility(View.VISIBLE);
      btnChat.setOnClickListener(mOnClickListener);
    }
    
    // Gallary and Adapter
    mListAdapter = new PhotoGalleryAdapter(getApplicationContext(),mOwner);
    mListView = (HorizontalListView)findViewById(R.id.lvAlbumPreview);
    mListView.setAdapter(mListAdapter);
    mListView.setOnItemClickListener(mOnItemClickListener);
    // Ero Gallary and Adapter
    mEroTitle = (TextView)findViewById(R.id.tvEroTitle);
    mEroViewGroup = (ViewGroup)findViewById(R.id.loEroAlbum);
    mListEroAdapter = new PhotoEroGalleryAdapter(getApplicationContext(),mOwner);
    mListEroView = (HorizontalListView)findViewById(R.id.lvEroAlbumPreview);
    mListEroView.setAdapter(mListEroAdapter);
    mListEroView.setOnItemClickListener(mOnItemClickListener);

    {
      // Info
      mName = (TextView)findViewById(R.id.tvProfileName);
      mCity = (TextView)findViewById(R.id.tvProfileCity);
      mHeight = (TextView)findViewById(R.id.tvProfileHeight);
      mWeight = (TextView)findViewById(R.id.tvProfileWeight);
      mEducation = (TextView)findViewById(R.id.tvProfileEducation);
      mCommunication = (TextView)findViewById(R.id.tvProfileCommutability);
      mCharacter = (TextView)findViewById(R.id.tvProfileCharacter);
      mAlcohol = (TextView)findViewById(R.id.tvProfileAlcohol);
      mFitness = (TextView)findViewById(R.id.tvProfileFitness);
      mMarriage = (TextView)findViewById(R.id.tvProfileMarriage);
      if(Data.s_Profile.sex==0)
        mMarriage.setText(getString(R.string.profile_marriage_female));
      mFinances = (TextView)findViewById(R.id.tvProfileFinances);
      mSmoking = (TextView)findViewById(R.id.tvProfileSmoking);
      //mJob = (TextView)findViewById(R.id.tvProfileJob);
      //mStatus = (TextView)findViewById(R.id.tvProfileStatus);
      mAbout = (TextView)findViewById(R.id.tvProfileAbout);
    }
    
    if(!mOwner)
      getUserProfile(mUserId);
    else 
      getAlbum();  // грузим галерею
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStart() {
    super.onStart();
    //App.bind(getBaseContext());
    
    if(mOwner) {
      getProfile();
      mResources.setResources(Data.s_Power,Data.s_Money);
      mResources.invalidate();
    }
  }
  //---------------------------------------------------------------------------  
  @Override
  protected void onStop() {
    //App.unbind();
    super.onStop();
  }
  //---------------------------------------------------------------------------
  // получение фото из галереи и отправка на сервер
  @Override
  protected void onActivityResult(int requestCode,int resultCode,Intent data) {
    if(requestCode == ALBUM_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
      updateAlbum();
    }
    if(requestCode == GALLARY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
      Uri imageUri = data != null ? data.getData() : null;
      if(imageUri==null)
        return;
      new AsyncTaskUploader().execute(imageUri);
    }
  }
  //---------------------------------------------------------------------------
  @Override
  protected void onDestroy() {
    release();
    System.gc();

    Debug.log(this,"-onDestroy");
    super.onDestroy();
  }
  //---------------------------------------------------------------------------
  // свой профиль
  private void getProfile() {
    Profile profile = Data.s_Profile;
    
    // avatar
    mFramePhoto.mOnlineState = true;
    Http.imageLoader(profile.getBigLink(),mFramePhoto);
    
    // основная информация
    mName.setText(profile.first_name);
    mCity.setText(profile.age+", "+profile.city_name);
    
    mHeight.setText(""+profile.questionary_height);
    findViewById(R.id.rowProfileHeight).setVisibility(View.VISIBLE);
    mWeight.setText(""+profile.questionary_weight);
    findViewById(R.id.rowProfileWeight).setVisibility(View.VISIBLE);
    
    // анкета
    FormInfo formInfo = new FormInfo(getApplicationContext(),profile.sex);
    mEducation.setText(formInfo.getEducation(profile.questionary_education_id));
    findViewById(R.id.rowProfileEducation).setVisibility(View.VISIBLE);
    mCommunication.setText(formInfo.getCommunication(profile.questionary_communication_id));
    findViewById(R.id.rowProfileCommutability).setVisibility(View.VISIBLE);
    mCharacter.setText(formInfo.getCharacter(profile.questionary_character_id));
    findViewById(R.id.rowProfileCharacter).setVisibility(View.VISIBLE);
    mAlcohol.setText(formInfo.getAlcohol(profile.questionary_alcohol_id));
    findViewById(R.id.rowProfileAlcohol).setVisibility(View.VISIBLE);
    mFitness.setText(formInfo.getFitness(profile.questionary_fitness_id));
    findViewById(R.id.rowProfileFitness).setVisibility(View.VISIBLE);
    mMarriage.setText(formInfo.getMarriage(profile.questionary_marriage_id));
    findViewById(R.id.rowProfileMarriage).setVisibility(View.VISIBLE);
    mFinances.setText(formInfo.getFinances(profile.questionary_finances_id));
    findViewById(R.id.rowProfileFinances).setVisibility(View.VISIBLE);
    mSmoking.setText(formInfo.getSmoking(profile.questionary_smoking_id));
    findViewById(R.id.rowProfileSmoking).setVisibility(View.VISIBLE);
    //mStatus.setText(profile.status);
    //mJob.setText(formInfo.getJob(profile.questionary_job_id));
    mAbout.setText(profile.status);
    findViewById(R.id.rowProfileAbout).setVisibility(View.VISIBLE);
    
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
      public void success(final ApiResponse response) {
        ProfileUser profile = ProfileUser.parse(userId,response);
        
        mHeaderTitle.setText(profile.first_name);
        
        mUserAvatarUrl=profile.avatars_small;
        
        // avatar
        mFramePhoto.mOnlineState = profile.online;
        Http.imageLoader(profile.getBigLink(),mFramePhoto);
        
        // грузим галерею
        getUserAlbum(userId);

        int fieldCounter=0;
        
        // основная информация
        mName.setText(profile.first_name);
        mCity.setText(profile.age+", "+profile.city_name);
        if(profile.questionary_height > 0) {
          mHeight.setText(""+profile.questionary_height);
          findViewById(R.id.rowProfileHeight).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        if(profile.questionary_weight > 0) {
          mWeight.setText(""+profile.questionary_weight);
          findViewById(R.id.rowProfileWeight).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        // анкета
        FormInfo formInfo = new FormInfo(ProfileActivity.this.getApplicationContext(),profile.sex);
        String value = formInfo.getEducation(profile.questionary_education_id);
        if(value!=null) {
          mEducation.setText(value);
          findViewById(R.id.rowProfileEducation).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        value = formInfo.getCommunication(profile.questionary_communication_id);
        if(value!=null) {
          mCommunication.setText(value);
          findViewById(R.id.rowProfileCommutability).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        value = formInfo.getCharacter(profile.questionary_character_id);
        if(value!=null) {
          mCharacter.setText(value);
          findViewById(R.id.rowProfileCharacter).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        value = formInfo.getAlcohol(profile.questionary_alcohol_id);
        if(value!=null) {
          mAlcohol.setText(value);
          findViewById(R.id.rowProfileAlcohol).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        value = formInfo.getFitness(profile.questionary_fitness_id);
        if(value!=null) {
          mFitness.setText(value);
          findViewById(R.id.rowProfileFitness).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        value = formInfo.getMarriage(profile.questionary_marriage_id);
        if(value!=null) {
          mMarriage.setText(value);
          findViewById(R.id.rowProfileMarriage).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        value = formInfo.getFinances(profile.questionary_finances_id);
        if(value!=null) {
          mFinances.setText(value);
          findViewById(R.id.rowProfileFinances).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        value = formInfo.getSmoking(profile.questionary_smoking_id);
        if(value!=null) {
          mSmoking.setText(value);
          findViewById(R.id.rowProfileSmoking).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        //mStatus.setText(profile.status);
        //mJob.setText(formInfo.getJob(profile.questionary_job_id));
        
        value = profile.status;
        if(value!=null && value.length()>1) {
          mAbout.setText(value);
          findViewById(R.id.rowProfileAbout).setVisibility(View.VISIBLE);
          fieldCounter++;
        }
        
        if(fieldCounter < 2) {
          View btnAsk = findViewById(R.id.btnProfileAsk);
          btnAsk.setVisibility(View.VISIBLE);
          btnAsk.setOnClickListener(mOnClickListener);
        }
       
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        mProgressDialog.cancel();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void getAlbum() {
    // кнопки добавления
    mPhotoList.add(new Album()); // добавление элемента кнопки загрузки
    mEroList.add(new Album());   // новых сообщений

    // сортируем эро и не эро
    LinkedList<Album> albumList = Data.s_Profile.albums;
    for(Album album : albumList)
      if(album.ero)
        mEroList.add(album);
      else
        mPhotoList.add(album);
    
    // обновляем галереи
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
    
    if(mPhotoList.size() > Data.s_gridColumn+1) {
      mGR.setVisibility(View.VISIBLE);
      mGL.setVisibility(View.VISIBLE);
    }

    if(mEroList.size() > Data.s_gridColumn+1) {
      mEGR.setVisibility(View.VISIBLE);
      mEGL.setVisibility(View.VISIBLE);
    }
  }
  //---------------------------------------------------------------------------
  private void updateAlbum() {
    AlbumRequest albumRequest = new AlbumRequest(getApplicationContext());
    albumRequest.uid  = Data.s_Profile.uid;
    albumRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        mPhotoList.clear();
        mEroList.clear();
        
        // кнопки добавления
        mPhotoList.add(new Album()); // добавление элемента кнопки загрузки
        mEroList.add(new Album());   // новых сообщений
        
        // сортируем эро и не эро
        LinkedList<Album> albumList = Album.parse(response);
        Data.s_Profile.albums.clear();
        Data.s_Profile.albums = albumList;
        for(Album album : albumList)
          if(album.ero)
            mEroList.add(album);
          else
            mPhotoList.add(album);
        
        // обнавляем галереи
        if(mPhotoList.size()>0) {
          mListAdapter.setDataList(mPhotoList);
        }
        mListAdapter.notifyDataSetChanged();

        if(mEroList.size()>0) {
          mListEroAdapter.setDataList(mEroList);
          mEroTitle.setVisibility(View.VISIBLE);
          mEroViewGroup.setVisibility(View.VISIBLE);
        }
        mListEroAdapter.notifyDataSetChanged();
        
        if(mPhotoList.size() > Data.s_gridColumn+1) {
          mGR.setVisibility(View.VISIBLE);
          mGL.setVisibility(View.VISIBLE);
        }

        if(mEroList.size() > Data.s_gridColumn+1) {
          mEGR.setVisibility(View.VISIBLE);
          mEGL.setVisibility(View.VISIBLE);
        }
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        mProgressDialog.cancel();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void getUserAlbum(int uid) {
    AlbumRequest albumRequest = new AlbumRequest(getApplicationContext());
    albumRequest.uid  = uid;
    albumRequest.callback(new ApiHandler() {
      @Override
      public void success(ApiResponse response) {
        // отключаем прогресс
        mProgressDialog.cancel();
        
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
        
        if(mPhotoList.size() > Data.s_gridColumn+1) {
          mGR.setVisibility(View.VISIBLE);
          mGL.setVisibility(View.VISIBLE);
        }

        if(mEroList.size() > Data.s_gridColumn+1) {
          mEGR.setVisibility(View.VISIBLE);
          mEGL.setVisibility(View.VISIBLE);
        }
      }
      @Override
      public void fail(int codeError,ApiResponse response) {
        mProgressDialog.cancel();
      }
    }).exec();
  }
  //---------------------------------------------------------------------------
  private void addPhoto(boolean bEro) {
    mAddEroState = bEro;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(getString(R.string.album_add_photo_title));
    View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.profile_add_photo,null);
    view.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(mOnAddPhotoClickListener);
    view.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(mOnAddPhotoClickListener);
    builder.setView(view);
    mAddPhotoDialog = builder.create();
    mAddPhotoDialog.show();   
  }
  //---------------------------------------------------------------------------
  public void release() {
    mName=mCity=mEroTitle=mHeight=mWeight=mEducation=mCommunication=null;
    mCharacter=mAlcohol=mFitness=mMarriage=mFinances=mSmoking=null;

    mProfileButton=null;
    mEroViewGroup=null;
    
    mListView=null;
    mListEroView=null;

    if(mListAdapter!=null)
      mListAdapter.release();
    mListAdapter=null;
    
    if(mListEroAdapter!=null)
      mListEroAdapter.release();
    mListEroAdapter=null;
    
    mProgressDialog=null;
    mAddPhotoDialog=null;
    
    if(mPhotoList!=null)
      mPhotoList.clear();
    mPhotoList=null;
    
    if(mEroList!=null) mEroList.clear();
    mEroList=null;
    
    if(Data.s_PhotoAlbum!=null) Data.s_PhotoAlbum.clear();
    Data.s_PhotoAlbum=null;
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
            public void success(ApiResponse response) {
              PhotoAdd add = PhotoAdd.parse(response);
              if(!add.completed)
                return; 
              
              Album album = new Album();
              album.big   = result[0];
              album.small = result[2];
              
              /*
              if(mAddEroState) {
                mEroList.add(album);
                mListEroAdapter.notifyDataSetChanged();
              } else {
                mPhotoList.add(album);
                mListAdapter.notifyDataSetChanged();
              }
              */
              updateAlbum();
              
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
              mProgressDialog.cancel();
            }
          }).exec();
        }
      });//runOnUiThread
    }
  }
  //---------------------------------------------------------------------------
  private View.OnClickListener mOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      switch(view.getId()) {
        case R.id.btnHeader: {
          // выпилили
        } break;
        case R.id.btnProfileChat: {
          if(mChatInvoke) {
            finish();
            return;
          }
          Imager.avatarUserPreloading(getApplicationContext(),mUserAvatarUrl);
          Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
          intent.putExtra(ChatActivity.INTENT_USER_ID,mUserId);
          intent.putExtra(ChatActivity.INTENT_USER_NAME,mName.getText());
          intent.putExtra(ChatActivity.INTENT_PROFILE_INVOKE,true);
          startActivity(intent);
        } break;
        case R.id.btnProfileEdit: {
          startActivity(new Intent(getApplicationContext(),EditProfileActivity.class));
        } break;
        case R.id.btnProfileExit: {
          Data.removeSSID(getApplicationContext());
          Intent intent = new Intent(getApplicationContext(), SocialActivity.class);
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
          startActivity(intent);
          finish();
        } break;
        case R.id.btnProfileBuying: {
          startActivity(new Intent(getApplicationContext(),BuyingActivity.class));
        } break;
        case R.id.btnProfileAsk: {
          findViewById(R.id.btnProfileAsk).setVisibility(View.INVISIBLE);
          //findViewById(R.id.btnProfileAsk).setEnabled(false);
          MessageRequest message = new MessageRequest(ProfileActivity.this.getApplicationContext());
          message.message = getString(R.string.profile_msg_ask); 
          message.userid  = mUserId;
          message.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
              Toast.makeText(getApplicationContext(),getString(R.string.profile_msg_sent),Toast.LENGTH_SHORT).show();
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
              mProgressDialog.cancel();
            }
          }).exec();
        } break;
      }
    }
  };
  //---------------------------------------------------------------------------
  private View.OnClickListener mOnAddPhotoClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      switch(view.getId()) {
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
  };
  //---------------------------------------------------------------------------
  private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
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

            startActivityForResult(intent,ALBUM_ACTIVITY_REQUEST_CODE);
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
              intent = new Intent(getApplicationContext(),PhotoEroAlbumActivity.class);
            }
            intent.putExtra(PhotoEroAlbumActivity.INTENT_USER_ID,mUserId);
            intent.putExtra(PhotoEroAlbumActivity.INTENT_ALBUM_POS,position);

            startActivityForResult(intent,ALBUM_ACTIVITY_REQUEST_CODE);
          }        
        } break;
      }
    }    
  };
  //---------------------------------------------------------------------------
}

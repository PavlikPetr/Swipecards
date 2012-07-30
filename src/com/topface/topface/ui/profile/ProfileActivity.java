package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.google.android.c2dm.C2DMessaging;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Album;
import com.topface.topface.data.Confirmation;
import com.topface.topface.data.Profile;
import com.topface.topface.data.Rate;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.PhotoAddRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.frames.FrameActivity;
import com.topface.topface.ui.profile.album.PhotoAlbumActivity;
import com.topface.topface.ui.profile.album.PhotoEroAlbumActivity;
import com.topface.topface.ui.profile.gallery.HorizontalListView;
import com.topface.topface.ui.profile.gallery.PhotoEroGalleryAdapter;
import com.topface.topface.ui.profile.gallery.PhotoGalleryAdapter;
import com.topface.topface.ui.views.FrameImageView;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.Socium;
import com.topface.topface.utils.Utils;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

/* "Профиль" */
public class ProfileActivity extends FrameActivity {
    // Data
    private int mUserId;    
    private int mMutualId;
    private boolean mIsOwner;
    private boolean mAddEroState;
    private boolean mChatInvoke;
    private Button mProfileButton;
    private View mMutualButton;
    private TextView mResourcesPower;
    private TextView mResourcesMoney;
    private TextView mHeaderTitle;
    private ViewGroup mEroViewGroup;
    private FrameImageView mFramePhoto;
    private HorizontalListView mListView;
    private HorizontalListView mListEroView;
    private PhotoGalleryAdapter mListAdapter;
    private PhotoEroGalleryAdapter mListEroAdapter;
    private LinkedList<Album> mPhotoList;
    private LinkedList<Album> mEroList;
    private AlertDialog mAddPhotoDialog;
    private LockerView mLoadingLocker;
    private ScrollView mProfileGroupView;
    private ProgressDialog mProgressDialog;
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
    private ProfileRequest profileRequest;
    private UserRequest userRequest;
    private AlbumRequest albumRequest;
    // Arrows
    private ImageView mGR;
    private ImageView mGL;
    private ImageView mEGL;
    private ImageView mEGR;
    //Constants
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_MUTUAL_ID = "mutual_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_CHAT_INVOKE = "chat_invoke";
    public static final int FORM_TOP = 0;
    public static final int FORM_BOTTOM = 1;
    public static final int GALLARY_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    public static final int ALBUM_ACTIVITY_REQUEST_CODE = 101;
    public static final int EDITOR_ACTIVITY_REQUEST_CODE = 102;
    //---------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_profile);
        Debug.log(this, "+onCreate");

        //System.gc();

        // Albums
        mPhotoList = new LinkedList<Album>();
        mEroList = new LinkedList<Album>();

        //mHeaderTitle = (TextView)findViewById(R.id.tvHeaderTitle);
        mFramePhoto = (FrameImageView)findViewById(R.id.ivProfileFramePhoto);

        // Resources
        mResourcesPower = (TextView)findViewById(R.id.tvResourcesPower);
        mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
        mResourcesPower.setText("" + CacheProfile.power + "%");
        mResourcesMoney = (TextView)findViewById(R.id.tvResourcesMoney);
        mResourcesMoney.setText("" + CacheProfile.money);

        // Header profile button 
        //mProfileButton = ((Button)findViewById(R.id.btnHeader));
        //mProfileButton.setOnClickListener(mOnClickListener);

        // Progress
        mLoadingLocker = (LockerView)findViewById(R.id.llvProfileLoading);
        //mProfileGroupView = (ScrollView)findViewById(R.id.svProfileForm);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage(getString(R.string.general_dialog_loading));

        // Arrows
        mGR = (ImageView)findViewById(R.id.ivProfileArrowGL);
        mGL = (ImageView)findViewById(R.id.ivProfileArrowGR);
        mEGR = (ImageView)findViewById(R.id.ivProfileArrowEGR);
        mEGL = (ImageView)findViewById(R.id.ivProfileArrowEGL);

        { // Params
            mChatInvoke = getIntent().getBooleanExtra(INTENT_CHAT_INVOKE, false); // пришли из чата    
            mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1); // свой - чужой профиль
            mMutualId = getIntent().getIntExtra(INTENT_MUTUAL_ID, -1);
            String name = getIntent().getStringExtra(INTENT_USER_NAME); // name

            /* !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
             * if(name!=null)
             * mHeaderTitle.setText(name); // пришли из likes, rates, chat
             * else if(name==null && mUserId>0)
             * mHeaderTitle.setText(""); // пришли из tops
             * else
             * mHeaderTitle.setText(getString(R.string.profile_header_title));
             * // свой профиль */
        }

        // Buttons
        if (mUserId == -1) { // СВОЙ ПРОФИЛЬ
            mIsOwner = true;

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

        } else { // ЧУЖОЙ ПРОФИЛЬ
            // Chat button
            View btnChat = findViewById(R.id.btnProfileChat);
            btnChat.setVisibility(View.VISIBLE);
            btnChat.setOnClickListener(mOnClickListener);

            // Mutual button
            mMutualButton = findViewById(R.id.btnProfileMutual);
            mMutualButton.setOnClickListener(mOnClickListener);
        }

        // Gallary and Adapter
        mListAdapter = new PhotoGalleryAdapter(getApplicationContext(), mIsOwner);
        mListView = (HorizontalListView)findViewById(R.id.lvAlbumPreview);
        mListView.setAdapter(mListAdapter);
        mListView.setOnItemClickListener(mOnItemClickListener);
        // Ero Gallary and Adapter
        mEroTitle = (TextView)findViewById(R.id.tvEroTitle);
        mEroViewGroup = (ViewGroup)findViewById(R.id.loEroAlbum);
        mListEroAdapter = new PhotoEroGalleryAdapter(getApplicationContext(), mIsOwner);
        mListEroView = (HorizontalListView)findViewById(R.id.lvEroAlbumPreview);
        mListEroView.setAdapter(mListEroAdapter);
        mListEroView.setOnItemClickListener(mOnItemClickListener);

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
        mFinances = (TextView)findViewById(R.id.tvProfileFinances);
        mSmoking = (TextView)findViewById(R.id.tvProfileSmoking);
        //mJob = (TextView)findViewById(R.id.tvProfileJob);
        //mStatus = (TextView)findViewById(R.id.tvProfileStatus);
        mAbout = (TextView)findViewById(R.id.tvProfileAbout);

        getProfile();
    }
    //---------------------------------------------------------------------------  
    @Override
    protected void onStart() {
        super.onStart();
        if (mIsOwner) {
            mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
            mResourcesPower.setText("" + CacheProfile.power + "%");
            mResourcesMoney.setText("" + CacheProfile.money);
        }
    }
    //---------------------------------------------------------------------------  
    @Override
    protected void onStop() {
        super.onStop();
    }
    //---------------------------------------------------------------------------
    // получение фото из галереи и отправка на сервер
    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        if (requestCode == EDITOR_ACTIVITY_REQUEST_CODE/* && resultCode ==
                                                        * RESULT_OK */) {
            setOwnerProfileInfo(CacheProfile.getProfile());
        }
        if (requestCode == ALBUM_ACTIVITY_REQUEST_CODE/* && resultCode ==
                                                       * RESULT_OK */)
            if (mIsOwner)
                updateOwnerAlbum();
        if (requestCode == GALLARY_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri imageUri = data != null ? data.getData() : null;
            if (imageUri == null)
                return;
            new AsyncTaskUploader().execute(imageUri);
        }
    }
    //---------------------------------------------------------------------------
    @Override
    protected void onDestroy() {

        if (profileRequest != null)
            profileRequest.cancel();
        if (userRequest != null)
            userRequest.cancel();
        if (albumRequest != null)
            albumRequest.cancel();

        release();
        System.gc();
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }
    //---------------------------------------------------------------------------
    private void getProfile() {
        if (mIsOwner) {
            profileRequest = new ProfileRequest(getApplicationContext());
            profileRequest.part = ProfileRequest.P_ALL;
            profileRequest.callback(new ApiHandler() {
                @Override
                public void success(final ApiResponse response) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setOwnerProfileInfo(Profile.parse(response));
                            updateOwnerAlbum(); // ХАК для обновления всех фотографий
                            mLoadingLocker.setVisibility(View.GONE);
                            //mProfileGroupView.setVisibility(View.VISIBLE);
                        }
                    });
                }
                @Override
                public void fail(int codeError,ApiResponse response) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                            mLoadingLocker.setVisibility(View.GONE);
                        }
                    });
                }
            }).exec();
        } else {
            userRequest = new UserRequest(getApplicationContext());
            userRequest.uids.add(mUserId);
            userRequest.callback(new ApiHandler() {
                @Override
                public void success(final ApiResponse response) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setUserProfile(User.parse(mUserId, response));
                            mLoadingLocker.setVisibility(View.GONE);
                            //mProfileGroupView.setVisibility(View.VISIBLE);
                        }
                    });
                }
                @Override
                public void fail(int codeError,ApiResponse response) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                            mLoadingLocker.setVisibility(View.GONE);
                        }
                    });
                }
            }).exec();
        }
    }
    //---------------------------------------------------------------------------
    // свой профиль
    private void setOwnerProfileInfo(Profile profile) {
        CacheProfile.setProfile(profile);

        mUserId = CacheProfile.uid;
        if (CacheProfile.sex == 0)
            mMarriage.setText(getString(R.string.profile_marriage_female));

        Http.imageLoader(CacheProfile.avatar_big, mFramePhoto);

        setOwnerAlbum();

        mFramePhoto.mOnlineState = true;

        // основная информация
        mName.setText(CacheProfile.first_name);
        mCity.setText(CacheProfile.age + ", " + CacheProfile.city_name);
        mHeight.setText("" + CacheProfile.questionary_height);
        findViewById(R.id.rowProfileHeight).setVisibility(View.VISIBLE);
        mWeight.setText("" + CacheProfile.questionary_weight);
        findViewById(R.id.rowProfileWeight).setVisibility(View.VISIBLE);

        // анкета
        FormInfo formInfo = new FormInfo(getApplicationContext(), CacheProfile.sex);
        mEducation.setText(formInfo.getEducation(CacheProfile.questionary_education_id));
        findViewById(R.id.rowProfileEducation).setVisibility(View.VISIBLE);
        mCommunication.setText(formInfo.getCommunication(CacheProfile.questionary_communication_id));
        findViewById(R.id.rowProfileCommutability).setVisibility(View.VISIBLE);
        mCharacter.setText(formInfo.getCharacter(CacheProfile.questionary_character_id));
        findViewById(R.id.rowProfileCharacter).setVisibility(View.VISIBLE);
        mAlcohol.setText(formInfo.getAlcohol(CacheProfile.questionary_alcohol_id));
        findViewById(R.id.rowProfileAlcohol).setVisibility(View.VISIBLE);
        mFitness.setText(formInfo.getFitness(CacheProfile.questionary_fitness_id));
        findViewById(R.id.rowProfileFitness).setVisibility(View.VISIBLE);
        mMarriage.setText(formInfo.getMarriage(CacheProfile.questionary_marriage_id));
        findViewById(R.id.rowProfileMarriage).setVisibility(View.VISIBLE);
        mFinances.setText(formInfo.getFinances(CacheProfile.questionary_finances_id));
        findViewById(R.id.rowProfileFinances).setVisibility(View.VISIBLE);
        mSmoking.setText(formInfo.getSmoking(CacheProfile.questionary_smoking_id));
        findViewById(R.id.rowProfileSmoking).setVisibility(View.VISIBLE);
        //mStatus.setText(profile.status);
        //mJob.setText(formInfo.getJob(profile.questionary_job_id));
        mAbout.setText(CacheProfile.status);
        findViewById(R.id.rowProfileAbout).setVisibility(View.VISIBLE);

    }
    //---------------------------------------------------------------------------
    private void setOwnerAlbum() {
        mPhotoList.clear();
        mEroList.clear();
        mPhotoList.add(new Album()); // добавление элемента кнопки загрузки
        mEroList.add(new Album()); // новых сообщений

        // сортируем эро и не эро
        LinkedList<Album> albumList = CacheProfile.albums;
        for (Album album : albumList)
            if (album.ero)
                mEroList.add(album);
            else
                mPhotoList.add(album);

        // обновляем галереи
        if (mPhotoList.size() > 0) {
            mListAdapter.setDataList(mPhotoList);
        }
        mListAdapter.notifyDataSetChanged();

        if (mEroList.size() > 0) {
            mListEroAdapter.setDataList(mEroList);
            mEroTitle.setVisibility(View.VISIBLE);
            mEroViewGroup.setVisibility(View.VISIBLE);
        }
        mListEroAdapter.notifyDataSetChanged();

        if (mPhotoList.size() > Data.GRID_COLUMN + 1) {
            mGR.setVisibility(View.VISIBLE);
            mGL.setVisibility(View.VISIBLE);
        }

        if (mEroList.size() > Data.GRID_COLUMN + 1) {
            mEGR.setVisibility(View.VISIBLE);
            mEGL.setVisibility(View.VISIBLE);
        }
    }
    //---------------------------------------------------------------------------
    private void updateOwnerAlbum() {
        albumRequest = new AlbumRequest(getApplicationContext());
        albumRequest.uid = CacheProfile.uid;
        albumRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                mPhotoList.clear();
                mEroList.clear();
                mPhotoList.add(new Album()); // добавление элемента кнопки загрузки
                mEroList.add(new Album()); // новых сообщений

                // сортируем эро и не эро
                LinkedList<Album> albumList = Album.parse(response);
                CacheProfile.albums.clear();
                CacheProfile.albums = albumList;
                for (Album album : albumList)
                    if (album.ero)
                        mEroList.add(album);
                    else
                        mPhotoList.add(album);

                // обновляем галереи
                if (mPhotoList.size() > 0)
                    mListAdapter.setDataList(mPhotoList);
                if (mEroList.size() > 0)
                    mListEroAdapter.setDataList(mEroList);

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEroList.size() > 0) {
                            mEroTitle.setVisibility(View.VISIBLE);
                            mEroViewGroup.setVisibility(View.VISIBLE);
                        }

                        mListAdapter.notifyDataSetChanged();
                        mListEroAdapter.notifyDataSetChanged();

                        if (mPhotoList.size() > Data.GRID_COLUMN + 1) {
                            mGR.setVisibility(View.VISIBLE);
                            mGL.setVisibility(View.VISIBLE);
                        }

                        if (mEroList.size() > Data.GRID_COLUMN + 1) {
                            mEGR.setVisibility(View.VISIBLE);
                            mEGL.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mLoadingLocker.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    // чужой профиль
    private void setUserProfile(User profile) {
        Http.imageLoader(profile.getBigLink(), mFramePhoto);

        setUserAlbum();

        if (!profile.mutual && mMutualId > 0)
            mMutualButton.setVisibility(View.VISIBLE);

        //mHeaderTitle.setText(profile.first_name);  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        mUserAvatarUrl = profile.avatars_small;
        mFramePhoto.mOnlineState = profile.online;

        int fieldCounter = 0;
        // основная информация
        mName.setText(profile.first_name);
        mCity.setText(profile.age + ", " + profile.city_name);
        if (profile.questionary_height > 0) {
            mHeight.setText("" + profile.questionary_height);
            findViewById(R.id.rowProfileHeight).setVisibility(View.VISIBLE);
            fieldCounter++;
        }
        if (profile.questionary_weight > 0) {
            mWeight.setText("" + profile.questionary_weight);
            findViewById(R.id.rowProfileWeight).setVisibility(View.VISIBLE);
            fieldCounter++;
        }
        // анкета
        FormInfo formInfo = new FormInfo(ProfileActivity.this.getApplicationContext(), profile.sex);
        String value = formInfo.getEducation(profile.questionary_education_id);
        if (value != null) {
            mEducation.setText(value);
            findViewById(R.id.rowProfileEducation).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        value = formInfo.getCommunication(profile.questionary_communication_id);
        if (value != null) {
            mCommunication.setText(value);
            findViewById(R.id.rowProfileCommutability).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        value = formInfo.getCharacter(profile.questionary_character_id);
        if (value != null) {
            mCharacter.setText(value);
            findViewById(R.id.rowProfileCharacter).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        value = formInfo.getAlcohol(profile.questionary_alcohol_id);
        if (value != null) {
            mAlcohol.setText(value);
            findViewById(R.id.rowProfileAlcohol).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        value = formInfo.getFitness(profile.questionary_fitness_id);
        if (value != null) {
            mFitness.setText(value);
            findViewById(R.id.rowProfileFitness).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        value = formInfo.getMarriage(profile.questionary_marriage_id);
        if (value != null) {
            mMarriage.setText(value);
            findViewById(R.id.rowProfileMarriage).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        value = formInfo.getFinances(profile.questionary_finances_id);
        if (value != null) {
            mFinances.setText(value);
            findViewById(R.id.rowProfileFinances).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        value = formInfo.getSmoking(profile.questionary_smoking_id);
        if (value != null) {
            mSmoking.setText(value);
            findViewById(R.id.rowProfileSmoking).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        //mStatus.setText(profile.status);
        //mJob.setText(formInfo.getJob(profile.questionary_job_id));

        value = profile.status;
        if (value != null && value.length() > 1) {
            mAbout.setText(value);
            findViewById(R.id.rowProfileAbout).setVisibility(View.VISIBLE);
            fieldCounter++;
        }

        if (fieldCounter < 2) {
            View btnAsk = findViewById(R.id.btnProfileAsk);
            btnAsk.setVisibility(View.VISIBLE);
            btnAsk.setOnClickListener(mOnClickListener);
        }
    }
    //---------------------------------------------------------------------------
    private void setUserAlbum() {
        albumRequest = new AlbumRequest(getApplicationContext());
        albumRequest.uid = mUserId;
        albumRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {

                // сортируем эро и не эро
                LinkedList<Album> albumList = Album.parse(response);
                for (Album album : albumList)
                    if (album.ero)
                        mEroList.add(album);
                    else
                        mPhotoList.add(album);

                // обнавляем галереи
                if (mPhotoList.size() > 0)
                    mListAdapter.setDataList(mPhotoList);
                if (mEroList.size() > 0)
                    mListEroAdapter.setDataList(mEroList);

                post(new Runnable() {
                    @Override
                    public void run() {
                        if (mEroList.size() > 0) {
                            mEroTitle.setVisibility(View.VISIBLE);
                            mEroViewGroup.setVisibility(View.VISIBLE);
                        }

                        mListAdapter.notifyDataSetChanged();
                        mListEroAdapter.notifyDataSetChanged();

                        if (mPhotoList.size() > Data.GRID_COLUMN + 1) {
                            mGR.setVisibility(View.VISIBLE);
                            mGL.setVisibility(View.VISIBLE);
                        }

                        if (mEroList.size() > Data.GRID_COLUMN + 1) {
                            mEGR.setVisibility(View.VISIBLE);
                            mEGL.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mLoadingLocker.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void addPhoto(boolean bEro) {
        mAddEroState = bEro;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.album_add_photo_title));
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.profile_add_photo, null);
        view.findViewById(R.id.btnAddPhotoAlbum).setOnClickListener(mOnAddPhotoClickListener);
        view.findViewById(R.id.btnAddPhotoCamera).setOnClickListener(mOnAddPhotoClickListener);
        builder.setView(view);
        mAddPhotoDialog = builder.create();
        mAddPhotoDialog.show();
    }
    //---------------------------------------------------------------------------
    public void release() {
        mName = mCity = mEroTitle = mHeight = mWeight = mEducation = mCommunication = null;
        mCharacter = mAlcohol = mFitness = mMarriage = mFinances = mSmoking = null;

        mProfileButton = null;
        mEroViewGroup = null;

        mListView = null;
        mListEroView = null;

        if (mListAdapter != null)
            mListAdapter.release();
        mListAdapter = null;

        if (mListEroAdapter != null)
            mListEroAdapter.release();
        mListEroAdapter = null;

        //mProgressDialog=null;
        mAddPhotoDialog = null;

        if (mPhotoList != null)
            mPhotoList.clear();
        mPhotoList = null;

        if (mEroList != null)
            mEroList.clear();
        mEroList = null;

        if (Data.photoAlbum != null)
            Data.photoAlbum.clear();
        Data.photoAlbum = null;
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
            Socium soc = new Socium(getApplicationContext());
            return soc.uploadPhoto(uri[0]);
        }
        @Override
        protected void onPostExecute(final String[] result) {
            super.onPostExecute(result);

            if (mAddEroState) {
                // попап с выбором цены эро фотографии
                final CharSequence[] items = {getString(R.string.profile_coin_1),getString(R.string.profile_coin_2),getString(R.string.profile_coin_3)};
                new AlertDialog.Builder(ProfileActivity.this).setTitle(getString(R.string.profile_ero_price)).setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int item) {
                        sendAddRequest(result, item + 1);
                    }
                }).create().show();
            } else
                sendAddRequest(result, 0);
        }
        private void sendAddRequest(final String[] result,final int price) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    PhotoAddRequest addPhotoRequest = new PhotoAddRequest(ProfileActivity.this.getApplicationContext());
                    addPhotoRequest.big = result[0];
                    addPhotoRequest.medium = result[1];
                    addPhotoRequest.small = result[2];
                    addPhotoRequest.ero = mAddEroState;
                    if (mAddEroState)
                        addPhotoRequest.cost = price;
                    addPhotoRequest.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                            Confirmation confirm = Confirmation.parse(response);
                            if (!confirm.completed)
                                return;

                            Album album = new Album();
                            album.big = result[0];
                            album.small = result[2];

                            post(new Runnable() {
                                @Override
                                public void run() {
                                    updateOwnerAlbum();
                                    mProgressDialog.hide();
                                }
                            });
                        }
                        @Override
                        public void fail(int codeError,ApiResponse response) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                                    mProgressDialog.hide();
                                }
                            });
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
            switch (view.getId()) {
                case R.id.btnHeader: {
                    // выпилили
                }
                    break;
                case R.id.btnProfileChat: {
                    if (mChatInvoke) {
                        finish();
                        return;
                    }
//                    Http.avatarUserPreloading(mUserAvatarUrl);
                    Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
                    intent.putExtra(ChatActivity.INTENT_USER_ID, mUserId);
                    intent.putExtra(ChatActivity.INTENT_USER_URL, mUserAvatarUrl);
                    intent.putExtra(ChatActivity.INTENT_USER_NAME, mName.getText());
                    intent.putExtra(ChatActivity.INTENT_PROFILE_INVOKE, true);
                    startActivity(intent);
                }
                    break;
                case R.id.btnProfileEdit: {
                    startActivityForResult(new Intent(getApplicationContext(), EditProfileActivity.class), EDITOR_ACTIVITY_REQUEST_CODE);
                }
                    break;
                case R.id.btnProfileExit: {
                    Data.removeSSID(getApplicationContext());
                    C2DMessaging.unregister(getApplicationContext());
                    finish();
                }
                    break;
                case R.id.btnProfileMutual: {
                    mMutualButton.setVisibility(View.INVISIBLE);
                    RateRequest rateRequest = new RateRequest(getApplicationContext());
                    rateRequest.userid = mUserId;
                    rateRequest.mutualid = mMutualId;
                    rateRequest.rate = 9;
                    rateRequest.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                            Rate rate = Rate.parse(response);
                            CacheProfile.power = rate.power;
                            CacheProfile.money = rate.money;
                            CacheProfile.average_rate = rate.average;
                        }
                        @Override
                        public void fail(int codeError,ApiResponse response) {
                            //
                        }
                    }).exec();
                }
                    break;
                case R.id.btnProfileBuying: {
                    startActivity(new Intent(getApplicationContext(), BuyingActivity.class));
                }
                    break;
                case R.id.btnProfileAsk: {
                    findViewById(R.id.btnProfileAsk).setVisibility(View.INVISIBLE);
                    //findViewById(R.id.btnProfileAsk).setEnabled(false);
                    MessageRequest message = new MessageRequest(ProfileActivity.this.getApplicationContext());
                    message.message = getString(R.string.profile_msg_ask);
                    message.userid = mUserId;
                    message.callback(new ApiHandler() {
                        @Override
                        public void success(ApiResponse response) {
                        	final Confirmation confirm = Confirmation.parse(response);
                            post(new Runnable() {
                                @Override
                                public void run() {
                                	if (confirm.completed) {
                                		Toast.makeText(ProfileActivity.this, getString(R.string.profile_msg_sent), Toast.LENGTH_SHORT).show();
                                	} else {
                                		Toast.makeText(ProfileActivity.this, getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
                                	}
                                    mLoadingLocker.setVisibility(View.GONE);
                                }
                            });
                        }
                        @Override
                        public void fail(int codeError,ApiResponse response) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(ProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                                    mLoadingLocker.setVisibility(View.GONE);
                                }
                            });
                        }
                    }).exec();
                }
                    break;
            }
        }
    };
    //---------------------------------------------------------------------------
    private View.OnClickListener mOnAddPhotoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnAddPhotoAlbum: {
                    Intent intent = new Intent();
                    intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                    break;
                case R.id.btnAddPhotoCamera: {
                    Intent intent = new Intent();
                    intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.profile_add_title)), GALLARY_IMAGE_ACTIVITY_REQUEST_CODE);
                }
                    break;
            }
            if (mAddPhotoDialog != null && mAddPhotoDialog.isShowing())
                mAddPhotoDialog.cancel();
        }
    };
    //---------------------------------------------------------------------------
    private AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent,View arg1,int position,long arg3) {
            switch (parent.getId()) {
                case R.id.lvAlbumPreview: { // ALBUM
                    if (position == 0 && mIsOwner == true) // нажатие на добавление фотки в своем альбоме
                        addPhoto(false);
                    else {
                        Intent intent = new Intent(getApplicationContext(), PhotoAlbumActivity.class);
                        if (mIsOwner == true) {
                            --position;
                            Data.photoAlbum = new LinkedList<Album>(); // ммм, передумать реализацию проброса массива линков
                            Data.photoAlbum.addAll(mPhotoList);
                            Data.photoAlbum.removeFirst();
                            intent.putExtra(PhotoAlbumActivity.INTENT_OWNER, true);
                        } else {
                            Data.photoAlbum = mPhotoList;
                        }
                        intent.putExtra(PhotoAlbumActivity.INTENT_USER_ID, mUserId);
                        intent.putExtra(PhotoAlbumActivity.INTENT_ALBUM_POS, position);

                        startActivityForResult(intent, ALBUM_ACTIVITY_REQUEST_CODE);
                    }
                }
                    break;
                case R.id.lvEroAlbumPreview: { // ERO ALBUM
                    if (position == 0 && mIsOwner == true) // нажатие на добавление эро фотки в своем альбоме
                        addPhoto(true);
                    else {
                        Intent intent = null;
                        if (mIsOwner == true) {
                            --position;
                            Data.photoAlbum = new LinkedList<Album>(); // ммм, передумать реализацию проброса массива линков
                            Data.photoAlbum.addAll(mEroList);
                            Data.photoAlbum.removeFirst();
                            intent = new Intent(getApplicationContext(), PhotoAlbumActivity.class);
                            intent.putExtra(PhotoAlbumActivity.INTENT_OWNER, true);
                        } else {
                            Data.photoAlbum = mEroList;
                            intent = new Intent(getApplicationContext(), PhotoEroAlbumActivity.class);
                        }
                        intent.putExtra(PhotoEroAlbumActivity.INTENT_USER_ID, mUserId);
                        intent.putExtra(PhotoEroAlbumActivity.INTENT_ALBUM_POS, position);

                        startActivityForResult(intent, ALBUM_ACTIVITY_REQUEST_CODE);
                    }
                }
                    break;
            }
        }
    };
    //---------------------------------------------------------------------------
    // FrameActivity
    //---------------------------------------------------------------------------
    @Override
    public void clearLayout() {

    }
    //---------------------------------------------------------------------------
    @Override
    public void fillLayout() {

    }
    //---------------------------------------------------------------------------
}

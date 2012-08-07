package com.topface.topface.ui.profile;

import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.Album;
import com.topface.topface.data.User;
import com.topface.topface.requests.AlbumRequest;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.profile.album.PhotoAlbumActivity;
import com.topface.topface.ui.views.IndicatorView;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.Http;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class UserProfileActivity extends FragmentActivity {
    private int mUserId;
    private int mMutualId;
    private boolean mChatInvoke;
   
    private ImageView mUserAvatar;
    private TextView  mUserName;
    private TextView  mUserCity;
    
    private Button mUserDelight;
    private Button mUserMutual;
    private Button mUserChat;
    
    private RadioButton mUserPhoto;
    private RadioButton mUserQuestionnaire;
    private RadioButton mUserGifts;
    private RadioButton mUserActions;
    
    private ViewPager mViewPager;
    
    private View[] mDataLayouts;
    private IndicatorView mIndicatorView;
    private LockerView mLockerView;
    private GridView mGridAlbum;
    private UserGridAdapter mUserGalleryAdapter;
    
    private ListView mListQuestionnaire;
    private UserListAdapter mUserListAdapter;

    public User mDataUser;

    
    private RateController mRateController;
    
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_MUTUAL_ID = "mutual_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_CHAT_INVOKE = "chat_invoke";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_user_profile);
        
        mRateController = new RateController(this);
        

        
        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1); // свой - чужой профиль
        mMutualId = getIntent().getIntExtra(INTENT_MUTUAL_ID, -1);        
        mChatInvoke = getIntent().getBooleanExtra(INTENT_CHAT_INVOKE, false); // пришли из чата
        
        // Header Name
        String name = getIntent().getStringExtra(INTENT_USER_NAME); // name
        
        mLockerView = (LockerView)findViewById(R.id.llvProfileLoading);
        
        mUserAvatar = (ImageView)findViewById(R.id.ivUserAvatar);
        mUserName   = (TextView)findViewById(R.id.ivUserName);
        mUserCity   = (TextView)findViewById(R.id.ivUserCity);
        
        mUserDelight = (Button)findViewById(R.id.btnUserDelight);
        mUserDelight.setOnClickListener(mActionsClickListener);
        mUserMutual = (Button)findViewById(R.id.btnUserMutual);
        mUserMutual.setOnClickListener(mActionsClickListener);
        mUserChat = (Button)findViewById(R.id.btnUserChat);
        mUserChat.setOnClickListener(mActionsClickListener);
        
        mUserPhoto = (RadioButton)findViewById(R.id.btnUserPhoto);
        mUserPhoto.setOnClickListener(mRatesClickListener);
        mUserQuestionnaire = (RadioButton)findViewById(R.id.btnUserQuestionnaire);
        mUserQuestionnaire.setOnClickListener(mRatesClickListener);
        mUserGifts = (RadioButton)findViewById(R.id.btnUserGifts);
        mUserGifts.setOnClickListener(mRatesClickListener);
        mUserActions = (RadioButton)findViewById(R.id.btnUserActions);
        mUserActions.setOnClickListener(mRatesClickListener);
        
        mUserPhoto.setChecked(true);
        
        mViewPager = (ViewPager)findViewById(R.id.UserViewPager);
        mViewPager.setAdapter(new UserProfilePageAdapter(getSupportFragmentManager()));
        mViewPager.setOnPageChangeListener(mOnPageChangeListener);
            
        mIndicatorView = (IndicatorView)findViewById(R.id.viewUserIndicator);
        mIndicatorView.setIndicator(R.id.btnUserPhoto);
        ViewTreeObserver vto = mIndicatorView.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ViewTreeObserver obs = mIndicatorView.getViewTreeObserver();
                obs.removeGlobalOnLayoutListener(this);

                mIndicatorView.setButtonMeasure(R.id.btnUserPhoto, mUserPhoto.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserQuestionnaire, mUserQuestionnaire.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserGifts, mUserGifts.getMeasuredWidth());
                mIndicatorView.setButtonMeasure(R.id.btnUserActions,mUserActions.getMeasuredWidth());
                mIndicatorView.reCompute();
            }

        });
        
        
        getUserProfile();
    }

    private void getUserProfile() {
        mLockerView.setVisibility(View.VISIBLE);
        UserRequest userRequest = new UserRequest(getApplicationContext());
        userRequest.uids.add(mUserId);
        userRequest.callback(new ApiHandler() {
            @Override
            public void success(final ApiResponse response) {
                mDataUser = User.parse(mUserId, response);
                Bitmap rawBitmap = Http.bitmapLoader(mDataUser.getBigLink());
                final Bitmap avatar = Utils.getRoundedBitmap(rawBitmap, 100, 100);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLockerView.setVisibility(View.INVISIBLE);
                        mUserAvatar.setImageBitmap(avatar);
                        mUserName.setText(mDataUser.first_name + ", " + mDataUser.age);
                        mUserCity.setText(mDataUser.city_name);
                        mUserListAdapter.setUserData(mDataUser);
                        mUserListAdapter.notifyDataSetChanged();
                    }
                });
            }
            @Override
            public void fail(int codeError, ApiResponse response) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserProfileActivity.this, getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }
    
/*
        Bundle bundle = new Bundle();
        bundle.putInt("nAndroids", n);
        fragment.setArguments(bundle); 
*/
  
    View.OnClickListener mActionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnUserDelight:
                    mRateController.onRate(mUserId, 10);
                    break;
                case R.id.btnUserMutual:
                    mRateController.onRate(mUserId, 9);
                    break;
                case R.id.btnUserChat:
                    Intent intent = new Intent(UserProfileActivity.this, ChatActivity.class);
                    intent.putExtra(ChatActivity.INTENT_USER_ID,   mDataUser.uid);
                    intent.putExtra(ChatActivity.INTENT_USER_URL,  mDataUser.getSmallLink());                
                    intent.putExtra(ChatActivity.INTENT_USER_NAME, mDataUser.first_name);                
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    };
    
    View.OnClickListener mRatesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            for (View dataView : mDataLayouts)
                dataView.setVisibility(View.INVISIBLE);
            
            mIndicatorView.setIndicator(view.getId());
            
            switch (view.getId()) {
                case R.id.btnUserPhoto:
                    mIndicatorView.setIndicator(R.id.btnUserPhoto);
                    break;
                case R.id.btnUserQuestionnaire:
                    mIndicatorView.setIndicator(R.id.btnUserQuestionnaire);
                    break;
                case R.id.btnUserGifts:
                    mIndicatorView.setIndicator(R.id.btnUserGifts);
                    break;
                case R.id.btnUserActions:
                    mIndicatorView.setIndicator(R.id.btnUserActions);
                    break;
                default:
                    break;
            }
        }
    };
    


    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
        @Override
        public void onPageScrolled(int arg0,float arg1,int arg2) {
        }
        @Override
        public void onPageSelected(int arg0) {
            mIndicatorView.setIndicator(arg0);
        }
    };
}

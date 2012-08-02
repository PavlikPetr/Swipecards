package com.topface.topface.ui;

import com.topface.topface.R;
import com.topface.topface.data.User;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.UserRequest;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.IndicatorView;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.http.Http;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class UserProfileActivity extends Activity {
    private int mUserId;
    private int mMutualId;
    private boolean mChatInvoke;
    private User mDataUser;
    
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
    
    private View[] mDataLayouts;
    private IndicatorView mIndicatorView;
    private LockerView mLockerView;
    
    
    public static final String INTENT_USER_ID = "user_id";
    public static final String INTENT_MUTUAL_ID = "mutual_id";
    public static final String INTENT_USER_NAME = "user_name";
    public static final String INTENT_CHAT_INVOKE = "chat_invoke";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Debug.log(this, "+onCreate");
        setContentView(R.layout.ac_user_profile);
        
        mUserId = getIntent().getIntExtra(INTENT_USER_ID, -1); // свой - чужой профиль
        mMutualId = getIntent().getIntExtra(INTENT_MUTUAL_ID, -1);        
        mChatInvoke = getIntent().getBooleanExtra(INTENT_CHAT_INVOKE, false); // пришли из чата    
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
        
        View q = findViewById(R.id.loUserGallery);
        View w = findViewById(R.id.loUserQuestionnaire);
        mDataLayouts = new View[] {q,w};
        mUserPhoto.setChecked(true);

        Bitmap bitmap = null;
        mIndicatorView = (IndicatorView)findViewById(R.id.viewUserIndicator);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_photo);
        mIndicatorView.setButtonMeasure(R.id.btnUserPhoto, bitmap.getWidth());
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_questionnaire);
        mIndicatorView.setButtonMeasure(R.id.btnUserQuestionnaire, bitmap.getWidth());
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_gifts);
        mIndicatorView.setButtonMeasure(R.id.btnUserGifts, bitmap.getWidth());
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.user_actions);
        mIndicatorView.setButtonMeasure(R.id.btnUserActions, bitmap.getWidth());
        mIndicatorView.setIndicator(R.id.btnUserPhoto);
        
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

    @Override
    protected void onDestroy() {
        Debug.log(this, "-onDestroy");
        super.onDestroy();
    }
    
    View.OnClickListener mActionsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnUserDelight:
                    break;
                case R.id.btnUserMutual:
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
                    mDataLayouts[0].setVisibility(View.VISIBLE);
                    break;
                case R.id.btnUserQuestionnaire:
                    mDataLayouts[1].setVisibility(View.VISIBLE);
                    break;
                case R.id.btnUserGifts:
                    mDataLayouts[0].setVisibility(View.VISIBLE);
                    break;
                case R.id.btnUserActions:
                    mDataLayouts[1].setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        }
    };    
    
}

package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Confirmation;
import com.topface.topface.data.NovicePower;
import com.topface.topface.data.Rate;
import com.topface.topface.data.Search;
import com.topface.topface.data.SkipRate;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.NovicePowerRequest;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.requests.SearchRequest;
import com.topface.topface.requests.SkipRateRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.FilterActivity;
import com.topface.topface.ui.adapters.DatingAlbumAdapter;
import com.topface.topface.ui.profile.ProfileActivity;
import com.topface.topface.ui.views.DatingAlbum;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Newbie;
import com.topface.topface.utils.Utils;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class DatingActivity extends BaseFragment implements View.OnClickListener, ILocker {
    // Data
    private boolean mIsHide;
    private int mCurrentUserPos;
    private int mCurrentPhotoPrevPos;
    private View mResourcesControl;
    private TextView mResourcesPower;
    private TextView mResourcesMoney;
    private Button mLoveBtn;
    private Button mSympathyBtn;
    private Button mSkipBtn;
    private Button mProfileBtn;
    private Button mChatBtn;
    private TextView mUserInfoName;
    private TextView mUserInfoCity;
    private TextView mUserInfoStatus;
    private TextView mCounter;
    private View mDatingGroup;
    private DatingAlbum mDatingAlbum;
    private DatingAlbumAdapter mDatingAlbumAdapter;
    private Dialog mCommentDialog;
    private EditText mCommentText;
    private LinkedList<Search> mUserSearchList;
    private InputMethodManager mInputManager;
    private ProgressBar mProgressBar;
    private Newbie mNewbie;
    private ImageView mNewbieView;
    private AlphaAnimation mAlphaAnimation;
    private SharedPreferences mPreferences;
    //---------------------------------------------------------------------------
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        View view = inflater.inflate(R.layout.ac_dating, null);
        Debug.log(this, "+onCreate");

        // Data
        mUserSearchList = new LinkedList<Search>();

        // Position
        mCurrentUserPos = -1;

        // Dating controls
        mDatingGroup = view.findViewById(R.id.loDatingGroup);

        // Preferences
        mPreferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);

        // Newbie
        mNewbie = new Newbie(mPreferences);
        mNewbieView = (ImageView)view.findViewById(R.id.ivNewbie);

        // Animation
        mAlphaAnimation = new AlphaAnimation(0.0F, 1.0F);
        mAlphaAnimation.setDuration(400L);

        // Resources
        mResourcesControl = view.findViewById(R.id.loDatingResources);
        mResourcesControl.setOnClickListener(this);
        mResourcesPower = (TextView)view.findViewById(R.id.tvResourcesPower);
        mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
        mResourcesPower.setText("" + CacheProfile.power + "%");
        mResourcesMoney = (TextView)view.findViewById(R.id.tvResourcesMoney);
        mResourcesMoney.setText("" + CacheProfile.money);

        // Control Buttons
        mLoveBtn = (Button)view.findViewById(R.id.btnDatingLove);
        mLoveBtn.setOnClickListener(this);
        mSympathyBtn = (Button)view.findViewById(R.id.btnDatingSympathy);
        mSympathyBtn.setOnClickListener(this);
        mSkipBtn = (Button)view.findViewById(R.id.btnDatingSkip);
        mSkipBtn.setOnClickListener(this);
        mProfileBtn = (Button)view.findViewById(R.id.btnDatingProfile);
        mProfileBtn.setOnClickListener(this);
        mChatBtn = (Button)view.findViewById(R.id.btnDatingChat);
        mChatBtn.setOnClickListener(this);

        // User Info
        mUserInfoName = ((TextView)view.findViewById(R.id.tvDatingUserName));
        mUserInfoCity = ((TextView)view.findViewById(R.id.tvDatingUserCity));
        mUserInfoStatus = ((TextView)view.findViewById(R.id.tvDatingUserStatus));

        // Counter
        mCounter = ((TextView)view.findViewById(R.id.tvDatingCounter));

        // Keyboard
        mInputManager = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        // Progress
        mProgressBar = (ProgressBar)view.findViewById(R.id.prsDatingLoading);

        // Dating Adapter
        mDatingAlbumAdapter = new DatingAlbumAdapter(getActivity(), this);
        mDatingAlbumAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mCounter.setText((mCurrentPhotoPrevPos + 1) + "/" + mUserSearchList.get(mCurrentUserPos).avatars_big.length);
            }
            @Override
            public void onInvalidated() {
            }
        });

        // Dating Album
        mDatingAlbum = ((DatingAlbum)view.findViewById(R.id.glrDatingAlbum));
        mDatingAlbum.setAdapter(mDatingAlbumAdapter);
        mDatingAlbum.setSpacing(0);
        mDatingAlbum.setFadingEdgeLength(0);
        mDatingAlbum.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0,View arg1,int position,long arg3) {
                if (position == 1 && mCurrentPhotoPrevPos == 0) {
                    hideControls();
                } else if (position == 0 && mCurrentPhotoPrevPos > 0) {
                    showControls();
                }
                mCurrentPhotoPrevPos = position;
                mCounter.setText((mCurrentPhotoPrevPos + 1) + "/" + mUserSearchList.get(mCurrentUserPos).avatars_big.length);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        mDatingAlbum.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0,View arg1,int position,long arg3) {
                if (mIsHide)
                    showControls();
                else
                    hideControls();
            }
        });

        // Comment window
        mCommentDialog = new Dialog(getActivity());
        mCommentDialog.setTitle(R.string.chat_comment);
        mCommentDialog.setContentView(R.layout.popup_comment); //,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
        mCommentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mCommentText = (EditText)mCommentDialog.findViewById(R.id.etPopupComment);
        //mCommentDialog.getWindow().setBackgroundDrawableResource(R.drawable.popup_comment);

        //updateData(false);
        return view;
    }
    //---------------------------------------------------------------------------
//    @Override
//    protected void onActivityResult(int requestCode,int resultCode,Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == Activity.RESULT_OK && requestCode == FilterActivity.INTENT_FILTER_ACTIVITY)
//            updateData(false);
//    }
    //---------------------------------------------------------------------------
    private void updateData(final boolean isAddition) {
        if (!isAddition)
            mProgressBar.setVisibility(View.VISIBLE);
        Debug.log(this, "update");
        SharedPreferences preferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        SearchRequest searchRequest = new SearchRequest(getActivity());
        searchRequest.limit = 20;
        searchRequest.geo = preferences.getBoolean(getString(R.string.cache_profile_filter_geo), false);
        searchRequest.online = preferences.getBoolean(getString(R.string.cache_profile_filter_online), false);
        searchRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                LinkedList<Search> userList = Search.parse(response);
                if (!isAddition) {
                    Debug.log(this, "update set");
                    mUserSearchList.clear();
                    mUserSearchList.addAll(userList);
                } else {
                    Debug.log(this, "update add");
                    mUserSearchList.addAll(userList);
                }
                if (!isAddition)
                    updateUI(new Runnable() {
                        @Override
                        public void run() {
                            mProgressBar.setVisibility(View.GONE);
                            showNextUser();
                            showControls();
                        }
                    });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.general_data_error), Toast.LENGTH_SHORT).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    @Override
    public void onClick(View view) {        
        switch (view.getId()) {
            case R.id.loDatingResources: {
                startActivity(new Intent(getActivity(), BuyingActivity.class));
            }
                break;
            case R.id.btnDatingLove: {
                onRate(10);
            }
                break;
            case R.id.btnDatingSympathy: {
                onRate(9);
            }
                break;
            case R.id.btnDatingSkip: {
                skipUser();
            }
                break;
            case R.id.btnDatingProfile: {
                Intent intent = new Intent(getActivity(), ProfileActivity.class);
                intent.putExtra(ProfileActivity.INTENT_USER_ID, mUserSearchList.get(mCurrentUserPos).uid);
                intent.putExtra(ProfileActivity.INTENT_USER_NAME, mUserSearchList.get(mCurrentUserPos).first_name);
                startActivity(intent);
            }
                break;
            case R.id.btnDatingChat: {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.INTENT_USER_ID, mUserSearchList.get(mCurrentUserPos).uid);
                intent.putExtra(ChatActivity.INTENT_USER_URL, mUserSearchList.get(mCurrentUserPos).getSmallLink());                
                intent.putExtra(ChatActivity.INTENT_USER_NAME, mUserSearchList.get(mCurrentUserPos).first_name);                
                startActivity(intent);
            }
                break;
            default:
        }
    }
    //---------------------------------------------------------------------------
    // HEARTS
    public void onRate(final int rate) {
        Debug.log(this, "rate:" + rate);
        if (mCurrentUserPos > mUserSearchList.size() - 1) {
            updateData(true);
            return;
        }
        if (rate < 10) {
            sendRate(mUserSearchList.get(mCurrentUserPos).uid, rate);
            showNextUser();
            return;
        }
        if (rate == 10 && CacheProfile.money <= 0) {
            startActivity(new Intent(getActivity(), BuyingActivity.class));
            return;
        }
        // кнопка на окне комментария оценки 10 и 9
        ((Button)mCommentDialog.findViewById(R.id.btnPopupCommentSend)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = mCommentText.getText().toString();
                if (comment.equals(""))
                    return;

                int uid = mUserSearchList.get(mCurrentUserPos).uid;

                // отправка комментария к оценке
                MessageRequest messageRequest = new MessageRequest(getActivity());
                messageRequest.message = comment;
                messageRequest.userid = uid;
                messageRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
						final Confirmation confirm = Confirmation.parse(response);
                    	updateUI(new Runnable() {
							
							@Override
							public void run() {
		                    	if (!confirm.completed) {
		                    		Toast.makeText(getActivity(), getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
		                    	}
							}
						});
                    	
                        //Toast.makeText(getApplicationContext(),getString(R.string.profile_msg_sent),Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void fail(int codeError,ApiResponse response) {
                    }
                }).exec();

                // отправка оценки
                sendRate(uid, rate);
                mCommentDialog.cancel();
                mCommentText.setText("");
                // скрыть клавиатуру
                mInputManager.hideSoftInputFromWindow(mCommentText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                // подгрузка следующего
                showNextUser();
            }
        });
        mCommentDialog.show(); // окно сообщения
    }
    //---------------------------------------------------------------------------
    private void sendRate(final int userid,final int rate) {
        Debug.log(this, "rate");
        RateRequest doRate = new RateRequest(getActivity());
        doRate.userid = userid;
        doRate.rate = rate;
        doRate.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Rate rate = Rate.parse(response);
                CacheProfile.power = rate.power;
                CacheProfile.money = rate.money;
                CacheProfile.average_rate = rate.average;
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
                        mResourcesPower.setText("" + CacheProfile.power + "%");
                        mResourcesMoney.setText("" + CacheProfile.money);
                    }
                });
            }
            @Override
            public void fail(int codeError,ApiResponse response) {
                updateUI(new Runnable() {
                    @Override
                    public void run() {
                        //Toast.makeText(DatingActivity.this,getString(R.string.general_data_error),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }
    //---------------------------------------------------------------------------
    private void showNextUser() {
        if (mCurrentUserPos < mUserSearchList.size() - 1) {
            ++mCurrentUserPos;
            lockControls();
            mDatingAlbum.setSelection(0);
            mDatingAlbumAdapter.setUserData(mUserSearchList.get(mCurrentUserPos));
            mDatingAlbumAdapter.notifyDataSetChanged();
            // User Info
            mUserInfoCity.setText("" + mUserSearchList.get(mCurrentUserPos).city_name);
            mUserInfoStatus.setText("" + mUserSearchList.get(mCurrentUserPos).status);
            mUserInfoName.setText("" + mUserSearchList.get(mCurrentUserPos).first_name + ", " + mUserSearchList.get(mCurrentUserPos).age);
            if (mUserSearchList.get(mCurrentUserPos).online)
                mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.im_online), null);
            else
                mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.im_offline), null);
            mCounter.setText((mCurrentPhotoPrevPos + 1) + "/" + mUserSearchList.get(mCurrentUserPos).avatars_big.length);
        }
        if (mCurrentUserPos == mUserSearchList.size() - 1 || mUserSearchList.size() - 6 <= mCurrentUserPos)
            updateData(true);

        showNewbie(); // NEWBIE
    }
    //---------------------------------------------------------------------------
    private void skipUser() {
        SkipRateRequest skipRateRequest = new SkipRateRequest(getActivity());
        skipRateRequest.userid = 0;
        skipRateRequest.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                SkipRate skipRate = SkipRate.parse(response);
                if (skipRate.completed) {
	                CacheProfile.power = skipRate.power;
	                CacheProfile.money = skipRate.money;
	                updateUI(new Runnable() {
	                    @Override
	                    public void run() {
	                        mResourcesPower.setBackgroundResource(Utils.getBatteryResource(CacheProfile.power));
	                        mResourcesPower.setText("" + CacheProfile.power + "%");
	                        mResourcesMoney.setText("" + CacheProfile.money);
	                    }
	                });
            	} else {
            		Toast.makeText(getActivity(), getString(R.string.general_server_error), Toast.LENGTH_SHORT).show();
            	}
            }
            @Override
            public void fail(int codeError,ApiResponse response) {

            }
        }).exec();
        showNextUser();
    }
    //---------------------------------------------------------------------------
    private void showNewbie() {
        mNewbieView.setVisibility(View.INVISIBLE);

        if (mNewbie.isDatingCompleted())
            return;

        SharedPreferences.Editor editor = mPreferences.edit();

        if (mNewbie.free_energy != true && CacheProfile.isNewbie == true) {
            mNewbie.free_energy = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_FREE_ENERGY, true);
            //mNewbieView.setImageResource(R.drawable.newbie_free_energy);
            mNewbieView.setBackgroundResource(R.drawable.newbie_free_energy);
            mNewbieView.setOnClickListener(mOnNewbieClickListener);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (mNewbie.rate_it != true) {
            mNewbie.rate_it = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_RATE_IT, true);
            //mNewbieView.setImageResource(R.drawable.newbie_rate_it);
            mNewbieView.setBackgroundResource(R.drawable.newbie_rate_it);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);
            mNewbieView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNewbieView.setVisibility(View.INVISIBLE);
                }
            });

        } else if (mNewbie.buy_energy != true && CacheProfile.power <= 30) {
            mNewbie.buy_energy = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_BUY_ENERGY, true);
            //mNewbieView.setImageResource(R.drawable.newbie_buy_energy);
            mNewbieView.setBackgroundResource(R.drawable.newbie_buy_energy);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);
            mNewbieView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mNewbieView.setVisibility(View.INVISIBLE);
                }
            });
        }

        editor.commit();
    }
    //---------------------------------------------------------------------------  
    @Override
    public void lockControls() {
        mUserInfoName.setVisibility(View.INVISIBLE);
        mUserInfoCity.setVisibility(View.INVISIBLE);
        mUserInfoStatus.setVisibility(View.INVISIBLE);
        mLoveBtn.setVisibility(View.INVISIBLE);
        mSympathyBtn.setVisibility(View.INVISIBLE);
        mSkipBtn.setVisibility(View.INVISIBLE);
        mProfileBtn.setVisibility(View.INVISIBLE);
        mChatBtn.setVisibility(View.INVISIBLE);
        //mRateControl.setBlock(false);
        //mDatingGroup.setEnabled(false);
        //mDatingGroup.setFocusable(false);
        //mDatingGroup.setClickable(false);
    }
    //---------------------------------------------------------------------------
    @Override
    public void unlockControls() {
        mUserInfoName.setVisibility(View.VISIBLE);
        mUserInfoCity.setVisibility(View.VISIBLE);
        mUserInfoStatus.setVisibility(View.VISIBLE);
        mLoveBtn.setVisibility(View.VISIBLE);
        mSympathyBtn.setVisibility(View.VISIBLE);
        mSkipBtn.setVisibility(View.VISIBLE);
        mProfileBtn.setVisibility(View.VISIBLE);
        mChatBtn.setVisibility(View.VISIBLE);
        //mRateControl.setBlock(true);
        //mDatingGroup.setEnabled(true);
        //mDatingGroup.setFocusable(true);
        //mDatingGroup.setClickable(true);
    }
    //---------------------------------------------------------------------------
    @Override
    public void showControls() {
        mDatingGroup.setVisibility(View.VISIBLE);
        mIsHide = false;
    }
    //---------------------------------------------------------------------------
    @Override
    public void hideControls() {
        mDatingGroup.setVisibility(View.INVISIBLE);
        mIsHide = true;
    }
    //---------------------------------------------------------------------------
    View.OnClickListener mOnNewbieClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mNewbieView.setVisibility(View.INVISIBLE);
            mResourcesPower.setBackgroundResource(R.anim.battery);
            mResourcesPower.setText("");
            final AnimationDrawable mailAnimation = (AnimationDrawable)mResourcesPower.getBackground();
            mResourcesPower.post(new Runnable() {
                public void run() {
                    if (mailAnimation != null)
                        mailAnimation.start();
                }
            });
            NovicePowerRequest novicePowerRequest = new NovicePowerRequest(getActivity());
            novicePowerRequest.callback(new ApiHandler() {
                @Override
                public void success(ApiResponse response) {
                    NovicePower novicePower = NovicePower.parse(response);
                    CacheProfile.power = (int)(novicePower.power * 0.01);
                    updateUI(new Runnable() {
                        @Override
                        public void run() {
                            mResourcesPower.setText("+100%");
                        }
                    });
                }
                @Override
                public void fail(int codeError,ApiResponse response) {
                }
            }).exec();
        }
    };
    //---------------------------------------------------------------------------
    // Menu
    //---------------------------------------------------------------------------
//    private static final int MENU_FILTER = 0;
//    @Override
//    public boolean onCreatePanelMenu(int featureId,Menu menu) {
//        menu.add(0, MENU_FILTER, 0, getString(R.string.dating_menu_one));
//        return super.onCreatePanelMenu(featureId, menu);
//    }
//    //---------------------------------------------------------------------------
//    @Override
//    public boolean onMenuItemSelected(int featureId,MenuItem item) {
//        switch (item.getItemId()) {
//            case MENU_FILTER:
//                Intent intent = new Intent(this.getApplicationContext(), FilterActivity.class);
//                startActivityForResult(intent, FilterActivity.INTENT_FILTER_ACTIVITY);
//                break;
//        }
//        return super.onMenuItemSelected(featureId, item);
//    }
    //---------------------------------------------------------------------------
    // FrameActivity
    //---------------------------------------------------------------------------
    @Override
    public void clearLayout() {
        Debug.log(this, "DatingActivity::clearLayout");
    }
    //---------------------------------------------------------------------------
    @Override
    public void fillLayout() {
        Debug.log(this, "DatingActivity::fillLayout");
    }
    //---------------------------------------------------------------------------
    @Override
    public void release() {
    }
    //---------------------------------------------------------------------------
}

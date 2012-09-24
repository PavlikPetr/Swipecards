package com.topface.topface.ui.fragments;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.NovicePower;
import com.topface.topface.data.Search;
import com.topface.topface.data.SkipRate;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.NovicePowerRequest;
import com.topface.topface.requests.SearchRequest;
import com.topface.topface.requests.SkipRateRequest;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.profile.UserProfileActivity;
import com.topface.topface.ui.profile.edit.EditContainerActivity;
import com.topface.topface.ui.views.ILocker;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Newbie;
import com.topface.topface.utils.RateController;
import com.topface.topface.utils.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DatingFragment extends BaseFragment implements View.OnClickListener, ILocker, RateController.OnRateControllerListener {
    private int mCurrentUserPos;
    private int mCurrentPhotoPrevPos;
    private View mResourcesControl;
    private TextView mResourcesPower;
    private TextView mResourcesMoney;
    private Button mLoveBtn;
    private Button mSympathyBtn;
    private Button mSkipBtn;
    private Button mPrevBtn;
    private Button mProfileBtn;
    private Button mChatBtn;
    private TextView mUserInfoName;
    private TextView mUserInfoCity;
    private TextView mUserInfoStatus;
    private TextView mCounter;
    private TextView mDatingLovePrice;
    private View mDatingGroup;
    private ImageSwitcher mImageSwitcher;
    private LinkedList<Search> mUserSearchList;
    private ProgressBar mProgressBar;
    private Newbie mNewbie;
    private ImageView mNewbieView;
    private AlphaAnimation mAlphaAnimation;
    private SharedPreferences mPreferences;
    private RateController mRateController;
    private RelativeLayout mDatingLoveBtnLayout;
    private View mNavigationHeader;    
    private Button mSettingsButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        
        View view = inflater.inflate(R.layout.ac_dating, null);
        
        // Data
        mUserSearchList = new LinkedList<Search>();
        
        // Navigation Header
        (view.findViewById(R.id.btnNavigationHome)).setOnClickListener((NavigationActivity)getActivity());
        ((TextView) view.findViewById(R.id.tvNavigationTitle)).setText(getResources().getString(R.string.dashbrd_btn_dating));
        mNavigationHeader = view.findViewById(R.id.loNavigationBar);
        mSettingsButton = (Button) view.findViewById(R.id.btnNavigationSettingsBar);
        mSettingsButton.setVisibility(View.VISIBLE);
        mSettingsButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(getActivity().getApplicationContext(), FilterActivity.class);
//				startActivityForResult(intent,FilterActivity.INTENT_FILTER_ACTIVITY);
				
				Intent intent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
				startActivityForResult(intent,EditContainerActivity.INTENT_EDIT_FILTER);
			}
		});
        
        // Rate Controller
        mRateController = new RateController(getActivity());
        mRateController.setOnRateControllerListener(this);

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
        mPrevBtn = (Button)view.findViewById(R.id.btnDatingPrev);
        mPrevBtn.setOnClickListener(this);
        mProfileBtn = (Button)view.findViewById(R.id.btnDatingProfile);
        mProfileBtn.setOnClickListener(this);
        mChatBtn = (Button)view.findViewById(R.id.btnDatingChat);
        mChatBtn.setOnClickListener(this);

        // Dating Love Price
        mDatingLoveBtnLayout = (RelativeLayout) view.findViewById(R.id.loDatingLove);
        mDatingLovePrice = (TextView) view.findViewById(R.id.tvDatingLovePrice);     
        mDatingLovePrice.measure(0, 0);
    	int dx = mLoveBtn.getCompoundDrawables()[1].getIntrinsicWidth() - mDatingLovePrice.getMeasuredWidth()/3;
        int dy = mLoveBtn.getCompoundDrawables()[1].getIntrinsicHeight() - mDatingLovePrice.getMeasuredHeight();
        mDatingLovePrice.setPadding(dx, dy, 0, 0);
        
        // User Info
        mUserInfoName = ((TextView)view.findViewById(R.id.tvDatingUserName));
        mUserInfoCity = ((TextView)view.findViewById(R.id.tvDatingUserCity));
        mUserInfoStatus = ((TextView)view.findViewById(R.id.tvDatingUserStatus));

        // Counter
        mCounter = ((TextView)view.findViewById(R.id.tvDatingCounter));

        // Progress
        mProgressBar = (ProgressBar)view.findViewById(R.id.prsDatingLoading);

        // Dating Album
        mImageSwitcher = ((ImageSwitcher)view.findViewById(R.id.glrDatingAlbum));
        mImageSwitcher.setOnPageChangeListener(mOnPageChangeListener);
        mImageSwitcher.setOnClickListener(mOnClickListener);
        
        return view;
    }

    @Override
	public void startActivityForResult(Intent intent, int requestCode) {
		intent.putExtra(EditContainerActivity.INTENT_REQUEST_KEY, requestCode);
		super.startActivityForResult(intent, requestCode);
	}
    
    private void updateData(final boolean isAddition) {
        if (!isAddition)
        	onUpdateStart(isAddition);
            
        Debug.log(this, "update");
        SharedPreferences preferences = getActivity().getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        SearchRequest searchRequest = new SearchRequest(getActivity());
        registerRequest(searchRequest);
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
                            onUpdateSuccess(isAddition);
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
                        onUpdateFail(isAddition);
                    }
                });
            }
        }).exec();
    }

    @Override
    public void onClick(View view) {        
        switch (view.getId()) {
            case R.id.loDatingResources: {
                startActivity(new Intent(getActivity(), BuyingActivity.class));
            }
                break;
            case R.id.btnDatingLove: {
                if (mCurrentUserPos > mUserSearchList.size() - 1) {
                    updateData(true);
                    return;
                } else {
                  mRateController.onRate(mUserSearchList.get(mCurrentUserPos).uid, 10);
                }
            }
                break;
            case R.id.btnDatingSympathy: {
                if (mCurrentUserPos > mUserSearchList.size() - 1) {
                    updateData(true);
                    return;
                } else {
                  mRateController.onRate(mUserSearchList.get(mCurrentUserPos).uid, 9);
                }
            }
                break;
            case R.id.btnDatingSkip: {
                skipUser();
            }
        		break;
            case R.id.btnDatingPrev: {
            	prevUser();
            }
            
                break;
            case R.id.btnDatingProfile: {
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                intent.putExtra(UserProfileActivity.INTENT_USER_ID, mUserSearchList.get(mCurrentUserPos).uid);
                intent.putExtra(UserProfileActivity.INTENT_USER_NAME, mUserSearchList.get(mCurrentUserPos).first_name);
                startActivity(intent);
            }
                break;
            case R.id.btnDatingChat: {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.INTENT_USER_ID, mUserSearchList.get(mCurrentUserPos).uid);
                intent.putExtra(ChatActivity.INTENT_USER_URL, mUserSearchList.get(mCurrentUserPos).getSmallLink());                
                intent.putExtra(ChatActivity.INTENT_USER_NAME, mUserSearchList.get(mCurrentUserPos).first_name);
                intent.putExtra(ChatActivity.INTENT_USER_SEX, mUserSearchList.get(mCurrentUserPos).sex);
                intent.putExtra(ChatActivity.INTENT_USER_AGE, mUserSearchList.get(mCurrentUserPos).age);
                intent.putExtra(ChatActivity.INTENT_USER_CITY, mUserSearchList.get(mCurrentUserPos).city_name);
                startActivity(intent);
            }
                break;            
            default:
        }
    }
    
    private void showNextUser() {
        if (mCurrentUserPos < mUserSearchList.size() - 1) {
            ++mCurrentUserPos;
            lockControls();

            mImageSwitcher.setData(mUserSearchList.get(mCurrentUserPos).photoLinks);
            mImageSwitcher.setCurrentItem(0);

            // User Info
            mUserInfoCity.setText("" + mUserSearchList.get(mCurrentUserPos).city_name);
            mUserInfoStatus.setText("" + mUserSearchList.get(mCurrentUserPos).status);
            mUserInfoName.setText("" + mUserSearchList.get(mCurrentUserPos).first_name + ", " + mUserSearchList.get(mCurrentUserPos).age);
            if (mUserSearchList.get(mCurrentUserPos).online)
                mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.im_online), null);
            else
                mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.im_offline), null);            
            
            if (mUserSearchList.get(mCurrentUserPos).sex == Static.BOY) {
            	mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, 
            			getResources().getDrawable(R.drawable.dating_man_selector), null, null);
            } else if (mUserSearchList.get(mCurrentUserPos).sex == Static.GIRL) {
            	mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, 
            			getResources().getDrawable(R.drawable.dating_woman_selector), null, null);
            }
            
            setCounter(mCurrentPhotoPrevPos);
        }
        if (mCurrentUserPos == mUserSearchList.size() - 1 || mUserSearchList.size() - 6 <= mCurrentUserPos)
            updateData(true);

        //showNewbie(); // NEWBIE
    }

    private void skipUser() {
        SkipRateRequest skipRateRequest = new SkipRateRequest(getActivity());
        registerRequest(skipRateRequest);
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
    
    private void prevUser() {
    	if (mCurrentUserPos > 0) {
            --mCurrentUserPos;
            lockControls();
            
            mImageSwitcher.setData(mUserSearchList.get(mCurrentUserPos).photoLinks);
            mImageSwitcher.setCurrentItem(0);

            // User Info
            mUserInfoCity.setText("" + mUserSearchList.get(mCurrentUserPos).city_name);
            mUserInfoStatus.setText("" + mUserSearchList.get(mCurrentUserPos).status);
            mUserInfoName.setText("" + mUserSearchList.get(mCurrentUserPos).first_name + ", " + mUserSearchList.get(mCurrentUserPos).age);
            if (mUserSearchList.get(mCurrentUserPos).online)
                mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.im_online), null);
            else
                mUserInfoName.setCompoundDrawablesWithIntrinsicBounds(null, null, getResources().getDrawable(R.drawable.im_offline), null);            
            
            if (mUserSearchList.get(mCurrentUserPos).sex == Static.BOY) {
            	mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, 
            			getResources().getDrawable(R.drawable.dating_man_selector), null, null);
            } else if (mUserSearchList.get(mCurrentUserPos).sex == Static.GIRL) {
            	mProfileBtn.setCompoundDrawablesWithIntrinsicBounds(null, 
            			getResources().getDrawable(R.drawable.dating_woman_selector), null, null);
            }
            
            setCounter(mCurrentPhotoPrevPos);
        }        
    	showNewbie(); // NEWBIE
    }

    private void showNewbie() {
        mNewbieView.setVisibility(View.INVISIBLE);

        if (mNewbie.isDatingCompleted())
            return;

        SharedPreferences.Editor editor = mPreferences.edit();

        if (mNewbie.free_energy != true && CacheProfile.isNewbie == true) {
            mNewbie.free_energy = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_FREE_ENERGY, true);
            mNewbieView.setBackgroundResource(R.drawable.newbie_free_energy);
            mNewbieView.setOnClickListener(mOnNewbieClickListener);
            mNewbieView.setVisibility(View.VISIBLE);
            mNewbieView.startAnimation(mAlphaAnimation);

        } else if (mNewbie.rate_it != true) {
            mNewbie.rate_it = true;
            editor.putBoolean(Static.PREFERENCES_NEWBIE_DATING_RATE_IT, true);
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
    
    public void setCounter(int position) {
        mCounter.setText((position + 1) + "/" + mUserSearchList.get(mCurrentUserPos).avatars_big.length);
    }

    @Override
    public void lockControls() {
        mUserInfoName.setVisibility(View.INVISIBLE);
        mUserInfoCity.setVisibility(View.INVISIBLE);
        mUserInfoStatus.setVisibility(View.INVISIBLE);
//        mLoveBtn.setVisibility(View.INVISIBLE);
        mSympathyBtn.setVisibility(View.INVISIBLE);
        mSkipBtn.setVisibility(View.INVISIBLE);
        mPrevBtn.setVisibility(View.INVISIBLE);
        mProfileBtn.setVisibility(View.INVISIBLE);
        mChatBtn.setVisibility(View.INVISIBLE);
        mDatingLoveBtnLayout.setVisibility(View.INVISIBLE);
        mCounter.setVisibility(View.INVISIBLE);
    }

    @Override
    public void unlockControls() {
        mUserInfoName.setVisibility(View.VISIBLE);
        mUserInfoCity.setVisibility(View.VISIBLE);
        mUserInfoStatus.setVisibility(View.VISIBLE);
//        mLoveBtn.setVisibility(View.VISIBLE);
        mSympathyBtn.setVisibility(View.VISIBLE);
        mSkipBtn.setVisibility(View.VISIBLE);
        mPrevBtn.setVisibility(View.VISIBLE);
        mProfileBtn.setVisibility(View.VISIBLE);
        mChatBtn.setVisibility(View.VISIBLE);
        mDatingLoveBtnLayout.setVisibility(View.VISIBLE);
        mCounter.setVisibility(View.VISIBLE);
    }

    @Override
    public void showControls() {
        unlockControls(); // remove
        mNavigationHeader.setVisibility(View.VISIBLE);
        mDatingGroup.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideControls() {
        mDatingGroup.setVisibility(View.INVISIBLE);
        mNavigationHeader.setVisibility(View.INVISIBLE);
    }

    @Override
    public void fillLayout() {
        updateData(false);
        Debug.log(this, "DatingActivity::fillLayout");
    }
    
    @Override
    public void clearLayout() {
        Debug.log(this, "DatingActivity::clearLayout");
    }

    @Override
    public void successRate() {
        showNextUser();
    }

	@Override
	protected void onUpdateStart(boolean isPushUpdating) {
		if (!isPushUpdating) {
			mProgressBar.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onUpdateSuccess(boolean isPushUpdating) {
		if (!isPushUpdating) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onUpdateFail(boolean isPushUpdating) {
		if (!isPushUpdating) {
			mProgressBar.setVisibility(View.GONE);
		}
	}

	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == Activity.RESULT_OK && requestCode == EditContainerActivity.INTENT_EDIT_FILTER) {
	    	hideControls();
	    	fillLayout();	        
	    } else {
	    	super.onActivityResult(requestCode, resultCode, data);
	    }
	}
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        private boolean mIsHide;
        @Override
        public void onClick(View v) {
            if (mIsHide) {
              showControls();
            } else {
              hideControls();
            }
            mIsHide = !mIsHide;
        }
    };
	
    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            if (position == 1 && mCurrentPhotoPrevPos == 0) {
                hideControls();
            } else if (position == 0 && mCurrentPhotoPrevPos > 0) {
                showControls();
            }
            mCurrentPhotoPrevPos = position;
            setCounter(mCurrentPhotoPrevPos);
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    };
    
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
            registerRequest(novicePowerRequest);
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
	
}

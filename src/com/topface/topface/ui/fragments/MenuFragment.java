package com.topface.topface.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.ServicesTextView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.CountersManager;

public class MenuFragment extends Fragment implements View.OnClickListener {
    private View mRootLayout;

    private Button[] mButtons;

    private TextView mTvNotifyLikes;
    private TextView mTvNotifyMutual;
    private TextView mTvNotifyDialogs;
    private TextView mTvNotifyVisitors;

    private FragmentMenuListener mFragmentMenuListener;
    private Button mDefaultMenuItem;
    BroadcastReceiver mBroadcastReceiver;
    private ImageViewRemote mMenuAvatar;
    private BroadcastReceiver mProfileUpdateReceiver;
    private ServicesTextView coins;
    private ServicesTextView likes;
    private LinearLayout mContainer;
    private ImageView rocket;
    private ImageView notEnoughData;
    private TextView mTvNotifyFans;

    public interface FragmentMenuListener {
        public void onMenuClick(int buttonId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfileUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(mMenuAvatar != null) {
                    mMenuAvatar.setPhoto(CacheProfile.photo);
                }
            }
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mProfileUpdateReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mBroadcastReceiver, new IntentFilter(CountersManager.UPDATE_COUNTERS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mProfileUpdateReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        if (mRootLayout != null)
            return mRootLayout;

        mRootLayout = inflater.inflate(R.layout.fragment_menu, null);


        // Buttons
        final Button btnProfile = (Button) mRootLayout.findViewById(R.id.btnFragmentProfile);
        btnProfile.setOnClickListener(this);
        mMenuAvatar = (ImageViewRemote) mRootLayout.findViewById(R.id.ivMenuAvatar);
        mMenuAvatar.setPhoto(CacheProfile.photo);
        mMenuAvatar.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                btnProfile.performClick();
            }
        });

        mDefaultMenuItem = (Button) mRootLayout.findViewById(R.id.btnFragmentDating);
        //Это сделано для того, чтобы правильно ресайзнуть счетчики монет и симпатий
        mContainer = (LinearLayout) mRootLayout.findViewById(R.id.countersLayout);
        coins = (ServicesTextView) mRootLayout.findViewById(R.id.menuCurCoins);
        likes = (ServicesTextView) mRootLayout.findViewById(R.id.menuCurLikes);
        coins.setOnMeasureListener(listener);
        likes.setOnMeasureListener(listener);


        notEnoughData = (ImageView) mRootLayout.findViewById(R.id.noData);
        rocket = (ImageView) mRootLayout.findViewById(R.id.rocket);


        Button buyButton = (Button) mRootLayout.findViewById(R.id.menuBuyBtn);
        buyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(ContainerActivity.getNewIntent(ContainerActivity.INTENT_BUYING_FRAGMENT));
            }
        });

        mButtons = new Button[]{
                btnProfile,
                mDefaultMenuItem,
                (Button) mRootLayout.findViewById(R.id.btnFragmentLikes),
                (Button) mRootLayout.findViewById(R.id.btnFragmentMutual),
                (Button) mRootLayout.findViewById(R.id.btnFragmentDialogs),
                (Button) mRootLayout.findViewById(R.id.btnFragmentVisitors),
                (Button) mRootLayout.findViewById(R.id.btnFragmentBookmarks),
                (Button) mRootLayout.findViewById(R.id.btnFragmentFans)
        };

        for (Button btn : mButtons) {
            btn.setOnClickListener(this);
        }

        // Notifications
        mTvNotifyLikes = (TextView) mRootLayout.findViewById(R.id.tvNotifyLikes);
        mTvNotifyMutual = (TextView) mRootLayout.findViewById(R.id.tvNotifyMutual);
        mTvNotifyDialogs = (TextView) mRootLayout.findViewById(R.id.tvNotifyDialogs);
        mTvNotifyFans = (TextView) mRootLayout.findViewById(R.id.tvNotifyFans);
        mTvNotifyVisitors = (TextView) mRootLayout.findViewById(R.id.tvNotifyVisitors);

        hide();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                refreshNotifications();
            }
        };

        return mRootLayout;
    }



    @Override
    public void onClick(View view) {
        unselectAllButtons();

        view.setSelected(true);

        if (mFragmentMenuListener != null)
            mFragmentMenuListener.onMenuClick(view.getId());
    }

    public void unselectAllButtons() {
        for (Button btn : mButtons)
            btn.setSelected(false);
    }

    public void setOnMenuListener(FragmentMenuListener onFragmentMenuListener) {
        mFragmentMenuListener = onFragmentMenuListener;
    }

    public void refreshNotifications() {
        if (CacheProfile.unread_likes > 0) {
            mTvNotifyLikes.setText(" " + CacheProfile.unread_likes + " ");
            mTvNotifyLikes.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyLikes.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_mutual > 0) {
            mTvNotifyMutual.setText(" " + CacheProfile.unread_mutual + " ");
            mTvNotifyMutual.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyMutual.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_messages > 0) {
            mTvNotifyDialogs.setText(" " + CacheProfile.unread_messages + " ");
            mTvNotifyDialogs.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyDialogs.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_visitors > 0) {
            mTvNotifyVisitors.setText(" " + CacheProfile.unread_visitors + " ");
            mTvNotifyVisitors.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyVisitors.setVisibility(View.INVISIBLE);
        }

        if (CacheProfile.unread_fans > 0) {
            mTvNotifyFans.setText(" " + CacheProfile.unread_fans + " ");
            mTvNotifyFans.setVisibility(View.VISIBLE);
        } else {
            mTvNotifyFans.setVisibility(View.INVISIBLE);
        }
    }

    public void show() {
        mRootLayout.setVisibility(View.VISIBLE);
        if (CacheProfile.premium) {
            rocket.setVisibility(View.VISIBLE);
        } else {
            rocket.setVisibility(View.GONE);
        }
        if (!CacheProfile.checkIsFillData()) {
            notEnoughData.setVisibility(View.VISIBLE);
            rocket.setVisibility(View.GONE);
        } else {
            notEnoughData.setVisibility(View.GONE);
        }
        coins.setText(Integer.toString(CacheProfile.money));
        likes.setText(Integer.toString(CacheProfile.likes));
    }

    public void hide() {
        mRootLayout.setVisibility(View.INVISIBLE);
    }

    public void setClickable(boolean clickable) {
        for (Button btn : mButtons) {
            btn.setClickable(clickable);
        }
    }

    public void selectDefaultMenu() {
        mDefaultMenuItem.setSelected(true);
    }

    public OnClickListener getProfileButtonOnClickListener() {
        final Button btnProfile = (Button) mRootLayout.findViewById(R.id.btnFragmentProfile);
        return new OnClickListener() {

            @Override
            public void onClick(View v) {
                btnProfile.performClick();
            }
        };
    }

    ServicesTextView.OnMeasureListener listener = new ServicesTextView.OnMeasureListener() {
        @Override
        public void onMeasure(int width, int height) {
            if(coins.getWidth() > 0 && likes.getWidth() > 0) {
                DisplayMetrics displaymetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
                int newWidth = (int)(displaymetrics.widthPixels * 0.7);
                int margin = (int)(10 * displaymetrics.density);
                int preCountedWidth = coins.getWidth() + likes.getWidth() + mContainer.getPaddingLeft() + mContainer.getPaddingRight() + margin;
                if (preCountedWidth >= newWidth) {
                    newWidth = preCountedWidth;
                    if(newWidth > displaymetrics.widthPixels) {
                        FragmentSwitchController.EXPANDING_PERCENT = 5;
                    } else {
                        FragmentSwitchController.EXPANDING_PERCENT =(int)( 100 * (1 - (double)newWidth/(double)displaymetrics.widthPixels));
                    }
                }

                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mContainer.getLayoutParams();
                params.width = newWidth;
                mContainer.setLayoutParams(params);
            }
        }
    };
}





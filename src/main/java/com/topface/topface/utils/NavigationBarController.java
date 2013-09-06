package com.topface.topface.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;

public class NavigationBarController {

    public static final int MAX_COUNTER = 999;
    private ViewGroup mNavigationHeader;
    private TextView mDialogsNotificator;
    private TextView mLikesNotificator;
    private TextView mMutualNotificator;

    public NavigationBarController(ViewGroup navigationHeader) {
        mNavigationHeader = navigationHeader;
        initElements();
    }

    private void initElements() {
        mDialogsNotificator = (TextView) mNavigationHeader.findViewById(R.id.tvNotificationDialogs);
        mMutualNotificator = (TextView) mNavigationHeader.findViewById(R.id.tvNotificationMutual);
        mLikesNotificator = (TextView) mNavigationHeader.findViewById(R.id.tvNotificationLikes);
    }

    public void refreshNotificators() {
        if (CacheProfile.unread_messages > 0) {

            mDialogsNotificator.setText(Integer.toString(CacheProfile.unread_messages > MAX_COUNTER ? MAX_COUNTER : CacheProfile.unread_messages));
            mDialogsNotificator.setVisibility(View.VISIBLE);
            mMutualNotificator.setVisibility(View.INVISIBLE);
            mLikesNotificator.setVisibility(View.INVISIBLE);
        } else if (CacheProfile.unread_mutual > 0) {
            mMutualNotificator.setText(Integer.toString(CacheProfile.unread_mutual > MAX_COUNTER ? MAX_COUNTER : CacheProfile.unread_mutual));
            mMutualNotificator.setVisibility(View.VISIBLE);
            mLikesNotificator.setVisibility(View.INVISIBLE);
            mDialogsNotificator.setVisibility(View.INVISIBLE);
        } else {
            mDialogsNotificator.setVisibility(View.INVISIBLE);
            mMutualNotificator.setVisibility(View.INVISIBLE);
            mLikesNotificator.setVisibility(View.INVISIBLE);
        }
//		else if(CacheProfile.unread_likes > 0) {
//			mLikesNotificator.setText(Integer.toString(CacheProfile.unread_likes));
//			mLikesNotificator.setVisibility(View.VISIBLE);
//			mDialogsNotificator.setVisibility(View.INVISIBLE);	
//			mMutualNotificator.setVisibility(View.INVISIBLE);			
//		}				
    }


}

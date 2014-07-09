package com.topface.topface.utils.controllers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.dialogs.PopularUserDialog;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.utils.CacheProfile;

/**
 * This controller blocks messages from popular users.
 */
public class PopularUserChatController {

    public static final int NO_BLOCK = -1;
    public static final int FIRST_STAGE = 35;
    public static final int SECOND_STAGE = 36;

    private int mStage = NO_BLOCK;

    private View mPopularChatBlocker;
    private PopularUserDialog mPopularMessageBlocker;
    private String mBlockText;
    private String mDialogTitle;
    private ChatFragment mChatFragment;
    private ViewGroup mLockScreen;

    public PopularUserChatController(ChatFragment chatFragment, ViewGroup lockScreen) {
        mChatFragment = chatFragment;
        mLockScreen = lockScreen;
    }

    public void setTexts(String dialogTitle, String blockText) {
        mBlockText = blockText;
        mDialogTitle = dialogTitle;
    }

    public boolean isAccessAllowed() {
        return CacheProfile.premium || mBlockText == null || !mBlockText.equals("");
    }

    public boolean checkChatBlock(History message) {
        return (mStage = message.type) == FIRST_STAGE;
    }

    public boolean checkMessageBlock(History message) {
        return (mStage = message.type) == SECOND_STAGE;
    }

    public boolean block(History message) {
        if (!isAccessAllowed()) {
            if (checkChatBlock(message)) {
                blockChat();
                return true;
            } else if (checkMessageBlock(message)) {
                initBlockDialog();
                return false;
            }
        } else if (mPopularChatBlocker != null && mPopularChatBlocker.getVisibility() == View.VISIBLE) {
            mPopularChatBlocker.setVisibility(View.GONE);
            mLockScreen.setVisibility(View.GONE);
        }
        return false;
    }

    public void blockChat() {
        for (int i = 0; i < mLockScreen.getChildCount(); i++) {
            View v = mLockScreen.getChildAt(i);
            if (v != mPopularChatBlocker) {
                mLockScreen.getChildAt(i).setVisibility(View.GONE);
            }
        }
        if (mPopularChatBlocker == null) {
            ViewStub stub = (ViewStub) mLockScreen.findViewById(R.id.famousBlockerStub);
            mPopularChatBlocker = stub.inflate();
            TextView lockText = (TextView) mPopularChatBlocker.findViewById(R.id.popular_user_lock_text);
            lockText.setText(mBlockText);
            mPopularChatBlocker.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.btnBuyVip:
                            EasyTracker.getTracker().sendEvent("Chat", "BuyVipStatus", "", 1L);
                            Intent intent = PurchasesActivity.createVipBuyIntent(null, "PopularUserChatBlock");
                            mChatFragment.startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                            break;
                    }
                }
            });
            mLockScreen.requestLayout();
            mLockScreen.invalidate();
        }
        mLockScreen.setVisibility(View.VISIBLE);
    }

    public void setState(int state) {
        mStage = state;
    }

    public void initBlockDialog() {
        if (mPopularMessageBlocker == null) {
            mPopularMessageBlocker = new PopularUserDialog(mDialogTitle, mBlockText);
        }
    }

    public boolean showBlockDialog() {
        if (mStage != NO_BLOCK) {
            if (mPopularMessageBlocker == null) {
                initBlockDialog();
            }
            Fragment dialog = mChatFragment.getFragmentManager().findFragmentByTag("POPULAR_USER_DIALOG");
            if (dialog == null || !dialog.isAdded()) {
                mPopularMessageBlocker.show(mChatFragment.getFragmentManager(), "POPULAR_USER_DIALOG");
            }
            return true;
        }
        return false;
    }

    public boolean isDialogOpened() {
        return mPopularMessageBlocker != null && mPopularMessageBlocker.isOpened();
    }

    public boolean isChatLocked() {
        return mStage == FIRST_STAGE;
    }

    public void reset() {
        mStage = NO_BLOCK;
    }

}

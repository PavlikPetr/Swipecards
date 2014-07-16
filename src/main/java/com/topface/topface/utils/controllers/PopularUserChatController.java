package com.topface.topface.utils.controllers;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.dialogs.PopularUserDialog;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;

import java.lang.ref.WeakReference;

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
    private WeakReference<ViewGroup> mLockScreenRef;
    private boolean mOff;

    public PopularUserChatController(ChatFragment chatFragment, ViewGroup lockScreen) {
        mChatFragment = chatFragment;
        mLockScreenRef = new WeakReference<ViewGroup>(lockScreen);
    }

    public void setTexts(String dialogTitle, String blockText) {
        mBlockText = blockText;
        mDialogTitle = dialogTitle;
    }

    public void setLockScreen(ViewGroup lockScreen) {
        mLockScreenRef = new WeakReference<ViewGroup>(lockScreen);
        if (isChatLocked()) {
            blockChat();
        }
    }

    public boolean isAccessAllowed() {
        return CacheProfile.premium || mBlockText == null || mBlockText.equals("") || mOff;
    }

    public boolean checkChatBlock(History message) {
        return message.type == FIRST_STAGE;
    }

    public boolean checkMessageBlock(History message) {
        return message.type == SECOND_STAGE;
    }

    public boolean block(History message) {
        if (!isAccessAllowed()) {
            if (checkChatBlock(message)) {
                mStage = FIRST_STAGE;
                blockChat();
                return true;
            } else if (checkMessageBlock(message)) {
                mStage = SECOND_STAGE;
                initBlockDialog();
                return false;
            }
        }
        return false;
    }

    public void unlockChat() {
        if (mPopularChatBlocker != null && mPopularChatBlocker.getVisibility() == View.VISIBLE) {
            mPopularChatBlocker.setVisibility(View.GONE);
            ViewGroup lockScreen = mLockScreenRef.get();
            if (lockScreen != null) {
                lockScreen.setVisibility(View.GONE);
            }
        }
    }

    public void blockChat() {
        if (isAccessAllowed()) {
            return;
        }
        ViewGroup lockScreen = mLockScreenRef.get();
        for (int i = 0; i < lockScreen.getChildCount(); i++) {
            lockScreen.getChildAt(i).setVisibility(View.GONE);
        }
        ViewStub stub = (ViewStub) lockScreen.findViewById(R.id.famousBlockerStub);
        if (stub != null) {
            mPopularChatBlocker = stub.inflate();
            TextView lockText = (TextView) mPopularChatBlocker.findViewById(R.id.popular_user_lock_text);
            lockText.setText(mBlockText);
            mPopularChatBlocker.findViewById(R.id.btnBuyVip).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.btnBuyVip:
                            EasyTracker.sendEvent("Chat", "BuyVipStatus", "", 1L);
                            Intent intent = PurchasesActivity.createVipBuyIntent(null, "PopularUserChatBlock");
                            mChatFragment.startActivityForResult(intent, PurchasesActivity.INTENT_BUY_VIP);
                            break;
                    }
                }
            });
            lockScreen.requestLayout();
            lockScreen.invalidate();
        }
        mPopularChatBlocker.setVisibility(View.VISIBLE);
        lockScreen.setVisibility(View.VISIBLE);
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
        if (!isAccessAllowed() && mStage != NO_BLOCK) {
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
        mDialogTitle = null;
        mBlockText = null;
        mOff = true;
    }

}

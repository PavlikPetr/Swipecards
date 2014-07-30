package com.topface.topface.utils.controllers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
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
import com.topface.topface.utils.CountersManager;
import com.topface.topface.utils.EasyTracker;

import java.lang.ref.WeakReference;

/**
 * This controller blocks messages from popular users.
 */
public class PopularUserChatController extends BroadcastReceiver {

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

    public static class SavedState implements Parcelable {

        public int stage;
        public String dialogTitle;
        public String blockText;
        public boolean off;

        public static final Parcelable.Creator<SavedState> CREATOR
                = new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };

        private SavedState() {
        }

        private SavedState(Parcel in) {
            stage = in.readInt();
            dialogTitle = in.readString();
            blockText = in.readString();
            off = in.readByte() == 1;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(stage);
            dest.writeString(dialogTitle);
            dest.writeString(blockText);
            dest.writeByte((byte) (off ? 1 : 0));
        }
    }

    public PopularUserChatController(ChatFragment chatFragment, ViewGroup lockScreen) {
        mChatFragment = chatFragment;
        mLockScreenRef = new WeakReference<>(lockScreen);
    }

    public void setTexts(String dialogTitle, String blockText) {
        mBlockText = blockText;
        mDialogTitle = dialogTitle;
    }

    public void setLockScreen(ViewGroup lockScreen) {
        mLockScreenRef = new WeakReference<>(lockScreen);
        if (isChatLocked()) {
            blockChat();
        }
    }

    public SavedState getSavedState() {
        SavedState ss = new SavedState();
        ss.stage = mStage;
        ss.dialogTitle = mDialogTitle;
        ss.blockText = mBlockText;
        ss.off = mOff;
        return ss;
    }

    public boolean isAccessAllowed() {
        return CacheProfile.premium || TextUtils.isEmpty(mBlockText) || mOff;
    }

    public boolean checkChatBlock(History message) {
        return message.type == FIRST_STAGE;
    }

    public boolean checkMessageBlock(History message) {
        return message.type == SECOND_STAGE;
    }

    public int block(History message) {
        mStage = NO_BLOCK;
        if (!isAccessAllowed()) {
            if (checkChatBlock(message)) {
                mStage = FIRST_STAGE;
                blockChat();
            } else if (checkMessageBlock(message)) {
                mStage = SECOND_STAGE;
                initBlockDialog();
            }
        }
        return mStage;
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
                            EasyTracker.sendEvent(mChatFragment.getTrackName(), "BuyVipStatus", "", 1L);
                            Intent intent = PurchasesActivity.createVipBuyIntent(null, "PopularUserChatBlock");
                            mChatFragment.startActivity(intent);
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

    public void setState(SavedState ss) {
        mStage = ss.stage;
        mDialogTitle = ss.dialogTitle;
        mBlockText = ss.blockText;
        mOff = ss.off;
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
        return !isAccessAllowed() && mStage == FIRST_STAGE;
    }

    public boolean isResponseLocked() {
        return !isAccessAllowed() && mStage == SECOND_STAGE;
    }

    public void reset() {
        mStage = NO_BLOCK;
        mDialogTitle = null;
        mBlockText = null;
        mOff = true;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getBooleanExtra(CountersManager.VIP_STATUS_EXTRA, false)) {
            if (isChatLocked()) {
                unlockChat();
            }
            reset();
        }
    }
}

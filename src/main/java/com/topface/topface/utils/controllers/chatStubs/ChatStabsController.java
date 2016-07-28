package com.topface.topface.utils.controllers.chatStubs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.data.HistoryListData;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.dialogs.PopularUserDialog;
import com.topface.topface.ui.views.RetryViewCreator;
import com.topface.topface.utils.IFragmentDelegate;
import com.topface.topface.utils.ListUtils;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ppavlik on 22.07.16.
 * keep all kinds of plugs chat and start it from here too
 */

public class ChatStabsController {

    public static final int NO_BLOCK = -1;
    public static final int SHOW_RETRY = -2;
    public static final int MUTUAL_SYMPATHY = 7;
    public static final int LOCK_CHAT = 35;
    public static final int LOCK_MESSAGE_SEND = 36;
    private static final String POPULAR_USER_DIALOG = "POPULAR_USER_DIALOG";
    private static final String LOCK_TYPE = "lock_type";
    private static final String PHOTO = "photo";
    private static final String HISTORY = "history";

    private int mLockType;
    private PopularUserStub mPopularUserStub;
    private MutualSympathyStub mMutualSympathyStub;
    private ViewStub mViewStub;
    private IFragmentDelegate mIFragmentDelegate;
    private History mMessage;
    private Photo mPhoto;
    private PopularUserDialog mPopularMessageBlocker;
    private View mRetryView;

    public ChatStabsController(@NotNull ViewStub stub, IFragmentDelegate delegate) {
        mViewStub = stub;
        mIFragmentDelegate = delegate;
    }

    public int getCurrentLockType() {
        return mLockType;
    }

    @SuppressWarnings("ConstantConditions")
    public void checkMessage(@NotNull History history) {
        if (history != null) {
            switch (mLockType) {
                case MUTUAL_SYMPATHY:
                    if (history.type == MUTUAL_SYMPATHY) {
                        mMessage = history;
                    } else {
                        mLockType = NO_BLOCK;
                    }
                    break;
                case LOCK_CHAT:
                case LOCK_MESSAGE_SEND:
                    if (history.type == mLockType) {
                        mMessage = history;
                    }
                    break;
            }
        }

    }

    private boolean isAccessAllowed() {
        return App.get().getProfile().premium;
    }

    public int getLockType(HistoryListData allHistory) {
        mLockType = NO_BLOCK;
        boolean isSympathy = false;
        if (allHistory != null && ListUtils.isNotEmpty(allHistory.items)) {
            isSympathy = true;
            for (History item : allHistory.items) {
                switch (item.type) {
                    case LOCK_CHAT:
                        mLockType = LOCK_CHAT;
                        isSympathy = false;
                        break;
                    case LOCK_MESSAGE_SEND:
                        mLockType = LOCK_MESSAGE_SEND;
                        isSympathy = false;
                        break;
                    case MUTUAL_SYMPATHY:
                        // просто ничего не делаем, пропускаем
                        break;
                    default:
                        isSympathy = false;
                        break;
                }
                mMessage = item;
                if (mLockType != NO_BLOCK) {
                    break;
                }
            }
        }
        mLockType = mLockType == NO_BLOCK && isSympathy ? MUTUAL_SYMPATHY : mLockType;
        return mLockType;
    }

    public void setPhoto(Photo photo) {
        mPhoto = photo;
    }

    public void block() {
        if (!isAccessAllowed()) {
            switch (mLockType) {
                case MUTUAL_SYMPATHY:
                    showMutualSympathyStub();
                    break;
                case LOCK_CHAT:
                    showPopularUserLock();
                    break;
            }
        }
    }

    public void showRetryView(View.OnClickListener onClick) {
        if (mViewStub != null) {
            mLockType = SHOW_RETRY;
            mRetryView = new RetryViewCreator
                    .Builder(mIFragmentDelegate.getActivity().getApplicationContext(), onClick)
                    .messageFontColor(R.color.text_color_gray).noShadow().build().getView();
            addView(mRetryView);
            setViewStubVisibility(false);
        }
    }

    public void dismissRetryView() {
        if (mLockType == SHOW_RETRY) {
            removeView(mRetryView);
        }
    }

    private void showPopularUserLock() {
        if (mPopularUserStub == null) {
            mPopularUserStub = new PopularUserStub(mViewStub, mMessage, mPhoto, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIFragmentDelegate != null) {
                        mIFragmentDelegate.getActivity()
                                .startActivity(PurchasesActivity.createVipBuyIntent(null, "PopularUserChatBlock"));
                    }
                }
            });
        } else {
            mPopularUserStub.updateData(mMessage, mPhoto);
        }
        setViewStubVisibility(true);
    }

    private void showMutualSympathyStub() {
        if (mMutualSympathyStub == null) {
            mMutualSympathyStub = new MutualSympathyStub(mViewStub, mMessage, mPhoto);
        } else {
            mMutualSympathyStub.updateData(mMessage, mPhoto);
        }
        setViewStubVisibility(true);
    }

    public void release() {
        mViewStub = null;
        if (mPopularUserStub != null) {
            mPopularUserStub.release();
            mPopularUserStub = null;
        }
        if (mMutualSympathyStub != null) {
            mMutualSympathyStub.release();
            mMutualSympathyStub = null;
        }
        if (mPopularMessageBlocker != null) {
            mPopularMessageBlocker.release();
            mPopularMessageBlocker = null;
        }
    }

    private void setViewStubVisibility(boolean visibility) {
        if (mViewStub != null) {
            mViewStub.setVisibility(visibility ? View.VISIBLE : View.GONE);
        }
    }

    private boolean removeView(View view) {
        if (mViewStub != null) {
            ViewParent viewParent = mViewStub.getParent();
            if (viewParent != null && viewParent instanceof ViewGroup) {
                final ViewGroup parent = (ViewGroup) viewParent;
                parent.removeView(view);
                return true;
            }
        }
        return false;
    }

    private boolean addView(View view) {
        if (mViewStub != null) {
            ViewParent viewParent = mViewStub.getParent();
            if (viewParent != null && viewParent instanceof ViewGroup) {
                final ViewGroup parent = (ViewGroup) viewParent;
                final ViewGroup.LayoutParams layoutParams = mViewStub.getLayoutParams();
                if (layoutParams != null) {
                    parent.addView(view, parent.getChildCount() - 1, layoutParams);
                } else {
                    parent.addView(view, parent.getChildCount() - 1);
                }
                return true;
            }
        }
        return false;
    }

    private boolean showPopularUserDialog() {
        if (mPopularMessageBlocker == null) {
            mPopularMessageBlocker = PopularUserDialog.newInstance(mMessage.dialogTitle, mMessage.blockText);
        }
        if (mIFragmentDelegate != null) {
            FragmentManager fragmentManager = mIFragmentDelegate.getActivity().getSupportFragmentManager();
            Fragment dialog = fragmentManager.findFragmentByTag(POPULAR_USER_DIALOG);
            if (dialog == null || !dialog.isAdded()) {
                mPopularMessageBlocker.show(fragmentManager, POPULAR_USER_DIALOG);
            }
            return true;
        }
        return false;
    }

    public boolean isMessageSendAvailable() {
        switch (mLockType) {
            case LOCK_MESSAGE_SEND:
            case LOCK_CHAT:
                showPopularUserDialog();
                return false;
            case MUTUAL_SYMPATHY:
                unlock();
                break;
        }
        return true;
    }

    public boolean isGiftSendAvailable() {
        switch (mLockType) {
            case LOCK_CHAT:
            case LOCK_MESSAGE_SEND:
                showPopularUserDialog();
                return false;
        }
        return true;
    }

    @SuppressLint("SwitchIntDef")
    public void giftSent() {
        switch (mLockType) {
            case MUTUAL_SYMPATHY:
                unlock();
                break;
        }
    }

    public void unlock() {
        setViewStubVisibility(false);
    }

    public boolean isChatLocked() {
        return !isAccessAllowed() && mLockType == LOCK_CHAT;
    }

    public boolean isResponseLocked() {
        return !isAccessAllowed() && mLockType == LOCK_MESSAGE_SEND;
    }

    @SuppressWarnings("ConstantConditions")
    public void onSaveInstanceState(@NotNull Bundle outState) {
        if (outState != null) {
            outState.putInt(LOCK_TYPE, mLockType);
            outState.putParcelable(PHOTO, mPhoto);
            outState.putParcelable(HISTORY, mMessage);
        }
    }

    @SuppressWarnings("ConstantConditions")
    public void onRestoreInstanceState(@NotNull Bundle outState) {
        if (outState != null) {
            mPhoto = outState.getParcelable(PHOTO);
            mMessage = outState.getParcelable(HISTORY);
            mLockType = mPhoto != null && mMessage != null ? outState.getInt(LOCK_TYPE) : NO_BLOCK;
            block();
        }
    }
}

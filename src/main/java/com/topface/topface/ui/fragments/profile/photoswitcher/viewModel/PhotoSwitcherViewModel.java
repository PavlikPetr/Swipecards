package com.topface.topface.ui.fragments.profile.photoswitcher.viewModel;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Gift;
import com.topface.topface.databinding.AcPhotosBinding;
import com.topface.topface.ui.GiftsActivity;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity.INTENT_GIFT;
import static com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity.INTENT_USER_ID;

public class PhotoSwitcherViewModel extends BaseViewModel<AcPhotosBinding> {

    @DrawableRes
    public final int GIFT_SELECTOR_RESOURCE = R.drawable.photoswitcher_send_gift_selector;
    private final static int EMPTY_UID = -1;

    private IActivityDelegate mIActivityDelegate;
    private int mUid;
    private View.OnClickListener mOnAvatarButtonClick;
    private View.OnClickListener mOnDeleteButtonClick;

    public final ObservableField<String> giftLink = new ObservableField<>();
    public final ObservableInt albumText = new ObservableInt();
    public final ObservableBoolean avatarVisibility = new ObservableBoolean();
    public final ObservableBoolean avatarEnable = new ObservableBoolean();
    public final ObservableInt avatarSrc = new ObservableInt(R.drawable.album_profile_button_selector);
    public final ObservableBoolean trashVisibility = new ObservableBoolean();
    public final ObservableBoolean trashEnable = new ObservableBoolean();
    public final ObservableInt trashSrc = new ObservableInt(R.drawable.album_delete_button_selector);

    public PhotoSwitcherViewModel(@NotNull AcPhotosBinding binding, @NotNull IActivityDelegate delegate) {
        super(binding);
        mIActivityDelegate = delegate;
        //noinspection ConstantConditions
        if (mIActivityDelegate == null) {
            throw new IllegalArgumentException("IActivityDelegate can not be null");
        }
        Intent intent = mIActivityDelegate.getIntent();
        parseUid(intent);
        showGift(extractUserGifts(intent));
    }

    private void parseUid(Intent intent) {
        mUid = EMPTY_UID;
        if (intent != null && intent.hasExtra(INTENT_USER_ID)) {
            mUid = intent.getIntExtra(INTENT_USER_ID, EMPTY_UID);
        }
        if (mUid == EMPTY_UID) {
            Debug.log(this, "Intent param is wrong");
            mIActivityDelegate.finish();
        }
    }

    private String extractUserGifts(Intent intent) {
        if (intent != null && intent.hasExtra(INTENT_GIFT)) {
            ArrayList<Gift> array = intent.getExtras().getParcelableArrayList(INTENT_GIFT);
            if (array != null && array.size() > 0) {
                return array.get(0).link;
            }
        }
        return null;
    }

    public void showGift(String link) {
        giftLink.set(link);
    }

    public void setButtonText(@StringRes int stringRes) {
        albumText.set(stringRes);
    }

    public final void onGiftClick(View view) {
        if (mIActivityDelegate != null) {
            mIActivityDelegate.startActivityForResult(
                    GiftsActivity.getSendGiftIntent(App.getContext(), mUid),
                    GiftsActivity.INTENT_REQUEST_GIFT);
        }
    }

    public final void onDeleteButtonClick(View view) {
        if (mOnDeleteButtonClick != null) {
            mOnDeleteButtonClick.onClick(view);
        }
    }

    public final void onAvatarButtonClick(View view) {
        if (mOnAvatarButtonClick != null) {
            mOnAvatarButtonClick.onClick(view);
        }
    }

    public void setOnAvatarButtonClickListener(View.OnClickListener listener) {
        mOnAvatarButtonClick = listener;
    }

    public void setOnDeleteButtonClickListener(View.OnClickListener listener) {
        mOnDeleteButtonClick = listener;
    }

    public final boolean isOwnerProfile() {
        return mUid == App.get().getProfile().uid;
    }

    public void setAvatarEnable(boolean state) {
        avatarEnable.set(state);
    }

    public void setAvatarVisibility(boolean state) {
        avatarVisibility.set(state);
    }

    public void setTrashEnable(boolean state) {
        trashEnable.set(state);
    }

    public void setTrashVisibility(boolean state) {
        trashVisibility.set(state);
    }

    public void setTrashSrc(@DrawableRes int src) {
        trashSrc.set(src);
    }

    @Override
    public void release() {
        super.release();
        mIActivityDelegate = null;
        mOnAvatarButtonClick = null;
        mOnDeleteButtonClick = null;
    }
}

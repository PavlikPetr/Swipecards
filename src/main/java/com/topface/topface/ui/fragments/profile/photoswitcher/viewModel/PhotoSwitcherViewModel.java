package com.topface.topface.ui.fragments.profile.photoswitcher.viewModel;

import android.content.Intent;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.databinding.AcPhotosBinding;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;

import static com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity.INTENT_USER_ID;

public class PhotoSwitcherViewModel extends BaseViewModel<AcPhotosBinding> {

    private final static int EMPTY_UID = -1;

    private IActivityDelegate mIActivityDelegate;
    private View.OnClickListener mOnAvatarButtonClick;
    private View.OnClickListener mOnDeleteButtonClick;

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
    }

    private void parseUid(Intent intent) {
        int mUid = EMPTY_UID;
        if (intent != null && intent.hasExtra(INTENT_USER_ID)) {
            mUid = intent.getIntExtra(INTENT_USER_ID, EMPTY_UID);
        }
        if (mUid == EMPTY_UID) {
            Debug.log(this, "Intent param is wrong");
            mIActivityDelegate.finish();
        }
    }

    public void setButtonText(@StringRes int stringRes) {
        albumText.set(stringRes);
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

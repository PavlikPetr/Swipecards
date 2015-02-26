package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.IUniversalUser;
import com.topface.topface.ui.IUserOnlineListener;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;

/**
 * Fragment with user photo in action bar options
 */
public abstract class UserAvatarFragment extends BaseFragment
        implements View.OnClickListener, IUserOnlineListener {

    private MenuItem mBarAvatar;
    private MenuItem mBarActions;
    private ActionBarTitleSetterDelegate mSetter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetter = new ActionBarTitleSetterDelegate(((ActionBarActivity) getActivity()).getSupportActionBar());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_profile);
        if (item != null && mBarAvatar != null) {
            item.setChecked(mBarAvatar.isChecked());
        }
        mBarAvatar = item;
        MenuItemCompat.getActionView(mBarAvatar).findViewById(R.id.ivBarAvatar).setOnClickListener(this);

        setActionBarAvatar(getUniversalUser());

        MenuItem barActionsItem = menu.findItem(R.id.action_user_actions_list);
        if (barActionsItem != null && mBarActions != null) {
            barActionsItem.setChecked(mBarActions.isChecked());
        }
        mBarActions = barActionsItem;
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_chat;
    }

    @Override
    public void clearContent() {
        ((ImageViewRemote) getView().findViewById(R.id.ivBarAvatar)).setPhoto(null);
//        mNameView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
    }

    @Override
    public void setOnline(boolean online) {
        if (mSetter != null) {
            mSetter.setOnline(online);
        }
    }

    @Override
    public void refreshActionBarTitles() {
        super.refreshActionBarTitles();
        if (mSetter != null) {
            IUniversalUser user = getUniversalUser();
            mSetter.setOnline(user.isOnline());
        }
    }

    protected void setActionBarAvatar(IUniversalUser user) {
        if (mBarAvatar == null) return;
        if (user.isEmpty() || user.isBanned() || user.isDeleted() || user.isPhotoEmpty()) {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setImageResource(user.getSex() == Static.GIRL ?
                            R.drawable.feed_banned_female_avatar :
                            R.drawable.feed_banned_male_avatar);
        } else {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setPhoto(user.getPhoto());
        }
    }

    protected abstract IUniversalUser getUniversalUser();

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivBarAvatar:
                onAvatarClick();
                break;
        }
    }

    public void onAvatarClick() {
        IUniversalUser user = getUniversalUser();
        startActivity(PhotoSwitcherActivity.
                getPhotoSwitcherIntent(user.getGifts(), user.getPhoto().position,
                        user.getId(), user.getPhotosCount(), user.getPhotos()));
    }

    @Override
    protected String getTitle() {
        IUniversalUser user = getUniversalUser();
        return user.isEmpty() ? getDefaultTitle() : user.getNameAndAge();
    }

    protected abstract String getDefaultTitle();

    @Override
    protected String getSubtitle() {
        IUniversalUser user = getUniversalUser();
        if (user.isEmpty() || TextUtils.isEmpty(user.getCity())) {
            return Static.EMPTY;
        } else {
            return user.getCity();
        }
    }
}

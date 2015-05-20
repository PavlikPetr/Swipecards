package com.topface.topface.ui.fragments;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.IUniversalUser;
import com.topface.topface.data.Photo;
import com.topface.topface.ui.IUserOnlineListener;
import com.topface.topface.ui.fragments.profile.PhotoSwitcherActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.actionbar.ActionBarTitleSetterDelegate;
import com.topface.topface.utils.actionbar.OverflowMenu;

/**
 * Fragment with user photo in action bar options
 */
public abstract class UserAvatarFragment extends BaseFragment
        implements View.OnClickListener, IUserOnlineListener {

    private MenuItem mBarAvatar;
    private MenuItem mBarActions;
    private ActionBarTitleSetterDelegate mSetter;
    private OverflowMenu mOverflowMenu;
    private IUniversalUser mUniversalUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetter = new ActionBarTitleSetterDelegate(((ActionBarActivity) getActivity()).getSupportActionBar());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        setOnline(false);
        if (mOverflowMenu != null) {
            mOverflowMenu.onReleaseOverflowMenu();
        }
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
        if (hasUserActions()) {
            if (barActionsItem != null && mBarActions != null) {
                barActionsItem.setChecked(mBarActions.isChecked());
            }
            mBarActions = barActionsItem;
            mOverflowMenu = createOverflowMenu(mBarActions);
        } else {
            barActionsItem.setVisible(false);
            barActionsItem.setEnabled(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                DisplayMetrics metrics = App.getContext().getResources().getDisplayMetrics();
                mBarAvatar.getActionView().setPadding(0, 0, (int) (4 * metrics.density), 0);
            }
        }
    }

    protected boolean hasUserActions() {
        return true;
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_avatar;
    }

    @Override
    public void clearContent() {
        ((ImageViewRemote) getView().findViewById(R.id.ivBarAvatar)).setPhoto(null);
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


    protected void setThrownActionBarAvatar(Photo photo) {
        if (mBarAvatar != null) {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setPhoto(photo);
        }
    }

    protected void setActionBarAvatar(IUniversalUser user) {
        if (mBarAvatar == null) return;
        if (user.isEmpty() || user.isBanned() || user.isDeleted() || user.isPhotoEmpty()) {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setImageResource(user.getSex() == Static.GIRL ?
                            R.drawable.rounded_avatar_female :
                            R.drawable.rounded_avatar_male);
        } else {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setPhoto(user.getPhoto());
        }
    }

    public final IUniversalUser getUniversalUser() {
        if (mUniversalUser == null) {
            mUniversalUser = createUniversalUser();
        }
        return mUniversalUser;
    }

    protected abstract IUniversalUser createUniversalUser();

    public final void invalidateUniversalUser() {
        mUniversalUser = null;
    }

    protected OverflowMenu getOverflowMenu() {
        return mOverflowMenu;
    }

    public boolean hasOverflowMenu() {
        return mOverflowMenu != null;
    }

    protected abstract OverflowMenu createOverflowMenu(MenuItem barActions);

    protected abstract void initOverflowMenuActions(OverflowMenu overflowMenu);

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mOverflowMenu != null) {
            mOverflowMenu.onMenuClicked(item);
        }
        return super.onOptionsItemSelected(item);
    }

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
        if (user != null && !user.isEmpty()) {
            startActivity(
                    PhotoSwitcherActivity.getPhotoSwitcherIntent(user.getGifts(),
                            user.getPhoto() != null ? user.getPhoto().position : 0,
                            user.getId(), user.getPhotosCount(), user.getPhotos())
            );
        }
    }

    public void closeOverflowMenu() {
        if (mBarActions != null && mBarActions.isChecked()) {
            onOptionsItemSelected(mBarActions);
        }
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

    protected MenuItem getBarActionsMenuItem() {
        return mBarActions;
    }
}

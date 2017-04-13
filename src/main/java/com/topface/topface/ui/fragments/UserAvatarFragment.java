package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.topface.topface.R;
import com.topface.topface.data.IUniversalUser;
import com.topface.topface.data.Photo;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.IUserOnlineListener;
import com.topface.topface.ui.fragments.profile.photoswitcher.view.PhotoSwitcherActivity;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.toolbar.view_models.NavigationToolbarViewModel;
import com.topface.topface.utils.actionbar.OverflowMenu;

/**
 * Fragment with user photo in action bar options
 */
public abstract class UserAvatarFragment extends BaseFragment
        implements View.OnClickListener, IUserOnlineListener {

    private MenuItem mBarAvatar;
    protected OverflowMenu mOverflowMenu;
    private IUniversalUser mUniversalUser;

    @Override
    public void onDestroy() {
        super.onDestroy();
        setOnline(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem item = menu.findItem(R.id.action_profile);
        if (item != null && mBarAvatar != null) {
            item.setChecked(mBarAvatar.isChecked());
        }
        mBarAvatar = item;
        MenuItemCompat.getActionView(mBarAvatar).findViewById(R.id.ivBarAvatarContainer).setOnClickListener(this);

        setActionBarAvatar(getUniversalUser());
        if (isNeedShowOverflowMenu()) {
            mOverflowMenu = createOverflowMenu(menu);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mOverflowMenu != null) {
            mOverflowMenu.onReleaseOverflowMenu();
        }
    }

    protected abstract boolean isNeedShowOverflowMenu();

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_avatar_and_menu;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void clearContent() {
        ((ImageViewRemote) getView().findViewById(R.id.ivBarAvatar)).setPhoto(null);
    }

    @Override
    public void setOnline(boolean online) {
        Activity activity = getActivity();
        if (activity instanceof ToolbarActivity) {
            if (((ToolbarActivity) activity).getToolbarViewModel() instanceof NavigationToolbarViewModel) {
                NavigationToolbarViewModel viewModel = (NavigationToolbarViewModel) ((ToolbarActivity) activity).getToolbarViewModel();
                viewModel.getExtraViewModel().isOnline().set(online);
            }
        }
    }

    @Override
    public void refreshActionBarTitles() {
        super.refreshActionBarTitles();
        setOnline(getUniversalUser().isOnline());
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
            showStubAvatar(user.getSex());
        } else {
            ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                    .findViewById(R.id.ivBarAvatar))
                    .setPhoto(user.getPhoto());
        }
    }

    protected void showStubAvatar(int sex) {
        ((ImageViewRemote) MenuItemCompat.getActionView(mBarAvatar)
                .findViewById(R.id.ivBarAvatar))
                .setImageResource(sex == Profile.GIRL ?
                        R.drawable.rounded_avatar_female :
                        R.drawable.rounded_avatar_male);
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

    protected abstract OverflowMenu createOverflowMenu(Menu barActions);

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
            case R.id.ivBarAvatarContainer:
                onAvatarClick();
                break;
        }
    }

    public void onAvatarClick() {
        IUniversalUser user = getUniversalUser();
        if (user != null && !user.isEmpty()) {
            startActivity(
                    PhotoSwitcherActivity.getPhotoSwitcherIntent(user.getPhoto() != null ?
                                    user.getPhoto().position : 0,
                            user.getId(), user.getPhotosCount(), user.getPhotos())
            );
        }
    }
    //TODO SETTOOLBARSETTINGS
//
//    @Override
//    protected String getTitle() {
//        IUniversalUser user = getUniversalUser();
//        return user.isEmpty() ? getDefaultTitle() : user.getNameAndAge();
//    }
//
//    protected abstract String getDefaultTitle();
//
//    @Override
//    protected String getSubtitle() {
//        IUniversalUser user = getUniversalUser();
//        return user.isEmpty() || TextUtils.isEmpty(user.getCity()) ? Utils.EMPTY : user.getCity();
//    }
//
//    @Override
//    protected Boolean isOnline() {
//        return getUniversalUser().isOnline();
//    }
}

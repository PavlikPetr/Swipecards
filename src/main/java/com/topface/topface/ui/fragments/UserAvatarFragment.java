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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSetter = new ActionBarTitleSetterDelegate(((ActionBarActivity) getActivity()).getSupportActionBar());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mOverflowMenu != null) {
            mOverflowMenu.onDestroy();
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
        startActivity(PhotoSwitcherActivity.
                getPhotoSwitcherIntent(user.getGifts(), user.getPhoto().position,
                        user.getId(), user.getPhotosCount(), user.getPhotos()));
    }

    public void closeOverflowMenu() {
        if (mBarActions != null && mBarActions.isChecked()) {
            onOptionsItemSelected(mBarActions);
//            mOutsideView.setVisibility(View.GONE);
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


}

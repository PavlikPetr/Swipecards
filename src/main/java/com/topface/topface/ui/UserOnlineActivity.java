package com.topface.topface.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

/**
 * Activity showing whether user online or not
 */
public abstract class UserOnlineActivity<T extends Fragment> extends CheckAuthActivity<T> implements IUserOnlineListener {

    private View mOnlineIcon;

    @Override
    public void setUserOnline(boolean online) {
        if (mOnlineIcon != null) {
            mOnlineIcon.setVisibility(online ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (Fragment f : fragments) {
                    if (f != null && f.getActivity() == this) {
                        f.onOptionsItemSelected(item);
                    }
                }
                if (isTaskRoot()) {
                    Intent i = new Intent(this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.feed.BlackListFragment;


public class BlackListActivity extends BaseFragmentActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(BlackListFragment.class.getSimpleName());
        FragmentTransaction beginTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == null) {
            beginTransaction.add(R.id.blackList, new BlackListFragment(), BlackListFragment.class.getSimpleName())
                    .commit();
        } else {
            beginTransaction.replace(R.id.blackList, fragment, BlackListFragment.class.getSimpleName())
                    .commit();
        }

    }

    @Override
    protected int getContentLayout() {
        return R.layout.ac_black_list_wrapper;
    }
}

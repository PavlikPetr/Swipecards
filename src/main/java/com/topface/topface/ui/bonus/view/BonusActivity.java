package com.topface.topface.ui.bonus.view;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.topface.topface.App;
import com.topface.topface.ui.SingleFragmentActivity;

public class BonusActivity extends SingleFragmentActivity {

    @Override
    protected String getFragmentTag() {
        return BonusFragment.class.getSimpleName();
    }

    @Override
    protected Fragment createFragment() {
        return new BonusFragment().newInstance(true);
    }

    public static Intent createIntent() {
        return new Intent(App.getContext(), BonusActivity.class);
    }
}

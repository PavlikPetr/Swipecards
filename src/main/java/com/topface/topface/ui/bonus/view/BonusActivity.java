package com.topface.topface.ui.bonus.view;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

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

    public static Intent getIntent(Context context) {
        return new Intent(context, BonusActivity.class);
    }
}

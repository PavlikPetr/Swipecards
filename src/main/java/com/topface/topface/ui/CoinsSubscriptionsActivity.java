package com.topface.topface.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.buy.CoinsSubscriptionsFragment;

public class CoinsSubscriptionsActivity extends CheckAuthActivity {

    public static final int INTENT_COINS_SUBSCRIPTION = 10;

    public static Intent createIntent(String from) {
        Intent intent = new Intent(App.getContext(), CoinsSubscriptionsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_COINS_SUBSCRIPTION);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        return intent;

    }

    @Override
    protected String getFragmentTag() {
        return CoinsSubscriptionsFragment.class.getSimpleName();
    }

    @Override
    protected Fragment createFragment() {
        return new CoinsSubscriptionsFragment();
    }
}

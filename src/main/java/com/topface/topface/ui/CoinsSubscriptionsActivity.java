package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;

import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;

public class CoinsSubscriptionsActivity extends CheckAuthActivity {

    public static final int INTENT_COINS_SUBSCRIPTION = 10;

    public static Intent getCoinsSubscriptionIntent(String from) {
        Intent intent = new Intent(App.getContext(), CoinsSubscriptionsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_COINS_SUBSCRIPTION);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        return intent;

    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ac_coins_subscriptions);
    }
}

package com.topface.topface.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.topface.billing.OpenIabFragment;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.PurchasesFragment;

public class PurchasesActivity extends CheckAuthActivity {

    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */
    public static final int INTENT_BUY_VIP = 1;
    public static final int INTENT_BUY = 2;

    public static Intent createVipBuyIntent(String extraText, String from) {
        Intent intent = new Intent(App.getContext(), PurchasesActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUY_VIP);
        intent.putExtra(PurchasesFragment.ARG_TAG_EXRA_TEXT, extraText);
        intent.putExtra(OpenIabFragment.ARG_TAG_SOURCE, from);
        intent.putExtra(PurchasesFragment.IS_VIP_PRODUCTS, true);
        return intent;
    }

    public static Intent createBuyingIntent(String from, int itemType, int itemPrice) {
        Intent intent = new Intent(App.getContext(), PurchasesActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUY);
        intent.putExtra(OpenIabFragment.ARG_TAG_SOURCE, from);
        if (itemType != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_TYPE, itemType);
        }
        if (itemPrice != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_PRICE, itemPrice);
        }
        return intent;
    }

    public static Intent createBuyingIntent(String from, int itemPrice) {
        return createBuyingIntent(from, -1, itemPrice);
    }

    public static Intent createBuyingIntent(String from) {
        return createBuyingIntent(from, -1, -1);
    }

    @Override
    protected String getFragmentTag() {
        return PurchasesFragment.class.getSimpleName();
    }

    @Override
    protected Fragment createFragment() {
        return new PurchasesFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Это супер мега хак, смотри документацию processRequestCode
        if (!OpenIabFragment.processRequestCode(
                getSupportFragmentManager(),
                requestCode,
                resultCode,
                data,
                PurchasesFragment.class //В нашем случае BillingFragment находится внутри PurchasesFragment
        )) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

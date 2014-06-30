package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;

import com.topface.billing.BillingFragment;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.PurchasesFragment;
import com.topface.topface.ui.fragments.buy.VipBuyFragment;

public class PurchasesActivity extends CheckAuthActivity {

    /**
     * Constant keys for different fragments
     * Values have to be > 0
     */
    public static final int INTENT_BUY_VIP = 1;
    public static final int INTENT_BUY = 2;

    public static Intent getVipBuyIntent(String extraText, String from) {
        Intent intent = new Intent(App.getContext(), PurchasesActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUY_VIP);
        intent.putExtra(VipBuyFragment.ARG_TAG_EXRA_TEXT, extraText);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        intent.putExtra(PurchasesFragment.IS_VIP_PRODUCTS, true);
        return intent;
    }

    public static Intent getBuyingIntent(String from, int itemType, int itemPrice) {
        Intent intent = new Intent(App.getContext(), PurchasesActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_BUY);
        intent.putExtra(BillingFragment.ARG_TAG_SOURCE, from);
        if (itemType != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_TYPE, itemType);
        }
        if (itemPrice != -1) {
            intent.putExtra(PurchasesFragment.ARG_ITEM_PRICE, itemPrice);
        }
        return intent;
    }

    public static Intent getBuyingIntent(String from, int itemPrice) {
        return getBuyingIntent(from, -1, itemPrice);
    }

    public static Intent getBuyingIntent(String from) {
        return getBuyingIntent(from, -1, -1);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ac_purchases);
    }
}

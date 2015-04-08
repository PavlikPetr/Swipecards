package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.topface.topface.R;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.views.ServicesTextView;

public class BuyWidgetController {
    private final View mRoot;
    Button mBuyButton;
    public boolean salesEnabled = false;
    ServicesTextView mCoins;
    ServicesTextView mLikes;
    private View.OnClickListener mBuyWidgetClickListener;


    public BuyWidgetController(final Context context, View root) {
        mBuyWidgetClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(PurchasesActivity.createBuyingIntent("Menu"));
            }
        };
        mRoot = root;
        mCoins = (ServicesTextView) root.findViewById(R.id.menuCurCoins);
        mLikes = (ServicesTextView) root.findViewById(R.id.menuCurLikes);
        mBuyButton = (Button) root.findViewById(R.id.menuBuyBtn);
        mRoot.setOnClickListener(mBuyWidgetClickListener);
        mBuyButton.setOnClickListener(mBuyWidgetClickListener);
        updateBalance();
    }

    public void updateBalance() {
        mCoins.setText(Integer.toString(CacheProfile.money));
        mLikes.setText(Integer.toString(CacheProfile.likes));
    }

    public void hide() {
        mRoot.setVisibility(View.GONE);
    }

    public void show() {
        mRoot.setVisibility(View.VISIBLE);
    }

    public void setSalesEnabled(boolean salesEnabled) {
        this.salesEnabled = salesEnabled;
        mBuyButton.setBackgroundResource(salesEnabled ? R.drawable.btn_sale_blue_selector : R.drawable.btn_blue_selector);
    }
}

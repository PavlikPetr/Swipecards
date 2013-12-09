package com.topface.topface.utils;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.topface.topface.R;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.views.ServicesTextView;

public class BuyWidgetController {
    Button mBuyButton;
    ServicesTextView mCoins;
    ServicesTextView mLikes;

    public BuyWidgetController(final Context context, View root) {
        mCoins = (ServicesTextView) root.findViewById(R.id.menuCurCoins);
        mLikes = (ServicesTextView) root.findViewById(R.id.menuCurLikes);
        mBuyButton = (Button) root.findViewById(R.id.menuBuyBtn);
        mBuyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(ContainerActivity.getBuyingIntent("Menu"));
            }
        });
        updateBalance();
    }

    public void updateBalance() {
        mCoins.setText(Integer.toString(CacheProfile.money));
        mLikes.setText(Integer.toString(CacheProfile.likes));
    }

    public void setButtonBackgroundResource(int resId) {
        mBuyButton.setBackgroundResource(resId);
    }
}

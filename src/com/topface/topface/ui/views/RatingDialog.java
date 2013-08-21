package com.topface.topface.ui.views;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ViewFlipper;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.utils.PopupManager;
import com.topface.topface.utils.Utils;

public class RatingDialog extends Dialog {
    private View.OnClickListener backClickListener;
    private String type;

    public RatingDialog(Context context, String type) {
        super(context);
        this.type = type;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup_rating);
        if (type.equals(PopupManager.STANDARD_RATE_TYPE)) {
            EasyTracker.getTracker().trackEvent("RatePopup", "StandardPopup", "show", 1L);
            ((ViewFlipper) findViewById(R.id.prFlipper)).setDisplayedChild(1);
        } else if (type.equals(PopupManager.LONG_RATE_TYPE)) {
            EasyTracker.getTracker().trackEvent("RatePopup", "LikeTopfacePopup", "show", 1L);
        }
        findViewById(R.id.btnRPYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewFlipper)findViewById(R.id.prFlipper)).setDisplayedChild(1);
                EasyTracker.getTracker().trackEvent("RatePopup", "LikeTopfacePopup", "Yes", 1L);
            }
        });

        findViewById(R.id.btnRPNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.getTracker().trackEvent("RatePopup", "LikeTopfacePopup", "No", 1L);
                cancel();
            }
        });
    }

    public void setOnBackClickListener(View.OnClickListener listener) {
        backClickListener = listener;
    }

    public void setOnRateClickListener(View.OnClickListener listener) {
        EasyTracker.getTracker().trackEvent("RatePopup", "StandardPopup", "Rate", 1L);
        findViewById(R.id.btnRatingPopupRate).setOnClickListener(listener);
    }

    public void setOnLateClickListener(View.OnClickListener listener) {
        EasyTracker.getTracker().trackEvent("RatePopup", "StandardPopup", "Late", 1L);
        findViewById(R.id.btnRatingPopupLate).setOnClickListener(listener);
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        EasyTracker.getTracker().trackEvent("RatePopup", "StandardPopup", "cancel", 1L);
        findViewById(R.id.btnRatingPopupCancel).setOnClickListener(listener);
    }

    @Override
    public void onBackPressed() {

        EasyTracker.getTracker().trackEvent("RatePopup", type.equals(PopupManager.STANDARD_RATE_TYPE)?"StandardPopup":"LikeTopfacePopup", "BackPressed", 1L);
        if (backClickListener != null) {
            backClickListener.onClick(null);
        }
        super.onBackPressed();
    }
}

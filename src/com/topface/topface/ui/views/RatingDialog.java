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
            EasyTracker.getTracker().sendEvent("RatePopup", "StandardPopup", "show", 1L);
            ((ViewFlipper) findViewById(R.id.prFlipper)).setDisplayedChild(1);
        } else if (type.equals(PopupManager.LONG_RATE_TYPE)) {
            EasyTracker.getTracker().sendEvent("RatePopup", "LikeTopfacePopup", "show", 1L);
            findViewById(R.id.btnRatingPopupCancel).setVisibility(View.GONE);
        }
        findViewById(R.id.btnRPYes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ViewFlipper)findViewById(R.id.prFlipper)).setDisplayedChild(1);
                EasyTracker.getTracker().sendEvent("RatePopup", "LikeTopfacePopup", "Yes", 1L);
            }
        });

        findViewById(R.id.btnRPNo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyTracker.getTracker().sendEvent("RatePopup", "LikeTopfacePopup", "No", 1L);
                cancel();
            }
        });
    }

    public void setOnBackClickListener(View.OnClickListener listener) {
        backClickListener = listener;
    }

    public void setOnRateClickListener(View.OnClickListener listener) {
        EasyTracker.getTracker().sendEvent("RatePopup", getStatisticsTagByType(), "Rate", 1L);
        findViewById(R.id.btnRatingPopupRate).setOnClickListener(listener);
    }

    public void setOnLateClickListener(View.OnClickListener listener) {
        EasyTracker.getTracker().sendEvent("RatePopup", getStatisticsTagByType(), "Later", 1L);
        findViewById(R.id.btnRatingPopupLate).setOnClickListener(listener);
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        EasyTracker.getTracker().sendEvent("RatePopup", "StandardPopup", "cancel", 1L);
        findViewById(R.id.btnRatingPopupCancel).setOnClickListener(listener);
    }

    private String getStatisticsTagByType() {
        return type.equals(PopupManager.STANDARD_RATE_TYPE)?"StandardPopup":"LikeTopfacePopup";
    }

    @Override
    public void onBackPressed() {

        EasyTracker.getTracker().sendEvent("RatePopup", getStatisticsTagByType(), "BackPressed", 1L);
        if (backClickListener != null) {
            backClickListener.onClick(null);
        }
        super.onBackPressed();
    }
}

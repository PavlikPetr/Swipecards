package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.BackgroundThread;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.Utils;

/**
 * Created by kirussell on 25.12.13.
 * User can rate an app through this dialog
 */
public class RateAppDialog extends BaseDialogFragment implements View.OnClickListener,
        DialogInterface.OnCancelListener {
    public static final String TAG = "RateAppDialog";
    public static final String RATING_POPUP = "RATING_POPUP";

    public static final long RATE_POPUP_TIMEOUT = DateUtils.DAY_IN_MILLISECONDS;

    public static final String OFF_RATE_TYPE = "OFF";
    private RatingBar mRatingBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Закрыть диалог можно
        setCancelable(true);
        //По стилю это у нас не диалог, а кастомный дизайн -
        //закрывает весь экран оверлеем и ниже ActionBar показывает контент
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_rate_app, container);
        getDialog().setOnCancelListener(this);

        mRatingBar = (RatingBar) root.findViewById(R.id.ratingBarStarts);

        root.findViewById(R.id.btnRate).setOnClickListener(this);
        root.findViewById(R.id.btnAskLater).setOnClickListener(this);
        root.findViewById(R.id.btnNoThanx).setOnClickListener(this);

        EasyTracker.getTracker().sendEvent("RatePopup", "FeaturePopup", "Show", 1L);
        return root;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Send statistics onBackPressed or on tap outside the dialog
        EasyTracker.getTracker().sendEvent("RatePopup", "FeaturePopup", "ManualCancel", 1L);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRate:
                rate();
                break;
            case R.id.btnAskLater:
                EasyTracker.getTracker().sendEvent("RatePopup", "FeaturePopup", "Later", 1L);
                saveRatingPopupStatus(System.currentTimeMillis());
                getDialog().dismiss();
                break;
            case R.id.btnNoThanx:
                // Используем label: Cancel, как в iOS
                EasyTracker.getTracker().sendEvent("RatePopup", "FeaturePopup", "Cancel", 1L);
                saveRatingPopupStatus(0);
                getDialog().dismiss();
                break;
        }
    }

    private void rate() {
        long rating = (long) mRatingBar.getRating();
        EasyTracker.getTracker().sendEvent("RatePopup", "FeaturePopup", "Rate", rating);
        if (rating <= 0) {
            new AlertDialog.Builder(getActivity())
                    .setMessage(R.string.rate_popup_error)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .show();
        } else if (rating >= 4) {
            saveRatingPopupStatus(0);
            Utils.goToMarket(getActivity());
            dismiss();
        } else {
            saveRatingPopupStatus(0);
            BaseDialogFragment dialog = SendFeedbackDialog.newInstance(
                    R.string.feedback_popup_title, getResources().getString(R.string.settings_low_rate_internal)
            );
            dialog.show(getActivity().getSupportFragmentManager(), SendFeedbackDialog.TAG);
            dismiss();
        }
    }

    private static void saveRatingPopupStatus(final long value) {
        new BackgroundThread() {
            @Override
            public void execute() {
                final SharedPreferences.Editor editor = App.getContext().getSharedPreferences(
                        Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE
                ).edit();
                editor.putLong(RATING_POPUP, value);
                editor.commit();
            }
        };
    }

    /**
     * Note: method has operations with disk(SharedPreferences)
     *
     * @return whether the rate popup is applicable or not
     */
    public static boolean isApplicable() {
        final SharedPreferences preferences = App.getContext().getSharedPreferences(
                Static.PREFERENCES_TAG_SHARED,
                Context.MODE_PRIVATE
        );

        long date_start = preferences.getLong(RATING_POPUP, 1);
        long date_now = new java.util.Date().getTime();

        if (date_start == 0 || (date_now - date_start < RATE_POPUP_TIMEOUT) || CacheProfile.getOptions().ratePopupType.equals(OFF_RATE_TYPE)) {
            return false;
        } else if (date_start == 1) {
            saveRatingPopupStatus(System.currentTimeMillis());
        }
        return true;
    }
}

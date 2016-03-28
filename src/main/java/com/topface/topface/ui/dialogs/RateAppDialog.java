package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.AppRateRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.statistics.RatePopupStatistics;
import com.topface.topface.utils.Utils;

import static com.topface.topface.ui.settings.FeedbackMessageFragment.FeedbackType.LOW_RATE_MESSAGE;

/**
 * Created by kirussell on 25.12.13.
 * User can rate an app through this dialog
 */
public class RateAppDialog extends AbstractDialogFragment implements View.OnClickListener,
        DialogInterface.OnCancelListener {
    public static final String TAG = "RateAppDialog";
    public static final String RATING_POPUP = "RATING_POPUP";
    public static final int GPLAY_ACTIVITY = 9999;

    private RatingBar mRatingBar;
    private boolean mIsNeedClose = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Закрыть диалог можно
        setCancelable(true);
        RatePopupStatistics.sendRatePopupShow();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIsNeedClose) {
            dismiss();
        }
    }

    @Override
    protected void initViews(View root) {
        getDialog().setOnCancelListener(this);
        mRatingBar = (RatingBar) root.findViewById(R.id.ratingBarStarts);
        root.findViewById(R.id.btnRate).setOnClickListener(this);
        root.findViewById(R.id.btnAskLater).setOnClickListener(this);
        root.findViewById(R.id.btnNoThanx).setOnClickListener(this);
    }

    @Override
    protected boolean isModalDialog() {
        return true;
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.dialog_rate_app;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Send statistics onBackPressed or on tap outside the dialog
        RatePopupStatistics.sendRatePopupClose();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRate:
                rate();
                break;
            case R.id.btnAskLater:
                RatePopupStatistics.sendRatePopupClickButtonLater();
                saveRatingPopupStatus(System.currentTimeMillis());
                getDialog().cancel();
                break;
            case R.id.btnNoThanx:
                // Используем label: Cancel, как в iOS
                RatePopupStatistics.sendRatePopupClickButtonClose();
                saveRatingPopupStatus(0);
                sendRateRequest(AppRateRequest.NO_RATE);
                getDialog().cancel();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GPLAY_ACTIVITY) {
            mIsNeedClose = true;
        }
    }

    private void rate() {
        long rating = (long) mRatingBar.getRating();
        RatePopupStatistics.sendRatePopupClickButtonRate(rating);
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
            sendRateRequest(rating);
            saveRatingPopupStatus(0);
            Utils.goToMarket(getActivity(), GPLAY_ACTIVITY);
        } else {
            sendRateRequest(rating);
            saveRatingPopupStatus(0);
            AbstractDialogFragment dialog = SendFeedbackDialog.newInstance(
                    R.string.feedback_popup_title,
                    String.format(LOW_RATE_MESSAGE.getTitle(), (int) rating)
            );
            dialog.show(getActivity().getSupportFragmentManager(), SendFeedbackDialog.TAG);
            dismiss();
        }
    }

    private void sendRateRequest(long rating) {
        new AppRateRequest(getActivity(), rating).callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
            }
        }).exec();
    }

    /**
     * Saves status of rate popup show
     *
     * @param value timestamp of last show; 0 - to stop showing
     */
    private static void saveRatingPopupStatus(final long value) {
        new BackgroundThread() {
            @Override
            public void execute() {
                final SharedPreferences.Editor editor = App.getContext().getSharedPreferences(
                        App.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE
                ).edit();
                editor.putLong(RATING_POPUP, value);
                editor.apply();
            }
        };
    }

    /**
     * Note: method has operations with disk(SharedPreferences)
     *
     * @return whether the rate popup is applicable or not
     */
    public static boolean isApplicable(long ratePopupTimeout, boolean ratePopupEnabled) {
        // need ProfileConfig
        final SharedPreferences preferences = App.getContext().getSharedPreferences(
                App.PREFERENCES_TAG_SHARED,
                Context.MODE_PRIVATE
        );
        long date_start = preferences.getLong(RATING_POPUP, -1);
        // first time do not show rate popup
        if (date_start == -1) {
            saveRatingPopupStatus(System.currentTimeMillis());
            return false;
        }
        long date_now = System.currentTimeMillis();
        return (date_start != 0
                && (date_now - date_start > ratePopupTimeout)
                && ratePopupEnabled);
    }
}

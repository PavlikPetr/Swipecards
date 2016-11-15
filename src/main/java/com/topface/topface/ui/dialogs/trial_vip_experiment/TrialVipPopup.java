package com.topface.topface.ui.dialogs.trial_vip_experiment;

import android.os.Bundle;
import android.view.View;

import com.topface.topface.R;

import org.jetbrains.annotations.NotNull;


public class TrialVipPopup extends BaseTrialVipPopup {

    public static final String TAG = "TrialVipPopup";

    @NotNull
    public static TrialVipPopup newInstance(boolean skipShowingCondition) {
        TrialVipPopup trialVipPopup = new TrialVipPopup();
        Bundle arg = new Bundle();
        arg.putBoolean(TrialVipPopup.SKIP_SHOWING_CONDITION, skipShowingCondition);
        trialVipPopup.setArguments(arg);
        return trialVipPopup;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getDialogStyleResId() {
        return R.style.Theme_Topface_NoActionBar_TrialVipPopup;
    }

    @Override
    protected void initViews(View root) {
        root.findViewById(R.id.get_trial_vip_button).setOnClickListener(this);
        root.findViewById(R.id.iv_close).setOnClickListener(this);
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.trial_vip_popup;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_trial_vip_button:
                onVipTrialPurchaseStart();
                break;
            case R.id.iv_close:
                getDialog().cancel();
                break;
        }
    }

    @Override
    public void setOnFragmentFinishDelegate(final IOnFragmentFinishDelegate delegate) {
        mOnFragmentFinishDelegate = delegate;
    }
}

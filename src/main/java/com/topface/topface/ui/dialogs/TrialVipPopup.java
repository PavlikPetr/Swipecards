package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.config.UserConfig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class TrialVipPopup extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "TrialVipPopup";
    public static final String SKIP_SHOWING_CONDITION = "skip_showing_condition";
    private OnFragmentActionsListener mOnFragmentActionsListener;

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
    protected boolean isModalDialog() {
        return false;
    }

    @Override
    public boolean isUnderActionBar() {
        return false;
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.trial_vip_popup;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.get_trial_vip_button:
                if (mOnFragmentActionsListener != null) {
                    mOnFragmentActionsListener.onSubscribeClick();
                }
                break;
            case R.id.iv_close:
                dismiss();
                break;
        }
    }

    private void incrPopupShowCounter() {
        UserConfig userConfig = App.getUserConfig();
        userConfig.setTrialVipPopupCounter(userConfig.getTrialVipCounter() + 1);
        userConfig.saveConfig();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Bundle args = getArguments();
        if (args != null && !args.getBoolean(SKIP_SHOWING_CONDITION)) {
            incrPopupShowCounter();
        }
    }

    public void setOnSubscribe(OnFragmentActionsListener listener) {
        mOnFragmentActionsListener = listener;
    }

    public interface OnFragmentActionsListener {
        void onSubscribeClick();

        void onFragmentFinish();
    }

    @Override
    public void onDestroy() {
        if (mOnFragmentActionsListener != null) {
            mOnFragmentActionsListener.onFragmentFinish();
        }
        super.onDestroy();
    }
}

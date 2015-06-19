package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.config.UserConfig;


public class TrialVipPopup extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "TrialVipPopup";
    private OnSubscribe mOnSubscribe;

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
                if (mOnSubscribe != null) {
                    mOnSubscribe.onClick();
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
        incrPopupShowCounter();
    }

    public void setOnSubscribe(OnSubscribe listener) {
        mOnSubscribe = listener;
    }

    public interface OnSubscribe {
        void onClick();
    }
}

package com.topface.topface.ui.dialogs;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuSettingsData;
import com.topface.topface.data.leftMenu.NavigationState;
import com.topface.topface.data.leftMenu.WrappedNavigationData;
import com.topface.topface.statistics.DatingLockPopupStatistics;
import com.topface.topface.utils.config.UserConfig;


public class DatingLockPopup extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "DatingLockPopup";
    private NavigationState mNavigationState;
    private boolean mIsRedirectedToSympathies;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNavigationState = App.getAppComponent().navigationState();
        mIsRedirectedToSympathies = false;
        DatingLockPopupStatistics.sendDatingPopupShow();
    }

    @Override
    protected int getDialogStyleResId() {
        return R.style.Theme_Topface_NoActionBar_DatingLockPopup;
    }

    @Override
    protected void initViews(View root) {
        root.findViewById(R.id.redirect_into_sympathy).setOnClickListener(this);
        root.findViewById(R.id.iv_close).setOnClickListener(this);
        Options options = App.from(getActivity()).getOptions();
        ((TextView) root.findViewById(R.id.title)).setText(options.notShown.title);
        ((TextView) root.findViewById(R.id.message)).setText(options.notShown.text);
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
        return R.layout.dating_lock_popup;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.redirect_into_sympathy:
                saveRedirectTime();
                if (isAdded()) {
                    mNavigationState
                            .emmitNavigationState(new WrappedNavigationData(new LeftMenuSettingsData(FragmentIdData.TABBED_LIKES),
                                    WrappedNavigationData.SELECT_EXTERNALY));
                }
                mIsRedirectedToSympathies = true;
                dismiss();
                break;
            case R.id.iv_close:
                getDialog().cancel();
                break;
        }
    }

    private void saveRedirectTime() {
        UserConfig userConfig = App.getUserConfig();
        userConfig.setDatingLockPopupRedirect(System.currentTimeMillis());
        userConfig.saveConfig();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mIsRedirectedToSympathies) {
            DatingLockPopupStatistics.sendDatingPopupRedirect();
        } else {
            DatingLockPopupStatistics.sendDatingPopupClose();
        }
    }

}

package com.topface.topface.ui.dialogs;

import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.topface.topface.R;
import com.topface.topface.statistics.DatingLockStatistics;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.BaseFragment;

/**
 * Created by onikitin on 26.11.14.
 */
public class DatingLockPopup extends AbstractDialogFragment implements View.OnClickListener {

    private Button mAnswerSympathy;
    private ImageView mClosePopup;
    public static final String TAG = "DatingLockPopup";
    public static final String DATING_LOCK_POPUP = "DATING_LOCK_POPUP";

    @Override
    protected void initViews(View root) {
        mAnswerSympathy = (Button) root.findViewById(R.id.redirect_into_sympathy);
        mAnswerSympathy.setOnClickListener(this);
        mClosePopup = (ImageView) root.findViewById(R.id.iv_close);
        mClosePopup.setOnClickListener(this);
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.dating_lock_popup;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.redirect_into_sympathy:
                ((NavigationActivity) getActivity()).showFragment(BaseFragment.FragmentId.LIKES);
                DatingLockStatistics.sendDatingPopupRedirect();
                break;
            case R.id.iv_close:
                DatingLockStatistics.sendDatingPopupClose();
                break;
        }
        dismiss();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        DatingLockStatistics.sendDatingPopupShow();
        super.show(manager, tag);
    }
}

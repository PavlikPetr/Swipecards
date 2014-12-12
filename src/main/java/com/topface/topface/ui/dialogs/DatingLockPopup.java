package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.topface.statistics.android.StatisticsTracker;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.CacheProfile;


public class DatingLockPopup extends AbstractDialogFragment implements View.OnClickListener {

    public static final String TAG = "DatingLockPopup";
    public static final String DATING_LOCK_POPUP_SHOW = "dating_lock_popup_show";
    public static final String DATING_LOCK_POPUP_CLOSE = "dating_lock_popup_close";
    public static final String DATING_LOCK_POPUP_REDIRECT = "dating_lock_popup_redirect";

    private DatingLockPopupRedirectListener mDatingLockPopupRedirectListener;
    private TextView mTitle;
    private TextView mMessage;
    private boolean mIsBackPressed = false;


    public interface DatingLockPopupRedirectListener {
        public void onRedirect();
    }

    public void setDatingLockPopupRedirectListener(DatingLockPopupRedirectListener listener) {
        this.mDatingLockPopupRedirectListener = listener;
    }

    private void send(String key) {
        StatisticsTracker.getInstance().setContext(App.getContext()).sendEvent(key, 1);
    }

    public void sendDatingPopupClose() {
        send(DATING_LOCK_POPUP_CLOSE);
    }

    public void sendDatingPopupRedirect() {
        send(DATING_LOCK_POPUP_REDIRECT);
    }

    public void sendDatingPopupShow() {
        send(DATING_LOCK_POPUP_SHOW);
    }


    @Override
    protected void initViews(View root) {
        root.findViewById(R.id.redirect_into_sympathy).setOnClickListener(this);
        root.findViewById(R.id.iv_close).setOnClickListener(this);
        mTitle = (TextView) root.findViewById(R.id.title);
        mTitle.setText(CacheProfile.getOptions().notShown.title);
        mMessage = (TextView) root.findViewById(R.id.message);
        mMessage.setText(CacheProfile.getOptions().notShown.text);
        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                //Срабатывает 2 раза. Отсеиваем второе нажатие, чтобы не отправлять статистику дважды
                if (!mIsBackPressed) {
                    mIsBackPressed = true;
                    sendDatingPopupClose();
                    dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setNeedPadding(false);
        super.onCreate(savedInstanceState);
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.dating_lock_popup;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.redirect_into_sympathy:
                mDatingLockPopupRedirectListener.onRedirect();
                sendDatingPopupRedirect();
                break;
            case R.id.iv_close:
                sendDatingPopupClose();
                break;
        }
        dismiss();
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        sendDatingPopupShow();
        super.show(manager, tag);
    }
}

package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.Static;
import com.topface.topface.ui.analytics.TrackedDialogFragment;

/**
 * Created by kirussell on 26/05/15.
 * Basic dialog fragment for common logic
 */
public abstract class BaseDialog extends TrackedDialogFragment {

    private DialogInterface.OnDismissListener mDismissListener;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, getDialogStyleResId());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getDialogLayoutRes(), container, false);
        initViews(view);
        return view;
    }

    protected abstract void initViews(View root);

    protected abstract int getDialogLayoutRes();

    protected abstract int getDialogStyleResId();

    @Override
    public final void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode != -1) {
            intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        }
        super.startActivityForResult(intent, requestCode);
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mDismissListener = listener;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (mDismissListener != null) {
            mDismissListener.onDismiss(dialog);
        }
    }
}

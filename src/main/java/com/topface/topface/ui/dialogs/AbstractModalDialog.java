package com.topface.topface.ui.dialogs;

import android.view.View;
import android.view.ViewStub;

import com.topface.topface.R;

/**
 * Created by kirussell on 22.01.14.
 * Modal Dialog class
 */
public abstract class AbstractModalDialog extends AbstractDialogFragment {

    private boolean mBtnCloseVisible = true;
    private View mBtnCloseView;

    @Override
    protected final void initViews(View root) {
        mBtnCloseView = root.findViewById(R.id.btnClose);
        mBtnCloseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseButtonClick(v);
            }
        });
        mBtnCloseView.setVisibility(mBtnCloseVisible ? View.VISIBLE : View.GONE);
        ViewStub stub = (ViewStub) root.findViewById(R.id.vsContent);
        stub.setLayoutResource(getContentLayoutResId());
        View view = stub.inflate();
        initContentViews(view);
    }

    protected void setCloseButton(boolean visible) {
        mBtnCloseVisible = visible;
        if (mBtnCloseView != null) {
            mBtnCloseView.setVisibility(mBtnCloseVisible ? View.VISIBLE : View.GONE);
        }
    }

    protected abstract void initContentViews(View root);

    protected abstract int getContentLayoutResId();

    @Override
    public final int getDialogLayoutRes() {
        return R.layout.dialog_modal;
    }

    protected abstract void onCloseButtonClick(View v);
}

package com.topface.topface.ui.dialogs;

import android.view.View;
import android.view.ViewStub;

import com.topface.topface.R;

/**
 * Created by kirussell on 22.01.14.
 * Modal Dialog class
 */
public abstract class AbstractModalDialog extends AbstractDialogFragment {
    @Override
    protected final void initViews(View root) {
        root.findViewById(R.id.btnClose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCloseButtonClick(v);
            }
        });
        ViewStub stub = (ViewStub) root.findViewById(R.id.vsContent);
        stub.setLayoutResource(getContentLayoutResId());
        View view = stub.inflate();
        initContentViews(view);
    }

    protected abstract void initContentViews(View root);

    protected abstract int getContentLayoutResId();

    @Override
    public final int getDialogLayoutRes() {
        return R.layout.dialog_modal;
    }

    protected abstract void onCloseButtonClick(View v);
}

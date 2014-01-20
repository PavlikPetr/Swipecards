package com.topface.topface.ui.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.ui.analytics.TrackedDialogFragment;

/**
 * Extend this class if you need DialogFragment with semi-transparent-black background
 */
public abstract class BaseDialogFragment extends TrackedDialogFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //По стилю это у нас не диалог, а кастомный дизайн -
        //закрывает весь экран оверлеем и ниже ActionBar показывает контент
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_Translucent);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_base, container, false);
        ViewStub stub = (ViewStub) root.findViewById(R.id.vsContent);
        stub.setLayoutResource(getDialogLayoutRes());
        View view = stub.inflate();
        initViews(view);
        return root;
    }

    /**
     * Gives access to content view of dialog which is defined
     * throw layout from getDialogLayoutRes() method
     *
     * @param root content view
     */
    protected abstract void initViews(View root);

    @Override
    public final void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode != -1) {
            intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        }
        super.startActivityForResult(intent, requestCode);
    }

    public abstract int getDialogLayoutRes();
}

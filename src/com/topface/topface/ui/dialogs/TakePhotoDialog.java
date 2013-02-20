package com.topface.topface.ui.dialogs;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.topface.topface.R;

public class TakePhotoDialog extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_take_photo, container, false);

        root.

        return root;
    }

    public static TakePhotoDialog newInstance() {
        return new TakePhotoDialog();
    }

}

package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.topface.topface.R;

public class ConfirmEmailDialog extends DialogFragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_confirm_email, container, false);

        root.findViewById(R.id.btnClose).setOnClickListener(this);

        return root;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.btnClose:
                final Dialog dialog = getDialog();
                if (dialog != null) dialog.dismiss();
                break;

        }
    }
}

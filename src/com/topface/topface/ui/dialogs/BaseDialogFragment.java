package com.topface.topface.ui.dialogs;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import com.topface.topface.Static;

public class BaseDialogFragment extends DialogFragment {

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (requestCode != -1) {
            intent.putExtra(Static.INTENT_REQUEST_KEY, requestCode);
        }
        super.startActivityForResult(intent, requestCode);
    }
}

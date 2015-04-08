package com.topface.topface.ui.dialogs;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;

import com.topface.topface.App;
import com.topface.topface.R;

/**
 * Dialog with info about application.
 */
public class AboutDialog extends AbstractEditDialog {
    public static AboutDialog newInstance(String title) {
        final AboutDialog editor = new AboutDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        editor.setArguments(args);
        return editor;
    }

    @Override
    protected void initViews(View root) {
        super.initViews(root);
        getTitleText().setTextAppearance(App.getContext(), R.style.SelectorDialogTitle_Blue);

        ViewStub buttonsStub = getButtonsStub();
        buttonsStub.setLayoutResource(R.layout.edit_dialog_button);
        View button = buttonsStub.inflate();
        Button okBtn = (Button) button.findViewById(R.id.edit_dialog_ok);
        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AboutDialog.this.dismiss();
            }
        });
    }
}

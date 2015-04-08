package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Utils;

/**
 * Dialog for editing text form items
 */
public class EditTextFormDialog extends AbstractEditDialog<FormItem> {

    public static EditTextFormDialog newInstance(String title, FormItem text,
                                                 final AbstractEditDialog.EditingFinishedListener<FormItem> editingFinishedListener) {
        final EditTextFormDialog editor = new EditTextFormDialog();
        Bundle args = new Bundle();
        args.putString(DIALOG_TITLE, title);
        args.putParcelable(DATA, text);
        editor.setArguments(args);
        editor.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                editingFinishedListener.onEditingFinished(editor.getAdapter().getData());
            }
        });
        return editor;
    }

    @Override
    protected int getDialogStyleResId() {
        return R.style.EditDialog_Text;
    }

    @Override
    protected void initViews(View root) {
        super.initViews(root);
        getTitleText().setTextAppearance(App.getContext(), R.style.SelectorDialogTitle_Blue);

        if (isButtonsVisible()) {
            ViewStub buttonsStub = getButtonsStub();
            buttonsStub.setLayoutResource(R.layout.edit_dialog_buttons);
            View buttons = buttonsStub.inflate();

            Button cancelBtn = (Button) buttons.findViewById(R.id.edit_dialog_cancel);
            cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EditTextFormDialog.this.dismiss();
                }
            });
            Button saveBtn = (Button) buttons.findViewById(R.id.edit_dialog_save);
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getAdapter().saveData();
                    EditTextFormDialog.this.dismiss();
                }
            });
        }
    }

    protected boolean isButtonsVisible() {
        return true;
    }

    @Override
    public void onPause() {
        closeKeyboard();
        super.onPause();
    }

    private void closeKeyboard() {
        Activity activity = getActivity();
        if (activity != null) {
            View focus = activity.getCurrentFocus();
            if (focus != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(focus.getWindowToken(), 0);
            }
        }
    }
}

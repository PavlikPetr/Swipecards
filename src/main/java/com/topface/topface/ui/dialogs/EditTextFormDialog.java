package com.topface.topface.ui.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.adapters.AbstractEditAdapter;
import com.topface.topface.ui.adapters.TextFormEditAdapter;
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
                editor.closeKeyboard();
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
        final TextView limitText = getLimitText();

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
                    TextFormEditAdapter adapter = getAdapter();
                    FormItem formItem = new FormItem(adapter.getData());
                    formItem.value = adapter.getItem(0);
                    if (formItem.isValueValid()) {
                        getAdapter().saveData();
                        EditTextFormDialog.this.dismiss();
                    } else {
                        Toast.makeText(App.getContext(), R.string.retry_cancel_editing_bad_value, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        FormItem data = getAdapter().getData();
        if (data.getTextLimitInterface() != null) {
            limitText.setText(data.value.length() + "/" + data.getTextLimitInterface().getLimit());
            getAdapter().setDataChangeListener(new AbstractEditAdapter.OnDataChangeListener<FormItem>() {
                @Override
                public void onDataChanged(FormItem data) {
                    limitText.setText(data.value.length() + "/" + data.getTextLimitInterface().getLimit());
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
        View view = getView();
        if (view != null) {
            Utils.hideSoftKeyboard(App.getContext(), view.getApplicationWindowToken());
        }
    }

    @Override
    public TextFormEditAdapter getAdapter() {
        return (TextFormEditAdapter) super.getAdapter();
    }
}

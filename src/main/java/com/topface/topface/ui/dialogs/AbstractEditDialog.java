package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.ui.adapters.AbstractEditAdapter;
import com.topface.topface.ui.adapters.EditAdapterFactory;

/**
 * Abstract dialog for editing fields
 */
public class AbstractEditDialog<T extends Parcelable> extends AbstractDialogFragment {
    public static final String DIALOG_TITLE = "dialog_title";
    public static final String DATA = "data";

    public interface EditingFinishedListener<T extends Parcelable> {

        void onEditingFinished(T data);
    }

    private String mTitle;
    private AbstractEditAdapter<T> mAdapter;
    private ListView mOptionsList;
    private TextView mTitleText;
    private ViewStub mButtonsStub;

    @Override
    protected void applyStyle() {
        setStyle(ConfirmEmailDialog.STYLE_NO_TITLE, R.style.EditDialog);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(DIALOG_TITLE);
            T data = args.getParcelable(DATA);
            mAdapter = new EditAdapterFactory().createAdapterFor(activity, data);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getDialogLayoutRes(), container, false);
        initViews(view);
        return view;
    }

    @Override
    protected void initViews(View root) {
        mOptionsList = (ListView) root.findViewById(R.id.edit_dialog_options_list);
        mOptionsList.setAdapter(mAdapter);
        mTitleText = (TextView) root.findViewById(R.id.edit_dialog_title);
        mTitleText.setText(mTitle);
        mButtonsStub = (ViewStub) root.findViewById(R.id.edit_dialog_buttons_stub);
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.edit_dialog;
    }

    public String getTitle() {
        return mTitle;
    }

    public AbstractEditAdapter<T> getAdapter() {
        return mAdapter;
    }

    protected ListView getOptionsList() {
        return mOptionsList;
    }

    protected TextView getTitleText() {
        return mTitleText;
    }

    protected ViewStub getButtonsStub() {
        return mButtonsStub;
    }
}

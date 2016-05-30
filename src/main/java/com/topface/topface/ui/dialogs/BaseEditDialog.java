package com.topface.topface.ui.dialogs;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.adapters.AbstractEditAdapter;
import com.topface.topface.ui.adapters.EditAdapterFactory;

/**
 * Abstract dialog for editing fields
 */
public class BaseEditDialog<T extends Parcelable> extends BaseDialog {
    public static final String DIALOG_TITLE = "dialog_title";
    public static final String DATA = "data";

    public interface EditingFinishedListener<T extends Parcelable> {

        void onEditingFinished(T data);
    }

    private String mTitle;
    private AbstractEditAdapter<T> mAdapter;
    private ListView mOptionsList;
    private TextView mTitleText;
    private TextView mLimitText;
    private ViewStub mButtonsStub;

    @Override
    protected int getDialogStyleResId() {
        return R.style.EditDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = savedInstanceState != null ? savedInstanceState : getArguments();
        if (bundle != null) {
            mTitle = bundle.getString(DIALOG_TITLE);
            T data = bundle.getParcelable(DATA);
            mAdapter = new EditAdapterFactory().createAdapterFor(getActivity(), data, App.from(getActivity()).getProfile());
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
        mLimitText = (TextView) root.findViewById(R.id.edit_dialog_limit);
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

    @SuppressWarnings("unused")
    protected ListView getOptionsList() {
        return mOptionsList;
    }

    protected TextView getTitleText() {
        return mTitleText;
    }

    protected TextView getLimitText() {
        return mLimitText;
    }

    protected ViewStub getButtonsStub() {
        return mButtonsStub;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        AbstractEditAdapter<T> adapter = getAdapter();
        if (adapter != null) {
            outState.putParcelable(DATA, adapter.getCurrentData());
            outState.putString(DIALOG_TITLE, mTitle);
        }
    }
}

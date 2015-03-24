package com.topface.topface.ui.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.ui.adapters.AbstractSelectorAdapter;
import com.topface.topface.ui.adapters.SelectorAdapterFactory;

/**
 * Abstract dialog for editing fields
 */
public class AbstractSelectorDialog<T extends Parcelable> extends AbstractDialogFragment {
    public static final String DIALOG_TITLE = "dialog_title";
    public static final String DATA = "data";

    public interface EditingFinishedListener<T extends Parcelable> {

        void onEditingFinished(T data);
    }

    private String mTitle;
    private AbstractSelectorAdapter mAdapter;
    private ListView mOptionsList;
    private TextView mTitleText;

    @Override
    protected void applyStyle() {
        setStyle(ConfirmEmailDialog.STYLE_NO_TITLE, R.style.Selector);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Bundle args = getArguments();
        if (args != null) {
            mTitle = args.getString(DIALOG_TITLE);
            T data = (T) args.getParcelable(DATA);
            mAdapter = new SelectorAdapterFactory().createSelectorFor(data);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_selector_dialog, container, false);
        initViews(view);
        return view;
    }

    @Override
    protected void initViews(View root) {
        mOptionsList = (ListView) root.findViewById(R.id.optionsList);
        mOptionsList.setAdapter(mAdapter);
        mTitleText = (TextView) root.findViewById(R.id.selector_dialog_title);
        mTitleText.setText(mTitle);
    }

    /**
     * This dialog uses it own layout. So no need for layout res.
     * @return 0
     */
    @Override
    public int getDialogLayoutRes() {
        return 0;
    }

    public String getTitle() {
        return mTitle;
    }

    public AbstractSelectorAdapter<T> getAdapter() {
        return mAdapter;
    }

    protected ListView getOptionsList() {
        return mOptionsList;
    }

    protected TextView getTitleText() {
        return mTitleText;
    }
}

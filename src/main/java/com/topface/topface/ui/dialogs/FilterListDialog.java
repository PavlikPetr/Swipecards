package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.topface.topface.R;
import com.topface.topface.ui.adapters.FilterDialogAdapter;
import com.topface.topface.utils.FormInfo;

public class FilterListDialog extends AbstractDialogFragment {

    public static final String TAG = "com.topface.topface.ui.dialogs.FilterListDialog_TAG";

    private ListView mList;
    private int mTitleId;
    private int mTargetId;
    private int mViewId;
    private DialogRowCliCkInterface mDialogRowCliCkInterface;
    private FormInfo mFormInfo;

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
    }

    public static FilterListDialog newInstance() {
        return new FilterListDialog();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, R.style.Theme_Topface_NoActionBar_Filterlist);
    }

    @Override
    protected void initViews(View root) {
        mList = (ListView) root.findViewWithTag("loFilterList");
        setAdapter();
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDialogRowCliCkInterface != null) {
                    mDialogRowCliCkInterface.onRowClickListener(mViewId, mFormInfo.getIdsByTitleId(mTitleId)[position]);
                }
                closeDialog();
            }
        });
    }

    @Override
    protected boolean isModalDialog() {
        return false;
    }

    private void setAdapter() {
        mList.setAdapter(new FilterDialogAdapter(getActivity(),
                R.layout.filter_edit_form_dialog_cell,
                mFormInfo.getEntriesByTitleId(mTitleId),
                mFormInfo.getEntry(mTitleId, mTargetId)));
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.filter_dialog_layout;
    }

    public interface DialogRowCliCkInterface {
        void onRowClickListener(int id, int item);
    }

    public void setData(int titleId, int targetId, int viewId,
                        DialogRowCliCkInterface listener, FormInfo formInfo) {
        mTitleId = titleId;
        mTargetId = targetId;
        mViewId = viewId;
        mDialogRowCliCkInterface = listener;
        mFormInfo = formInfo;
    }
}

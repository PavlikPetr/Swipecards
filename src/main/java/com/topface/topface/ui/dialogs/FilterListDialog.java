package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.topface.topface.R;
import com.topface.topface.ui.adapters.FilterDialogAdapter;
import com.topface.topface.ui.edit.FilterFragment;
import com.topface.topface.utils.FormInfo;

public class FilterListDialog extends BaseDialog {

    public static final String TAG = "com.topface.topface.ui.dialogs.FilterListDialog_TAG";
    private static final String SEX = "sex";
    private static final String PROFILE_TYPE = "profile_type";
    private static final String TITLE_ID = "title_id";
    private static final String TARGET_ID = "target_id";
    private static final String VIEW_ID = "view_id";

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFormInfo = new FormInfo(getActivity(), savedInstanceState.getInt(SEX),
                    savedInstanceState.getInt(PROFILE_TYPE));
            mTitleId = savedInstanceState.getInt(TITLE_ID);
            mTargetId = savedInstanceState.getInt(TARGET_ID);
            mViewId = savedInstanceState.getInt(VIEW_ID);
            Fragment fragment = getParentFragment();
            if (fragment instanceof FilterFragment) {
                mDialogRowCliCkInterface = ((FilterFragment) fragment).getDialogOnItemClickListener();
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState);
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

    private void setAdapter() {
        if (mFormInfo != null)
        mList.setAdapter(new FilterDialogAdapter(getActivity(),
                R.layout.filter_edit_form_dialog_cell,
                mFormInfo.getEntriesByTitleId(mTitleId),
                mFormInfo.getEntry(mTitleId, mTargetId)));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.putInt(SEX, mFormInfo.getSex());
            outState.putInt(PROFILE_TYPE, mFormInfo.getProfileType());
            outState.putInt(TITLE_ID, mTitleId);
            outState.putInt(TARGET_ID, mTargetId);
            outState.putInt(VIEW_ID, mViewId);
        }
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.filter_dialog_layout;
    }

    @Override
    protected int getDialogStyleResId() {
        return R.style.EditDialog;
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

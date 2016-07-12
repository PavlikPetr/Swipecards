package com.topface.topface.ui.edit.filter.view;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.DatingFilter;
import com.topface.topface.databinding.FilterFragmentBinding;
import com.topface.topface.ui.edit.AbstractEditFragment;
import com.topface.topface.ui.edit.filter.model.FilterData;
import com.topface.topface.ui.edit.filter.viewModel.FilterViewModel;
import com.topface.topface.utils.IActivityDelegate;

import static com.topface.topface.ui.edit.FilterFragment.INTENT_DATING_FILTER;

public class FilterFragment extends AbstractEditFragment {

    public static String TAG = "filter_fragment_tag";
    public static final String INTENT_DATING_FILTER = "topface_dating_filter";
    private static final String PAGE_NAME = "Filter";

    private FilterFragmentBinding mBinding;
    private FilterViewModel mViewModel;
    private FilterData mFilter;

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mBinding = DataBindingUtil.inflate(inflater, R.layout.filter_fragment, container, false);
        initFilter();
        mViewModel = new FilterViewModel(mBinding, (IActivityDelegate) getActivity(), mFilter);
        mBinding.setViewModel(mViewModel);
        return mBinding.getRoot();
    }

    private void initFilter() {
        mFilter = new FilterData(new DatingFilter());
        try {
            DatingFilter dating = App.get().getProfile().dating;
            mFilter = new FilterData((dating != null) ? dating.clone() : new DatingFilter());
        } catch (CloneNotSupportedException e) {
            Debug.error(e);
        }
    }

    @Override
    protected boolean hasChanges() {
        return (mFilter != null && mViewModel != null) && mFilter.equals(new FilterData(mViewModel));
    }

    @Override
    protected void saveChanges(final Handler handler) {
        if (hasChanges()) {
            Intent intent = new Intent();
            intent.putExtra(INTENT_DATING_FILTER, mFilter);
            getActivity().setResult(Activity.RESULT_OK, intent);
        } else {
            getActivity().setResult(Activity.RESULT_CANCELED);
        }
        handler.sendEmptyMessage(0);
    }

    @Override
    protected void lockUi() {
        mViewModel.isEnabled.set(false);
    }

    @Override
    protected void unlockUi() {
        mViewModel.isEnabled.set(true);
    }

    @Override
    protected void refreshSaveState() {
        super.refreshSaveState();

    }

    @Override
    protected void prepareRequestSend() {
        super.prepareRequestSend();

    }

    @Override
    protected void finishRequestSend() {
        super.finishRequestSend();

    }

    @Override
    protected String getTitle() {
        return getString(R.string.filter_screen_title);
    }

}

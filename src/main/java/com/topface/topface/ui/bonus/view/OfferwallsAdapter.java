package com.topface.topface.ui.bonus.view;

import android.os.Bundle;

import com.topface.topface.R;
import com.topface.topface.databinding.OfferwallItemBinding;
import com.topface.topface.ui.adapters.BaseRecyclerViewAdapter;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.ui.bonus.viewModel.OfferwallItemViewModel;

public class OfferwallsAdapter extends BaseRecyclerViewAdapter<OfferwallItemBinding, IOfferwallBaseModel> {
    @Override
    protected Bundle getUpdaterEmitObject() {
        return null;
    }

    @Override
    protected int getItemLayout() {
        return R.layout.offerwall_item;
    }

    @Override
    protected void bindData(OfferwallItemBinding binding, int position) {
        binding.setViewModel(new OfferwallItemViewModel(getData().get(position)));
    }

}

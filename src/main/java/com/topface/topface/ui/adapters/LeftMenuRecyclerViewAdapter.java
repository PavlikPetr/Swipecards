package com.topface.topface.ui.adapters;

import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.text.SpannableString;

import com.topface.topface.BR;
import com.topface.topface.R;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.FixedViewInfo;
import com.topface.topface.data.HeaderFooterData;
import com.topface.topface.data.leftMenu.LeftMenuData;
import com.topface.topface.data.leftMenu.LeftMenuHeaderViewData;
import com.topface.topface.databinding.LeftMenuItemBinding;
import com.topface.topface.viewModels.LeftMenuHeaderViewModel;
import com.topface.topface.viewModels.LeftMenuItemViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class LeftMenuRecyclerViewAdapter extends BaseHeaderFooterRecyclerViewAdapter<LeftMenuItemBinding, LeftMenuData> {

    public LeftMenuRecyclerViewAdapter(ArrayList<LeftMenuData> data) {
        super();
        addData(data);
    }

    @Override
    protected Bundle getUpdaterEmmitObject() {
        return null;
    }

    @Override
    protected int getItemLayout() {
        return R.layout.left_menu_item;
    }

    @Override
    protected void bindData(LeftMenuItemBinding binding, int position) {
        binding.setViewModel(new LeftMenuItemViewModel(getData().get(position)));
    }

    @Override
    protected void bindHeader(ViewDataBinding binding, int position) {
        binding.setVariable(BR.viewModel, new LeftMenuHeaderViewModel((HeaderFooterData<LeftMenuHeaderViewData>) getHeaderItem(position)));
    }

    @Override
    protected void bindFooter(ViewDataBinding binding, int position) {

    }

    @NotNull
    @Override
    protected Class<LeftMenuItemBinding> getItemBindingClass() {
        return LeftMenuItemBinding.class;
    }

    public void updateCounters(CountersData countersData) {
        ArrayList<LeftMenuData> data = getData();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                LeftMenuData item = data.get(i);
                int count = countersData.getCounterByFragmentId(item.getSettings().getFragmentId());
                if (count >= 0 && count != item.getBadgeCount()) {
                    item.setBadgeCount(count);
                    notifyItemChange(i);
                }
            }
        }
    }

    public void updateTitle(int fragmentId, String title) {
        updateTitle(fragmentId, new SpannableString(title));
    }

    public void updateTitle(int fragmentId, SpannableString title) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setTitle(title);
            notifyItemChange(pos);
        }
    }

    public void updateIcon(int fragmentId, String icon) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setIcon(icon);
            notifyItemChange(pos);
        }
    }

    public void updateSelected(int fragmentId, boolean isSelected) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setSelected(isSelected);
            notifyItemChange(pos);
        }
    }

    private int getDataPositionByFragmentId(int fragmentId) {
        ArrayList<LeftMenuData> data = getData();
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getSettings().getUniqueKey() == fragmentId) {
                return i;
            }
        }
        return EMPTY_POS;
    }

    public void updateHeader(HeaderFooterData<LeftMenuHeaderViewData> data) {
        ArrayList<FixedViewInfo> headers = getHeadersData();
        if (headers.size() > 0) {
            headers.get(0).setData(data);
            notifyItemChanged(0);
        }
    }

    private void addItemsAfterPosition(ArrayList<LeftMenuData> data, int position) {
        position = position < 0 ? 0 : position + 1;
        getData().addAll(position, data);
        notifyDataSetChanged();
    }

    public void addItemAfterFragment(LeftMenuData data, int... fragmentId) {
        int pos = getDataPositionByFragmentId(data.getSettings().getUniqueKey());
        if (pos == EMPTY_POS) {
            ArrayList<LeftMenuData> items = new ArrayList<>();
            items.add(data);
            addItemsAfterFragment(items, fragmentId);
        }
    }

    public void addItemsAfterFragment(ArrayList<LeftMenuData> data, int... fragmentId) {
        int pos = EMPTY_POS;
        for (int item : fragmentId) {
            pos = getDataPositionByFragmentId(item);
            if (pos != EMPTY_POS) {
                break;
            }
        }
        if (pos != EMPTY_POS) {
            addItemsAfterPosition(data, pos);
        } else {
            addItems(data);
        }
    }

    public void removeItem(LeftMenuData data) {
        int pos = getDataPositionByFragmentId(data.getSettings().getUniqueKey());
        if (pos != EMPTY_POS) {
            getData().remove(pos);
            notifyItemRemoved(pos);
        }
    }

    private void addItems(ArrayList<LeftMenuData> data) {
        addItemsAfterPosition(data, getData().size() - 1);
    }

    private void addItem(LeftMenuData data) {
        ArrayList<LeftMenuData> items = new ArrayList<>();
        items.add(data);
        addItems(items);
    }

    public void updateEditorsItem(LeftMenuData data) {
        int pos = getDataPositionByFragmentId(data.getSettings().getUniqueKey());
        if (pos == EMPTY_POS) {
            addItem(data);
        }
    }

    public void removeItem(int fragmentId) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().remove(pos);
            notifyDataSetChanged();
        }
    }
}

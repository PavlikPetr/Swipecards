package com.topface.topface.ui.adapters;

import android.annotation.SuppressLint;
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
import com.topface.topface.utils.ListUtils;
import com.topface.topface.viewModels.LeftMenuHeaderViewModel;
import com.topface.topface.viewModels.LeftMenuItemViewModel;

import java.util.ArrayList;

public class LeftMenuRecyclerViewAdapter extends BaseHeaderFooterRecyclerViewAdapter<LeftMenuItemBinding, LeftMenuData> {

    public LeftMenuRecyclerViewAdapter(ArrayList<LeftMenuData> data) {
        super();
        addData(data);
        setHasStableIds(true);
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public long getItemId(int position) {
        switch (getItemType(position)) {
            case TYPE_ITEM:
                return ListUtils.isEntry(position, getData()) ?
                        getData().get(position).getSettings().getUniqueKey() :
                        super.getItemId(position);
        }
        return super.getItemId(position);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(hasStableIds);
    }

    @Override
    protected Bundle getUpdaterEmitObject() {
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

    public void updateCounters(CountersData countersData) {
        ArrayList<LeftMenuData> data = getData();
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                LeftMenuData item = data.get(i);
                int count = countersData.getCounterByFragmentId(item.getSettings().getFragmentId());
                if (count >= 0 && !String.valueOf(count).equals(item.getBadge())) {
                    item.setBadge(String.valueOf(count));
                }
            }
            notifyDataSetChanged();
        }
    }

    public void updateTitle(int fragmentId, String title) {
        updateTitle(fragmentId, new SpannableString(title));
    }

    public void updateTitle(int fragmentId, SpannableString title) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setTitle(title);
            notifyDataSetChanged();
        }
    }

    public void updateIcon(int fragmentId, String icon) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setIcon(icon);
            notifyDataSetChanged();
        }
    }

    public void updateSelected(int fragmentId, boolean isSelected) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setSelected(isSelected);
            notifyItemChange(pos);
        }
    }

    public int getDataPositionByFragmentId(int fragmentId) {
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
            notifyDataSetChanged();
        }
    }

    private void addItemsAfterPosition(ArrayList<LeftMenuData> data, int position) {
        if (ListUtils.isNotEmpty(data)) {
            position = position < 0 ? 0 : position + 1;
            getData().addAll(position, data);
            notifyDataSetChanged();
        }
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
            notifyDataSetChanged();
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

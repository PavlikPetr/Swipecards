package com.topface.topface.ui.adapters;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.CountersData;
import com.topface.topface.data.leftMenu.FragmentIdData;
import com.topface.topface.data.leftMenu.LeftMenuData;
import com.topface.topface.databinding.LeftMenuItemBinding;
import com.topface.topface.viewModels.LeftMenuItemViewModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * Created by ppavlik on 06.05.16.\
 */
public class LeftMenuRecyclerViewAdapter extends BaseRecyclerViewAdapter<LeftMenuItemBinding, LeftMenuData> {

    public static final int EMPTY_POS = -1;

    private ItemEventListener.OnRecyclerViewItemClickListener<LeftMenuData> mClickListener;

    public LeftMenuRecyclerViewAdapter(ArrayList<LeftMenuData> data, ItemEventListener.OnRecyclerViewItemClickListener<LeftMenuData> listener) {
        super();
        addData(data);
        mClickListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        setOnItemClickListener(mClickListener);
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
                    notifyItemChanged(i);
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
            notifyItemChanged(pos);
        }
    }

    public void updateIcon(int fragmentId, String icon) {
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setIcon(icon);
            notifyItemChanged(pos);
        }
    }

    public void updateSelected(int fragmentId, boolean isSelected) {
        Debug.showChunkedLogError("NewMenuFragment", "updateSelected id " + fragmentId + " isSelected " + isSelected);
        int pos = getDataPositionByFragmentId(fragmentId);
        if (pos != EMPTY_POS) {
            getData().get(pos).setSelected(isSelected);
            notifyItemChanged(pos);
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

    private void addItemsAfterPosition(ArrayList<LeftMenuData> data, int position) {
        position = position < 0 ? 0 : position + 1;
        getData().addAll(position, data);
        notifyDataSetChanged();
    }

    public void addItemAfterFragment(LeftMenuData data, int... fragmentId) {
        ArrayList<LeftMenuData> items = new ArrayList<>();
        items.add(data);
        addItemsAfterFragment(items, fragmentId);
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

    public void addItems(ArrayList<LeftMenuData> data) {
        addItemsAfterPosition(data, getData().size() - 1);
    }
}

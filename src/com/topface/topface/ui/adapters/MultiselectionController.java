package com.topface.topface.ui.adapters;

import android.widget.BaseAdapter;
import com.topface.topface.data.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class MultiselectionController {

    private List<Object> mSelected = new ArrayList<Object>();
    private boolean mMultiSelection = false;
    private IMultiSelectionListener mSelectionListener;
    private BaseAdapter mAdapter;

    public MultiselectionController(BaseAdapter adapter) {
        mAdapter = adapter;
    }

    public void startMultiSelection() {
        if (!mSelected.isEmpty()) mSelected.clear();
        mMultiSelection = true;
    }

    public void onSelection(int position) {
        if (isSelected(position)) {
            removeSelection(position);
        } else {
            addSelection(position);
        }
        if (mSelectionListener != null) mSelectionListener.onSelected(mSelected.size());
    }

    public void onSelection(Object item) {
        if (isSelected(item)) {
            removeSelection(item);
        } else {
            addSelection(item);
        }
        if (mSelectionListener != null) mSelectionListener.onSelected(mSelected.size());
    }

    public void addSelection(int position) {
        if (mAdapter != null) {
            mSelected.add(mAdapter.getItem(position));
            mAdapter.notifyDataSetChanged();
        }
    }

    public void addSelection(Object item) {
        if (mAdapter != null) {
            mSelected.add(item);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void removeSelection(int position) {
        removeSelection(position, true);
    }

    public void removeSelection(int position, boolean notify) {
        if (mAdapter != null) {
            mSelected.remove(mAdapter.getItem(position));
            if (notify) mAdapter.notifyDataSetChanged();
        }
    }

    public void removeSelection(Object item, boolean notify) {
        if (mAdapter != null) {
            mSelected.remove(item);
            if (notify) mAdapter.notifyDataSetChanged();
        }
    }

    public void removeSelection(Object item) {
        removeSelection(item, true);
    }

    public boolean isSelected(int position) {
        if (mAdapter == null) return false;
        return isMultiSelectionMode() && mSelected.contains(mAdapter.getItem(position));
    }

    public boolean isSelected(Object item) {
        return isMultiSelectionMode() && mSelected.contains(item);
    }

    public void finishMultiSelection() {
        if (!mSelected.isEmpty()) mSelected.clear();
        mMultiSelection = false;
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    public boolean isMultiSelectionMode() {
        return mMultiSelection;
    }

    public void deleteAllSelectedItems() {
        for (Object item : mSelected) {
            removeSelection(item,false);
        }
        if (mAdapter != null) mAdapter.notifyDataSetChanged();
    }

    public int selectedCount() {
        return mSelected.size();
    }

    public void setMultiSelectionListener(IMultiSelectionListener listener) {
        mSelectionListener = listener;
    }

    public List<Object> getSelected() {
        return mSelected;
    }

    public interface IMultiSelectionListener {
        public void onSelected(int size);

    }
}

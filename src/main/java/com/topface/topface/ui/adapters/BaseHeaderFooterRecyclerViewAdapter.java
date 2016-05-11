package com.topface.topface.ui.adapters;


import android.annotation.SuppressLint;
import android.databinding.ViewDataBinding;
import android.support.annotation.IntDef;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.topface.topface.data.FixedViewInfo;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Created by ppavlik on 11.05.16.
 * Base adapter wit headers and footers views
 */
public abstract class BaseHeaderFooterRecyclerViewAdapter<T extends ViewDataBinding, D> extends BaseRecyclerViewAdapter<T, D> {

    public static final int EMPTY_POS = -1;

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 2;

    @IntDef({TYPE_HEADER, TYPE_ITEM, TYPE_FOOTER})
    public @interface ItemType {
    }

    private ArrayList<FixedViewInfo> mHeaders = new ArrayList<>();
    private ArrayList<FixedViewInfo> mFooters = new ArrayList<>();
    private int mCurrentPosition;

    public void setHeader(FixedViewInfo data) {
        mHeaders.add(data);
    }

    public void setFooter(FixedViewInfo data) {
        mFooters.add(data);
    }


    @SuppressLint("SwitchIntDef")
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, @ItemType int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int localPos = getLocalPosition(mCurrentPosition);
        if (localPos != EMPTY_POS && viewType != TYPE_ITEM) {
            switch (viewType) {
                case TYPE_FOOTER:
                    return new ItemViewHolder(inflater.inflate(mFooters.get(localPos).getResId(), parent, false), mItemEventListener);
                case TYPE_HEADER:
                    return new ItemViewHolder(inflater.inflate(mHeaders.get(localPos).getResId(), parent, false), mItemEventListener);
            }
        }
        return new ItemViewHolder(inflater.inflate(getItemLayout(), parent, false), mItemEventListener);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        int localPos = getLocalPosition(position);
        if (localPos == EMPTY_POS) {
            return;
        }
        switch (getItemType(position)) {
            case TYPE_ITEM:
                bindData(getItemBinding(holder), localPos);
                break;
            case TYPE_HEADER:
                bindHeader(holder.getBinding(), localPos);
                break;
            case TYPE_FOOTER:
                bindFooter(holder.getBinding(), localPos);
                break;
        }
    }

    private int getLocalPosition(int position) {
        int localPos = EMPTY_POS;
        switch (getItemType(position)) {
            case TYPE_ITEM:
                localPos = position - mHeaders.size();
                break;
            case TYPE_HEADER:
                localPos = position;
                break;
            case TYPE_FOOTER:
                localPos = position - mHeaders.size() - getData().size();
                break;
        }
        return localPos;
    }

    @ItemType
    private int getItemType(int pos) {
        if (pos < mHeaders.size()) {
            return TYPE_HEADER;
        } else if (pos >= mHeaders.size() + getData().size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    @Override
    public int getItemViewType(int position) {
        mCurrentPosition = position;
        return getItemType(position);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + mHeaders.size() + mFooters.size();
    }

    @Nullable
    public Object getHeaderItem(int pos) {
        if (mHeaders != null && mHeaders.size() > pos) {
            return mHeaders.get(pos).getData();
        }
        return null;
    }

    @Nullable
    public Object getFooterItem(int pos) {
        if (mFooters != null && mFooters.size() > pos) {
            return mFooters.get(pos).getData();
        }
        return null;
    }

    public ArrayList<FixedViewInfo> getHeadersData() {
        return mHeaders;
    }

    public ArrayList<FixedViewInfo> getFootersData() {
        return mFooters;
    }

    public void notifyItemChange(int pos) {
        if (pos < getItemCount()) {
            notifyItemChanged(pos + mHeaders.size());
        }
    }

    protected abstract void bindHeader(ViewDataBinding binding, int position);

    protected abstract void bindFooter(ViewDataBinding binding, int position);
}

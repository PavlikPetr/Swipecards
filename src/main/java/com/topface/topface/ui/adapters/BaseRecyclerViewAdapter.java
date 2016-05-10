package com.topface.topface.ui.adapters;


import android.annotation.SuppressLint;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.framework.utils.Debug;
import com.topface.topface.BR;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiberal on 02.05.16.
 */
public abstract class BaseRecyclerViewAdapter<T extends ViewDataBinding, D> extends RecyclerView.Adapter<BaseRecyclerViewAdapter.ItemViewHolder> implements IAdapterDataInteractor<D> {

    public static final int EMPTY_POS = -1;

    private static final int TYPE_HEADER = 1;
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_FOOTER = 2;

    @IntDef({TYPE_HEADER, TYPE_ITEM, TYPE_FOOTER})
    public @interface ItemType {
    }

    private ArrayList<D> mAdapterData = new ArrayList<>();
    private Observable<Bundle> updateObservable;
    private Subscriber<? super Bundle> mUpdateSubscriber;
    @Nullable
    private ItemEventListener<D> mItemEventListener;
    private ItemEventListener.OnRecyclerViewItemClickListener<D> mItemClick;
    private ItemEventListener.OnRecyclerViewItemLongClickListener<D> mItemLongClick;
    private ArrayList<FixedViewInfo> mHeaders = new ArrayList<>();
    private ArrayList<FixedViewInfo> mFooters = new ArrayList<>();
    //// TODO: 04.05.16
    /*
    @Nullable
    private View mHeaderView;
    @Nullable
    private View mFooterView;
    */

    public BaseRecyclerViewAdapter() {
        updateObservable = Observable.create(new Observable.OnSubscribe<Bundle>() {
            @Override
            public void call(Subscriber<? super Bundle> subscriber) {
                mUpdateSubscriber = subscriber;
                mUpdateSubscriber.onNext(new Bundle());
            }
        });
    }

    public void setHeader(FixedViewInfo data) {
        mHeaders.add(data);
    }

    public void setFooter(FixedViewInfo data) {
        mFooters.add(data);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mItemEventListener = new ItemEventListener<D>(recyclerView) {
            @Override
            public D getDataItem(int pos) {
                return mAdapterData.get(pos);
            }
        };
        if (mItemClick != null) {
            mItemEventListener.registerClickListener(mItemClick);
        }
        if (mItemLongClick != null) {
            mItemEventListener.registerLongClickListener(mItemLongClick);
        }
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = ((LinearLayoutManager) manager);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    if (!getData().isEmpty()) {
                        int firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                        int visibleItemCount = lastVisibleItem - firstVisibleItem;
                        if (mUpdateSubscriber != null && visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= mAdapterData.size() - 1) {
                            mUpdateSubscriber.onNext(getUpdaterEmmitObject());
                        }
                    } else {
                        mUpdateSubscriber.onNext(new Bundle());
                    }
                }
            });
        } else {
            Debug.debug(this, "Wrong layout manager");
        }
    }

    @SuppressLint("SwitchIntDef")
    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewPos) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        int localPos = getLocalPosition(viewPos);
        @ItemType
        int itemType = getItemType(viewPos);
        if (localPos != EMPTY_POS && itemType != TYPE_ITEM) {
            switch (getItemType(viewPos)) {
                case TYPE_FOOTER:
                    return new ItemViewHolder(mFooters.get(localPos).getView().getRoot(), mItemEventListener);
                case TYPE_HEADER:
                    return new ItemViewHolder(mFooters.get(localPos).getView().getRoot(), mItemEventListener);
            }
        }
        return new ItemViewHolder(inflater.inflate(getItemLayout(), null, false), mItemEventListener);
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
                bindHeader(mHeaders.get(localPos).getView(), localPos);
                break;
            case TYPE_FOOTER:
                bindFooter(mFooters.get(localPos).getView(), localPos);
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
                localPos = position - mHeaders.size() - mAdapterData.size();
                break;
        }
        return localPos;
    }

    @ItemType
    private int getItemType(int pos) {
        if (pos < mHeaders.size()) {
            return TYPE_HEADER;
        } else if (pos >= mHeaders.size() + mAdapterData.size()) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ITEM;
        }
    }

    // хак, чтобы получить в onCreateViewHolder позицию
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return mAdapterData.size();
    }

    public void addData(ArrayList<D> data) {
        mAdapterData.addAll(data);
        // TODO: 04.05.16 обновлять только вставленное
        notifyDataSetChanged();
    }

    public ArrayList<D> getData() {
        return mAdapterData;
    }

    @Nullable
    public Object getHeaderItem(int pos) {
        if (mHeaders != null && mHeaders.size() > pos) {
            mHeaders.get(pos);
        }
        return null;
    }

    @Nullable
    public Object getFooterItem(int pos) {
        if (mFooters != null && mFooters.size() > pos) {
            mFooters.get(pos);
        }
        return null;
    }

    public Observable<Bundle> getUpdaterObservable() {
        return updateObservable;
    }

    private T getItemBinding(BaseRecyclerViewAdapter.ItemViewHolder holder) {
        return getItemBindingClass().cast(holder.getBinding());
    }

    public void setOnItemClickListener(ItemEventListener.OnRecyclerViewItemClickListener<D> itemClick) {
        if (mItemEventListener != null) {
            mItemEventListener.registerClickListener(itemClick);
        } else {
            mItemClick = itemClick;
        }
    }

    public void setOnItemLongClickListener(ItemEventListener.OnRecyclerViewItemLongClickListener<D> itemLongClick) {
        if (mItemEventListener != null) {
            mItemEventListener.registerLongClickListener(itemLongClick);
        } else {
            mItemLongClick = itemLongClick;
        }
    }

    protected abstract Bundle getUpdaterEmmitObject();

    @LayoutRes
    protected abstract int getItemLayout();

    protected abstract void bindData(T binding, int position);

    protected abstract void bindHeader(ViewDataBinding binding, int position);

    protected abstract void bindFooter(ViewDataBinding binding, int position);

    @NotNull
    protected abstract Class<T> getItemBindingClass();

    public static class ItemViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
        private T mBinding;

        public ItemViewHolder(View view, ItemEventListener listener) {
            super(view);
            mBinding = DataBindingUtil.bind(view);
            if (listener != null) {
                mBinding.setVariable(BR.clickListener, listener);
                mBinding.setVariable(BR.longClickListener, listener);
            }
        }

        @Nullable
        public T getBinding() {
            return mBinding;
        }
    }

    public class FixedViewInfo<K extends ViewDataBinding, L> {
        private K mViewDataBinding;
        private L mData;

        public FixedViewInfo(K view, L data) {
            mViewDataBinding = view;
            mData = data;
        }

        public K getView() {
            return mViewDataBinding;
        }

        public L getData() {
            return mData;
        }
    }
}

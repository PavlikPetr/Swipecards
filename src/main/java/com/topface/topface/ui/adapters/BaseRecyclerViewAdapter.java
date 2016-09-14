package com.topface.topface.ui.adapters;


import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.framework.utils.Debug;
import com.topface.topface.BR;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

public abstract class BaseRecyclerViewAdapter<T extends ViewDataBinding, D> extends RecyclerView.Adapter<BaseRecyclerViewAdapter.ItemViewHolder> implements IAdapterDataInteractor<D> {

    public static final int EMPTY_POS = -1;

    private ArrayList<D> mAdapterData = new ArrayList<>();
    private Observable<Bundle> updateObservable;
    private Subscriber<? super Bundle> mUpdateSubscriber;
    @Nullable
    protected ItemEventListener<D> mItemEventListener;
    private ItemEventListener.OnRecyclerViewItemClickListener<D> mItemClick;
    private ItemEventListener.OnRecyclerViewItemLongClickListener<D> mItemLongClick;
    private RecyclerView mRecyclerView;

    public BaseRecyclerViewAdapter() {
        updateObservable = Observable.create(new Observable.OnSubscribe<Bundle>() {
            @Override
            public void call(Subscriber<? super Bundle> subscriber) {
                mUpdateSubscriber = subscriber;
                mUpdateSubscriber.onNext(new Bundle());
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        mItemEventListener = new ItemEventListener<D>() {
            @Override
            public D getDataItem(int pos) {
                return mAdapterData.get(pos);
            }

            @Override
            public int getPosition(View v) {
                return getPositionByView(v);
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
                    if (mUpdateSubscriber != null) {
                        if (!getData().isEmpty()) {
                            int firstVisibleItem = linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                            int lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                            int visibleItemCount = lastVisibleItem - firstVisibleItem;
                            if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= mAdapterData.size() - 1) {
                                mUpdateSubscriber.onNext(getUpdaterEmitObject());
                            }
                        } else {
                            mUpdateSubscriber.onNext(new Bundle());
                        }
                    }
                }
            });
        } else {
            Debug.debug(this, "Wrong layout manager");
        }
    }

    protected int getPositionByView(View v) {
        return mRecyclerView.getLayoutManager().getPosition(v);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new ItemViewHolder(DataBindingUtil.inflate(inflater, getItemLayout(), parent, false), mItemEventListener);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        bindData((T) holder.binding, position);
    }

    @Override
    public int getItemCount() {
        return mAdapterData.size();
    }

    public void addData(ArrayList<D> data, int position) {
        boolean notifyAll = mAdapterData.isEmpty();
        if (position == EMPTY_POS) {
            int startUpdatePosition = mAdapterData.size() == 0 ? 0 : mAdapterData.size();
            mAdapterData.addAll(data);
            notifyAdapterData(startUpdatePosition, data.size(), notifyAll);
        } else {
            mAdapterData.addAll(position, data);
            notifyAdapterData(position, data.size(), notifyAll);
        }
    }

    private void notifyAdapterData(int positionStart, int itemCount, boolean notifyAll) {
        if (notifyAll) {
            notifyDataSetChanged();
        } else {
            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    public void addFirst(ArrayList<D> data) {
        addData(data, 0);
    }

    public void addFirst(D data) {
        ArrayList<D> list = new ArrayList<>();
        list.add(data);
        addFirst(list);
    }

    public void addData(ArrayList<D> data) {
        addData(data, EMPTY_POS);
    }

    public void clearData() {
        mAdapterData.clear();
        notifyDataSetChanged();
    }

    public ArrayList<D> getData() {
        return mAdapterData;
    }

    public D getDataItem(int position) {
        return mAdapterData.get(position);
    }

    public Observable<Bundle> getUpdaterObservable() {
        return updateObservable;
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

    @Nullable
    protected abstract Bundle getUpdaterEmitObject();

    @LayoutRes
    protected abstract int getItemLayout();

    protected abstract void bindData(T binding, int position);

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        public ViewDataBinding binding;

        public ItemViewHolder(View view, ItemEventListener listener) {
            super(view);
            binding = DataBindingUtil.bind(view);
            bindClickListeners(listener);
        }

        public ItemViewHolder(ViewDataBinding binding, ItemEventListener<?> mItemEventListener) {
            super(binding.getRoot());
            this.binding = binding;
            bindClickListeners(mItemEventListener);
        }

        private void bindClickListeners(ItemEventListener listener) {
            if (listener != null) {
                binding.setVariable(BR.clickListener, listener);
                binding.setVariable(BR.longClickListener, listener);
            }
        }
    }
}

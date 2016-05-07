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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by tiberal on 02.05.16.
 */
public abstract class BaseRecyclerViewAdapter<T extends ViewDataBinding, D> extends RecyclerView.Adapter<BaseRecyclerViewAdapter.ItemViewHolder> implements IAdapterDataInteractor<D> {
    private ArrayList<D> mAdapterData = new ArrayList<>();
    private Observable<Bundle> updateObservable;
    private Subscriber<? super Bundle> mUpdateSubscriber;
    @Nullable
    private ItemEventListener<D> mItemEventListener;
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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mItemEventListener = new ItemEventListener<D>(recyclerView) {
            @Override
            public D getDataItem(int pos) {
                return mAdapterData.get(pos);
            }
        };
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
                        if (mUpdateSubscriber!=null && visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= mAdapterData.size() - 1) {
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

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //switch for item type
        return new ItemViewHolder(inflater.inflate(getItemLayout(), null, false), mItemEventListener);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        bindData(getItemBinding(holder), position);
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

    public Observable<Bundle> getUpdaterObservable() {
        return updateObservable;
    }

    private T getItemBinding(BaseRecyclerViewAdapter.ItemViewHolder holder) {
        return getItemBindingClass().cast(holder.getBinding());
    }

    public void setOnItemClickListener(ItemEventListener.OnRecyclerViewItemClickListener<D> itemClick) {
        if (mItemEventListener != null) {
            mItemEventListener.registerClickListener(itemClick);
        }
    }

    public void setOnItemLongClickListener(ItemEventListener.OnRecyclerViewItemLongClickListener<D> itemLongClick) {
        if (mItemEventListener != null) {
            mItemEventListener.registerLongClickListener(itemLongClick);
        }
    }

    protected abstract Bundle getUpdaterEmmitObject();

    @LayoutRes
    protected abstract int getItemLayout();

    protected abstract void bindData(T binding, int position);

    @NotNull
    protected abstract Class<T> getItemBindingClass();

    public static class ItemViewHolder<T extends ViewDataBinding> extends RecyclerView.ViewHolder {
        private T mBinding;
        @SuppressWarnings("unused")
        private ViewDataBinding mFooterOrHeaderBinding;
        private View mHeaderOrFooterView;

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

        @SuppressWarnings("unused")
        @NotNull
        public View getHolderView() {
            return mHeaderOrFooterView;
        }

        @Nullable
        public ViewDataBinding getFooterOrHeaderBinding() {
            return mFooterOrHeaderBinding;
        }
    }
}

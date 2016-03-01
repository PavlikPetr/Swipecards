package com.topface.topface.ui.adapters;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.data.FeedGift;

import java.util.ArrayList;

/**
 * Адаптер, который помогает юзать старый адаптер подарков, для RecyclerView
 * Created by tiberal on 08.02.16.
 */
public abstract class UserGiftsRecyclerAdapter extends RecyclerView.Adapter<UserGiftsRecyclerAdapter.ViewHolder> {

    private final GiftsAdapter mGridAdapter;
    private final LoadingListAdapter.Updater mUpdateCallback;

    public UserGiftsRecyclerAdapter(Context context, LoadingListAdapter.Updater updateCallback) {
        mGridAdapter = new GiftsAdapter(context, new FeedList<FeedGift>(), updateCallback);
        mUpdateCallback = updateCallback;
    }

    @Override
    public UserGiftsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mGridAdapter.getViewByType(viewType, 0, null, parent);
        return new ViewHolder(view, (GiftsAdapter.ViewHolder) view.getTag());
    }

    protected abstract void handleOldViewHolder(GiftsAdapter.ViewHolder oldHolder, FeedGift feedGift);

    @Override
    public void onBindViewHolder(UserGiftsRecyclerAdapter.ViewHolder holder, int position) {
        FeedGift feedGift = mGridAdapter.getData().get(position);
        if (feedGift != null) {
            handleOldViewHolder(holder.getOldHolder(), feedGift);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mGridAdapter.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return mGridAdapter.getCount();
    }

    @Override
    public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
        final GridLayoutManager gridLayoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {

            final int padding = (int) recyclerView.getContext().getResources().getDimension(R.dimen.user_gifts_padding);
            final int spanCount = gridLayoutManager.getSpanCount();

            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (((gridLayoutManager.getPosition(view) + 1) % spanCount) == 0) {
                    outRect.right = padding;
                    outRect.left = padding;
                    outRect.top = padding;
                    return;
                }
                outRect.left = padding;
                outRect.top = padding;
            }
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = gridLayoutManager.findFirstVisibleItemPosition();
                int lastVisibleItem = gridLayoutManager.findLastVisibleItemPosition();
                int visibleItemCount = lastVisibleItem - firstVisibleItem;
                if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= getData().size() - 1) {
                    FeedList<FeedGift> data = getData();
                    if (mUpdateCallback != null && !data.isEmpty() && data.getLast().isLoader()) {
                        mUpdateCallback.onUpdate();
                    }
                }
            }
        });
    }

    public void add(FeedGift item) {
        mGridAdapter.add(item);
    }

    public FeedList<FeedGift> getData() {
        return mGridAdapter.getData();
    }

    public void setData(ArrayList<FeedGift> feedGifts, boolean more) {
        mGridAdapter.setData(feedGifts, more);
    }

    public boolean isEmpty() {
        return mGridAdapter.isEmpty();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private GiftsAdapter.ViewHolder mOldHolder;

        public ViewHolder(View itemView, GiftsAdapter.ViewHolder holder) {
            super(itemView);
            mOldHolder = holder;
        }

        public GiftsAdapter.ViewHolder getOldHolder() {
            return mOldHolder;
        }
    }
}

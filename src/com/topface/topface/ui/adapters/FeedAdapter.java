package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;

import java.util.LinkedList;

/**
 *
 * @param <T>
 */
public class FeedAdapter<T extends IListLoader> extends LoadingListAdapter implements AbsListView.OnScrollListener {

    private Context mContext;
    private LinkedList<T> mData;
    private LayoutInflater mInflater;
    private Updater mUpdateCallback;

    public FeedAdapter (Context context, LinkedList<T> data, Updater updateCallback) {
        mContext = context;
        mData = data;
        mInflater = LayoutInflater.from(context);
        mLoaderRetrier = getLoaderRetrier();
        mLoaderRetrierText = getLoaderRetrierText();
        mLoaderRetrierProgress = getLoaderRetrierProgress();
        mUpdateCallback = updateCallback;
    }

    public FeedAdapter (Context context, LinkedList<T> data) {
        this(context, data, null);
    }

    private ProgressBar getLoaderRetrierProgress() {
        return (ProgressBar) mLoaderRetrier.findViewById(R.id.prsLoader);
    }

    private TextView getLoaderRetrierText() {
        return (TextView) mLoaderRetrier.findViewById(R.id.tvLoaderText);
    }

    private View getLoaderRetrier() {
        return mInflater.inflate(R.layout.item_list_loader_retrier, null, false);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        int type = getItemViewType(position);
        View resultView;

        switch (type) {
            case T_LOADER:
                resultView = getLoaderView();
                break;
            case T_RETRIER:
                resultView = getRetrierView();
                break;
            default:
                resultView = getContentView(position, view, viewGroup);
        }

        return resultView;
    }

    private View getRetrierView() {
        mLoaderRetrierProgress.setVisibility(View.INVISIBLE);
        mLoaderRetrierText.setVisibility(View.VISIBLE);
        return mLoaderRetrier;
    }

    private View getLoaderView() {
        mLoaderRetrierProgress.setVisibility(View.VISIBLE);
        mLoaderRetrierText.setVisibility(View.INVISIBLE);
        return mLoaderRetrier;
    }

    protected View getContentView(int position, View view, ViewGroup viewGroup) {
        return null;
    }

    protected Context getContext() {
        return mContext;
    }

    protected LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {}

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            if (mUpdateCallback != null && mData.size() > 0 && mData.getLast().isLoader()) {
                mUpdateCallback.onFeedUpdate();
            }
        }
    }

    public static interface Updater {
        void onFeedUpdate();
    }

}

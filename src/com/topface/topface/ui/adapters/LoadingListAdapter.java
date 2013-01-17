package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.LoaderData;

/**
 * Adapter Operates with IListLoader interface.
 * Contains 2 basic types of views:
 * - Loader(typeId:T_LOADER = 0)
 * - Retrier(typeId:T_RETRIER = 1)
 * Methods which working with types are Overridden
 *
 * @author kirussell
 */
public abstract class LoadingListAdapter<T extends LoaderData> extends BaseAdapter {

    public static final int T_OTHER = 0;
    public static final int T_LOADER = 1;
    public static final int T_RETRIER = 2;
    private static final int TYPE_COUNT = 3;

    protected FeedList<T> mData;

    protected Context mContext;
    protected LayoutInflater mInflater;
    protected Updater mUpdateCallback;
    protected View mLoaderRetrier;
    protected TextView mLoaderRetrierText;
    protected ProgressBar mLoaderRetrierProgress;

    public LoadingListAdapter(Context context, Updater updateCallback) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mUpdateCallback = updateCallback;
        mLoaderRetrier = getLoaderRetrier();
        mLoaderRetrierText = getLoaderRetrierText();
        mLoaderRetrierProgress = getLoaderRetrierProgress();
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    @Override
    public T getItem(int i) {
        return mData.hasItem(i) ? mData.get(i) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    /**
     * returns T_LOADER, T_RETRIER and T_NONE for other types
     */
    @Override
    public int getItemViewType(int position) {
        IListLoader item = getItem(position);
        if (item == null) return T_OTHER;
        if (getItem(position).isLoader())
            return T_LOADER;
        else if (getItem(position).isLoaderRetry())
            return T_RETRIER;
        else
            return T_OTHER;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != T_LOADER;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    public static interface Updater {
        void onFeedUpdate();
    }

    public FeedList<T> getData() {
        if (mData == null) {
            mData = new FeedList<T>();
        }
        return mData;
    }

    public void showLoaderItem() {
        removeLoaderItem();
        getData().add(getLoaderItem());
        notifyDataSetChanged();
    }

    public void showRetryItem() {
        removeLoaderItem();
        getData().add(getRetryItem());
        notifyDataSetChanged();
    }

    protected View getRetrierView() {
        mLoaderRetrierProgress.setVisibility(View.INVISIBLE);
        mLoaderRetrierText.setVisibility(View.VISIBLE);
        return mLoaderRetrier;
    }

    protected View getLoaderView() {
        mLoaderRetrierProgress.setVisibility(View.VISIBLE);
        mLoaderRetrierText.setVisibility(View.INVISIBLE);
        return mLoaderRetrier;
    }

    protected int getLoaderRetrierLayout() {
        return R.layout.item_list_loader_retrier;
    }

    protected int getLoaderTextViewId() {
        return R.id.tvLoaderText;
    }

    protected int getLoaderProgreesBarId() {
        return R.id.prsLoader;
    }

    protected void addLoaderItem(boolean hasMore) {
        FeedList<T> currentData = getData();
        if (hasMore && !currentData.isEmpty()) {
            currentData.add(getLoaderItem());
        }
    }

    @SuppressWarnings("unchecked")
    private T getLoaderItem() {
        //noinspection unchecked
        return getLoaderReqtrierFactory().getLoader();
    }

    @SuppressWarnings("unchecked")
    private T getRetryItem() {
        //noinspection unchecked
        return (T) new LoaderData(IListLoader.ItemType.RETRY);
    }

    protected void removeLoaderItem() {
        FeedList<T> data = getData();
        if (!data.isEmpty()) {
            T lastItem = data.getLast();
            if (lastItem.isLoader() || lastItem.isLoaderRetry()) {
                data.removeLast();
            } else {
                T firstItem = data.getFirst();
                if (firstItem.isLoader() || firstItem.isLoaderRetry()) {
                    data.removeFirst();
                }
            }
        }
    }

    private View getLoaderRetrier() {
        return mInflater.inflate(getLoaderRetrierLayout(), null, false);
    }

    private TextView getLoaderRetrierText() {
        return (TextView) mLoaderRetrier.findViewById(getLoaderTextViewId());
    }

    private ProgressBar getLoaderRetrierProgress() {
        return (ProgressBar) mLoaderRetrier.findViewById(getLoaderProgreesBarId());
    }

    public abstract ILoaderRetrierFactory<T> getLoaderReqtrierFactory();

    public interface ILoaderRetrierFactory<T> {
        T getLoader();
        T getRetrier();
    }

    public void release() {
        if (mData != null) {
            mData.clear();
        }
        mData = null;
        mInflater = null;
    }

}

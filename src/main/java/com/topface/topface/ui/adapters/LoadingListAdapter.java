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
import com.topface.topface.utils.adapter_utils.IInjectViewBucketRegistrator;
import com.topface.topface.utils.adapter_utils.InjectViewBucket;
import com.topface.topface.utils.adapter_utils.ViewInjectManager;
import com.topface.topface.utils.ListUtils;
import com.topface.topface.utils.loadcontollers.LoadController;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

/**
 * Adapter Operates with IListLoader interface.
 * Contains 2 basic types of views:
 * - Loader(typeId:T_LOADER = 0)
 * - Retrier(typeId:T_RETRIER = 1)
 * Methods which working with types are Overridden
 *
 * @author kirussell
 */
public abstract class LoadingListAdapter<T extends LoaderData> extends BaseAdapter implements IInjectViewBucketRegistrator {

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
    protected LoadController mLoadController;
    private boolean mMore;

    private ViewInjectManager injectManager;

    public LoadingListAdapter(Context context, FeedList<T> data, Updater updateCallback) {
        mContext = context.getApplicationContext();
        injectManager = new ViewInjectManager(mContext);
        mInflater = LayoutInflater.from(mContext);
        mLoadController = initLoadController();
        mData = new FeedList<>();
        if (data != null) {
            mData.addAll(data);
        }
        mUpdateCallback = updateCallback;
        mLoaderRetrier = getLoaderRetrier();
        mLoaderRetrierText = getLoaderRetrierText();
        mLoaderRetrierProgress = getLoaderRetrierProgress();
    }

    @Override
    public int getCount() {
        return mData != null ? mData.size() : 0;
    }

    protected abstract LoadController initLoadController();

    @Override
    @Nullable
    public T getItem(int position) {
        if (injectManager.isFakePosition(position)) {
            return null;
        }
        int pos = injectManager.getTruePosition(position);
        return ListUtils.isEntry(pos, mData) ? mData.get(pos) : null;
    }

    @Override
    public long getItemId(int position) {
        return injectManager.getTruePosition(position);
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        View injectView = injectManager.getView(position);
        int type = getItemViewType(position);
        return injectView != null ? injectView : getViewByType(type, position, view, viewGroup);
    }

    protected View getViewByType(int type, int position, View view, ViewGroup viewGroup) {
        switch (type) {
            case T_LOADER:
                return getLoaderView();
            case T_RETRIER:
                return getRetrierView();
            default:
                return getContentView(position, view, viewGroup);
        }
    }

    protected abstract View getContentView(int position, View convertView, ViewGroup viewGroup);

    /**
     * returns T_LOADER, T_RETRIER and T_NONE for other types
     */
    @Override
    public int getItemViewType(int position) {
        IListLoader item = getItem(position);
        if (item == null) {
            return T_OTHER;
        }
        //noinspection ConstantConditions
        if (getItem(position).isLoader()) {
            return T_LOADER;
        } else //noinspection ConstantConditions
            if (getItem(position).isRetrier()) {
                return T_RETRIER;
            } else {
                return T_OTHER;
            }
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) != T_LOADER;
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    public void add(int i, T item) {
        getData().add(i, item);
    }

    public void add(T item) {
        getData().add(item);
    }

    public interface Updater {
        void onUpdate();
    }

    public FeedList<T> getData() {
        if (mData == null) {
            mData = new FeedList<>();
        }
        return mData;
    }

    @NotNull
    public FeedList<T> getEquals(T item) {
        FeedList<T> list = new FeedList<>();
        for (T currentItem : getData()) {
            if (currentItem.equals(item)) {
                list.add(currentItem);
            }
        }
        return list;
    }

    public void setData(ArrayList<T> data, boolean more) {
        setData(data, more, true);
    }

    public void addAll(ArrayList<T> data, boolean more) {
        addAll(data, more, true);
    }

    public void addFirst(ArrayList<T> data, boolean more) {
        addFirst(data, more, true);
    }

    protected void setData(ArrayList<T> dataList, boolean more, boolean notify) {
        removeLoaderItem();
        ArrayList<T> data = getData();
        data.clear();
        data.addAll(dataList);
        mMore = more;
        addLoaderItem(more);
        if (notify) notifyDataSetChanged();
    }

    protected boolean isNeedMore() {
        return mMore;
    }

    protected void addAll(ArrayList<T> dataList, boolean more, boolean notify) {
        removeLoaderItem();
        if (!dataList.isEmpty()) getData().addAll(dataList);
        addLoaderItem(more);
        if (notify) notifyDataSetChanged();
    }

    @SuppressWarnings("unused")
    protected void addFirst(ArrayList<T> data, boolean more, boolean notify) {
        if (data != null) {
            if (!data.isEmpty()) {
                getData().addAllFirst(data);
            }
        }
        if (notify) notifyDataSetChanged();
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
        mMore = hasMore;
        if (hasMore && !currentData.isEmpty()) {
            currentData.add(getLoaderItem());
        }
    }

    @SuppressWarnings("unchecked")
    protected final T getLoaderItem() {
        //noinspection unchecked
        return getLoaderRetrierCreator().getLoader();
    }

    @SuppressWarnings("unchecked")
    protected final T getRetryItem() {
        //noinspection unchecked
        return getLoaderRetrierCreator().getRetrier();
    }

    protected void removeLoaderItem() {
        FeedList<T> data = getData();
        if (!data.isEmpty()) {
            T lastItem = data.getLast();
            if (lastItem != null && (lastItem.isLoader() || lastItem.isRetrier())) {
                data.removeLast();
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

    public abstract ILoaderRetrierCreator<T> getLoaderRetrierCreator();

    public interface ILoaderRetrierCreator<T> {
        T getLoader();

        T getRetrier();
    }

    public void release() {
        mUpdateCallback = null;
        mLoadController = null;
        mLoaderRetrier = null;
        mLoaderRetrierProgress = null;
        mLoaderRetrierText = null;
        if (mData != null) {
            mData.clear();
        }
        mData = null;
        mInflater = null;
    }

    @Override
    public void registerViewBucket(InjectViewBucket bucket) {
        injectManager.registerInjectViewBucket(bucket);
    }

    @Override
    public void removeViewBucket(InjectViewBucket bucket) {
        injectManager.removeInjectViewBucket(bucket);
    }

    @Override
    public void removeAllBuckets() {
        injectManager.removeAllBuckets();
    }
}

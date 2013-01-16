package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;

/**
 * Adapter Operates with IListLoader interface.
 * Contains 2 basic types of views:
 * - Loader(typeId:T_LOADER = 0)
 * - Retrier(typeId:T_RETRIER = 1)
 * Methods which working with types are Overridden
 *
 * @author kirussell
 */
public class LoadingListAdapter extends BaseAdapter {

    public static final int T_OTHER = 0;
    public static final int T_LOADER = 1;
    public static final int T_RETRIER = 2;
    private static final int TYPE_COUNT = 3;

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
        return 0;
    }

    @Override
    public IListLoader getItem(int position) {
        return null;
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

    protected View getLoaderRetrier() {
        return mInflater.inflate(R.layout.item_list_loader_retrier, null, false);
    }

    protected TextView getLoaderRetrierText() {
        return (TextView) mLoaderRetrier.findViewById(R.id.tvLoaderText);
    }

    protected ProgressBar getLoaderRetrierProgress() {
        return (ProgressBar) mLoaderRetrier.findViewById(R.id.prsLoader);
    }

    public static interface Updater {
        void onFeedUpdate();
    }
}

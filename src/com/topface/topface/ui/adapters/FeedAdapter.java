package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.AbstractFeedItem;
import com.topface.topface.ui.views.ImageViewRemote;


/**
 * @param <T>
 */
public abstract class FeedAdapter<T extends AbstractFeedItem> extends LoadingListAdapter implements AbsListView.OnScrollListener {

    private Context mContext;
    private FeedList<T> mData;
    private LayoutInflater mInflater;
    private Updater mUpdateCallback;
    private long mLastUpdate = 0;
    public static final int LIMIT = 40;
    private static final long CACHE_TIMEOUT = 1000 * 5 * 60; //5 минут
    private OnAvatarClickListener<T> mOnAvatarClickListener;

    public FeedAdapter(Context context, FeedList<T> data, Updater updateCallback) {
        mContext = context;
        mData = data == null ? new FeedList<T>() : data;
        mInflater = LayoutInflater.from(context);
        mLoaderRetrier = getLoaderRetrier();
        mLoaderRetrierText = getLoaderRetrierText();
        mLoaderRetrierProgress = getLoaderRetrierProgress();
        mUpdateCallback = updateCallback;
    }

    static class FeedViewHolder {
        public ImageViewRemote avatar;
        public TextView name;
        public TextView city;
        public TextView time;
        public ImageView online;
        public TextView text;
        public ImageView heart;
    }

    public FeedAdapter(Context context, Updater updateCallback) {
        this(context, null, updateCallback);
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
        return mData.hasItem(i) ? mData.get(i) : null;
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

    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        FeedViewHolder holder = null;

        if (convertView != null) {
            holder = (FeedViewHolder) convertView.getTag();
        }

        T item = getItem(position);

        //Если нам попался лоадер или пустой convertView, т.е. у него нет тега с данными, то заново пересоздаем этот элемент
        if (holder == null) {
            convertView = getInflater().inflate(getItemLayout(), null, false);
            holder = getEmptyHolder(convertView, item);
        }

        if (item != null) {
            holder.avatar.setPhoto(item.photo);
            holder.name.setText(getName(item));
            holder.city.setText(item.city_name);
            holder.online.setVisibility(item.online ? View.VISIBLE : View.INVISIBLE);
        }

        convertView.setTag(holder);

        return convertView;
    }

    private String getName(T item) {
        return item.first_name + ", " + item.age;
    }

    protected Context getContext() {
        return mContext;
    }

    protected LayoutInflater getInflater() {
        return mInflater;
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            if (mUpdateCallback != null && !mData.isEmpty() && mData.getLast().isLoader()) {
                mUpdateCallback.onFeedUpdate();
            }
        }
    }

    public static interface Updater {
        void onFeedUpdate();
    }

    public void setData(FeedList<T> data) {
        removeLoaderItem();
        FeedList<T> currentData = getData();
        currentData.clear();
        currentData.addAll(data);

        addLoaderItem();

        notifyDataSetChanged();
        setLastUpdate();

    }

    private void setLastUpdate() {
        mLastUpdate = System.currentTimeMillis();
    }

    private void addLoaderItem() {
        FeedList<T> currentData = getData();
        if (!currentData.isEmpty() && currentData.size() > LIMIT / 2) {
            currentData.add(getLoaderItem());
        }
    }

    public void addData(FeedList<T> data) {
        removeLoaderItem();
        if (data != null && !data.isEmpty()) {
            getData().addAll(data);
            addLoaderItem();
        }
        notifyDataSetChanged();
        setLastUpdate();
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

    private void removeLoaderItem() {
        FeedList<T> data = getData();
        if (!data.isEmpty()) {
            T lastItem = data.getLast();
            if (lastItem.isLoader() || lastItem.isLoaderRetry()) {
                data.removeLast();
            }
        }
    }

    public T getLastFeedItem() {
        T item = null;
        if (!isEmpty()) {
            FeedList<T> data = getData();
            int dataSize = data.size();

            int feedIndex = data.getLast().isLoader() || data.getLast().isLoaderRetry() ?
                    dataSize - 2 :
                    dataSize - 1;
            if (data.hasItem(feedIndex)) {
                item = data.get(feedIndex);
            }
        }

        return item;
    }

    protected FeedViewHolder getEmptyHolder(View convertView, final T item) {
        FeedViewHolder holder = new FeedViewHolder();

        holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.ivAvatar);
        //Слушаем событие клика на автарку
        if (mOnAvatarClickListener != null) {
            holder.avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnAvatarClickListener.onAvatarClick(item, v);
                }
            });
        }
        holder.name = (TextView) convertView.findViewById(R.id.tvName);
        holder.city = (TextView) convertView.findViewById(R.id.tvCity);
        holder.online = (ImageView) convertView.findViewById(R.id.ivOnline);

        return holder;
    }

    abstract protected T getNewItem(IListLoader.ItemType type);

    protected T getRetryItem() {
        return getNewItem(IListLoader.ItemType.RETRY);
    }

    protected T getLoaderItem() {
        return getNewItem(IListLoader.ItemType.LOADER);
    }

    abstract protected int getItemLayout();

    public boolean isNeedUpdate() {
        return isEmpty() || (System.currentTimeMillis() > mLastUpdate + CACHE_TIMEOUT);
    }

    public void setOnAvatarClickListener(OnAvatarClickListener<T> listener) {
        mOnAvatarClickListener = listener;
    }

    public static interface OnAvatarClickListener<T> {
        public void onAvatarClick(T item, View view);
    }
}

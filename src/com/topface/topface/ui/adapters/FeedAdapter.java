package com.topface.topface.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.ui.views.ImageViewRemote;

import java.util.Collections;


/**
 * @param <T>
 */
public abstract class FeedAdapter<T extends FeedItem> extends LoadingListAdapter<T> implements AbsListView.OnScrollListener {

    protected static final int T_NEW_VIP = 3;
    protected static final int T_VIP = 4;
    protected static final int T_NEW = 5;
    protected static final int T_COUNT = 6;

    private long mLastUpdate = 0;
    public static final int LIMIT = 40;
    private static final long CACHE_TIMEOUT = 1000 * 5 * 60; //5 минут
    private OnAvatarClickListener<T> mOnAvatarClickListener;

    public FeedAdapter(Context context, FeedList<T> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    public int getLimit() {
        return LIMIT;
    }

    protected static class FeedViewHolder {
        public ImageViewRemote avatar;
        public TextView name;
        public TextView city;
        public TextView time;
        public ImageView online;
        public TextView unreadCounter;
        public TextView text;
        public ImageView heart;
        public ViewFlipper flipper;
        public Button flippedBtn;
        public View dataLayout;
        public ImageView deleteIndicator;
    }

    public FeedAdapter(Context context, Updater updateCallback) {
        this(context, null, updateCallback);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + T_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        int superType = super.getItemViewType(position);
        if (superType == T_OTHER) {
            if (getItem(position).unread && getItem(position).user.premium) {
                return T_NEW_VIP;
            } else if (getItem(position).unread && !getItem(position).user.premium) {
                return T_NEW;
            } else if (!getItem(position).unread && getItem(position).user.premium) {
                return T_VIP;
            } else {
                return T_OTHER;
            }
        } else {
            return superType;
        }
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

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        FeedViewHolder holder = null;

        if (convertView != null) {
            holder = (FeedViewHolder) convertView.getTag();
        }

        final T item = getItem(position);
        final int type = getItemViewType(position);

        //Если нам попался лоадер или пустой convertView, т.е. у него нет тега с данными, то заново пересоздаем этот элемент
        if (holder == null) {
            int layoutId;
            if (type == T_NEW) {
                layoutId = getNewItemLayout();
            } else if (type == T_NEW_VIP) {
                layoutId = getNewVipItemLayout();
            } else if (type == T_VIP || type == LikesListAdapter.T_SELECTED_FOR_MUTUAL_VIP) {
                layoutId = getVipItemLayout();
            } else {
                layoutId = getItemLayout();
            }
            convertView = getInflater().inflate(layoutId, null, false);
            holder = getEmptyHolder(convertView, item);
        }

        if (item != null) {
            // установка аватарки пользователя
            if (item.user.banned || item.user.deleted || item.user.photo == null || item.user.photo.isEmpty()) {
                holder.avatar.setResourceSrc(item.user.sex == Static.BOY ?
                        R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar);
                if (item.user.banned || item.user.deleted) {
                    holder.avatar.setOnClickListener(null);
                }
            } else {
                holder.avatar.setPhoto(item.user.photo);
                setListenerOnAvatar(holder.avatar, item);
            }

            // установка имени
            holder.name.setText(item.user.getNameAndAge());
            if (item.user.deleted || item.user.banned) {
                holder.name.setTextColor(Color.GRAY);
            } else {
                holder.name.setTextColor(Color.WHITE);
            }

            // установка городв
            if (item.user.city != null) {
                if (item.user.deleted || item.user.banned) {
                    holder.city.setTextColor(Color.GRAY);
                } else {
                    holder.city.setTextColor(Color.WHITE);
                }
                holder.city.setText(item.user.city.name);
            }

            // установка иконки онлайн
            if (item.user.deleted || item.user.banned) {
                holder.online.setVisibility(View.INVISIBLE);
            } else {
                holder.online.setVisibility(item.user.online ? View.VISIBLE : View.INVISIBLE);
            }
        }

        convertView.setTag(holder);

        return convertView;
    }

    private void setListenerOnAvatar(ImageViewRemote avatar, final T item) {
        //Слушаем событие клика на автарку
        if (mOnAvatarClickListener != null) {
            avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnAvatarClickListener.onAvatarClick(item, v);
                }
            });
        }
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
                mUpdateCallback.onUpdate();
            }
        }
    }

    public void setData(FeedListData<T> data) {
        removeLoaderItem();
        FeedList<T> currentData = getData();
        currentData.clear();
        currentData.addAll(data.items);

        addLoaderItem(data.more);

        notifyDataSetChanged();
        setLastUpdate();

    }

    public void setData(FeedList<T> data) {
        mData = data;
        notifyDataSetChanged();
        setLastUpdate();
    }

    protected void setLastUpdate() {
        mLastUpdate = System.currentTimeMillis();
    }

    public void addData(FeedListData<T> data) {
        removeLoaderItem();
        if (data != null) {
            if (!data.items.isEmpty()) {
                getData().addAll(data.items);
            }
            addLoaderItem(data.more);
        }
        notifyDataSetChanged();
        setLastUpdate();
    }

    public void addDataFirst(FeedListData<T> data) {
        removeLoaderItem();
        if (data != null) {
            Collections.reverse(data.items);
            if (!data.items.isEmpty()) {
                for (T item : data.items) {
                    getData().addFirst(item);
                }
            }
            addLoaderItem(data.more);
        }
        notifyDataSetChanged();
        setLastUpdate();
    }

    public boolean removeItem(int position) {
        boolean result = false;
        FeedList<T> data = getData();
        if (data.hasItem(position)) {
            result = true;
            data.remove(position);
            notifyDataSetChanged();
        }
        return result;
    }

    public T getLastFeedItem() {
        T item = null;
        if (!isEmpty()) {
            FeedList<T> data = getData();
            int dataSize = data.size();

            int feedIndex = data.getLast().isLoader() || data.getLast().isRetrier() ?
                    dataSize - 2 :
                    dataSize - 1;
            if (data.hasItem(feedIndex)) {
                item = data.get(feedIndex);
            }
        }

        return item;
    }

    public T getFirstItem() {
        T item = null;
        if (!isEmpty()) {
            FeedList<T> data = getData();
            item = data.getFirst();
        }
        return item;
    }

    protected FeedViewHolder getEmptyHolder(View convertView, final T item) {
        FeedViewHolder holder = new FeedViewHolder();

        holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.ivAvatar);
        holder.name = (TextView) convertView.findViewById(R.id.tvName);
        holder.city = (TextView) convertView.findViewById(R.id.tvCity);
        holder.online = (ImageView) convertView.findViewById(R.id.ivOnline);
        holder.flipper = (ViewFlipper) convertView.findViewById(R.id.vfFlipper);
        holder.flippedBtn = (Button) convertView.findViewById(R.id.btnMutual);

        return holder;
    }

    abstract protected int getItemLayout();

    abstract protected int getNewItemLayout();

    abstract protected int getVipItemLayout();

    abstract protected int getNewVipItemLayout();

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

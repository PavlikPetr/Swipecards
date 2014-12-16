package com.topface.topface.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.ui.views.FeedItemViewConstructor;
import com.topface.topface.ui.views.FeedItemViewConstructor.TypeAndFlag;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.loadcontollers.FeedLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


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


    @SuppressWarnings("unchecked")
    private MultiselectionController<T> mSelectionController = new MultiselectionController(this);

    @SuppressWarnings("unchecked")
    public FeedAdapter(Context context, FeedList<T> data, Updater updateCallback) {
        super(context, data, updateCallback);
        mSelectionController = new MultiselectionController(this);
    }

    public int getLimit() {
        return LIMIT;
    }

    protected static class FeedViewHolder {
        public ImageViewRemote avatar;
        public TextView name;
        public TextView text;
        public TextView time;
        public TextView unreadCounter;
        public ImageView heart;
        public Drawable background;
    }

    public FeedAdapter(Context context, Updater updateCallback) {
        this(context, null, updateCallback);
    }

    @Override
    protected LoadController initLoadController() {
        return new FeedLoadController();
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

    protected TypeAndFlag getViewCreationFlag() {
        return new TypeAndFlag();
    }

    protected void setItemMessage(T item, TextView messageView) {
        if (item.user.city != null) {
            messageView.setText(item.user.city.name);
        }
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {

        FeedViewHolder holder = null;

        if (convertView != null) {
            holder = (FeedViewHolder) convertView.getTag();
        }

        final T item = getItem(position);
        final int type = getItemViewType(position);
        int flag = 0;

        //Если нам попался лоадер или пустой convertView, т.е. у него нет тега с данными, то заново пересоздаем этот элемент
        if (holder == null) {
            TypeAndFlag typeAndFlag = getViewCreationFlag();
            flag = typeAndFlag.flag;

            if (type == T_NEW || type == T_NEW_VIP) {
                typeAndFlag.flag |= FeedItemViewConstructor.Flag.NEW;
            }
            if (type == T_VIP || type == T_NEW_VIP) {
                typeAndFlag.flag |= FeedItemViewConstructor.Flag.VIP;
            }
            convertView = FeedItemViewConstructor.construct(mContext, typeAndFlag);
            holder = getEmptyHolder(convertView, item);
        }

        if (item != null) {
            // установка аватарки пользователя
            if (item.user.banned || item.user.deleted || item.user.photo == null || item.user.photo.isEmpty()) {
                holder.avatar.setRemoteSrc("drawable://" + (item.user.sex == Static.BOY ?
                        R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar));
                if (item.user.banned || item.user.deleted) {
                    holder.avatar.setOnClickListener(null);
                } else {
                    setListenerOnAvatar(holder.avatar, item);
                }
            } else {
                holder.avatar.setPhoto(item.user.photo);
                setListenerOnAvatar(holder.avatar, item);
            }

            // установка имени
            holder.name.setText(item.user.getNameAndAge());
            if ((item.user.deleted || item.user.banned)) {
                flag |= FeedItemViewConstructor.Flag.BANNED;
                FeedItemViewConstructor.setBanned(holder.name, flag);
            }

            // установка сообщения фида
            setItemMessage(item, holder.text);

            // установка иконки онлайн
            FeedItemViewConstructor.setOnline(holder.name, (!(item.user.deleted || item.user.banned) && item.user.online));
        }

        convertView.setTag(holder);
        if (mSelectionController.isSelected(position)) {
            convertView.setBackgroundResource(R.drawable.list_item_bg_selected);
        } else {
            setBackground(convertView, holder);
        }
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

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void setBackground(View convertView, FeedViewHolder holder) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            convertView.setBackground(holder.background);
        } else {
            convertView.setBackgroundDrawable(holder.background);
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
    public final void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1 - mLoadController.getItemsOffsetByConnectionType()) {
            loadOlderItems();
        }
    }


    /**
     * Method tries to load older items (if there is loader item at the end of listView)
     */
    public final void loadOlderItems() {
        if (mUpdateCallback != null && !mData.isEmpty() && mData.getLast().isLoader()) {
            mUpdateCallback.onUpdate();
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

    @SuppressWarnings("UnusedDeclaration")
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

    public boolean removeItems(List<T> items) {
        boolean result = getData().removeAll(items);
        notifyDataSetChanged();
        return result;
    }

    @SuppressWarnings("UnusedDeclaration")
    public boolean removeByUserId(int userId) {
        boolean result = false;
        FeedList<T> feeds = getData();
        for (T feed : feeds) {
            if (feed.user.id == userId) {
                result = feeds.remove(feed);
                notifyDataSetChanged();
                break;
            }
        }
        return result;
    }


    /**
     * @param userIds Users' ids.
     * @return True if at least one element with id from {@code userIds} was deleted.
     * False otherwise.
     */
    public boolean removeByUserIds(int[] userIds) {
        boolean result = false;
        FeedList<T> feeds = getData();
        for (int id : userIds) {
            for (T feed : feeds) {
                if (feed.user != null && feed.user.id == id) {
                    result |= feeds.remove(feed);
                    break;
                }
            }
        }
        if (result) {
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

        holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.ifp_avatar);
        holder.name = (TextView) convertView.findViewById(R.id.ifp_name);
        holder.text = (TextView) convertView.findViewById(R.id.ifp_text);
        holder.background = convertView.getBackground();

        return holder;
    }

    public boolean isNeedUpdate() {
        return isEmpty() || (System.currentTimeMillis() > mLastUpdate + CACHE_TIMEOUT);
    }

    public void setOnAvatarClickListener(OnAvatarClickListener<T> listener) {
        mOnAvatarClickListener = listener;
    }

    public static interface OnAvatarClickListener<T> {
        public void onAvatarClick(T item, View view);
    }

    public List<String> getSelectedFeedIds() {
        List<String> ids = new ArrayList<>();
        if (mSelectionController != null) {
            List<T> selected = mSelectionController.getSelected();
            for (T aSelected : selected) {
                ids.add(aSelected.id);
            }
        }
        return ids;
    }

    public List<Integer> getSelectedUsersIds() {
        List<Integer> ids = new ArrayList<>();
        if (mSelectionController != null) {
            List<T> selected = mSelectionController.getSelected();
            for (T aSelected : selected) {
                if (aSelected != null && aSelected.user != null) {
                    ids.add(aSelected.user.id);
                }
            }
        }
        return ids;
    }

    public List<String> getSelectedUsersStringIds() {
        List<String> ids = new ArrayList<>();
        if (mSelectionController != null) {
            List<T> selected = mSelectionController.getSelected();
            for (T aSelected : selected) {
                if (aSelected != null && aSelected.user != null) {
                    ids.add(Integer.toString(aSelected.user.id));
                }
            }
        }
        return ids;
    }

    public List<T> getSelectedItems() {
        List<T> result = new ArrayList<>();
        result.addAll(mSelectionController.getSelected());
        return result;
    }

    public void finishMultiSelection() {
        mSelectionController.finishMultiSelection();
    }

    @SuppressWarnings("UnusedDeclaration")
    public int selectedCount() {
        return mSelectionController.selectedCount();
    }

    public void setMultiSelectionListener(MultiselectionController.IMultiSelectionListener listener) {
        mSelectionController.setMultiSelectionListener(listener);
    }

    @SuppressWarnings("UnusedDeclaration")
    public void deleteAllSelectedItems() {
        mSelectionController.deleteAllSelectedItems();
    }

    public void startMultiSelection(int selectionLimit) {
        mSelectionController.startMultiSelection(selectionLimit);
    }

    public void onSelection(int position) {
        mSelectionController.onSelection(position);
    }

    public void onSelection(T item) {
        mSelectionController.onSelection(item);
    }

    public boolean isMultiSelectionMode() {
        return mSelectionController.isMultiSelectionMode();
    }
}

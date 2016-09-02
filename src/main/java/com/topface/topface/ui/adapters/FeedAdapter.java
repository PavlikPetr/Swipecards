package com.topface.topface.ui.adapters;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedItem;
import com.topface.topface.data.FeedListData;
import com.topface.topface.data.Profile;
import com.topface.topface.ui.views.FeedItemViewConstructor;
import com.topface.topface.ui.views.FeedItemViewConstructor.TypeAndFlag;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.FeedLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.topface.topface.receivers.ConnectionChangeReceiver.ConnectionType.CONNECTION_WIFI;


/**
 * @param <T>
 */
public abstract class FeedAdapter<T extends FeedItem> extends LoadingListAdapter<T> implements AbsListView.OnScrollListener {

    protected static final int T_NEW_VIP = 3;
    protected static final int T_VIP = 4;
    protected static final int T_NEW = 5;
    protected static final int T_COUNT = 7;

    private long mLastUpdate = 0;
    public static final int LIMIT = 40;
    private static final long CACHE_TIMEOUT = 1000 * 5 * 60; //5 минут
    private OnAvatarClickListener<T> mOnAvatarClickListener;

    private MultiselectionController<T> mSelectionController;

    public FeedAdapter(Context context, FeedList<T> data, Updater updateCallback) {
        super(context, data, updateCallback);
        mSelectionController = new MultiselectionController<>(this);
    }

    public FeedList<T> getDataForCache() {
        return getDataForCache(App.getContext().getResources().getIntArray(R.array.feed_limit)[CONNECTION_WIFI.getInt()]);
    }

    public FeedList<T> getDataForCache(int count) {
        FeedList<T> data = getData();
        FeedList<T> result = new FeedList<>();
        int addedCount = 0;
        int iter = 0;
        while (addedCount < count && iter < data.size()) {
            T currentItem = data.get(iter);
            if (!currentItem.isLoader()
                    && !currentItem.isRetrier() && currentItem.user != null) {
                result.add(currentItem);
                addedCount++;
            }
            iter++;
        }
        return result;
    }

    public int getLimit() {
        return LIMIT;
    }

    protected static class FeedViewHolder {
        public ImageViewRemote avatarImage;
        public FrameLayout avatar;
        public TextView name;
        public TextView text;
        public TextView age;
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
        T item = getItem(position);
        if (superType == T_OTHER && item != null) {
            if (item.unread && item.user.premium) {
                return T_NEW_VIP;
            } else if (item.unread && !item.user.premium) {
                return T_NEW;
            } else if (!item.unread && item.user.premium) {
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
        String text = null;
        if (item.user.deleted) {
            text = getContext().getString(R.string.user_is_deleted);
        } else if (item.user.banned) {
            text = getContext().getString(R.string.user_is_banned);
        } else if (item.user.city != null) {
            text = item.user.city.name;
        }
        if (text != null) {
            messageView.setText(text);
        }
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        FeedViewHolder holder = null;
        if (convertView != null) {
            holder = (FeedViewHolder) convertView.getTag();
        }
        final int type = getItemViewType(position);
        int flag = 0;
        final T item = getItem(position);
        //Если нам попался лоадер или пустой convertView, т.е. у него нет тега с данными, то заново пересоздаем этот элемент
        if (holder == null) {
            TypeAndFlag typeAndFlag = getViewCreationFlag();

            if (type == T_NEW || type == T_NEW_VIP) {
                typeAndFlag.flag |= FeedItemViewConstructor.Flag.NEW;
            }
            if (type == T_VIP || type == T_NEW_VIP) {
                typeAndFlag.flag |= FeedItemViewConstructor.Flag.VIP;
            }
            flag = typeAndFlag.flag;
            convertView = FeedItemViewConstructor.construct(mContext, typeAndFlag, isLikesListAdapter());
            holder = getEmptyHolder(convertView, item);
        }

        if (item != null) {
            // установка аватарки пользователя
            // какую аватарку использовать по умолчанию для забаненных и во время загрузки нормальной
            int defaultAvatarResId = (item.user.sex == Profile.BOY ?
                    R.drawable.feed_banned_male_avatar : R.drawable.feed_banned_female_avatar);
            holder.avatarImage.setStubResId(defaultAvatarResId);

            if (item.user.banned || item.user.deleted || item.user.photo == null || item.user.photo.isEmpty()) {
                holder.avatarImage.setRemoteSrc(Utils.getLocalResUrl(defaultAvatarResId));
                if (item.user.banned || item.user.deleted) {
                    holder.avatar.setOnClickListener(null);
                } else {
                    setListenerOnAvatar(holder.avatar, item);
                }
            } else {
                holder.avatarImage.setPhoto(item.user.photo);
                setListenerOnAvatar(holder.avatar, item);
            }

            // установка имени
            holder.name.setText(item.user.firstName);
            if ((item.user.deleted || item.user.banned)) {
                flag |= FeedItemViewConstructor.Flag.BANNED;
            }
            FeedItemViewConstructor.setBanned(holder.name, flag);

            // установка возраста
            String age = "";
            if (item.user.age > 0) {
                age = String.valueOf(item.user.age);
                if (!TextUtils.isEmpty(item.user.firstName)) {
                    age = ", " + age;
                }
            }
            holder.age.setText(age);
            FeedItemViewConstructor.setBanned(holder.age, flag);

            // установка сообщения фида
            setItemMessage(item, holder.text);

            // установка иконки онлайн
            FeedItemViewConstructor.setOnline(holder.age, (!(item.user.deleted || item.user.banned) && item.user.online));
        }

        convertView.setTag(holder);
        if (mSelectionController.isSelected(position)) {
            convertView.setBackgroundResource(isLikesListAdapter() ? R.drawable.feed_list_item_selected : R.drawable.list_item_bg_selected);
        } else {
            setBackground(convertView, holder);
        }
        return convertView;
    }

    private void setListenerOnAvatar(FrameLayout avatar, final T item) {
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

    private boolean isLikesListAdapter() {
        return this instanceof LikesListAdapter;
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
        loadOlderItemsIfNeeded(firstVisibleItem, visibleItemCount, totalItemCount);
    }

    public void loadOlderItemsIfNeeded(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
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

    public void makeAllItemsRead() {
        for (T item : getData()) {
            item.unread = false;
        }
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

    public boolean removeByUserId(int userId) {
        boolean result = false;
        FeedList<T> feeds = getData();
        for (T feed : feeds) {
            if (feed.user != null && feed.user.id == userId) {
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
        if (!getData().isEmpty()) {
            FeedList<T> data = getData();
            int dataSize = data.size();

            FeedItem last = data.getLast();
            int feedIndex = last.isLoader() || last.isRetrier() ?
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
            item = getData().getFirst();
        }
        return item;
    }

    protected FeedViewHolder getEmptyHolder(View convertView, final T item) {
        FeedViewHolder holder = new FeedViewHolder();

        holder.avatar = (FrameLayout) convertView.findViewById(!isLikesListAdapter() ? R.id.ifp_avatar : R.id.avatar_frame);
        holder.avatarImage = (ImageViewRemote) convertView.findViewById(!isLikesListAdapter() ? R.id.ifp_avatar_image : R.id.avatar);
        holder.name = (TextView) convertView.findViewById(R.id.ifp_name);holder.age = (TextView) convertView.findViewById(R.id.ifp_age);
        holder.text = (TextView) convertView.findViewById(!isLikesListAdapter() ? R.id.ifp_text : R.id.text);
        holder.background = convertView.getBackground();

        return holder;
    }

    public boolean isNeedUpdate() {
        return isEmpty() || (System.currentTimeMillis() > mLastUpdate + CACHE_TIMEOUT);
    }

    public void setOnAvatarClickListener(OnAvatarClickListener<T> listener) {
        mOnAvatarClickListener = listener;
    }

    public interface OnAvatarClickListener<T> {
        void onAvatarClick(T item, View view);
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

    public void removeAllData() {
        getData().clear();
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

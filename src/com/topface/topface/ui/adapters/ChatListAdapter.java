package com.topface.topface.ui.adapters;

import android.content.Context;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.*;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.VirusLikesRequest;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class ChatListAdapter extends LoadingListAdapter<History> implements AbsListView.OnScrollListener {

    static class ViewHolder {
        View dateDivider;
        TextView dateDividerText;
        ImageViewRemote avatar;
        TextView message;
        TextView date;
        ImageViewRemote gift;
        ImageView mapBackground;
        ProgressBar prgsAddress;
        Button likeRequest;
        View userInfo;
        View loader;
        View retrier;
    }

    private static final int T_WAITING = 3;
    private static final int T_USER = 5;
    private static final int T_FRIEND = 6;
    private static final int T_USER_GIFT = 7;
    private static final int T_FRIEND_GIFT = 8;
    private static final int T_USER_MAP = 9;
    private static final int T_FRIEND_MAP = 10;
    private static final int T_USER_REQUEST = 11;
    private static final int T_FRIEND_REQUEST = 12;

    private static final int T_COUNT = 13;

    private HashMap<History, ApiRequest> mHashRepeatRequests = new HashMap<History, ApiRequest>();
    private ArrayList<History> mWaitingItems = new ArrayList<History>();
    private ArrayList<History> mUnrealItems = new ArrayList<History>();
    private ArrayList<History> mShowDatesList = new ArrayList<History>();
    private AddressesCache mAddressesCache = new AddressesCache();
    private View.OnClickListener mOnClickListener;
    private ChatFragment.OnListViewItemLongClickListener mLongClickListener;

    private View mHeaderView;

    private FeedUser user;

    public ChatListAdapter(Context context, FeedList<History> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    @Override
    public History getItem(int i) {
        return super.getItem(getPosition(i));
    }

    private int getPosition(int index) {
        return getCount() - index - 1;
    }

    @Override
    public int getItemViewType(int position) {
        int superType = super.getItemViewType(position);
        History item = getItem(position);
        if (superType == T_OTHER && item != null) {
            if (item.isWaitingItem() || item.isRepeatItem()) return T_WAITING;
            boolean output = (item.target == FeedDialog.USER_MESSAGE);
            switch (item.type) {
                case FeedDialog.GIFT:
                    return output ? T_USER_GIFT : T_FRIEND_GIFT;
                case FeedDialog.MAP:
                    return output ? T_USER_MAP : T_FRIEND_MAP;
                case FeedDialog.ADDRESS:
                    return output ? T_USER_MAP : T_FRIEND_MAP;
                case FeedDialog.LIKE_REQUEST:
                    return output ? T_USER_REQUEST : T_FRIEND_REQUEST;
                default:
                    return output ? T_USER : T_FRIEND;
            }
        } else {
            return superType;
        }
    }

    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        final int type = getItemViewType(position);
        final History item = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflateConvertView(convertView, holder, type, item);
            if (convertView != null) {
                convertView.setTag(holder);
            }
        } else
            holder = (ViewHolder) convertView.getTag();

        setTypeDifferences(position, holder, type, item);
        if (type != T_WAITING) {
            setViewInfo(holder, item);
            setLongClickListener(position, convertView, holder);
        }

        return convertView;

    }

    public void setUser(FeedUser user) {
        this.user = user;
        if (mHeaderView != null && user != null) {
            ((ImageViewRemote) mHeaderView.findViewById(R.id.ivFriendAvatar)).setPhoto(user.photo);
        }
    }

    public FeedUser getUser() {
        return this.user;
    }

    public void addHeader(ListView parentView) {
        if (mHeaderView == null) {
            try {
                mHeaderView = mInflater.inflate(R.layout.list_header_chat_no_messages_informer, null);
                parentView.addHeaderView(mHeaderView);
                parentView.setStackFromBottom(false);
                mHeaderView.setVisibility(View.GONE);
            } catch (OutOfMemoryError e) {
                Debug.error("Add header OOM", e);
            }
        }
    }

    private void removeHeader(ListView parentView) {
        if (mHeaderView != null) {
            parentView.removeHeaderView(mHeaderView);
            parentView.setStackFromBottom(true);
            mHeaderView = null;
        } else {
            Debug.log("ChatListAdapter.removeHeader(): no header to remove remove header");
        }
    }

    @Override
    public void setData(ArrayList<History> dataList, boolean more) {
        super.setData(dataList, more, false);
        prepareDates();
        notifyDataSetChanged();
    }

    public void setData(ArrayList<History> dataList, boolean more, ListView parentView) {
        super.setData(dataList, more, false);
        prepareDates();
        notifyDataSetChanged();
        parentView.setSelection(getCount() - 1);
        if (getCount() > 0) {
            removeHeader(parentView);
        } else {
            mHeaderView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void addAll(ArrayList<History> dataList, boolean more) {
        super.addAll(dataList, more, false);
        prepareDates();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<History> dataList, boolean more, ListView parentView) {
        this.addAll(dataList, more);
        parentView.setSelection(dataList.size() + (more ? 2 : 0));
        if (getCount() > 0) {
            removeHeader(parentView);
        }
    }

    @Override
    public void addFirst(ArrayList<History> data, boolean more) {
        if (!mUnrealItems.isEmpty()) removeUnrealItems();
        super.addFirst(data, more, false);
        prepareDates();
        notifyDataSetChanged();
    }

    public void addFirst(ArrayList<History> data, boolean more, ListView parentView) {
        int scroll = parentView.getScrollY();
        this.addFirst(data, more);
        parentView.scrollTo(parentView.getScrollX(), scroll);
        if (getCount() > 0) {
            removeHeader(parentView);
        }
    }

    private void addSentMessage(History item) {
        getData().addFirst(item);
        if (item.isWaitingItem()) {
            mWaitingItems.add(item);
        } else {
            mUnrealItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void addSentMessage(History item, ListView parentView) {
        this.addSentMessage(item);
        parentView.setSelection(getCount() - 1);
        if (getCount() > 0) {
            removeHeader(parentView);
        }
    }

    public void replaceMessage(History emptyItem, History unrealItem, ListView parentView) {
        FeedList<History> data = getData();
        int positionToReplace = -1;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) == emptyItem) {
                positionToReplace = i;
            }
        }
        if (positionToReplace != -1) {
            data.remove(positionToReplace);
            data.add(positionToReplace, unrealItem);
            mWaitingItems.remove(emptyItem);
            mUnrealItems.add(unrealItem);
        }
        notifyDataSetChanged();
        parentView.setSelection(getCount() - 1);
    }

    public void showRetrySendMessage(History emptyItem, ApiRequest request) {
        emptyItem.setLoaderTypeFlags(IListLoader.ItemType.REPEAT);
        mHashRepeatRequests.put(emptyItem, request);
        notifyDataSetChanged();
    }

    private void removeUnrealItems() {
        removeUnrealItems(getData());
        mUnrealItems.clear();
    }

    private void removeUnrealItems(Collection<History> data) {
        data.removeAll(mUnrealItems);
    }

    private void prepareDates() {
        FeedList<History> data = getData();
        mShowDatesList.clear();
        for (int i = data.size() - 1; i >= 0; i--) {
            History currItem = data.get(i);
            if (currItem.isLoaderOrRetrier()) continue;

            History prevItem = (i + 1 >= data.size()) ? null : data.get(i + 1);
            if (prevItem == null || prevItem.isLoaderOrRetrier()) {
                mShowDatesList.add(currItem);
                continue;
            }

            if (DateUtils.isWithinADay(prevItem.created, currItem.created)) {
                mShowDatesList.add(currItem);
            }
        }

    }

    private void setTypeDifferences(int position, ViewHolder holder, int type, final History item) {
        History prevItem = getItem(position - 1);
        boolean avatar = prevItem == null || prevItem.target != item.target;
        boolean output = (item.target == FeedDialog.USER_MESSAGE);
        boolean showDate = mShowDatesList.contains(item);

        Photo userPhoto = user != null ? user.photo : null;

        switch (type) {
            case T_WAITING:
                if (item.isRepeatItem()) {
                    holder.loader.setVisibility(View.GONE);
                    holder.retrier.setVisibility(View.VISIBLE);
                    holder.retrier.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ApiRequest request = mHashRepeatRequests.get(item);
                            if (request != null) request.exec();
                            mHashRepeatRequests.remove(item);
                            item.setLoaderTypeFlags(IListLoader.ItemType.WAITING);
                            notifyDataSetChanged();
                        }
                    });
                } else {
                    holder.loader.setVisibility(View.VISIBLE);
                    holder.retrier.setVisibility(View.GONE);
                }
                return;
            case T_FRIEND:
            case T_USER:
                if (avatar) {
                    holder.avatar.setOnClickListener(output ? null : mOnClickListener);
                    holder.avatar.setPhoto(output ? CacheProfile.photo : userPhoto);
                    holder.avatar.setVisibility(View.VISIBLE);
                    holder.userInfo.setBackgroundResource(output ? R.drawable.bg_message_user : R.drawable.bg_message_friend);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                    holder.userInfo.setBackgroundResource(output ? R.drawable.bg_message_user_ext : R.drawable.bg_message_friend_ext);
                }
                break;
            case T_FRIEND_GIFT:
            case T_USER_GIFT:
            case T_FRIEND_MAP:
            case T_USER_MAP:
                if (avatar) {
                    holder.avatar.setOnClickListener(output ? null : mOnClickListener);
                    holder.avatar.setPhoto(output ? CacheProfile.photo : userPhoto);
                    holder.avatar.setVisibility(View.VISIBLE);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                }
                break;
            case T_FRIEND_REQUEST:
            case T_USER_REQUEST:
                if (avatar) {
                    holder.avatar.setOnClickListener(output ? null : mOnClickListener);
                    holder.avatar.setPhoto(output ? CacheProfile.photo : userPhoto);
                    holder.avatar.setVisibility(View.VISIBLE);
                    holder.userInfo.setBackgroundResource(output ? R.drawable.bg_message_user : R.drawable.bg_message_friend);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                    holder.userInfo.setBackgroundResource(output ? R.drawable.bg_message_user_ext : R.drawable.bg_message_friend_ext);
                }

                if (type == T_FRIEND_REQUEST) {
                    holder.likeRequest.setTag(position);
                    holder.likeRequest.setOnClickListener(mLikeRequestListener);
                }
                break;
        }

        if (showDate) {
            holder.dateDivider.setVisibility(View.VISIBLE);
            holder.dateDividerText.setText(DateUtils.getFormattedDate(mContext, item.created).toUpperCase());
        } else {
            holder.dateDivider.setVisibility(View.GONE);
        }
    }

    private View inflateConvertView(View convertView, ViewHolder holder, int type, History item) {
        boolean output = (item.target == FeedDialog.USER_MESSAGE);
        switch (type) {
            case T_WAITING:
                convertView = mInflater.inflate(R.layout.item_chat_list_loader_retrier, null, false);
                holder.retrier = convertView.findViewById(R.id.tvLoaderText);
                holder.loader = convertView.findViewById(R.id.prsLoader);
                return convertView;
            case T_FRIEND:
            case T_USER:
                convertView = mInflater.inflate(output ? R.layout.chat_user : R.layout.chat_friend, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                holder.userInfo = convertView.findViewById(R.id.user_info);
                break;
            case T_FRIEND_GIFT:
            case T_USER_GIFT:
                convertView = mInflater.inflate(output ? R.layout.chat_user_gift : R.layout.chat_friend_gift, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.gift = (ImageViewRemote) convertView.findViewById(R.id.ivChatGift);
                break;
            case T_FRIEND_MAP:
            case T_USER_MAP:
                convertView = mInflater.inflate(output ? R.layout.chat_user_map : R.layout.chat_friend_map, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.message = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
                holder.mapBackground = (ImageView) convertView.findViewById(R.id.ivMapBg);
                holder.prgsAddress = (ProgressBar) convertView.findViewById(R.id.prgsMapAddress);
                break;
            case T_FRIEND_REQUEST:
                convertView = mInflater.inflate(R.layout.chat_friend_request, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                holder.userInfo = convertView.findViewById(R.id.user_info);
                holder.likeRequest = (Button) convertView.findViewById(R.id.btn_chat_like_request);
                break;
            case T_USER_REQUEST:
                convertView = mInflater.inflate(R.layout.chat_user, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                holder.userInfo = convertView.findViewById(R.id.user_info);
                break;
        }

        holder.dateDivider = convertView.findViewById(R.id.loDateDivider);
        holder.dateDividerText = (TextView) holder.dateDivider.findViewById(R.id.tvChatDateDivider);

        return convertView;
    }

    private void setViewInfo(ViewHolder holder, History item) {
        boolean output = (item.target == FeedDialog.USER_MESSAGE);
        switch (item.type) {
            case FeedDialog.MAP:
            case FeedDialog.ADDRESS:
                holder.message.setText(Static.EMPTY);
                holder.mapBackground.setBackgroundResource(item.type == FeedDialog.MAP ? R.drawable.chat_item_place :
                        R.drawable.chat_item_map);
                holder.mapBackground.setTag(item);
                holder.mapBackground.setOnClickListener(mOnClickListener);
                mAddressesCache.mapAddressDetection(item, holder.message, holder.prgsAddress);
                break;
            case FeedDialog.GIFT:
                holder.gift.setRemoteSrc(item.link);
                break;
            case FeedDialog.MESSAGE_WISH:
                holder.message.setText(mContext.getString(output ? R.string.chat_wish_out : R.string.chat_wish_in));
                break;
            case FeedDialog.MESSAGE_SEXUALITY:
                holder.message.setText(mContext.getString(output ? R.string.chat_sexuality_out :
                        R.string.chat_sexuality_in));
                break;
            case FeedDialog.LIKE:
                holder.message.setText(mContext.getString(output ? R.string.chat_like_out : R.string.chat_like_in));
                break;
            case FeedDialog.SYMPHATHY:
                holder.message.setText(mContext.getString(output ? R.string.chat_mutual_out : R.string.chat_mutual_in));
                break;
            case FeedDialog.MESSAGE_WINK:
                holder.message.setText(mContext.getString(output ? R.string.chat_wink_out : R.string.chat_wink_in));
                break;
            case FeedDialog.LIKE_REQUEST:
                holder.message.setText(item.text);
                break;
            case FeedDialog.DEFAULT:
            case FeedDialog.MESSAGE:
            case FeedDialog.PROMOTION:
            default:
                if (holder != null && holder.message != null) {
                    holder.message.setText(
                            Html.fromHtml(item.text != null ? item.text : "")
                    );
                }
                break;
        }
        if (holder != null) {
            if (holder.message != null) holder.message.setMovementMethod(LinkMovementMethod.getInstance());
            if (holder.date != null) holder.date.setText(DateUtils.getFormattedDateHHmm(item.created));
        }

    }

    public void setOnItemLongClickListener(ChatFragment.OnListViewItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnAvatarListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    @SuppressWarnings("deprecation")
    public void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
        Toast.makeText(mContext, R.string.general_msg_copied, Toast.LENGTH_SHORT).show();
    }

    public void removeItem(int position) {
        getData().remove(position);
        notifyDataSetChanged();
    }

    public int getFirstItemId() {
        FeedList<History> data = getData();
        for (History item : data) {
            if (!item.isLoaderOrRetrier() && item.id > 0) {
                return item.id;
            }
        }
        return -1;
    }

    public int getLastItemId() {
        FeedList<History> data = getData();
        for (int i = data.size() - 1; i >= 0; i--) {
            History item = data.get(i);
            if (!item.isLoaderOrRetrier() && item.id > 0) {
                return item.id;
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<History> getDataCopy() {
        ArrayList<History> dataClone = null;
        try {
            dataClone = (ArrayList<History>) getData().clone();
            if (!dataClone.isEmpty() && dataClone.get(dataClone.size() - 1).isLoaderOrRetrier()) {
                dataClone.remove(dataClone.size() - 1);
            }
            removeUnrealItems(dataClone);
        } catch (OutOfMemoryError e) {
            Debug.error(e);
        }
        return dataClone;
    }

    private void setLongClickListener(final int position, View convertView, final ViewHolder holder) {
        if (holder.message != null && convertView != null) {
            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mLongClickListener.onLongClick(position, holder.message);
                    return false;
                }
            });
            holder.message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    mLongClickListener.onLongClick(position, holder.message);
                    return false;
                }
            });
        }
    }

    @Override
    protected int getLoaderRetrierLayout() {
        return R.layout.item_chat_list_loader_retrier;
    }

    @Override
    public ILoaderRetrierCreator<History> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<History>() {
            @Override
            public History getLoader() {
                History result = new History();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                result.created = 0;
                return result;
            }

            @Override
            public History getRetrier() {
                History result = new History();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                result.created = 0;
                return result;
            }
        };
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem == 0) {
            FeedList<History> data = getData();
            if (mUpdateCallback != null && !data.isEmpty() && data.getLast().isLoader()) {
                mUpdateCallback.onUpdate();
            }
        }
    }

    private View.OnClickListener mLikeRequestListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = (Integer) v.getTag();
            final History item = getItem(position);
            if (item != null) {
                EasyTracker.getTracker().trackEvent("VirusLike", "Click", "Chat", 0L);

                new VirusLikesRequest(item.id, mContext).callback(new DataApiHandler<VirusLike>() {

                    @Override
                    protected void success(VirusLike data, ApiResponse response) {
                        EasyTracker.getTracker().trackEvent("VirusLike", "Success", "Chat", 0L);
                        //После заврешения запроса удаляем элемент
                        removeItem(getPosition(position));
                        //И предлагаем отправить пользователю запрос своим друзьям не из приложения
                        data.sendFacebookRequest(
                                "Chat",
                                mContext,
                                new VirusLike.VirusLikeDialogListener(mContext) {

                                    private void showCompleteMessage() {
                                        Toast.makeText(
                                                mContext,
                                                Utils.getQuantityString(
                                                        R.plurals.virus_request_likes_cnt,
                                                        CacheProfile.likes,
                                                        CacheProfile.likes
                                                ),
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }

                                    @Override
                                    public void onComplete(Bundle values) {
                                        super.onComplete(values);
                                        showCompleteMessage();
                                    }

                                    @Override
                                    public void onCancel() {
                                        super.onCancel();
                                        showCompleteMessage();
                                    }
                                }
                        );
                    }

                    @Override
                    protected VirusLike parseResponse(ApiResponse response) {
                        return new VirusLike(response);
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        EasyTracker.getTracker().trackEvent("VirusLike", "Fail", "Chat", 0L);
                        Utils.showErrorMessage(getContext());
                    }
                }).exec();
            }
        }
    };
}

package com.topface.topface.ui.adapters;

import android.content.Context;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.History;
import com.topface.topface.data.VirusLike;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.VirusLikesRequest;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.AddressesCache;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.*;


public class ChatListAdapter extends LoadingListAdapter implements AbsListView.OnScrollListener{

    // class ViewHolder
    static class ViewHolder {
        ImageViewRemote avatar;
        TextView message;
        TextView date;
        ImageViewRemote gift;
        TextView address;
        ImageView mapBackground;
        ProgressBar prgsAddress;
        Button likeRequest;
        View userInfo;
        // View mInfoGroup;
    }

    private LockerView mLockerView;
    private FeedList<History> mDataList; // data
    private LinkedList<Integer> mItemLayoutList; // types
    private HashMap<Integer, String> mItemTimeList; // date
    private View.OnClickListener mOnClickListener;
    private SimpleDateFormat mDateFormat = new SimpleDateFormat("HH:mm");
    private AddressesCache mAddressesCache;

    // Type Item
    private static final int T_USER_PHOTO = 3;
    private static final int T_USER_EXT = 4;
    private static final int T_FRIEND_PHOTO = 5;
    private static final int T_FRIEND_EXT = 6;
    private static final int T_DATE = 7;
    private static final int T_USER_GIFT_PHOTO = 8;
    private static final int T_USER_GIFT_EXT = 9;
    private static final int T_FRIEND_GIFT_PHOTO = 10;
    private static final int T_FRIEND_GIFT_EXT = 11;
    private static final int T_USER_MAP_PHOTO = 12;
    private static final int T_USER_MAP_EXT = 13;
    private static final int T_FRIEND_MAP_PHOTO = 14;
    private static final int T_FRIEND_MAP_EXT = 15;
    private static final int T_USER_REQUEST = 16;
    private static final int T_USER_REQUEST_EXT = 17;
    private static final int T_FRIEND_REQUEST = 18;
    private static final int T_FRIEND_REQUEST_EXT = 19;

    private static final int T_COUNT = 20;

    private final static SimpleDateFormat mDowFormat = new SimpleDateFormat("EEEE");


    ChatFragment.OnListViewItemLongClickListener mLongClickListener;


    public ChatListAdapter(Context context, ArrayList<History> dataList, Updater updateCallback) {
        super(context, updateCallback);
        mContext = context;
        mLockerView = lockerView;
        mItemLayoutList = new LinkedList<Integer>();
        mItemTimeList = new HashMap<Integer, String>();
        mAddressesCache = new AddressesCache();

        prepare(dataList, true);
    }

    public void setOnItemLongClickListener(ChatFragment.OnListViewItemLongClickListener l) {
        mLongClickListener = l;
    }

    public void setOnAvatarListener(View.OnClickListener onAvatarListener) {
        mOnClickListener = onAvatarListener;
    }

    @Override
    public int getCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    @Override
    public History getItem(int position) {
        return mDataList.hasItem(position) ? mDataList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
        int superType = super.getItemViewType(position);
        if (superType == T_OTHER) {
            return mItemLayoutList.get(position);
        } else {
            return superType;
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        final int type = getItemViewType(position);
        final History item = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflateConvertView(position, convertView, holder, type, item);
            if (convertView != null) {
                convertView.setTag(holder);
            }
        } else
            holder = (ViewHolder) convertView.getTag();

        // setting date for divider
        if (type == T_DATE) {
            holder.date.setText(mItemTimeList.get(position));
            return convertView;
        }

        // setting only visual information
        if (setImageInfo(holder, type, item)) return convertView;

        // setting textual information
        setMessageText(holder, item);

        setLongClickListener(position, convertView, holder);
        return convertView;
    }

    private View inflateConvertView(int position, View convertView, ViewHolder holder, int type, History item) {
        if (type == T_DATE) {
            convertView = mInflater.inflate(R.layout.chat_date_divider, null, false);
            holder.date = (TextView) convertView.findViewById(R.id.tvChatDateDivider);
            return convertView;
        } else if (type == T_FRIEND_REQUEST || type == T_FRIEND_REQUEST_EXT) {
            Button likeRequestBtn = (Button) convertView.findViewById(R.id.btn_chat_like_request);
            likeRequestBtn.setTag(position);
            likeRequestBtn.setOnClickListener(mLikeRequestListener);
        }

        boolean output = (item.target == FeedDialog.USER_MESSAGE);
        switch (type) {
            case T_FRIEND_PHOTO:
            case T_FRIEND_EXT:
            case T_USER_PHOTO:
            case T_USER_EXT:
                convertView = mInflater.inflate(output ? R.layout.chat_user : R.layout.chat_friend, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                holder.userInfo = convertView.findViewById(R.id.user_info);
                if (type == T_FRIEND_PHOTO || type == T_USER_PHOTO) {
                    holder.avatar.setOnClickListener(!output ? null : mOnClickListener);
                    holder.avatar.setPhoto(output ? CacheProfile.photo : item.user.photo);
                    holder.userInfo.setBackgroundResource(output ? R.drawable.bg_message_user : R.drawable.bg_message_friend);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                    holder.userInfo.setBackgroundResource(output ? R.drawable.bg_message_user_ext : R.drawable.bg_message_friend_ext);
                }
                break;
            case T_FRIEND_GIFT_PHOTO:
            case T_FRIEND_GIFT_EXT:
            case T_USER_GIFT_PHOTO:
            case T_USER_GIFT_EXT:
                convertView = mInflater.inflate(output ? R.layout.chat_user_gift : R.layout.chat_friend_gift, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.gift = (ImageViewRemote) convertView.findViewById(R.id.ivChatGift);
                if (type == T_FRIEND_GIFT_PHOTO || type == T_USER_GIFT_PHOTO) {
                    holder.avatar.setOnClickListener(!output ? null : mOnClickListener);
                    holder.avatar.setPhoto(output ? CacheProfile.photo : item.user.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                }
                break;
            case T_FRIEND_MAP_PHOTO:
            case T_FRIEND_MAP_EXT:
            case T_USER_MAP_PHOTO:
            case T_USER_MAP_EXT:
                convertView = mInflater.inflate(output ? R.layout.chat_user_map : R.layout.chat_friend_map, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                holder.address = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
                holder.mapBackground = (ImageView) convertView.findViewById(R.id.ivMapBg);
                holder.prgsAddress = (ProgressBar) convertView.findViewById(R.id.prgsMapAddress);
                if (type == T_FRIEND_MAP_PHOTO || type == T_USER_MAP_PHOTO) {
                    holder.avatar.setOnClickListener(!output ? null : mOnClickListener);
                    holder.avatar.setPhoto(output ? CacheProfile.photo : item.user.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                }
                break;
            case T_FRIEND_REQUEST:
            case T_FRIEND_REQUEST_EXT:
                convertView = mInflater.inflate(R.layout.chat_friend_request, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                if (type == T_FRIEND_REQUEST) {
                    holder.avatar.setOnClickListener(mOnClickListener);
                    holder.avatar.setPhoto(item.user.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                }
                holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                Button likeRequestBtn = (Button) convertView.findViewById(R.id.btn_chat_like_request);
                likeRequestBtn.setTag(position);
                likeRequestBtn.setOnClickListener(mLikeRequestListener);
                break;
            case T_USER_REQUEST:
            case T_USER_REQUEST_EXT:
                convertView = mInflater.inflate(R.layout.chat_user, null, false);
                holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                if (type == T_USER_REQUEST) {
                    holder.avatar.setOnClickListener(mOnClickListener);
                    holder.avatar.setPhoto(CacheProfile.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                } else {
                    holder.avatar.setVisibility(View.INVISIBLE);
                }
                holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                break;
        }
        return convertView;
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

    private boolean setImageInfo(ViewHolder holder, int type, History item) {
        if (type == T_USER_GIFT_PHOTO || type == T_USER_GIFT_EXT
                || type == T_FRIEND_GIFT_PHOTO || type == T_FRIEND_GIFT_EXT) {
            holder.gift.setRemoteSrc(item.link);
            return true;
        } else if (type == T_USER_MAP_PHOTO || type == T_USER_MAP_EXT
                || type == T_FRIEND_MAP_PHOTO || type == T_FRIEND_MAP_EXT) {
            holder.address.setText(Static.EMPTY);

            if (item.type == FeedDialog.MAP) {
                holder.mapBackground.setBackgroundResource(R.drawable.chat_item_place);
            } else {
                holder.mapBackground.setBackgroundResource(R.drawable.chat_item_map);
            }

            holder.mapBackground.setTag(item);
            holder.mapBackground.setOnClickListener(mOnClickListener);

            mAddressesCache.mapAddressDetection(item, holder.address, holder.prgsAddress);
            return true;
        }
        return false;
    }

    private void setMessageText(ViewHolder holder, History item) {
        boolean output = (item.target == FeedDialog.FRIEND_MESSAGE);
        switch (item.type) {
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
                holder.message.setText(Html.fromHtml(item.text));
                break;
        }

        holder.message.setMovementMethod(LinkMovementMethod.getInstance());
        holder.date.setText(mDateFormat.format(item.created));
    }

    public void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
        Toast.makeText(mContext, R.string.general_msg_copied, Toast.LENGTH_SHORT).show();
    }

    public void addSentMessage(History msg) {
        int position = mDataList.size() - 1;
        History prevHistory = null;
        if (position >= 0) {
            prevHistory = getLastRealMessage(); //get(mDataList.size() - 1);
        }

        int type = getItemType(prevHistory, msg);
        mItemLayoutList.add(type);
        mDataList.add(msg);
    }

    /**
     * @return последнее "реальное" сообщение в чате, т.е. такое у которого есть id
     */
    private History getLastRealMessage() {
        int cnt = getCount();
        for (int i = cnt - 1; i >= 0; i--) {
            History lastItem = mDataList != null ? mDataList.get(i) : null;
            if (lastItem != null) {
                if (lastItem.id > 0) {
                    return lastItem;
                }
            }
        }
        return null;
    }

    public void setData(ArrayList<History> dataList) {
        prepare(dataList, true);
    }


    private void prepare(ArrayList<History> dataList, boolean doNeedClear) {
        // because of stackFromBottom of PullToRefreshListView does not work
        Collections.reverse(dataList);

        if (mDataList == null) mDataList = new FeedList<History>();
        if (doNeedClear) mDataList.clear();

        long numb = Data.midnight - Utils.DAY * 5;
        History prevHistory = null;
        long prevDate = 0;

        if (!doNeedClear && mDataList.size() != 0) {
            if (mDataList.getLast() != null) {
                prevDate = mDataList.getLast().created;
            }
            prevDate = getSimplifiedDate(prevDate);
            prevHistory = mDataList.getLast();
        }

        for (History history : dataList) {
            if (history == null) continue;

            // Date
            prevDate = setDate(numb, prevDate, history, history.created);
            // Type
            int itemType = getItemType(prevHistory, history);

            prevHistory = history;
            mItemLayoutList.add(itemType);
            mDataList.add(history);
        }
    }

    /**
     * Setting date divider if needed
     *
     * @param numb
     * @param prevDate
     * @param history
     * @param created
     * @return previous date
     */
    private long setDate(long numb, long prevDate, History history, long created) {
        String formattedDate = Static.EMPTY;
        if (created < numb) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(created);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            created = cal.getTimeInMillis();
            if (prevDate != created)
                formattedDate = DateFormat.format("dd MMMM", history.created).toString();

        } else {
            created = getSimplifiedDate(created);
            formattedDate = getFormattedDate(history, created);
        }

        if (prevDate != created) {
            mItemLayoutList.add(T_DATE);
            mItemTimeList.put(mItemLayoutList.size() - 1, formattedDate.toUpperCase());
            mDataList.add(null);
            prevDate = created;
        }

        return prevDate;
    }

    private long getSimplifiedDate(long date) {
        long simplifiedDate = date;
        if (simplifiedDate > Data.midnight) {
            simplifiedDate = Data.midnight;
        } else if (simplifiedDate > Data.midnight - Utils.DAY) {
            simplifiedDate = Data.midnight - Utils.DAY;
        } else if (simplifiedDate > Data.midnight - Utils.DAY * 2) {
            simplifiedDate = Data.midnight - Utils.DAY * 2;
        } else if (simplifiedDate > Data.midnight - Utils.DAY * 3) {
            simplifiedDate = Data.midnight - Utils.DAY * 3;
        } else if (simplifiedDate > Data.midnight - Utils.DAY * 4) {
            simplifiedDate = Data.midnight - Utils.DAY * 4;
        } else if (simplifiedDate > Data.midnight - Utils.DAY * 5) {
            simplifiedDate = Data.midnight - Utils.DAY * 5;
        }
        return simplifiedDate;
    }

    private String getFormattedDate(History history, long created) {
        String formattedDate;
        if (created == Data.midnight) {
            formattedDate = mContext.getString(R.string.time_today);
        } else if (created == Data.midnight - Utils.DAY) {
            formattedDate = mContext.getString(R.string.time_yesterday);
        } else {
            formattedDate = mDowFormat.format(history.created);
        }
        return formattedDate;
    }

    private int getItemType(History prevHistory, History history) {
        int item_type;

        boolean output = (history.target == FeedDialog.USER_MESSAGE);
        switch (history.type) {
            case FeedDialog.GIFT:
                if (prevHistory != null && history.target == prevHistory.target) {
                    item_type = output ? T_USER_GIFT_EXT : T_FRIEND_GIFT_EXT;
                } else {
                    item_type = output ? T_USER_GIFT_PHOTO : T_FRIEND_GIFT_PHOTO;
                }
                break;
            case FeedDialog.MAP:
                if (prevHistory != null && history.target == prevHistory.target) {
                    item_type = output ? T_USER_MAP_EXT : T_FRIEND_MAP_EXT;
                } else {
                    item_type = output ? T_USER_MAP_PHOTO : T_FRIEND_MAP_PHOTO;
                }
                break;
            case FeedDialog.ADDRESS:
                if (prevHistory != null && history.target == prevHistory.target) {
                    item_type = output ? T_USER_MAP_EXT : T_FRIEND_MAP_EXT;
                } else {
                    item_type = output ? T_USER_MAP_PHOTO : T_FRIEND_MAP_PHOTO;
                }
                break;
            case FeedDialog.LIKE_REQUEST:
                if (prevHistory != null && history.target == prevHistory.target) {
                    item_type = output ? T_USER_REQUEST_EXT : T_FRIEND_REQUEST_EXT;
                } else {
                    item_type = output ? T_USER_REQUEST : T_FRIEND_REQUEST;
                }
                break;
            default:
                if (prevHistory != null && history.target == prevHistory.target) {
                    item_type = output ? T_USER_EXT : T_FRIEND_EXT;
                } else {
                    item_type = output ? T_USER_PHOTO : T_FRIEND_PHOTO;
                }
                break;
        }

        return item_type;
    }

    public void release() {
        if (mDataList != null) {
            mDataList.clear();
        }
        if (mItemLayoutList != null) {
            mItemLayoutList.clear();
        }
        mDataList = null;
        mInflater = null;
        mItemLayoutList = null;
    }

    public History removeItem(int position) {
        History item = null;
        if (!mDataList.isEmpty() && position < mDataList.size() && position > 0) {
            item = mDataList.get(position);
            removeAtPosition(position);
            notifyDataSetChanged();
        }
        return item;
    }

    private void removeAtPosition(int position) {
        for (int i = position; i < mDataList.size() - 1; i++) {
            mDataList.set(i, mDataList.get(i + 1));
        }
        mDataList.remove(mDataList.size() - 1);
        for (int i = position; i < mItemLayoutList.size() - 1; i++) {
            mItemLayoutList.set(i, mItemLayoutList.get(i + 1));
        }
        mItemLayoutList.remove(mItemLayoutList.size() - 1);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<History> getDataCopy() {
        //noinspection unchecked
        ArrayList<History> dataClone = (ArrayList<History>) mDataList.clone();
        Collections.reverse(dataClone);
        return dataClone;

    }

    public void addAll(ArrayList<History> dataList) {
        prepare(dataList, false);
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (visibleItemCount != 0 && firstVisibleItem + visibleItemCount >= totalItemCount - 1) {
            if (mUpdateCallback != null && !mDataList.isEmpty() && mDataList.getLast().isLoader()) {
                mUpdateCallback.onFeedUpdate();
            }
        }
    }

    protected History getRetryItem() {
        //noinspection unchecked
        return (History) new HistoryLoader(IListLoader.ItemType.RETRY);
    }

    @SuppressWarnings("unchecked")
    protected History getLoaderItem() {
        //noinspection unchecked
        return (History) new HistoryLoader(IListLoader.ItemType.LOADER);
    }

    private View.OnClickListener mLikeRequestListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = (Integer) v.getTag();
            final History item = getItem(position);
            if (item != null) {
                lockView();
                EasyTracker.getTracker().trackEvent("VirusLike", "Click", "Chat", 0L);

                new VirusLikesRequest(item.id, mContext).callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        EasyTracker.getTracker().trackEvent("VirusLike", "Success", "Chat", 0L);
                        //После заврешения запроса удаляем элемент
                        removeItem(position);

                        //И предлагаем отправить пользователю запрос своим друзьям не из приложения
                        new VirusLike(response).sendFacebookRequest(
                                "Chat",
                                mContext,
                                new VirusLike.VirusLikeDialogListener(mContext)
                        );
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        EasyTracker.getTracker().trackEvent("VirusLike", "Fail", "Chat", 0L);
                        Utils.showErrorMessage(getContext());
                    }

                    @Override
                    public void always(ApiResponse response) {
                        super.always(response);
                        unlockView();
                    }
                }).exec();
            }
        }
    };

    private void lockView() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.VISIBLE);
        }
    }

    private void unlockView() {
        if (mLockerView != null) {
            mLockerView.setVisibility(View.GONE);
        }
    }
}

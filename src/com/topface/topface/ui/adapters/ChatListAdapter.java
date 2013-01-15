package com.topface.topface.ui.adapters;

import android.content.Context;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.format.DateFormat;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
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
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.MemoryCacheTemplate;
import com.topface.topface.utils.OsmManager;
import com.topface.topface.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("deprecation")
public class ChatListAdapter extends BaseAdapter {

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

    private Context mContext;
    private LayoutInflater mInflater;
    private FeedList<History> mDataList; // data
    private LinkedList<Integer> mItemLayoutList; // types
    private HashMap<Integer, String> mItemTimeList; // date
    private View.OnClickListener mOnClickListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private MemoryCacheTemplate<String, String> mAddressesCache;
    // Type Item
    private static final int T_USER_PHOTO = 0;
    private static final int T_USER_EXT = 1;
    private static final int T_FRIEND_PHOTO = 2;
    private static final int T_FRIEND_EXT = 3;
    private static final int T_DATE = 4;
    private static final int T_USER_GIFT_PHOTO = 5;
    private static final int T_USER_GIFT_EXT = 6;
    private static final int T_FRIEND_GIFT_PHOTO = 7;
    private static final int T_FRIEND_GIFT_EXT = 8;
    private static final int T_USER_MAP_PHOTO = 9;
    private static final int T_USER_MAP_EXT = 10;
    private static final int T_FRIEND_MAP_PHOTO = 11;
    private static final int T_FRIEND_MAP_EXT = 12;
    private static final int T_USER_REQUEST = 13;
    private static final int T_USER_REQUEST_EXT = 14;
    private static final int T_FRIEND_REQUEST = 15;
    private static final int T_FRIEND_REQUEST_EXT = 16;

    private static final int T_COUNT = 17;


    ChatFragment.OnListViewItemLongClickListener mLongClickListener;


    public ChatListAdapter(Context context, ArrayList<History> dataList) {
        mContext = context;
        mItemLayoutList = new LinkedList<Integer>();
        mItemTimeList = new HashMap<Integer, String>();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mAddressesCache = new MemoryCacheTemplate<String, String>();

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
        return mItemLayoutList.get(position);
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
        boolean output = (item.target == FeedDialog.USER_MESSAGE);
        switch (type) {
            case T_DATE:
                convertView = mInflater.inflate(R.layout.chat_date_divider, null, false);
                holder.date = (TextView) convertView.findViewById(R.id.tvChatDateDivider);
                break;
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
                if(type == T_FRIEND_GIFT_PHOTO  || type == T_USER_GIFT_PHOTO) {
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
                if (type == T_FRIEND_MAP_PHOTO  || type == T_USER_MAP_PHOTO) {
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
        View convertView;
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

            mapAddressDetection(item, holder.address, holder.prgsAddress);
            return true;
        }
        return false;
    }

    private void setMessageText(ViewHolder holder, History item) {
        switch (item.type) {
            case FeedDialog.DEFAULT:
                holder.message.setText(Html.fromHtml(item.text));
                break;
            case FeedDialog.MESSAGE:
                holder.message.setText(Html.fromHtml(item.text));
                break;
            case FeedDialog.MESSAGE_WISH:
                switch (item.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wish_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wish_out));
                        break;
                }
                break;
            case FeedDialog.MESSAGE_SEXUALITY:
                switch (item.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_sexuality_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_sexuality_out));
                        break;
                }
                break;
            case FeedDialog.LIKE:
                switch (item.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_like_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_like_out));
                        break;
                }
                break;
            case FeedDialog.SYMPHATHY:
                switch (item.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_mutual_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_mutual_out));
                        break;
                }
                break;
            case FeedDialog.MESSAGE_WINK:
                switch (item.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wink_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wink_out));
                        break;
                }
                break;
            case FeedDialog.PROMOTION:
                holder.message.setText(Html.fromHtml(item.text));
                break;
            case FeedDialog.LIKE_REQUEST:
                holder.message.setText(item.text);
                break;
            default:
                holder.message.setText(Html.fromHtml(item.text));
                break;
        }

        holder.message.setMovementMethod(LinkMovementMethod.getInstance());
        holder.date.setText(dateFormat.format(item.created));
    }

    public void copyText(String text) {
        ClipboardManager clipboard =
                (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
        Toast.makeText(mContext, R.string.general_msg_copied, Toast.LENGTH_SHORT).show();
    }

    public void addSentMessage(History msg) {
        int position = mDataList.size() - 1;
        History prevHistory = null;
        if (position >= 0) {
            prevHistory = getLastRealMessage(); //get(mDataList.size() - 1);
        }

        int type = getItemType(prevHistory,msg);
        mItemLayoutList.add(type);

        if (msg.type == FeedDialog.GIFT) {
            if (prevHistory == null)
                mItemLayoutList.add(T_USER_GIFT_PHOTO);
            else {
                if (prevHistory.target == FeedDialog.USER_MESSAGE)
                    mItemLayoutList.add(T_USER_GIFT_EXT);
                else
                    mItemLayoutList.add(T_USER_GIFT_PHOTO);
            }

        } else if (msg.type == FeedDialog.MAP) {
            if (prevHistory == null)
                mItemLayoutList.add(T_USER_MAP_PHOTO);
            else {
                if (prevHistory.target == FeedDialog.USER_MESSAGE)
                    mItemLayoutList.add(T_USER_MAP_EXT);
                else
                    mItemLayoutList.add(T_USER_MAP_PHOTO);

            }
        } else if (msg.type == FeedDialog.ADDRESS) {
            if (prevHistory == null)
                mItemLayoutList.add(T_USER_MAP_PHOTO);
            else {
                if (prevHistory.target == FeedDialog.USER_MESSAGE)
                    mItemLayoutList.add(T_USER_MAP_EXT);
                else
                    mItemLayoutList.add(T_USER_MAP_PHOTO);
            }
        } else {
            if (prevHistory == null) {
                mItemLayoutList.add(T_USER_PHOTO);
            } else {
                if (prevHistory.target == FeedDialog.USER_MESSAGE)
                    mItemLayoutList.add(T_USER_EXT);
                else
                    mItemLayoutList.add(T_USER_PHOTO);
            }
        }


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

    public void setDataList(ArrayList<History> dataList) {
        prepare(dataList, true);
    }

    private void prepare(ArrayList<History> dataList, boolean doNeedClear) {
        // because of stackFromBottom of PullToRefreshListView does not work
        Collections.reverse(dataList);

        SimpleDateFormat dowFormat = new SimpleDateFormat("EEEE");

        long day = 1000 * 60 * 60 * 24;
        long numb = Data.midnight - day * 5;

        if (mDataList != null) {
            if (doNeedClear) {
                mDataList.clear();
            }
        } else {
            mDataList = new FeedList<History>();
        }
        if (doNeedClear) {
            mItemLayoutList.clear();
        }

        int prev_target = -1;
        long prev_date = 0;
        if (!doNeedClear && mDataList.size() != 0) {
            if (mDataList.getLast() != null) {
                prev_date = mDataList.getLast().created;
                if (prev_date > Data.midnight) {
                    prev_date = Data.midnight;
                } else if (prev_date > Data.midnight - day) {
                    prev_date = Data.midnight - day;
                } else if (prev_date > Data.midnight - day * 2) {
                    prev_date = Data.midnight - day * 2;
                } else if (prev_date > Data.midnight - day * 3) {
                    prev_date = Data.midnight - day * 3;
                } else if (prev_date > Data.midnight - day * 4) {
                    prev_date = Data.midnight - day * 4;
                } else if (prev_date > Data.midnight - day * 5) {
                    prev_date = Data.midnight - day * 5;
                }
                prev_target = mDataList.getLast().target;
            }
        }

        for (History history : dataList) {
            if (history == null) {
                continue;
            }

            long created = history.created;

            // Date
            {
                String formatedDate = Static.EMPTY;
                if (created < numb) {

                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(created);
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);

                    created = cal.getTimeInMillis();
                    if (prev_date != created)
                        formatedDate = DateFormat.format("dd MMMM", history.created).toString();

                } else if (created > Data.midnight) {
                    created = Data.midnight;
                    formatedDate = mContext.getString(R.string.time_today); // м.б.
                    // переделать
                    // на
                    // HashMap
                } else {
                    if (created > Data.midnight - day) {
                        created = Data.midnight - day;
                        formatedDate = mContext.getString(R.string.time_yesterday);
                    } else if (created > Data.midnight - day * 2) {
                        created = Data.midnight - day * 2;
                        formatedDate = dowFormat.format(history.created);
                    } else if (created > Data.midnight - day * 3) {
                        created = Data.midnight - day * 3;
                        formatedDate = dowFormat.format(history.created);
                    } else if (created > Data.midnight - day * 4) {
                        created = Data.midnight - day * 4;
                        formatedDate = dowFormat.format(history.created);
                    } else if (created > Data.midnight - day * 5) {
                        created = Data.midnight - day * 5;
                        formatedDate = dowFormat.format(history.created);
                    }
                }

                if (prev_date != created) {
                    mItemLayoutList.add(T_DATE);
                    mItemTimeList.put(mItemLayoutList.size() - 1, formatedDate.toUpperCase());
                    mDataList.add(null);
                    prev_date = created;
                }
            }

            // Type
            int item_type = getItemType(prev_target, history);

            prev_target = history.target;

            mItemLayoutList.add(item_type);
            mDataList.add(history);
        }
    }

    private int getItemType(History prevHistory, History history) {
        int item_type;

        if (history.target == FeedDialog.FRIEND_MESSAGE) {
            switch (history.type) {
                case FeedDialog.GIFT:
                    if (prevHistory == null) item_type = T_FRIEND_GIFT_PHOTO;
                    else {
                        if (history.target == prevHistory.target) item_type = T_FRIEND_GIFT_EXT;
                        else item_type = T_FRIEND_GIFT_PHOTO;
                    }
                    break;
                case FeedDialog.MAP:
                    if (prevHistory == null) item_type = T_FRIEND_MAP_PHOTO;
                    else {
                        if (history.target == prevHistory.target) item_type = T_FRIEND_MAP_EXT;
                        else item_type = T_FRIEND_MAP_PHOTO;
                    }
                    break;
                case FeedDialog.ADDRESS:
                    if (prevHistory == null) item_type = T_FRIEND_MAP_PHOTO;
                    else {
                        if (history.target == prevHistory.target) item_type = T_FRIEND_MAP_EXT;
                        else item_type = T_FRIEND_MAP_PHOTO;
                    }
                    break;
                case FeedDialog.LIKE_REQUEST:
                    if (prevHistory == null) item_type = T_FRIEND_REQUEST;
                    else {
                        if (history.target == prevHistory.target) item_type = T_FRIEND_REQUEST_EXT;
                        else item_type = T_FRIEND_REQUEST;
                    }
                    break;
                default:
                    if (prevHistory == null) item_type = T_FRIEND_PHOTO;
                    else {
                        if (history.target == prevHistory.target) item_type = T_FRIEND_EXT;
                        else item_type = T_FRIEND_PHOTO;
                    }
                    break;
            }
        } else {
            switch (history.type) {
                case FeedDialog.GIFT:
                    if (prevHistory == null) item_type = T_USER_GIFT_PHOTO;
                    else {
                        if (history.target == prevHistory.target) item_type = T_USER_GIFT_EXT;
                        else item_type = T_USER_GIFT_PHOTO;
                    }
                    break;
                case FeedDialog.MAP:
                    if (prevHistory == null) item_type = T_USER_MAP_PHOTO;
                    else {
                    if (history.target == prevHistory.target) {
                        item_type = T_USER_MAP_EXT;
                    else
                        item_type = T_USER_MAP_PHOTO;

                    }
                    break;
                case FeedDialog.ADDRESS:
                    if (history.target == prevHistory.target) {
                        item_type = T_USER_MAP_EXT;
                    } else {
                        item_type = T_USER_MAP_PHOTO;
                    }
                    break;
                case FeedDialog.LIKE_REQUEST:
                    if (history.target == prevHistory.target) {
                        item_type = T_USER_REQUEST_EXT;
                    } else {
                        item_type = T_USER_REQUEST;
                    }
                    break;
                default:
                    if (history.target == prevHistory.target) {
                        item_type = T_USER_EXT;
                    } else {
                        item_type = T_USER_PHOTO;
                    }
                    break;
            }
        }
        return item_type;
    }

    private void mapAddressDetection(final History history, final TextView tv,
                                     final ProgressBar prgsBar) {
        if (history.geo != null) {
            final String key = history.geo.getCoordinates().toString();
            String cachedAddress = mAddressesCache.get(key);

            if (cachedAddress != null) {
                tv.setText(cachedAddress);
                return;
            }

            prgsBar.setVisibility(View.VISIBLE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final String address = OsmManager.getAddress(
                            history.geo.getCoordinates().getLatitude(),
                            history.geo.getCoordinates().getLongitude()
                    );
                    mAddressesCache.put(key, address);
                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(address);
                            prgsBar.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        }

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

    private View.OnClickListener mLikeRequestListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int position = (Integer) v.getTag();
            final History item = getItem(position);
            if (item != null) {
                new VirusLikesRequest(item.id, mContext).callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        //После заврешения запроса удаляем элемент
                        removeItem(position);

                        //И предлагаем отправить пользователю запрос своим друзьям не из приложения
                        new VirusLike(response).sendFacebookRequest(
                                mContext,
                                new VirusLike.VirusLikeDialogListener(mContext)
                        );
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        Utils.showErrorMessage(getContext());
                    }
                }).exec();
            }
        }
    };
}

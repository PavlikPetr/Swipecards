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
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.MemoryCacheTemplate;
import com.topface.topface.utils.OsmManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

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
    private static final int T_COUNT = 13;


    ChatFragment.OnListViewItemLongClickListener mLongClickListener;


    public ChatListAdapter(Context context, LinkedList<History> dataList) {
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
        final History history = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();

            switch (type) {
                case T_FRIEND_PHOTO:
                    convertView = mInflater.inflate(R.layout.chat_friend, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                    holder.avatar.setOnClickListener(mOnClickListener);
                    holder.avatar.setPhoto(history.user.photo);
                    break;
                case T_FRIEND_EXT:
                    convertView = mInflater.inflate(R.layout.chat_friend_ext, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                    break;
                case T_USER_PHOTO:
                    convertView = mInflater.inflate(R.layout.chat_user, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                    holder.avatar.setPhoto(CacheProfile.photo);
                    break;
                case T_USER_EXT:
                    convertView = mInflater.inflate(R.layout.chat_user_ext, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.message = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.date = (TextView) convertView.findViewById(R.id.chat_date);
                    break;
                case T_DATE:
                    convertView = mInflater.inflate(R.layout.chat_date_divider, null, false);
                    holder.date = (TextView) convertView.findViewById(R.id.tvChatDateDivider);
                    break;
                case T_USER_GIFT_PHOTO:
                    convertView = mInflater.inflate(R.layout.chat_user_gift, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.gift = (ImageViewRemote) convertView.findViewById(R.id.ivChatGift);
                    holder.avatar.setPhoto(CacheProfile.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                    break;
                case T_USER_GIFT_EXT:
                    convertView = mInflater.inflate(R.layout.chat_user_gift, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.gift = (ImageViewRemote) convertView.findViewById(R.id.ivChatGift);
                    holder.avatar.setVisibility(View.INVISIBLE);
                    break;
                case T_FRIEND_GIFT_PHOTO:
                    convertView = mInflater.inflate(R.layout.chat_friend_gift, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.gift = (ImageViewRemote) convertView.findViewById(R.id.ivChatGift);
                    holder.avatar.setOnClickListener(mOnClickListener);
                    holder.avatar.setPhoto(history.user.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                    break;
                case T_FRIEND_GIFT_EXT:
                    convertView = mInflater.inflate(R.layout.chat_friend_gift, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.gift = (ImageViewRemote) convertView.findViewById(R.id.ivChatGift);
                    holder.avatar.setVisibility(View.INVISIBLE);
                    break;
                case T_USER_MAP_PHOTO:
                    convertView = mInflater.inflate(R.layout.chat_user_map, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.address = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
                    holder.mapBackground = (ImageView) convertView.findViewById(R.id.ivUserMapBg);
                    holder.prgsAddress = (ProgressBar) convertView.findViewById(R.id.prgsUserMapAddress);
                    holder.avatar.setPhoto(CacheProfile.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                    break;
                case T_USER_MAP_EXT:
                    convertView = mInflater.inflate(R.layout.chat_user_map, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.address = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
                    holder.mapBackground = (ImageView) convertView.findViewById(R.id.ivUserMapBg);
                    holder.prgsAddress = (ProgressBar) convertView.findViewById(R.id.prgsUserMapAddress);
                    holder.avatar.setVisibility(View.INVISIBLE);
                    break;
                case T_FRIEND_MAP_PHOTO:
                    convertView = mInflater.inflate(R.layout.chat_friend_map, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.address = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
                    holder.mapBackground = (ImageView) convertView.findViewById(R.id.ivFriendMapBg);
                    holder.prgsAddress = (ProgressBar) convertView
                            .findViewById(R.id.prgsFriendMapAddress);
                    holder.avatar.setOnClickListener(mOnClickListener);
                    holder.avatar.setPhoto(history.user.photo);
                    holder.avatar.setVisibility(View.VISIBLE);
                    break;
                case T_FRIEND_MAP_EXT:
                    convertView = mInflater.inflate(R.layout.chat_friend_map, null, false);
                    holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.left_icon);
                    holder.address = (TextView) convertView.findViewById(R.id.tvChatMapAddress);
                    holder.mapBackground = (ImageView) convertView.findViewById(R.id.ivFriendMapBg);
                    holder.prgsAddress = (ProgressBar) convertView
                            .findViewById(R.id.prgsFriendMapAddress);
                    holder.avatar.setVisibility(View.INVISIBLE);
                    break;
            }

            if (convertView != null) {
                convertView.setTag(holder);
            }

        } else
            holder = (ViewHolder) convertView.getTag();

        // setting visual information
        if (type == T_DATE) {
            holder.date.setText(mItemTimeList.get(position));
            return convertView;
        } else if (type == T_USER_GIFT_PHOTO || type == T_USER_GIFT_EXT
                || type == T_FRIEND_GIFT_PHOTO || type == T_FRIEND_GIFT_EXT) {
            holder.gift.setRemoteSrc(history.link);
            return convertView;
        } else if (type == T_USER_MAP_PHOTO || type == T_USER_MAP_EXT
                || type == T_FRIEND_MAP_PHOTO || type == T_FRIEND_MAP_EXT) {
            holder.address.setText(Static.EMPTY);

            if (history.type == FeedDialog.MAP) {
                holder.mapBackground.setBackgroundResource(R.drawable.chat_item_place);
            } else {
                holder.mapBackground.setBackgroundResource(R.drawable.chat_item_map);
            }

            holder.mapBackground.setTag(history);
            holder.mapBackground.setOnClickListener(mOnClickListener);

            mapAddressDetection(history, holder.address, holder.prgsAddress);
            return convertView;
        }

        // setting textual information
        switch (history.type) {
            case FeedDialog.DEFAULT:
                holder.message.setText(Html.fromHtml(history.text));
                break;
            case FeedDialog.PHOTO:
//			if (history.code > 100500) {
//				holder.message.setText(history.text);
//				//holder.message.setText(mContext.getString(R.string.chat_money_in) + ".");
//				break;
//			}
                holder.message.setText("TARGET IT");
//			switch (history.target) {
//			case FeedDialog.FRIEND_MESSAGE:
//				holder.message.setText(mContext.getString(R.string.chat_rate_in) + " " + history.code + ".");
//				break;
//			case FeedDialog.USER_MESSAGE:
//				holder.message.setText(mContext.getString(R.string.chat_rate_out) + " " + history.code + ".");
//				break;
//			}
                break;
            case FeedDialog.MESSAGE:
                holder.message.setText(Html.fromHtml(history.text));
                break;
            case FeedDialog.MESSAGE_WISH:
                switch (history.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wish_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wish_out));
                        break;
                }
                break;
            case FeedDialog.MESSAGE_SEXUALITY:
                switch (history.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_sexuality_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_sexuality_out));
                        break;
                }
                break;
            case FeedDialog.LIKE:
                switch (history.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_like_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_like_out));
                        break;
                }
                break;
            case FeedDialog.SYMPHATHY:
                switch (history.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_mutual_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_mutual_out));
                        break;
                }
                break;
            case FeedDialog.MESSAGE_WINK:
                switch (history.target) {
                    case FeedDialog.FRIEND_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wink_in));
                        break;
                    case FeedDialog.USER_MESSAGE:
                        holder.message.setText(mContext.getString(R.string.chat_wink_out));
                        break;
                }
                break;
            case FeedDialog.RATE:
                holder.message.setText("RATE IT");
//			switch (history.target) {
//			case FeedDialog.FRIEND_MESSAGE:
//				holder.message.setText(mContext.getString(R.string.chat_rate_in) + " " + history.code + ".");
//				break;
//			case FeedDialog.USER_MESSAGE:
//				holder.message.setText(mContext.getString(R.string.chat_rate_out) + " " + history.code + ".");
//				break;
//			}
                break;
            case FeedDialog.PROMOTION:
                holder.message.setText(Html.fromHtml(history.text));
                break;
            default:
                holder.message.setText(Html.fromHtml(history.text));
                break;
        }

        holder.message.setMovementMethod(LinkMovementMethod.getInstance());

        holder.date.setText(dateFormat.format(history.created));
        // Utils.formatTime(holder.date, msg.created);
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
        return convertView;
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

    public void setDataList(LinkedList<History> dataList) {
        prepare(dataList, true);
    }

    private void prepare(LinkedList<History> dataList, boolean doNeedClear) {
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
            int item_type;
            if (history.target == FeedDialog.FRIEND_MESSAGE) {
                switch (history.type) {
                    case FeedDialog.GIFT:
                        if (history.target == prev_target)
                            item_type = T_FRIEND_GIFT_EXT;
                        else
                            item_type = T_FRIEND_GIFT_PHOTO;
                        break;
                    case FeedDialog.MAP:
                        if (history.target == prev_target)
                            item_type = T_FRIEND_MAP_EXT;
                        else
                            item_type = T_FRIEND_MAP_PHOTO;
                        break;
                    case FeedDialog.ADDRESS:
                        if (history.target == prev_target)
                            item_type = T_FRIEND_MAP_EXT;
                        else
                            item_type = T_FRIEND_MAP_PHOTO;
                        break;
                    default:
                        if (history.target == prev_target)
                            item_type = T_FRIEND_EXT;
                        else
                            item_type = T_FRIEND_PHOTO;
                        break;
                }
            } else {
                switch (history.type) {
                    case FeedDialog.GIFT:
                        if (history.target == prev_target)
                            item_type = T_USER_GIFT_EXT;
                        else
                            item_type = T_USER_GIFT_PHOTO;
                        break;
                    case FeedDialog.MAP:
                        if (history.target == prev_target) {
                            item_type = T_USER_MAP_EXT;
                        } else {
                            item_type = T_USER_MAP_PHOTO;
                        }
                        break;
                    case FeedDialog.ADDRESS:
                        if (history.target == prev_target) {
                            item_type = T_USER_MAP_EXT;
                        } else {
                            item_type = T_USER_MAP_PHOTO;
                        }
                        break;
                    default:
                        if (history.target == prev_target) {
                            item_type = T_USER_EXT;
                        } else {
                            item_type = T_USER_PHOTO;
                        }
                        break;
                }
            }

            prev_target = history.target;

            mItemLayoutList.add(item_type);
            mDataList.add(history);
        }
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
    public LinkedList<History> getDataCopy() {
        //noinspection unchecked
        LinkedList<History> dataClone = (LinkedList<History>) mDataList.clone();
        Collections.reverse(dataClone);
        return dataClone;

    }

    public void addAll(LinkedList<History> dataList) {
        prepare(dataList, false);
    }
}

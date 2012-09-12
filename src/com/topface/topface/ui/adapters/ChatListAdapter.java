package com.topface.topface.ui.adapters;

import java.util.LinkedList;

import android.text.Html;
import android.widget.ImageView;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.imageloader.DefaultImageLoader;
import com.topface.topface.imageloader.RoundPostProcessor;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {

    private RoundPostProcessor mPostProcessor;
    private String mUserAvatar;

    //---------------------------------------------------------------------------
    // class ViewHolder
    static class ViewHolder {
        RoundedImageView mAvatar;
        TextView mMessage;
        TextView mDate;
    }

    //---------------------------------------------------------------------------
    // Data
    private Context mContext;
    private int mUserId;
    private LinkedList<History> mList;
    private View.OnClickListener mOnAvatarListener;
    private LayoutInflater mInflater;
    private LinkedList<Integer> mItemLayoutList = new LinkedList<Integer>();
    // Type Item
    private static final int T_USER_PHOTO = 0;
    private static final int T_USER_EXT = 1;
    private static final int T_FRIEND_PHOTO = 2;
    private static final int T_FRIEND_EXT = 3;
    private static final int T_COUNT = 4;

    //---------------------------------------------------------------------------
    public ChatListAdapter(Context context, int userId, String avatar, LinkedList<History> dataList) {
        mContext = context;
        mList = dataList;
        mUserId = userId;
        mUserAvatar = avatar;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPostProcessor = new RoundPostProcessor(AvatarManager.AVATAR_ROUND_RADIUS);
        prepare(dataList);
    }

    //---------------------------------------------------------------------------
    public void setOnAvatarListener(View.OnClickListener onAvatarListener) {
        mOnAvatarListener = onAvatarListener;
    }

    /**
     * При долгом клике на TextView будет скопирован его текст и показано сообщение об этом
     */
    private View.OnLongClickListener mOnTextClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            TextView messageView = (TextView) v;

            Utils.copyTextToClipboard(messageView.getText().toString(), mContext);
            Toast.makeText(mContext, R.string.chat_message_copied, Toast.LENGTH_SHORT).show();

            return false;
        }
    };

    @Override
    public int getCount() {
        return mList.size();
    }

    //---------------------------------------------------------------------------
    @Override
    public History getItem(int position) {
        return mList.get(position);
    }

    //---------------------------------------------------------------------------
    @Override
    public long getItemId(int position) {
        return position;
    }

    //---------------------------------------------------------------------------
    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }

    //---------------------------------------------------------------------------
    @Override
    public int getItemViewType(int position) {
        return mItemLayoutList.get(position);
    }

    //---------------------------------------------------------------------------
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case T_FRIEND_PHOTO: {
                    convertView = mInflater.inflate(R.layout.chat_friend, null, false);
                    holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
                    holder.mAvatar.setOnClickListener(mOnAvatarListener);
                    loadAvatar(mUserAvatar, holder.mAvatar);
                }
                break;
                case T_FRIEND_EXT: {
                    convertView = mInflater.inflate(R.layout.chat_friend_ext, null, false);
                    holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
                }
                break;
                case T_USER_PHOTO: {
                    convertView = mInflater.inflate(R.layout.chat_user, null, false);
                    holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
                    loadAvatar(CacheProfile.avatar_small, holder.mAvatar);
                }
                break;
                case T_USER_EXT: {
                    convertView = mInflater.inflate(R.layout.chat_user_ext, null, false);
                    holder.mAvatar = (RoundedImageView) convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView) convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView) convertView.findViewById(R.id.chat_date);
                }
                break;
            }

            if (holder.mMessage != null) {
                holder.mMessage.setOnLongClickListener(mOnTextClickListener);
            }

            if (convertView != null) {
                convertView.setTag(holder);
            }

        } else
            holder = (ViewHolder) convertView.getTag();

        History msg = getItem(position);

        switch (msg.type) {
            case History.DEFAULT:
                holder.mMessage.setText(Html.fromHtml(msg.text));
                break;
            case History.PHOTO: {
                if (msg.code > 100500) {
                    holder.mMessage.setText(mContext.getString(R.string.chat_money_in) + /*" " + msg.code +*/ ".");
                    break;
                }
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_rate_in) + " " + msg.code + ".");
                    }
                    break;
                    case T_USER_PHOTO:
                    case T_USER_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_rate_out) + " " + msg.code + ".");
                    }
                    break;
                }
            }
            break;
            case History.GIFT:
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_gift_in));
                    }
                    break;
                    case T_USER_PHOTO:
                    case T_USER_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_gift_out));
                    }
                    break;
                }
                break;
            case History.MESSAGE:
                holder.mMessage.setText(Html.fromHtml(msg.text));
                break;
            case History.MESSAGE_WISH:
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_wish_in));
                    }
                    break;
                    case T_USER_PHOTO:
                    case T_USER_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_wish_out));
                    }
                    break;
                }
                break;
            case History.MESSAGE_SEXUALITY:
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_in));
                    }
                    break;
                    case T_USER_PHOTO:
                    case T_USER_EXT: {
                        holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_out));
                    }
                    break;
                }
                break;
        }

        Utils.formatTime(holder.mDate, msg.created);

        return convertView;
    }

    //---------------------------------------------------------------------------
    public void addSentMessage(History msg) {
        int position = mList.size() - 1;
        if (position < 0)
            mItemLayoutList.add(T_USER_PHOTO);
        else {
            History history = mList.get(mList.size() - 1);
            if (history.owner_id == mUserId)
                mItemLayoutList.add(T_USER_PHOTO);
            else
                mItemLayoutList.add(T_USER_EXT);
        }

        mList.add(msg);
    }

    //---------------------------------------------------------------------------
    public void setDataList(LinkedList<History> dataList) {
        prepare(dataList);
        mList.addAll(dataList);
    }

    //---------------------------------------------------------------------------
    public void prepare(LinkedList<History> dataList) {
        int prev_id = 0;
        for (History history : dataList) {
            if (history.owner_id == mUserId)
                if (history.owner_id == prev_id)
                    mItemLayoutList.add(T_FRIEND_EXT);
                else
                    mItemLayoutList.add(T_FRIEND_PHOTO);
            else if (history.owner_id == prev_id)
                mItemLayoutList.add(T_USER_EXT);
            else
                mItemLayoutList.add(T_USER_PHOTO);
            prev_id = history.owner_id;
        }
    }

    //---------------------------------------------------------------------------
    public void release() {
        if (mList != null)
            mList.clear();
        mList = null;
        mInflater = null;
        if (mItemLayoutList != null)
            mItemLayoutList.clear();
        mItemLayoutList = null;
    }

    private void loadAvatar(String url, ImageView view) {
        DefaultImageLoader.getInstance().displayImage(url, view, mPostProcessor);
    }
    //---------------------------------------------------------------------------
}

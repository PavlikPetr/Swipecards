package com.topface.topface.ui.adapters;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.StorageCache;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter {
    //---------------------------------------------------------------------------
    // class ViewHolder
    static class ViewHolder {
        RoundedImageView mAvatar;
        TextView mMessage;
        TextView mDate;
        ImageView mGift;
        View mInfoGroup;
    }
    //---------------------------------------------------------------------------
    // Data
    private Context mContext;
    private int mUserId;
    private LayoutInflater mInflater;
    private LinkedList<History> mDataList; // data
    private LinkedList<Integer> mItemLayoutList; // types
    private HashMap<Integer, String> mItemTimeList; // date
    private View.OnClickListener mOnAvatarListener;
    // Type Item
    private static final int T_USER_PHOTO = 0;
    private static final int T_USER_EXT = 1;
    private static final int T_FRIEND_PHOTO = 2;
    private static final int T_FRIEND_EXT = 3;
    private static final int T_DATE = 4;
    private static final int T_COUNT = 5;
    //---------------------------------------------------------------------------
    public ChatListAdapter(Context context,int userId,LinkedList<History> dataList) {
        mContext = context;
        mUserId = userId;
        mItemLayoutList = new LinkedList<Integer>();
        mItemTimeList = new HashMap<Integer, String>();
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        prepare(dataList);
    }
    //---------------------------------------------------------------------------
    public void setOnAvatarListener(View.OnClickListener onAvatarListener) {
        mOnAvatarListener = onAvatarListener;
    }
    //---------------------------------------------------------------------------
    @Override
    public int getCount() {
        return mDataList.size();
    }
    //---------------------------------------------------------------------------
    @Override
    public History getItem(int position) {
        return mDataList.get(position);
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
    public View getView(int position,View convertView,ViewGroup parent) {
        final ViewHolder holder;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case T_FRIEND_PHOTO: {
                    convertView = mInflater.inflate(R.layout.chat_friend, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView)convertView.findViewById(R.id.chat_date);
                    holder.mAvatar.setOnClickListener(mOnAvatarListener);
                    if (Data.userAvatar != null)
                        holder.mAvatar.setImageBitmap(Data.userAvatar);
                    holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                    holder.mGift = (ImageView)convertView.findViewById(R.id.chat_gift);
                } break;
                case T_FRIEND_EXT: {
                    convertView = mInflater.inflate(R.layout.chat_friend_ext, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView)convertView.findViewById(R.id.chat_date);
                    holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                    holder.mGift = (ImageView)convertView.findViewById(R.id.chat_gift);
                } break;
                case T_USER_PHOTO: {
                    convertView = mInflater.inflate(R.layout.chat_user, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView)convertView.findViewById(R.id.chat_date);
                    holder.mAvatar.setImageBitmap(Data.ownerAvatar);
                    holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                    holder.mGift = (ImageView)convertView.findViewById(R.id.chat_gift);
                } break;
                case T_USER_EXT: {
                    convertView = mInflater.inflate(R.layout.chat_user_ext, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView)convertView.findViewById(R.id.chat_date);
                    holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                    holder.mGift = (ImageView)convertView.findViewById(R.id.chat_gift);
                } break;
                case T_DATE: {
                    convertView = mInflater.inflate(R.layout.chat_date_divider, null, false);
                    holder.mDate = (TextView)convertView.findViewById(R.id.tvChatDateDivider);
                } break;
            }

            convertView.setTag(holder);

        } else
            holder = (ViewHolder)convertView.getTag();

        final History msg = getItem(position);
        
        // Date divider
        if(type == T_DATE) {
            holder.mDate.setText("d: "+mItemTimeList.get(position));
            return convertView;
        }

        // восстанавливаем состояние объектов
        holder.mInfoGroup.setVisibility(View.VISIBLE);
        holder.mGift.setVisibility(View.GONE);

        switch (msg.type) {
            case History.DEFAULT:
                holder.mMessage.setText(msg.text);
                break;
            case History.PHOTO: {
                if (msg.code > 100500) {
                    holder.mMessage.setText(mContext.getString(R.string.chat_money_in) + ".");
                    break;
                }
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_rate_in) + " " + msg.code + ".");
                        break;
                    case T_USER_PHOTO:
                    case T_USER_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_rate_out) + " " + msg.code + ".");
                        break;
                }
            } break;
            case History.GIFT: {
              holder.mInfoGroup.setVisibility(View.INVISIBLE);
              holder.mGift.setVisibility(View.VISIBLE);
              
              Bitmap bitmap = (new StorageCache(mContext)).load(msg.gift);
              if(bitmap != null) {
                  holder.mGift.setImageBitmap(bitmap);
              } else {
                  new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap rawBitmap = Http.bitmapLoader(msg.link);
                        final Bitmap roundedBitmap = Utils.getScaleAndRoundBitmap(rawBitmap, rawBitmap.getWidth(), rawBitmap.getHeight(), 1.5f);
                        holder.mGift.post(new Runnable() {
                          @Override
                          public void run() {
                              holder.mGift.setImageBitmap(roundedBitmap);
                          }
                      });
                    }
                }).start();
              }

            } break;
            case History.MESSAGE:
                holder.mMessage.setText(msg.text);
                break;
            case History.MESSAGE_WISH: {
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_wish_in));
                        break;
                    case T_USER_PHOTO:
                    case T_USER_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_wish_out));
                        break;
                } 
            } break;
            case History.MESSAGE_SEXUALITY: {
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_in));
                        break;
                    case T_USER_PHOTO:
                    case T_USER_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_sexuality_out));
                        break;
                }
            } break;
        }

        Utils.formatTime(holder.mDate, msg.created);

        return convertView;
    }
    //---------------------------------------------------------------------------
    public void addSentMessage(History msg) {
        int position = mDataList.size() - 1;
        if (position < 0)
            mItemLayoutList.add(T_USER_PHOTO);
        else {
            History history = mDataList.get(mDataList.size() - 1);
            if (history.owner_id == mUserId)
                mItemLayoutList.add(T_USER_PHOTO);
            else
                mItemLayoutList.add(T_USER_EXT);
        }

        mDataList.add(msg);
    }
    //---------------------------------------------------------------------------
    public void setDataList(LinkedList<History> dataList) {
        prepare(dataList);
    }
    //---------------------------------------------------------------------------
    public void prepare(LinkedList<History> dataList) {
        long now = System.currentTimeMillis() / 1000;
        long day = 60*60*24;
        
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        
        long noon = cal.getTimeInMillis() / 1000;
        
        mDataList = new LinkedList<History>();
        
        int count = dataList.size();
        int prev_id = 0;
        long prev_date = 0;
        boolean z = false;
        for (int i = 0; i < count; i++) {
            History history = dataList.get(i);
            
            long g = history.created;
            
            mItemLayoutList.add(T_DATE);  // DATE
            mItemTimeList.put(mItemLayoutList.size()-1, Utils.formatDate(mContext, history.created));
            
            mDataList.add(null);
            z = true;


            if (history.owner_id == mUserId) {
                if (history.owner_id == prev_id)
                    mItemLayoutList.add(T_FRIEND_EXT);
                else
                    mItemLayoutList.add(T_FRIEND_PHOTO);
                } else if (history.owner_id == prev_id)
                    mItemLayoutList.add(T_USER_EXT);
                else
                    mItemLayoutList.add(T_USER_PHOTO);
                prev_id = history.owner_id;
                
                mDataList.add(history);
            }

    }
    //---------------------------------------------------------------------------
    public void release() {
        if (mDataList != null)
            mDataList.clear();
        mDataList = null;
        mInflater = null;
        if (mItemLayoutList != null)
            mItemLayoutList.clear();
        mItemLayoutList = null;
    }
    //---------------------------------------------------------------------------
}

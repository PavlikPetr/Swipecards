package com.topface.topface.ui.adapters;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import com.google.android.maps.MapView;
import com.topface.topface.Data;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.History;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.Http;
import com.topface.topface.utils.StorageCache;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.format.DateFormat;
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
        TextView mAddress;
        //View mInfoGroup;
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
    private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    private int mGiftFrameWidth;
    // Type Item
    private static final int T_USER_PHOTO = 0;
    private static final int T_USER_EXT = 1;
    private static final int T_FRIEND_PHOTO = 2;
    private static final int T_FRIEND_EXT = 3;
    private static final int T_DATE = 4;
    private static final int T_USER_GIFT = 5;
    private static final int T_FRIEND_GIFT = 6;
    private static final int T_USER_MAP = 7;
    private static final int T_FRIEND_MAP = 8;
    private static final int T_COUNT = 9;
    //---------------------------------------------------------------------------
    public ChatListAdapter(Context context,int userId,LinkedList<History> dataList) {
        mContext = context;
        mUserId = userId;
        mItemLayoutList = new LinkedList<Integer>();
        mItemTimeList = new HashMap<Integer, String>();
        mGiftFrameWidth = BitmapFactory.decodeResource(context.getResources(), R.drawable.chat_gift_frame).getWidth();
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
                    //holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                } break;
                case T_FRIEND_EXT: {
                    convertView = mInflater.inflate(R.layout.chat_friend_ext, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView)convertView.findViewById(R.id.chat_date);
                    //holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                    //holder.mMap = (MapView)convertView.findViewById(R.id.chat_map);
                } break;
                case T_USER_PHOTO: {
                    convertView = mInflater.inflate(R.layout.chat_user, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView)convertView.findViewById(R.id.chat_date);
                    holder.mAvatar.setImageBitmap(Data.ownerAvatar);
                    //holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                } break;
                case T_USER_EXT: {
                    convertView = mInflater.inflate(R.layout.chat_user_ext, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mMessage = (TextView)convertView.findViewById(R.id.chat_message);
                    holder.mDate = (TextView)convertView.findViewById(R.id.chat_date);
                    //holder.mInfoGroup = convertView.findViewById(R.id.user_info);
                } break;
                case T_DATE: {
                    convertView = mInflater.inflate(R.layout.chat_date_divider, null, false);
                    holder.mDate = (TextView)convertView.findViewById(R.id.tvChatDateDivider);
                } break;
                case T_USER_GIFT: {
                    convertView = mInflater.inflate(R.layout.chat_user_gift, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mGift = (ImageView)convertView.findViewById(R.id.ivChatGift);
                    //holder.mDate = (TextView)convertView.findViewById(R.id.tvChatDateDivider);
                } break;
                case T_FRIEND_GIFT: {
                    convertView = mInflater.inflate(R.layout.chat_friend_gift, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mGift = (ImageView)convertView.findViewById(R.id.ivChatGift);
                    //holder.mDate = (TextView)convertView.findViewById(R.id.tvChatDateDivider);
                } break;
                case T_USER_MAP: {
                    convertView = mInflater.inflate(R.layout.chat_user_map, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mAddress = (TextView)convertView.findViewById(R.id.tvChatMapAddress);
                    //holder.mDate = (TextView)convertView.findViewById(R.id.tvChatDateDivider);
                } break;
                case T_FRIEND_MAP: {
                    convertView = mInflater.inflate(R.layout.chat_friend_map, null, false);
                    holder.mAvatar = (RoundedImageView)convertView.findViewById(R.id.left_icon);
                    holder.mAddress = (TextView)convertView.findViewById(R.id.tvChatMapAddress);
                    //holder.mDate = (TextView)convertView.findViewById(R.id.tvChatDateDivider);
                } break;
            }

            convertView.setTag(holder);

        } else
            holder = (ViewHolder)convertView.getTag();
        
        
        final History history = getItem(position);
        
        // Date divider
//        if(type == T_DATE) {
//            holder.mDate.setText(mItemTimeList.get(position));
//            return convertView;
//        }
        
        switch (type) {
            case T_DATE: {
                holder.mDate.setText(mItemTimeList.get(position));
                return convertView;
            }
            case T_USER_GIFT: {
                giftLoading(holder.mGift, history);
                holder.mAvatar.setImageBitmap(Data.ownerAvatar);
                return convertView;
            }
            case T_FRIEND_GIFT: {
                giftLoading(holder.mGift, history);
                holder.mAvatar.setOnClickListener(mOnAvatarListener);
                if (Data.userAvatar != null)
                    holder.mAvatar.setImageBitmap(Data.userAvatar);                
                return convertView;
            }
            case T_USER_MAP: {
                holder.mAddress.setText("address");
                holder.mAvatar.setImageBitmap(Data.ownerAvatar);
                return convertView;
            }
            case T_FRIEND_MAP: {
                holder.mAddress.setText("address");
                holder.mAvatar.setOnClickListener(mOnAvatarListener);
                if (Data.userAvatar != null)
                    holder.mAvatar.setImageBitmap(Data.userAvatar);
                return convertView;
            }
        }

        // восстанавливаем состояние объектов
        //holder.mInfoGroup.setVisibility(View.VISIBLE);
        //holder.mGift.setVisibility(View.GONE);
        
        switch (history.type) {
            case History.DEFAULT:
                holder.mMessage.setText(history.text);
                break;
            case History.PHOTO: {
                if (history.code > 100500) {
                    holder.mMessage.setText(mContext.getString(R.string.chat_money_in) + ".");
                    break;
                }
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_rate_in) + " " + history.code + ".");
                        break;
                    case T_USER_PHOTO:
                    case T_USER_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_rate_out) + " " + history.code + ".");
                        break;
                }
            } break;
            case History.GIFT: {
              //holder.mInfoGroup.setVisibility(View.INVISIBLE);
              /*
              Bitmap bitmap = (new StorageCache(mContext)).load(msg.gift);
              if(bitmap != null) {
                  bitmap = Utils.getScaleAndRoundBitmapOut(bitmap, bitmap.getWidth(), bitmap.getHeight(), 1.5f);
                  holder.mGift.setImageBitmap(bitmap);
                  holder.mGift.setVisibility(View.VISIBLE);
              } else {
                  new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap rawBitmap = Http.bitmapLoader(msg.link);
                        if (rawBitmap == null)
                            return;
                        final Bitmap roundedBitmap = Utils.getScaleAndRoundBitmapOut(rawBitmap, rawBitmap.getWidth(), rawBitmap.getHeight(), 1.5f);
                        holder.mGift.post(new Runnable() {
                          @Override
                          public void run() {
                              if (holder.mGift!=null) {
                                  holder.mGift.setImageBitmap(roundedBitmap);
                                  holder.mGift.setVisibility(View.VISIBLE);
                              }
                          }
                      });
                    }
                }).start();
              }
              */
            } break;
            case History.MESSAGE:
                holder.mMessage.setText(history.text);
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
            case History.MESSAGE_WINK: {
                switch (type) {
                    case T_FRIEND_PHOTO:
                    case T_FRIEND_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_wink_in));
                        break;
                    case T_USER_PHOTO:
                    case T_USER_EXT:
                        holder.mMessage.setText(mContext.getString(R.string.chat_wink_out));
                        break;
                }
            } break;
            default:
                holder.mMessage.setText("");
                break;
        }

        holder.mDate.setText(dateFormat.format(history.created));
        //Utils.formatTime(holder.mDate, msg.created);

        return convertView;
    }
    //---------------------------------------------------------------------------
    public void addSentMessage(History msg) {
        if (msg.type == History.MESSAGE ) {
            int position = mDataList.size() - 1;
            if (position < 0) {
                mItemLayoutList.add(T_USER_PHOTO);
            } else {
                History history = mDataList.get(mDataList.size() - 1);
                if (history.owner_id == mUserId)
                    mItemLayoutList.add(T_USER_PHOTO);
                else
                    mItemLayoutList.add(T_USER_EXT);
            }
        } else if(msg.type == History.GIFT) {
            mItemLayoutList.add(T_USER_GIFT);            
        }
        mDataList.add(msg);
    }
    //---------------------------------------------------------------------------
    public void setDataList(LinkedList<History> dataList) {
        prepare(dataList);
    }
    //---------------------------------------------------------------------------
    private void prepare(LinkedList<History> dataList) {

        SimpleDateFormat dowFormat = new SimpleDateFormat("EEEE");

        long day  = 1000*60*60*24;
        long numb = Data.midnight - day * 5;  
        
        mDataList = new LinkedList<History>();

        int prev_id = 0;
        long prev_date = 0;
        int count = dataList.size();
        for (int i = 0; i < count; i++) {
            History history = dataList.get(i);
            
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
                    formatedDate = mContext.getString(R.string.time_today);   // м.б. переделать на HashMap
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
                    mItemTimeList.put(mItemLayoutList.size()-1, formatedDate.toUpperCase());
                    mDataList.add(null);
                    prev_date = created;
                }
            }

            // Type
            int item_type = 0;
            if (history.owner_id == mUserId) {
                if(history.type == History.GIFT) {
                    item_type = T_FRIEND_GIFT;
                } else if (history.owner_id == prev_id) {
                    item_type = T_FRIEND_EXT;
                } else {
                    item_type = T_FRIEND_PHOTO;
                }
            } else { 
                if(history.type == History.GIFT) {
                    item_type = T_USER_GIFT;
                } else if (history.owner_id == prev_id) {
                    item_type = T_USER_EXT;
                } else {
                    item_type = T_USER_PHOTO;
                }
            }
            
            prev_id = history.owner_id;
            mItemLayoutList.add(item_type);
            mDataList.add(history);
        }

    }
    //---------------------------------------------------------------------------
    private void giftLoading(final ImageView iv, final History history) {
        Bitmap bitmap = (new StorageCache(mContext)).load(history.gift);
        if(bitmap != null) {
            bitmap = Utils.getScaleAndRoundBitmapOut(bitmap, mGiftFrameWidth, mGiftFrameWidth, 1.5f);
            iv.setImageBitmap(bitmap);
            iv.setVisibility(View.VISIBLE);
        } else {
            new Thread(new Runnable() {
              @Override
              public void run() {
                  Bitmap rawBitmap = Http.bitmapLoader(history.link);
                  if (rawBitmap == null)
                      return;
                  final Bitmap roundedBitmap = Utils.getScaleAndRoundBitmapOut(rawBitmap, mGiftFrameWidth, mGiftFrameWidth, 1.5f);
                  iv.post(new Runnable() {
                    @Override
                    public void run() {
                        if (iv!=null) {
                            iv.setImageBitmap(roundedBitmap);
                            iv.setVisibility(View.VISIBLE);
                        }
                    }
                });
              }
          }).start();
        }
        
    }
    //---------------------------------------------------------------------------
    public void release() {
        if (mDataList != null)
            mDataList.clear();
        if (mItemLayoutList != null)
            mItemLayoutList.clear();
        mDataList = null;
        mInflater = null;
        mItemLayoutList = null;
    }
    //---------------------------------------------------------------------------
}

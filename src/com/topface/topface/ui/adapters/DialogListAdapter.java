package com.topface.topface.ui.adapters;

import java.util.LinkedList;
import com.topface.topface.R;
import com.topface.topface.data.Dialog;
import com.topface.topface.data.History;
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DialogListAdapter extends BaseAdapter {
    //---------------------------------------------------------------------------
    // class ViewHolder
    //---------------------------------------------------------------------------
    static class ViewHolder {
        public ImageView mAvatar;
        public TextView mName;
        public TextView mCity;
        public TextView mText;
        public TextView mTime;
        public ImageView mArrow;
        public ImageView mOnline;
    }
    //---------------------------------------------------------------------------
    // Data
    private Context mContext;
    private LayoutInflater mInflater;
    private AvatarManager<Dialog> mAvatarManager;
    private int mOwnerCityID;
    // Constants
    private static final int T_ALL = 0;
    private static final int T_CITY = 1;
    private static final int T_COUNT = 2;
    //private static final String TIME_TEMPLATE = "dd MMM, kk:mm";
    //---------------------------------------------------------------------------
    public DialogListAdapter(Context context,AvatarManager<Dialog> avatarManager) {
        mContext = context;
        mAvatarManager = avatarManager;
        //mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mInflater = LayoutInflater.from(context);
        mOwnerCityID = CacheProfile.city_id;
    }
    //---------------------------------------------------------------------------
    @Override
    public int getCount() {
        return mAvatarManager.size();
    }
    //---------------------------------------------------------------------------
    @Override
    public Dialog getItem(int position) {
        return mAvatarManager.get(position);
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
        return mAvatarManager.get(position).city_id == mOwnerCityID ? T_CITY : T_ALL;
    }
    //---------------------------------------------------------------------------
    @Override
    public View getView(int position,View convertView,ViewGroup parent) {
        ViewHolder holder;

        //int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();

            convertView = mInflater.inflate(R.layout.item_inbox_gallery, null, false);

            holder.mAvatar = (ImageView)convertView.findViewById(R.id.ivAvatar);
            holder.mName = (TextView)convertView.findViewById(R.id.tvName);
            holder.mCity = (TextView)convertView.findViewById(R.id.tvCity);
            holder.mText = (TextView)convertView.findViewById(R.id.tvText);
            holder.mTime = (TextView)convertView.findViewById(R.id.tvTime);
            holder.mArrow = (ImageView)convertView.findViewById(R.id.ivArrow);
            holder.mOnline = (ImageView)convertView.findViewById(R.id.ivOnline);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder)convertView.getTag();

        Dialog dialog = getItem(position);

        mAvatarManager.getImage(position, holder.mAvatar);

        holder.mName.setText(dialog.first_name + ", " + dialog.age);
        holder.mCity.setText("  " + dialog.city_name);
        holder.mText.setText(dialog.text);
        /* switch(type) {
         * case T_ALL:
         * holder.mCity.setTextColor(Color.parseColor("#FFFFFF"));
         * //(R.color.color_item_all);
         * break;
         * case T_CITY:
         * holder.mCity.setTextColor(Color.parseColor("#FFCF72"));
         * //(R.color.color_item_city);
         * break;
         * } */

        // text
        switch (dialog.type) {
            case Dialog.DEFAULT:
                holder.mText.setText(dialog.text);
                break;
            case Dialog.GIFT:
                if(dialog.target==Dialog.TARGET_IN)
                    holder.mText.setText(mContext.getString(R.string.chat_gift_in));
                else
                    holder.mText.setText(mContext.getString(R.string.chat_gift_out));
                break;
            case Dialog.MESSAGE:
                holder.mText.setText(dialog.text);
                break;
            case Dialog.LIKE:
                holder.mText.setText(dialog.text);
                break;
            case Dialog.SYMPATHY:
                holder.mText.setText(dialog.text);
                break;
            default:
                holder.mText.setText("");
                break;
        }
        if (dialog.online)
            holder.mOnline.setVisibility(View.VISIBLE);
        else
            holder.mOnline.setVisibility(View.INVISIBLE);

        holder.mTime.setText(Utils.formatTime(mContext, dialog.created));
        //holder.mArrow.setImageResource(R.drawable.im_item_arrow); // ??? зачем

        return convertView;
    }
    //---------------------------------------------------------------------------
    public void release() {
        mInflater = null;
        mAvatarManager = null;
    }
    //---------------------------------------------------------------------------
}

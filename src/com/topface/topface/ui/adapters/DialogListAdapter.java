package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Dialog;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Utils;

import java.util.LinkedList;

public class DialogListAdapter extends FeedAdapter<Dialog> {

    public static final String MESSAGE_OF_UNKNOWN_TYPE = "";

    public DialogListAdapter(Context context, LinkedList<Dialog> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    static class ViewHolder {
        public ImageViewRemote avatar;
        public TextView name;
        public TextView city;
        public TextView mText;
        public TextView mTime;
        public ImageView mOnline;
    }

    private static final int T_CITY = 3;
    private static final int T_COUNT = 1;

    @Override
    public int getViewTypeCount() {
        return super.getViewTypeCount() + T_COUNT;
    }

    @Override
    public int getItemViewType(int position) {
    	int typeOfSuperMethod = super.getItemViewType(position);
    	if (typeOfSuperMethod == T_OTHER) {
    		return getItem(position).city_id == CacheProfile.city_id ?
                    T_CITY :
                    T_OTHER;
    	} else {
    		return typeOfSuperMethod;
    	}
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = getInflater().inflate(R.layout.item_inbox_gallery, null, false);
            holder = getEmptyHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        Dialog dialog = getItem(position);

        holder.avatar.setRemoteSrc(dialog.getNormalLink());
        holder.name.setText(dialog.first_name + ", " + dialog.age);
        holder.city.setText(dialog.city_name);
        holder.mText.setText(dialog.text);
        holder.mText.setText(getDialogText(dialog));
        holder.mOnline.setVisibility(dialog.online ? View.VISIBLE : View.INVISIBLE);
        holder.mTime.setText(Utils.formatTime(getContext(), dialog.created));

        return convertView;
    }

    private ViewHolder getEmptyHolder(View convertView) {
        ViewHolder holder = new ViewHolder();

        holder.avatar = (ImageViewRemote) convertView.findViewById(R.id.ivAvatar);
        holder.name = (TextView) convertView.findViewById(R.id.tvName);
        holder.city = (TextView) convertView.findViewById(R.id.tvCity);
        holder.mText = (TextView) convertView.findViewById(R.id.tvText);
        holder.mTime = (TextView) convertView.findViewById(R.id.tvTime);
        holder.mOnline = (ImageView) convertView.findViewById(R.id.ivOnline);

        return holder;
    }

    private String getDialogText(Dialog dialog) {
        String text;
        switch (dialog.type) {
            case Dialog.DEFAULT:
            case Dialog.MESSAGE:
            case Dialog.MESSAGE_WISH:
            case Dialog.MESSAGE_SEXUALITY:
            case Dialog.LIKE:
            case Dialog.SYMPHATHY:
            case Dialog.MESSAGE_WINK:
            case Dialog.RATE:
            case Dialog.PROMOTION:
            case Dialog.PHOTO:
            case Dialog.MAP:
                text = dialog.text;
                break;

            case Dialog.GIFT:
                text = (dialog.target == Dialog.FRIEND_MESSAGE) ?
                        getContext().getString(R.string.chat_gift_in) :
                        getContext().getString(R.string.chat_gift_out);
                break;

            default:
                text = MESSAGE_OF_UNKNOWN_TYPE;
        }
        return text;
    }

}

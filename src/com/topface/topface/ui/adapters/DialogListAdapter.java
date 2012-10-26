package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.utils.Utils;

import java.util.Calendar;

public class DialogListAdapter extends FeedAdapter<FeedDialog> {

    public static final int ITEM_LAYOUT = R.layout.item_feed_dialog;

    public static final String MESSAGE_OF_UNKNOWN_TYPE = "";

    public DialogListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        FeedDialog dialog = getItem(position);
        holder.text.setText(getDialogText(dialog));
        holder.time.setText(Utils.formatTime(getContext(), dialog.created));

        return convertView;
    }

    private String getDialogText(FeedDialog dialog) {
        String text;
        switch (dialog.type) {
            case FeedDialog.DEFAULT:
            case FeedDialog.MESSAGE:
            case FeedDialog.MESSAGE_WISH:
            case FeedDialog.MESSAGE_SEXUALITY:
            case FeedDialog.LIKE:
            case FeedDialog.SYMPHATHY:
            case FeedDialog.MESSAGE_WINK:
            case FeedDialog.RATE:
            case FeedDialog.PROMOTION:
            case FeedDialog.PHOTO:
            case FeedDialog.MAP:
                text = dialog.text;
                break;

            case FeedDialog.GIFT:
                text = (dialog.target == FeedDialog.FRIEND_MESSAGE) ?
                        getContext().getString(R.string.chat_gift_in) :
                        getContext().getString(R.string.chat_gift_out);
                break;

            default:
                text = MESSAGE_OF_UNKNOWN_TYPE;
        }
        return text;
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView, FeedDialog item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.text = (TextView) convertView.findViewById(R.id.tvText);
        holder.time = (TextView) convertView.findViewById(R.id.tvTime);
        return holder;
    }

    @Override
    protected int getItemLayout() {
        return ITEM_LAYOUT;
    }

}

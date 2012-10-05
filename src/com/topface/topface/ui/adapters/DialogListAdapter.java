package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.Dialog;

public class DialogListAdapter extends FeedAdapter<Dialog> {

    public static final int ITEM_LAYOUT = R.layout.item_inbox_gallery;

    public static final String MESSAGE_OF_UNKNOWN_TYPE = "";

    public DialogListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        Dialog dialog = getItem(position);
        holder.text.setText(getDialogText(dialog));

        return convertView;
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

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView) {
        FeedViewHolder holder = super.getEmptyHolder(convertView);
        holder.text = (TextView) convertView.findViewById(R.id.tvText);
        return holder;
    }

    @Override
    protected Dialog getNewItem(IListLoader.ItemType type) {
        return new Dialog(type);
    }

    @Override
    protected int getItemLayout() {
        return ITEM_LAYOUT;
    }

}

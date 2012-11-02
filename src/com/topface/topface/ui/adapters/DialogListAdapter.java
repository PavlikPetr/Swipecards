package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.utils.Utils;

public class DialogListAdapter extends FeedAdapter<FeedDialog> {

	public static final int NEW_ITEM_LAYOUT = R.layout.item_new_feed_dialog;
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

        if (getItemViewType(position) == T_NEW) {
        	int unreadCounter = getUnreadCounter(dialog);
            if (unreadCounter > 0) {
            	holder.unreadCounter.setVisibility(View.VISIBLE);
            	holder.unreadCounter.setText(Integer.toString(unreadCounter));
            } else {
            	holder.unreadCounter.setVisibility(View.GONE);
            }
        }
        
        return convertView;
    }

    private String getDialogText(FeedDialog dialog) {
        String text;
        switch (dialog.type) {
            case FeedDialog.DEFAULT:
            case FeedDialog.MESSAGE:
            case FeedDialog.MESSAGE_WISH:
            case FeedDialog.MESSAGE_SEXUALITY:            
            case FeedDialog.MESSAGE_WINK:
            case FeedDialog.RATE:
            case FeedDialog.PROMOTION:
            case FeedDialog.PHOTO:
            	text = dialog.text;
            	break;
            case FeedDialog.LIKE:
            	text = (dialog.target == FeedDialog.FRIEND_MESSAGE) ?
                        getContext().getString(R.string.chat_like_in) :
                        getContext().getString(R.string.chat_like_out);
                break;
            case FeedDialog.SYMPHATHY:
            	text = (dialog.target == FeedDialog.FRIEND_MESSAGE) ?
                        getContext().getString(R.string.chat_symphathy_in) :
                        getContext().getString(R.string.chat_symphathy_out);
                        
            	break;
            case FeedDialog.ADDRESS:
            	text = "{{map}} "+dialog.text;
                break;
            case FeedDialog.MAP:
                text = "{{map}} "+dialog.text;
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
    
    private int getUnreadCounter(FeedDialog dialog) {
        int counter = 0;
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
            	counter = 2;
            	break;
            case FeedDialog.ADDRESS:
            case FeedDialog.MAP:                
            case FeedDialog.GIFT:
            default:
                counter = 0;
                break;
        }
        return counter;
    }

    @Override
    protected FeedViewHolder getEmptyHolder(View convertView, FeedDialog item) {
        FeedViewHolder holder = super.getEmptyHolder(convertView, item);
        holder.text = (TextView) convertView.findViewById(R.id.tvText);
        if (item.unread) {
        	holder.unreadCounter = (TextView) convertView.findViewById(R.id.tvUnreadCounter);
        }
        holder.time = (TextView) convertView.findViewById(R.id.tvTime);
        return holder;
    }

    @Override
    protected int getItemLayout() {
        return ITEM_LAYOUT;
    }

	@Override
	protected int getNewItemLayout() {
		return NEW_ITEM_LAYOUT;
	}

}

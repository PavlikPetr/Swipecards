package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.utils.DateUtils;

import java.util.Collections;

public class DialogListAdapter extends FeedAdapter<FeedDialog> {

    public static final int NEW_ITEM_LAYOUT = R.layout.item_new_feed_dialog;
    public static final int ITEM_LAYOUT = R.layout.item_feed_dialog;
    public static final int NEW_VIP_ITEM_LAYOUT = R.layout.item_new_feed_vip_dialog;
    public static final int VIP_ITEM_LAYOUT = R.layout.item_feed_vip_dialog;

    public DialogListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();

        FeedDialog dialog = getItem(position);
        setDialogText(dialog, holder.text);
        holder.time.setText(DateUtils.getFormattedDate(mContext, dialog.created));

        if (getItemViewType(position) == T_NEW) {
            int unreadCounter = getUnreadCounter(dialog);
            if (unreadCounter > 1) {
                holder.unreadCounter.setVisibility(View.VISIBLE);
                holder.unreadCounter.setText(Integer.toString(unreadCounter));
            } else {
                holder.unreadCounter.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    private void setDialogText(FeedDialog dialog, TextView view) {
        String text = null;
        int image = 0;

        if (dialog.user.deleted) {
            text = getContext().getString(R.string.user_is_deleted);
        } else if (dialog.user.banned) {
            text = getContext().getString(R.string.user_is_banned);
        } else {
            switch (dialog.type) {
                case FeedDialog.DEFAULT:
                case FeedDialog.MESSAGE:
                case FeedDialog.MESSAGE_WISH:
                case FeedDialog.MESSAGE_SEXUALITY:
                case FeedDialog.MESSAGE_WINK:
                case FeedDialog.RATE:
                case FeedDialog.PROMOTION:
                case FeedDialog.PHOTO:
                    image = (dialog.target == FeedDialog.OUTPUT_USER_MESSAGE) ?
                            R.drawable.ico_outbox : 0;
                    break;
                case FeedDialog.LIKE:
                    if (dialog.target == FeedDialog.INPUT_FRIEND_MESSAGE) {
                        text = getContext().getString(R.string.chat_like_in);
                    } else {
                        text = getContext().getString(R.string.chat_like_out);
                        image = R.drawable.ico_outbox;
                    }
                    break;
                case FeedDialog.SYMPHATHY:
                    if (dialog.target == FeedDialog.INPUT_FRIEND_MESSAGE) {
                        text = getContext().getString(R.string.chat_mutual_in);
                    } else {
                        text = getContext().getString(R.string.chat_mutual_out);
                        image = R.drawable.ico_outbox;
                    }
                    break;
                case FeedDialog.ADDRESS:
                    image = R.drawable.ico_map;
                    break;
                case FeedDialog.GIFT:
                    image = R.drawable.ico_gift;
                    text = (dialog.target == FeedDialog.INPUT_FRIEND_MESSAGE) ?
                            getContext().getString(R.string.chat_gift_in) :
                            getContext().getString(R.string.chat_gift_out);
                    break;
            }
        }
        //Если иконка или текст пустые, то ставим данные по умолчанию
        image = (image == 0 && dialog.target == FeedDialog.OUTPUT_USER_MESSAGE) ?
                R.drawable.ico_outbox : 0;
        text = (text == null) ? dialog.text : text;

        view.setCompoundDrawablesWithIntrinsicBounds(image, 0, 0, 0);
        view.setText(text);
    }

    private int getUnreadCounter(FeedDialog dialog) {
        int counter;
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
                counter = dialog.unreadCounter;
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
    public void addDataFirst(FeedListData<FeedDialog> data) {
        removeLoaderItem();
        if (data != null) {
            Collections.reverse(data.items);
            if (!data.items.isEmpty()) {
                for (FeedDialog item : data.items) {
                    if (!addItemToStartOfFeed(item)) {
                        getData().addFirst(item);
                    }
                }
            }
            addLoaderItem(data.more);
        }
        notifyDataSetChanged();
        setLastUpdate();
    }

    private boolean addItemToStartOfFeed(FeedDialog item) {
        for (FeedDialog dialog : getData()) {
            if (item.user.id == dialog.user.id) {
                setItemToStartOfFeed(dialog, item);
                return true;
            }
        }
        return false;
    }

    private void setItemToStartOfFeed(FeedDialog dialog, FeedDialog item) {
        getData().remove(dialog);
        getData().addFirst(item);
    }

    @Override
    protected int getItemLayout() {
        return ITEM_LAYOUT;
    }

    @Override
    protected int getNewItemLayout() {
        return NEW_ITEM_LAYOUT;
    }

    @Override
    protected int getVipItemLayout() {
        return VIP_ITEM_LAYOUT;
    }

    @Override
    protected int getNewVipItemLayout() {
        return NEW_VIP_ITEM_LAYOUT;
    }

    @Override
    public ILoaderRetrierCreator<FeedDialog> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedDialog>() {
            @Override
            public FeedDialog getLoader() {
                FeedDialog result = new FeedDialog(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedDialog getRetrier() {
                FeedDialog result = new FeedDialog(null);
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }
}

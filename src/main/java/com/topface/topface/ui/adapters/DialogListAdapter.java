package com.topface.topface.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.FeedListData;
import com.topface.topface.ui.views.FeedItemViewConstructor;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.ad.NativeAd;

import java.util.Collections;

public class DialogListAdapter extends FeedAdapter<FeedDialog> {


    public DialogListAdapter(Context context, Updater updateCallback) {
        super(context, updateCallback);
    }

    @Override
    protected FeedItemViewConstructor.TypeAndFlag getViewCreationFlag() {
        return new FeedItemViewConstructor.TypeAndFlag(FeedItemViewConstructor.Type.TIME_COUNT);
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        convertView = super.getContentView(position, convertView, viewGroup);
        FeedViewHolder holder = (FeedViewHolder) convertView.getTag();
        FeedDialog dialog = getItem(position);
        if (holder != null) {
            holder.time.setText(dialog.createdRelative);
            int itemType = getItemViewType(position);
            FeedItemViewConstructor.setCounter(holder.unreadCounter,
                    ((itemType == T_NEW || itemType == T_NEW_VIP) && (!App.from(getContext()).getOptions().hidePreviewDialog)) ?
                            getUnreadCounter(dialog) :
                            0
            );
        }
        return convertView;
    }

    @Override
    protected void setItemMessage(FeedDialog item, TextView messageView) {
        setDialogText(item, messageView);
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
                case FeedDialog.GIFT:
                    image = R.drawable.ico_gift;
                    text = (dialog.target == FeedDialog.INPUT_FRIEND_MESSAGE) ?
                            getContext().getString(R.string.chat_gift_in) :
                            getContext().getString(R.string.chat_gift_out);
                    break;
            }
        }
        if (dialog.unread && App.from(getContext()).getOptions().hidePreviewDialog) {
            text = Utils.getQuantityString(R.plurals.notification_many_messages,
                    dialog.unreadCounter, dialog.unreadCounter);
            view.setTextColor(getContext().getResources().getColor(R.color.hidden_dialog_preview_text_color));
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
        holder.unreadCounter = (TextView) convertView.findViewById(R.id.ifp_counter);
        holder.time = (TextView) convertView.findViewById(R.id.ifp_time);
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
            if (dialog.user != null && item.user.id == dialog.user.id) {
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
    public ILoaderRetrierCreator<FeedDialog> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<FeedDialog>() {
            @Override
            public FeedDialog getLoader() {
                FeedDialog result = new FeedDialog();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                return result;
            }

            @Override
            public FeedDialog getRetrier() {
                FeedDialog result = new FeedDialog();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                return result;
            }
        };
    }

    @Override
    protected INativeAdItemCreator<FeedDialog> getNativeAdItemCreator() {
        return new INativeAdItemCreator<FeedDialog>() {
            @Override
            public FeedDialog getAdItem(NativeAd nativeAd) {
                return new FeedDialog(nativeAd);
            }
        };
    }

    public void replacePreview(int position, FeedDialog dialog) {
        FeedDialog item = getData().get(position);
        item.type = dialog.type;
        item.text = dialog.text;
        notifyDataSetChanged();
    }
}

package com.topface.topface.ui.adapters;

import android.content.Context;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.nineoldandroids.animation.ObjectAnimator;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.FeedDialog;
import com.topface.topface.data.History;
import com.topface.topface.data.SendGiftAnswer;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.CustomMovementMethod;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.IActivityDelegate;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.loadcontollers.ChatLoadController;
import com.topface.topface.utils.loadcontollers.LoadController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class ChatListAdapter extends LoadingListAdapter<History> implements AbsListView.OnScrollListener {

    private static final int T_RETRY = 3;
    private static final int T_USER = 4;
    private static final int T_FRIEND = 5;
    private static final int T_USER_GIFT = 6;
    private static final int T_FRIEND_GIFT = 7;
    private static final int T_USER_POPULAR_1 = 8;
    private static final int T_USER_POPULAR_2 = 9;
    private static final int T_AUTO_REPLY = 10;
    private static final int T_COUNT = 11;
    private HashMap<History, ApiRequest> mHashRequestByWaitingRetryItem = new HashMap<>();
    private ArrayList<History> mUnrealItems = new ArrayList<>();
    private ArrayList<History> mShowDatesList = new ArrayList<>();
    private OnBuyVipButtonClick mBuyVipButtonClickListener;
    private CustomMovementMethod mCustomMovementMethod;

    public ChatListAdapter(IActivityDelegate iActivityDelegate, FeedList<History> data, Updater updateCallback, OnBuyVipButtonClick listener) {
        super(iActivityDelegate.getApplicationContext(), data, updateCallback);
        mBuyVipButtonClickListener = listener;
        if (!data.isEmpty()) {
            prepareDates();
        }
        if (getData().size() > ChatFragment.LIMIT) {
            addLoaderItem(true);
        }
        for (History item : data) {
            if (item.isWaitingItem() || item.isRepeatItem() || item.isFake()) {
                mUnrealItems.add(item);
            }
        }
        mCustomMovementMethod = new CustomMovementMethod(iActivityDelegate);
    }

    public static int getItemType(History item) {
        boolean output = isOutboxMessage(item);
        switch (item.type) {
            case FeedDialog.GIFT:
                return output ? T_USER_GIFT : T_FRIEND_GIFT;
            case FeedDialog.MESSAGE_POPULAR_STAGE_1:
                return T_USER_POPULAR_1;
            case FeedDialog.MESSAGE_POPULAR_STAGE_2:
                return T_USER_POPULAR_2;
            case FeedDialog.MESSAGE_AUTO_REPLY:
                return T_AUTO_REPLY;
            default:
                return output ? T_USER : T_FRIEND;
        }
    }

    @Override
    protected LoadController initLoadController() {
        return new ChatLoadController();
    }

    @Override
    public History getItem(int i) {
        return super.getItem(getPosition(i));
    }

    public int getPosition(int index) {
        return getCount() - index - 1;
    }

    @Override
    public int getItemViewType(int position) {
        int superType = super.getItemViewType(position);
        History item = getItem(position);
        if (superType == T_OTHER && item != null) {
            if (item.isRepeatItem()) {
                return T_RETRY;
            }
            return ChatListAdapter.getItemType(item);
        } else {
            return superType;
        }
    }

    @Override
    public int getViewTypeCount() {
        return T_COUNT;
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        final int type = getItemViewType(position);
        final History item = getItem(position);
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflateConvertView(holder, type, item);
            if (convertView != null) {
                convertView.setTag(holder);
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        setTypeDifferences(holder, type, item, position);
        if (type != T_RETRY) {
            setViewInfo(holder, item);
        }
        return convertView;
    }

    public void setData(FeedList<History> data) {
        setData(data, getData().size() > ChatFragment.LIMIT);
    }

    @Override
    public void setData(ArrayList<History> dataList, boolean more) {
        setData(dataList, more, null);
    }

    public void setData(ArrayList<History> dataList, boolean more, ListView parentView) {
        super.setData(dataList, more, false);
        identifyUnrealItems();
        prepareDates();
        notifyDataSetChanged();
        if (parentView != null) {
            parentView.setSelection(getCount() - 1);
        }
    }

    private void identifyUnrealItems() {
        for (History item : mData) {
            if ("0".equals(item.id)) {
                mUnrealItems.add(item);
            }
        }
    }

    @Override
    public void addAll(ArrayList<History> dataList, boolean more) {
        super.addAll(dataList, more, false);
        prepareDates();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<History> dataList, boolean more, ListView parentView) {
        boolean wasEmpty = isEmpty();
        this.addAll(dataList, more);
        parentView.setSelection(dataList.size() + (more ? 2 : 0));
        if (wasEmpty) {
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(parentView, "alpha", 0, 1);
            alphaAnimator.setDuration(150);
            alphaAnimator.start();
        }
    }

    public void forceStopLoader() {
        removeLoaderItem();
        notifyDataSetChanged();
    }

    /**
     * compares current data set with new from server
     * and updates current items.
     * clears incoming data, to avoid usless notifyDataSetChanged()
     *
     * @param data new data from server
     */
    private void compareAndUpdateData(ArrayList<History> data) {
        ArrayList<History> currentData = getData();
        for (int j = 0; j < currentData.size(); j++) {
            History item = currentData.get(j);
            for (int i = 0; i < data.size(); i++) {
                History newItem = data.get(i);
                if (item.isEqualsEnough(newItem)) {
                    currentData.set(j, newItem);
                    data.remove(i);
                    break;
                }
            }
        }
    }

    @Override
    public void addFirst(ArrayList<History> data, boolean more) {
        compareAndUpdateData(data);

        if (!mUnrealItems.isEmpty()) removeUnrealItems();
        super.addFirst(data, more, false);
        prepareDates();
        if (!data.isEmpty()) {
            notifyDataSetChanged();
        }
    }

    public void addFirst(ArrayList<History> data, boolean more, ListView parentView) {
        int scroll = parentView.getScrollY();
        this.addFirst(data, more);
        parentView.scrollTo(parentView.getScrollX(), scroll);
    }

    protected void addSentMessage(History item) {
        getData().addFirst(item);
        prepareDates();
        if (!item.isWaitingItem()) {
            mUnrealItems.add(item);
        }
        notifyDataSetChanged();
    }

    public void addGift(SendGiftAnswer giftAnswer) {
        addSentMessage(giftAnswer.history);
    }

    public void addSentMessage(History item, ListView parentView, ApiRequest request) {
        mHashRequestByWaitingRetryItem.put(item, request);
        this.addSentMessage(item);
        parentView.setSelection(getCount() - 1);
    }

    public void replaceMessage(History emptyItem, History unrealItem, ListView parentView) {
        FeedList<History> data = getData();
        int positionToReplace = -1;
        mHashRequestByWaitingRetryItem.remove(emptyItem);
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i) == emptyItem) {
                positionToReplace = i;
                break;
            }
        }
        if (positionToReplace != -1) {
            data.set(positionToReplace, unrealItem);
            mUnrealItems.add(unrealItem);
        }

        prepareDates();
        parentView.setSelection(getCount() - 1);
    }

    public void showRetrySendMessage(History emptyItem, ApiRequest request) {
        emptyItem.setLoaderTypeFlags(IListLoader.ItemType.REPEAT);
        mHashRequestByWaitingRetryItem.put(emptyItem, request);
        notifyDataSetChanged();
    }

    public void removeLastItem() {
        getData().remove(0);
        notifyDataSetChanged();
    }

    private void removeUnrealItems() {
        removeUnrealItems(getData());
        mUnrealItems.clear();
    }

    private void removeUnrealItems(Collection<History> data) {
        data.removeAll(mUnrealItems);
    }

    private void prepareDates() {
        FeedList<History> data = getData();
        mShowDatesList.clear();
        for (int i = data.size() - 1; i >= 0; i--) {
            History currItem = data.get(i);
            if (currItem.isLoaderOrRetrier()) continue;

            History prevItem = (i + 1 >= data.size()) ? null : data.get(i + 1);
            if (prevItem == null || prevItem.isLoaderOrRetrier()) {
                mShowDatesList.add(currItem);
                continue;
            }

            if (DateUtils.isWithinADay(prevItem.created, currItem.created)) {
                mShowDatesList.add(currItem);
            }
        }
    }

    private void setTypeDifferences(ViewHolder holder, int type, final History item, int position) {
        boolean showDate = mShowDatesList.contains(item);
        switch (type) {
            case T_RETRY:
                if (item != null && item.isRepeatItem() && holder != null) {
                    holder.loader.setVisibility(View.GONE);
                    holder.retrier.setVisibility(View.VISIBLE);
                    holder.retrier.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ApiRequest request = mHashRequestByWaitingRetryItem.get(item);
                            if (request == null) {
                                return;
                            }
                            resendCanceledRequest(request);
                            mHashRequestByWaitingRetryItem.remove(item);
                            item.setLoaderTypeFlags(IListLoader.ItemType.WAITING);
                            notifyDataSetChanged();
                        }
                    });
                }
                return;
            case T_FRIEND_GIFT:
            case T_USER_GIFT:
                holder.message.setVisibility(View.GONE);
                break;
            case T_AUTO_REPLY:
                if (position == getCount() - 1) {
                    holder.buyVip.setVisibility(View.VISIBLE);
                    holder.buyVip.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mBuyVipButtonClickListener != null) {
                                mBuyVipButtonClickListener.onClick();
                            }
                        }
                    });
                } else {
                    holder.buyVip.setVisibility(View.GONE);
                    holder.buyVip.setOnClickListener(null);
                }
                break;
        }
        if (holder != null && holder.dateDivider != null) {
            if (showDate) {
                holder.dateDivider.setVisibility(View.VISIBLE);
                holder.dateDividerText.setText(item.createdRelative);
            } else {
                holder.dateDivider.setVisibility(View.GONE);
            }
        }
    }

    private void resendCanceledRequest(ApiRequest request) {
        if (request != null) {
            request.canceled = false;
            request.getHandler().setCancel(false);
            request.exec();
        }
    }

    private View inflateConvertView(ViewHolder holder, int type, History item) {
        boolean output = isOutboxMessage(item);
        View convertView;
        int prsLoaderId = R.id.prsLoader;
        int chatImageId = R.id.chat_image;

        if (type == T_RETRY) {
            convertView = mInflater.inflate(R.layout.item_chat_list_loader_retrier, null, false);
            holder.retrier = convertView.findViewById(R.id.tvLoaderText);
            holder.loader = convertView.findViewById(prsLoaderId);
            return convertView;
        }

        switch (type) {
            case T_FRIEND_GIFT:
            case T_USER_GIFT:
                convertView = mInflater.inflate(output ? R.layout.chat_user_gift : R.layout.chat_friend_gift, null, false);
                holder.gift = (ImageViewRemote) convertView.findViewById(chatImageId);
                break;
            case T_AUTO_REPLY:
                convertView = mInflater.inflate(R.layout.chat_friend_auto_reply, null, false);
                holder.buyVip = (Button) convertView.findViewById(R.id.chat_auto_reoly_buy_vip);
                break;
            case T_FRIEND:
            case T_USER:
            case T_USER_POPULAR_1:
            case T_USER_POPULAR_2:
            default:
                convertView = mInflater.inflate(output ? R.layout.chat_user : R.layout.chat_friend, null, false);
                break;
        }

        holder.message = (TextView) convertView.findViewById(R.id.chat_message);
        holder.date = (TextView) convertView.findViewById(R.id.chat_date);
        holder.dateDivider = convertView.findViewById(R.id.loDateDivider);
        holder.dateDividerText = (TextView) holder.dateDivider.findViewById(R.id.tvChatDateDivider);

        return convertView;
    }

    private static boolean isOutboxMessage(History item) {
        return item.target == FeedDialog.OUTPUT_USER_MESSAGE;
    }

    private void setViewInfo(ViewHolder holder, History item) {
        boolean output = isOutboxMessage(item);
        switch (item.type) {
            case FeedDialog.GIFT:
                holder.gift.setRemoteSrc(item.link);
                if (holder.message != null) {
                    holder.message.setVisibility(setMessageHtmlContent(holder, item) ? View.VISIBLE : View.GONE);
                }
                break;
            case FeedDialog.MESSAGE_WISH:
                holder.message.setText(mContext.getString(output ? R.string.chat_wish_out : R.string.chat_wish_in));
                break;
            case FeedDialog.MESSAGE_AUTO_REPLY:
                holder.message.setText(mContext.getString(R.string.chat_auto_reply_message));
                break;
            case FeedDialog.MESSAGE_SEXUALITY:
                holder.message.setText(mContext.getString(output ? R.string.chat_sexuality_out :
                        R.string.chat_sexuality_in));
                break;
            case FeedDialog.LIKE:
                holder.message.setText(mContext.getString(output ? R.string.chat_like_out : R.string.chat_like_in));
                break;
            case FeedDialog.SYMPHATHY:
                holder.message.setText(mContext.getString(output ? R.string.chat_mutual_out : R.string.chat_mutual_in));
                break;
            case FeedDialog.MESSAGE_WINK:
                holder.message.setText(mContext.getString(output ? R.string.chat_wink_out : R.string.chat_wink_in));
                break;
            case FeedDialog.LIKE_REQUEST:
                holder.message.setText(item.text);
                break;
            case FeedDialog.DEFAULT:
            case FeedDialog.MESSAGE:
            case FeedDialog.PROMOTION:
            case FeedDialog.MESSAGE_POPULAR_STAGE_1:
            case FeedDialog.MESSAGE_POPULAR_STAGE_2:
            default:
                setMessageHtmlContent(holder, item);
                break;
        }
        if (holder != null) {
            if (holder.date != null) {
                holder.date.setText(item.createdFormatted);
            }
        }

    }

    private boolean setMessageHtmlContent(ViewHolder holder, History item) {
        if (holder != null && holder.message != null) {
            if (item.text != null && !item.text.equals(Utils.EMPTY)) {
                holder.message.setText(Html.fromHtml(item.text));
                // Проверяем наличие в textView WEB_URLS | EMAIL_ADDRESSES | PHONE_NUMBERS | MAP_ADDRESSES;
                // Если нашли, то добавим им кликабельность
                // в остальных случаях holder.message будет кликаться на onItemClickListener
                if (Linkify.addLinks(holder.message, Linkify.ALL)) {
                    holder.message.setMovementMethod(mCustomMovementMethod);
                    holder.message.setFocusable(false);
                }
                return true;
            } else {
                holder.message.setText("");
                return false;
            }
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    public void copyText(String text) {
        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            clipboard.setText(text);
            Utils.showToastNotification(R.string.general_msg_copied, Toast.LENGTH_SHORT);
        }
    }

    public void removeItem(int position) {
        getData().remove(position);
        prepareDates();
        notifyDataSetChanged();
    }

    @SuppressWarnings("UnusedDeclaration")
    public void removeItem(History item) {
        getData().remove(item);
    }

    public String getFirstItemId() {
        FeedList<History> data = getData();
        for (History item : data) {
            if (isReal(item)) {
                return item.id;
            }
        }
        return null;
    }

    private boolean isReal(History item) {
        return !item.isLoaderOrRetrier() && item.id != null && !item.id.equals("0");
    }

    public String getLastItemId() {
        FeedList<History> data = getData();

        for (int i = data.size() - 1; i >= 0; i--) {
            History item = data.get(i);
            if (item != null) {
                if (isReal(item)) {
                    return item.id;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<History> getDataCopy() {
        ArrayList<History> dataClone = null;
        try {
            dataClone = (ArrayList<History>) getData().clone();
            if (!dataClone.isEmpty() && dataClone.get(dataClone.size() - 1).isLoaderOrRetrier()) {
                dataClone.remove(dataClone.size() - 1);
            }
        } catch (OutOfMemoryError e) {
            Debug.error(e);
        }
        return dataClone;
    }

    @Override
    protected int getLoaderRetrierLayout() {
        return R.layout.item_chat_list_loader_retrier;
    }

    @Override
    public ILoaderRetrierCreator<History> getLoaderRetrierCreator() {
        return new ILoaderRetrierCreator<History>() {
            @Override
            public History getLoader() {
                History result = new History();
                result.setLoaderTypeFlags(IListLoader.ItemType.LOADER);
                result.created = 0;
                return result;
            }

            @Override
            public History getRetrier() {
                History result = new History();
                result.setLoaderTypeFlags(IListLoader.ItemType.RETRY);
                result.created = 0;
                return result;
            }
        };
    }

    public int getOutboxMessageCount() {
        return getMessageCount(true);
    }

    public int getInboxMessageCount() {
        return getMessageCount(false);
    }

    private int getMessageCount(boolean isOutbox) {
        int outboxCount = 0;
        int inboxCount = 0;
        for (History history : getData()) {
            if (isOutboxMessage(history)) {
                outboxCount++;
            } else {
                inboxCount++;
            }
        }
        return isOutbox ? outboxCount : inboxCount;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        FeedList<History> data = getData();
        if (mUpdateCallback != null && !data.isEmpty() && firstVisibleItem <= mLoadController.getItemsOffsetByConnectionType()
                && isNeedMore()) {
            mUpdateCallback.onUpdate();
        }
    }

    /*
        Костылик, чтоб не текло при перевороте девайса. Заменяем старый контекст на новый.
    */
    public void updateActivityDelegate(IActivityDelegate delegate) {
        if (mCustomMovementMethod != null) {
            mCustomMovementMethod.setIActivityDelegate(delegate);
        }
    }

    static class ViewHolder {
        View dateDivider;
        TextView dateDividerText;
        TextView message;
        TextView date;
        ImageViewRemote gift;
        View loader;
        View retrier;
        Button buyVip;
    }

    public interface OnBuyVipButtonClick {
        void onClick();
    }
}

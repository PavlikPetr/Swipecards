package com.topface.topface.utils.controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.data.HistoryListData;
import com.topface.topface.data.search.SearchUser;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.HistoryRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.statistics.DatingMessageStatistics;
import com.topface.topface.ui.ChatActivity;
import com.topface.topface.ui.PurchasesActivity;
import com.topface.topface.ui.fragments.feed.DialogsFragment;
import com.topface.topface.ui.views.KeyboardListenerLayout;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.http.IRequestClient;

/**
 * Controller to handle dating message send footer.
 */
public class DatingInstantMessageController {

    private Activity mActivity;
    private IRequestClient mRequestClient;

    private ImageButton mMessageSend;
    private View mGiftSend;
    private ViewFlipper mFooterFlipper;
    private EditText mMessageText;
    private int mMaxMessageSize;
    private SendLikeAction mSendLikeAction;
    private Animation mSpin;

    private boolean mIsEnabled;
    private boolean mIsSendEnadled;

    private String mLastMsgFromConfig;

    public DatingInstantMessageController(Activity activity, KeyboardListenerLayout root,
                                          View.OnClickListener clickListener,
                                          IRequestClient requestClient, String text,
                                          final View datingButtons, final View userInfoStatus,
                                          SendLikeAction sendLikeAction, TextView.OnEditorActionListener mEditorActionListener) {
        mActivity = activity;
        mSendLikeAction = sendLikeAction;

        root.setKeyboardListener(new KeyboardListenerLayout.KeyboardListener() {
            @Override
            public void keyboardOpened() {
                datingButtons.setVisibility(View.GONE);
                userInfoStatus.setVisibility(View.GONE);
            }

            @Override
            public void keyboardClosed() {
                datingButtons.setVisibility(View.VISIBLE);
                userInfoStatus.setVisibility(View.VISIBLE);
            }
        });
        mFooterFlipper = (ViewFlipper) root.findViewById(R.id.dating_footer);
        mFooterFlipper.setVisibility(View.VISIBLE);
        mGiftSend = root.findViewById(R.id.send_gift_button);
        mMessageText = (EditText) root.findViewById(R.id.edChatBox);
        mMessageText.setOnEditorActionListener(mEditorActionListener);
        mMessageSend = (ImageButton) root.findViewById(R.id.btnSend);
        mSpin = AnimationUtils.loadAnimation(mActivity, R.anim.loader_rotate);
        mMessageText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (mIsEnabled && mIsSendEnadled && isMessageValid()) {
                    mMessageSend.setEnabled(true);
                } else {
                    mMessageSend.setEnabled(false);
                }
            }
        });
        UserConfig userConfig = App.getUserConfig();
        String defaultMessage = userConfig.getDatingMessage();
        if (TextUtils.isEmpty(defaultMessage) && !TextUtils.isEmpty(text)) {
            userConfig.setDatingMessage(text);
            userConfig.saveConfig();
        }
        setInstantMessageText(defaultMessage.isEmpty() ? text : defaultMessage);
        mMessageText.setHint(activity.getString(R.string.dating_message));
        mMessageText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        mMessageSend.setOnClickListener(clickListener);
        mGiftSend.setOnClickListener(clickListener);
        root.findViewById(R.id.chat_btn).setOnClickListener(clickListener);
        root.findViewById(R.id.skip_btn).setOnClickListener(clickListener);

        mMaxMessageSize = CacheProfile.getOptions().maxMessageSize;
        mRequestClient = requestClient;
    }

    public boolean sendMessage(SearchUser user) {
        if (!tryChat(user)) {
            setSendEnabled(true);
            return false;
        }
        final Editable editText = mMessageText.getText();
        final String editString = editText == null ? "" : editText.toString();
        if (editText == null || TextUtils.isEmpty(editString.trim()) || user.id == 0) {
            return false;
        }
        if (editText.length() > mMaxMessageSize) {
            Utils.showToastNotification(
                    String.format(mActivity.getString(R.string.message_too_long), mMaxMessageSize),
                    Toast.LENGTH_SHORT);
            return false;
        }

        final MessageRequest messageRequest = new MessageRequest(user.id, editString, mActivity, true);
        mRequestClient.registerRequest(messageRequest);
        messageRequest.callback(new DataApiHandler<History>() {
            @Override
            protected void success(History data, IApiResponse response) {
                LocalBroadcastManager.getInstance(mActivity)
                        .sendBroadcast(new Intent(DialogsFragment.REFRESH_DIALOGS));
                editText.clear();
                Utils.hideSoftKeyboard(mActivity, mMessageText);

                UserConfig userConfig = App.getUserConfig();
                if (!userConfig.getDatingMessage().equals(editString)) {
                    userConfig.setDatingMessage(editString);
                    userConfig.saveConfig();
                }

                DatingMessageStatistics.sendDatingMessageSent();
                mSendLikeAction.sendLike();
            }

            @Override
            protected History parseResponse(ApiResponse response) {
                return new History(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (response.isCodeEqual(ErrorCodes.PREMIUM_ACCESS_ONLY)) {
                    startPurchasesActivity(CacheProfile.getOptions().instantMessagesForNewbies.getText(), "InstantMessageLimitExceeded");
                } else {
                    Utils.showErrorMessage();
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                setSendEnabled(true);
            }

            @Override
            public void cancel() {
                super.cancel();
                setSendEnabled(true);
            }
        }).exec();
        return true;
    }

    private void startPurchasesActivity(String message, String statisticKey) {
        mActivity.startActivityForResult(
                PurchasesActivity.createVipBuyIntent(
                        message,
                        statisticKey
                ),
                PurchasesActivity.INTENT_BUY_VIP
        );
    }

    private boolean tryChat(SearchUser user) {
        if (CacheProfile.getOptions().instantMessagesForNewbies.isEnabled()) {
            return true;
        }

        if (CacheProfile.premium || user.isMutualPossible || !CacheProfile.getOptions().blockChatNotMutual) {
            return true;
        } else {
            startPurchasesActivity(mActivity.getString(R.string.chat_block_not_mutual),
                    "DatingInstantMessage");
            DatingMessageStatistics.sendVipBuyScreenTransition();
            return false;
        }
    }

    public void openChat(FragmentActivity activity, SearchUser user) {
        if (user != null) {
            Intent intent = ChatActivity.createIntent(user.id, user.getNameAndAge(), user.city.name, null, user.photo, false);
            activity.startActivityForResult(intent, ChatActivity.REQUEST_CHAT);
            EasyTracker.sendEvent("Dating", "Additional", "Chat", 1L);
        }
    }

    public void setEnabled(boolean isEnabled) {
        mGiftSend.setEnabled(isEnabled);
        mMessageText.setEnabled(isEnabled);
        mMessageSend.setEnabled(isEnabled && isMessageValid());
        mIsSendEnadled = mIsEnabled = isEnabled;
    }

    /**
     * по возможности обновляет дефолтный текст на новый, в случае если в конфиге что-то изменилось
     * например при смене локали приложения
     * если же пользователь менял текст, то обновлять нельзя
     */
    public void updateMessageIfNeed() {
        String textCurrent = mMessageText.getText().toString();
        String textNewFromConfig = CacheProfile.getOptions().instantMessageFromSearch.getText();

        if (TextUtils.isEmpty(mLastMsgFromConfig)) {
            // такое бывает при первом запуске приложения после установки
            // фрагмент уже есть, а текста из Options еще нет
            mLastMsgFromConfig = textNewFromConfig;
            setInstantMessageText(mLastMsgFromConfig);
        } else {
            if (textCurrent.equals(mLastMsgFromConfig)) {
                if (!textNewFromConfig.equals(mLastMsgFromConfig)) {
                    mLastMsgFromConfig = textNewFromConfig;
                    setInstantMessageText(mLastMsgFromConfig);
                }
            }
        }
    }

    private boolean isMessageValid() {
        return !mMessageText.getText().toString().trim().isEmpty();
    }

    public void setSendEnabled(boolean isEnabled) {
        mGiftSend.setEnabled(isEnabled);
        mMessageText.setEnabled(isEnabled);
        mMessageSend.setEnabled(isEnabled && isMessageValid());
        int sendWidth = mMessageSend.getWidth();
        int sendHeight = mMessageSend.getHeight();
        if (isEnabled) {
            mMessageSend.clearAnimation();
            mMessageSend.setImageResource(R.drawable.btn_send_message_selector);
        } else {
            mMessageSend.setImageResource(R.drawable.spinner_white_16);
            mMessageSend.startAnimation(mSpin);
        }
        mMessageSend.setMinimumWidth(sendWidth);
        mMessageSend.setMinimumHeight(sendHeight);

        mIsSendEnadled = isEnabled;
    }

    public void displayMessageField() {
        mFooterFlipper.setDisplayedChild(0);
    }

    public void displayExistingDialogButtons() {
        mFooterFlipper.setDisplayedChild(1);
    }

    public void setInstantMessageText(String text) {
        if (text == null) {
            mMessageText.getText().clear();
        } else {
            mMessageText.setText(text);
            mMessageText.setSelection(text.length());
        }
    }

    public void reset() {
        String defaultMessage = App.getUserConfig().getDatingMessage();
        setInstantMessageText(defaultMessage);
    }

    public void instantSend(final SearchUser user) {
        if (user != null && user.id > 0) {
            HistoryRequest chatRequest = new HistoryRequest(mActivity, user.id);
            mRequestClient.registerRequest(chatRequest);
            setSendEnabled(false);
            EasyTracker.sendEvent("Dating", "SendMessage", "try-sent", 1L); // Event for clicking send button
            chatRequest.callback(new DataApiHandler<HistoryListData>() {

                @Override
                protected void success(HistoryListData data, IApiResponse response) {
                    if (data != null && !data.items.isEmpty()) {
                        displayExistingDialogButtons();
                        EasyTracker.sendEvent("Dating", "SendMessage", "dialog-exists", 1L); // Event for existing dialog
                        setSendEnabled(true);
                    } else {
                        DatingInstantMessageController.this.sendMessage(user);
                        EasyTracker.sendEvent("Dating", "SendMessage", "message-sent", 1L); // Event for successfull sent
                    }
                }

                @Override
                protected HistoryListData parseResponse(ApiResponse response) {
                    return new HistoryListData(response.jsonResult, History.class);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    setSendEnabled(true);
                }

                @Override
                public void cancel() {
                    super.cancel();
                    setSendEnabled(true);
                }
            }).exec();
        }
    }

    public interface SendLikeAction {

        void sendLike();
    }
}

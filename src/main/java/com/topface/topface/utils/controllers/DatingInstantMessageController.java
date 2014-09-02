package com.topface.topface.utils.controllers;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
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

    public interface SendLikeAction {

        void sendLike();
    }

    public DatingInstantMessageController(Activity activity, KeyboardListenerLayout root,
                                          View.OnClickListener clickListener,
                                          IRequestClient requestClient, String text,
                                          final View datingButtons, final View userInfoStatus,
                                          SendLikeAction sendLikeAction) {
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
                if (s.toString().trim().isEmpty()) {
                    mMessageSend.setEnabled(false);
                } else {
                    mMessageSend.setEnabled(true);
                }
            }
        });
        UserConfig userConfig = App.getUserConfig();
        String defaultMessage = userConfig.getDefaultDatingMessage();
        if (defaultMessage.isEmpty()) {
            userConfig.setDefaultDatingMessage(text);
            userConfig.saveConfig();
        }
        mMessageText.setText(defaultMessage.isEmpty() ? text : defaultMessage);
        mMessageText.setHint(activity.getString(R.string.dating_message));
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
            Toast.makeText(mActivity,
                    String.format(mActivity.getString(R.string.message_too_long), mMaxMessageSize),
                    Toast.LENGTH_SHORT).show();
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
                if (!userConfig.getDefaultDatingMessage().equals(editString)) {
                    userConfig.setDefaultDatingMessage(editString);
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
                Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                setSendEnabled(true);
            }
        }).exec();
        return true;
    }

    private boolean tryChat(SearchUser user) {
        if (CacheProfile.premium || user.isMutualPossible || !CacheProfile.getOptions().block_chat_not_mutual) {
            return true;
        } else {
            mActivity.startActivityForResult(
                    PurchasesActivity.createVipBuyIntent(
                            mActivity.getString(R.string.chat_block_not_mutual),
                            "DatingInstantMessage"
                    ),
                    PurchasesActivity.INTENT_BUY_VIP
            );
            DatingMessageStatistics.sendVipBuyScreenTransition();
            return false;
        }
    }

    public void openChat(FragmentActivity activity, SearchUser user) {
        Intent intent = new ChatActivity.IntentBuilder(activity).feedUser(user).
                initialMessage(mMessageText.getText().toString()).build();
        activity.startActivityForResult(intent, ChatActivity.INTENT_CHAT);
        EasyTracker.sendEvent("Dating", "Additional", "Chat", 1L);
    }

    public void setEnabled(boolean isEnabled) {
        mGiftSend.setEnabled(isEnabled);
        mMessageText.setEnabled(isEnabled);
        mMessageSend.setEnabled(isEnabled && isMessageValid());
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
    }

    public void displayMessageField() {
        mFooterFlipper.setDisplayedChild(0);
    }

    public void displayExistingDialogButtons() {
        mFooterFlipper.setDisplayedChild(1);
    }

    public void reset() {
        mMessageText.setText(App.getUserConfig().getDefaultDatingMessage());
    }

    public void instantSend(final SearchUser user) {
        if (user.id > 0) {
            HistoryRequest chatRequest = new HistoryRequest(mActivity, user.id);
            mRequestClient.registerRequest(chatRequest);
            chatRequest.limit = 1;
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
            }).exec();
        }
    }
}

package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.dialogs.AbstractDialogFragment;
import com.topface.topface.utils.Utils;

public class QuickMessageFragment extends AbstractDialogFragment implements View.OnClickListener {
    public static final String TAG = "QuickMessageFragment";
    private static final String ARG_USER_ID = "user_id";
    private EditText mEditBox;
    private int mUserId;
    private OnQuickMessageSentListener mListener;
    private View mMessageBox;
    private TextView mMessage;
    private View mLoader;
    private boolean mActionBarIsShowing;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity != null && activity instanceof ActionBarActivity) {
            android.support.v7.app.ActionBar actionBar = ((ActionBarActivity) activity).getSupportActionBar();
            mActionBarIsShowing = actionBar != null && actionBar.isShowing();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUserId = getArguments().getInt(ARG_USER_ID);
        setNeedActionBarIndent(false);
    }

    @Override
    protected void initViews(View root) {
        root.findViewById(R.id.btnChatAdd).setVisibility(View.GONE);
        root.findViewById(R.id.btnSend).setOnClickListener(this);
        // close button
        View closeButton = root.findViewById(R.id.close_button_image);
        closeButton.setOnClickListener(this);
        final RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) closeButton.getLayoutParams();
        if (!mActionBarIsShowing && params != null) {
            params.topMargin = Utils.getPxFromDp(8);
        }
        closeButton.setLayoutParams(params);
        // edit text for user message input
        mMessageBox = root.findViewById(R.id.chatUserMessage);
        mMessage = (TextView) root.findViewById(R.id.chat_message);
        mEditBox = (EditText) root.findViewById(R.id.edChatBox);
        mLoader = root.findViewById(R.id.quickMessageLoader);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public int getDialogLayoutRes() {
        return R.layout.fragment_quick_message;
    }

    @Override
    public void onResume() {
        super.onResume();
        FragmentActivity activity = getActivity();
        Utils.showSoftKeyboard(activity, mEditBox);
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentActivity activity = getActivity();
        Utils.hideSoftKeyboard(activity, mEditBox);
        if (activity instanceof NavigationActivity) {
            ((NavigationActivity) activity).setPopupVisible(false);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                sendMessage();
                break;
            case R.id.close_button_image:
                if (mListener != null) {
                    mListener.onCancel(this);
                }
        }
    }

    private boolean sendMessage() {
        Utils.hideSoftKeyboard(getActivity(), mEditBox);
        final Editable editText = mEditBox.getText();
        final String editString = editText == null ? "" : editText.toString();
        if (editText == null || TextUtils.isEmpty(editString.trim()) || mUserId == 0) {
            return false;
        }

        showLoader();
        editText.clear();

        final MessageRequest messageRequest = new MessageRequest(mUserId, editString, getActivity());
        messageRequest.callback(new DataApiHandler<History>() {
            @Override
            protected void success(History data, IApiResponse response) {
                showMessage(data.text);
                if (mListener != null) {
                    mListener.onMessageSent(data.text, QuickMessageFragment.this);
                }
            }

            @Override
            protected History parseResponse(ApiResponse response) {
                return new History(response);
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                hideLoader();
                //Возвращаем текст
                if (isAdded()) {
                    Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                    editText.append(editString);
                }
            }
        }).exec();

        return true;
    }

    private void showLoader() {
        mLoader.setVisibility(View.VISIBLE);
    }

    private void hideLoader() {
        mLoader.setVisibility(View.GONE);
    }

    private void showMessage(String text) {
        hideLoader();
        mMessageBox.setVisibility(View.VISIBLE);
        mMessage.setText(text);

    }

    /**
     * Создает новый инстанс фрагмента, добавляя нужные аргументы
     *
     * @param userId   id пользователя, которому будет отправлено сообщение
     * @param listener листенер событий отправки сообщений или отмены
     * @return фрагмент отправки сообщений
     */
    public static QuickMessageFragment newInstance(int userId, OnQuickMessageSentListener listener) {
        QuickMessageFragment fragment = new QuickMessageFragment();
        fragment.setListener(listener);
        Bundle args = new Bundle();
        args.putInt(ARG_USER_ID, userId);
        fragment.setArguments(args);

        return fragment;
    }

    public void setListener(OnQuickMessageSentListener listener) {
        mListener = listener;
    }
}

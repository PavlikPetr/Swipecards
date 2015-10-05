package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.requests.ComplainRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.Utils;

public class ComplainsMessageFragment extends BaseFragment {

    public static final String CLASS_NAME = "class";
    public static final String TYPE_NAME = "type";
    public static final String USER_ID = "userId";
    public static final String FEED_ID = "FEED_ID";
    private ComplainRequest.ClassNames className;
    private ComplainRequest.TypesNames typeName;
    private int userId;
    private EditText description;
    private View mComplainLocker;
    private String feedId;
    private MenuItem mSendMenuItem;

    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.hideSoftKeyboard(getActivity(), description);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.complains_message_fragment, container, false);
        // to prevent clicks through fragment
        root.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        Bundle arguments = getArguments();
        className = (ComplainRequest.ClassNames) arguments.getSerializable(CLASS_NAME);
        typeName = (ComplainRequest.TypesNames) arguments.getSerializable(TYPE_NAME);
        mComplainLocker = root.findViewById(R.id.complainLocker);
        ComplainsFragment.ComplainItem item = ComplainsFragment.getItemIdByClassAndType(className, typeName);
        userId = arguments.getInt(USER_ID);
        feedId = arguments.getString(FEED_ID);
        TextView title = (TextView) root.findViewById(R.id.tvReason);
        title.setText(item.title);

        description = (EditText) root.findViewById(R.id.etDescription);

        return root;
    }

    private void sendComplainRequest() {
        Utils.hideSoftKeyboard(getActivity(), description);
        mSendMenuItem.setEnabled(false);
        ComplainRequest request = new ComplainRequest(getActivity(), userId, className, typeName);
        if (!description.getText().toString().isEmpty()) {
            request.setDescription(description.getText().toString());
        }
        if (feedId != null) {
            request.setFeedId(feedId);
        }
        mComplainLocker.setVisibility(View.VISIBLE);
        request.callback(new ApiHandler() {
            @Override
            public void success(IApiResponse response) {
                if (getActivity() != null) {
                    Utils.showToastNotification(R.string.general_complain_sended, Toast.LENGTH_SHORT);
                    getActivity().finish();
                }
            }

            @Override
            public void fail(int codeError, IApiResponse response) {
                if (getActivity() != null) {
                    Utils.showErrorMessage();
                }
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (isAdded()) {
                    mComplainLocker.setVisibility(View.GONE);
                    mSendMenuItem.setEnabled(true);
                }
            }
        }).exec();
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_complain);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        mSendMenuItem = menu.findItem(R.id.action_send);
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_send;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                sendComplainRequest();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

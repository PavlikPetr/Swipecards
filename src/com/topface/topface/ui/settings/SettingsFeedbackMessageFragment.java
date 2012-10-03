package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedbackReport;
import com.topface.topface.ui.edit.AbstractEditFragment;

public class SettingsFeedbackMessageFragment extends AbstractEditFragment {

    public static final String INTENT_FEEDBACK_TYPE = "feedback message_type";

    public static final int UNKNOWN = 0;
    public static final int ERROR_MESSAGE = 1;
    public static final int DEVELOPERS_MESSAGE = 2;
    public static final int PAYMENT_MESSAGE = 3;
    public static final int COOPERATION_MESSAGE = 4;

    private EditText mEditText;

    private Report mReport = new Report();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.item_edit_form_input, null);

        // Navigation bar
        getActivity().findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);

        Button btnBack = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        btnBack.setVisibility(View.VISIBLE);
        btnBack.setText(R.string.settings_feedback);
        btnBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);
        mSaveButton.setText(R.string.navigation_send);
        mSaveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveChanges();
            }
        });

        Bundle extras = getActivity().getIntent().getExtras();
        switch (extras.getInt(INTENT_FEEDBACK_TYPE, UNKNOWN)) {
            case ERROR_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_error_message);
                break;
            case DEVELOPERS_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_ask_developer);
                break;
            case PAYMENT_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_payment_problems);
                break;
            case COOPERATION_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_cooperation);
                break;
            case UNKNOWN:
                mReport.subject = getResources().getString(R.string.settings_feedback);
                break;
        }

        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle)).setText(mReport.subject);

        // EditText
        root.findViewById(R.id.tvTitle).setVisibility(View.GONE);
        mEditText = (EditText) root.findViewById(R.id.edText);
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        mEditText.addTextChangedListener(new TextWatcher() {

            String before = Static.EMPTY;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = s.toString();
                if (before.isEmpty() && !after.isEmpty() || !before.isEmpty() && after.isEmpty()) {
                    mReport.body = after;
                    refreshSaveState();
                }
            }
        });

        try {
            PackageInfo pInfo;
            pInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
            mReport.topface_version = pInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return root;
    }

    @Override
    protected boolean hasChanges() {
        return !mReport.body.isEmpty();
    }

    @Override
    protected void saveChanges() {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

        mReport.body = mEditText.getText().toString();
        prepareRequestSend();
        FeedbackReport feedbackRequest = new FeedbackReport(getActivity().getApplicationContext());
        feedbackRequest.subject = mReport.getSubject();
        feedbackRequest.text = mReport.getBody();
        feedbackRequest.extra = mReport.getExtra();
        feedbackRequest.callback(new ApiHandler() {

            @Override
            public void success(ApiResponse response) throws NullPointerException {
                mEditText.setText(Static.EMPTY);
                mReport.body = Static.EMPTY;
                finishRequestSend();
                updateUI(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(),
                                getString(R.string.settings_feedback_success_msg),
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                });

            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                finishRequestSend();
                updateUI(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), getString(R.string.general_data_error),
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).exec();
    }

    class Report {
        String subject;
        String body = Static.EMPTY;
        String topface_version = "unknown";
        String android_SDK = android.os.Build.VERSION.SDK;
        String android_RELEASE = android.os.Build.VERSION.RELEASE;
        String android_CODENAME = android.os.Build.VERSION.CODENAME;
        String device = android.os.Build.DEVICE;
        String model = android.os.Build.MODEL;

        public String getSubject() {
            return subject;
        }

        public String getBody() {
            return body;
        }

        public String getExtra() {
            StringBuilder strBuilder = new StringBuilder();

            strBuilder.append("Topface version:").append(topface_version).append("\n");
            strBuilder.append("Android version:").append(android_CODENAME).append("/");
            strBuilder.append(android_RELEASE).append("/").append(android_SDK).append("\n");
            strBuilder.append("Device:").append(device).append("/").append(model);

            return strBuilder.toString();
        }
    }
}

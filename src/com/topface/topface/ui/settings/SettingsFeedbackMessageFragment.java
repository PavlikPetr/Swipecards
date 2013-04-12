package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.FeedbackReport;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.edit.AbstractEditFragment;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.ui.views.LockerView;
import com.topface.topface.utils.ActionBar;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import java.util.Locale;

public class SettingsFeedbackMessageFragment extends AbstractEditFragment {

    public static final String INTENT_FEEDBACK_TYPE = "feedback_message_type";

    public static final int UNKNOWN = 0;
    public static final int ERROR_MESSAGE = 1;
    public static final int DEVELOPERS_MESSAGE = 2;
    public static final int PAYMENT_MESSAGE = 3;
    public static final int COOPERATION_MESSAGE = 4;

    private EditText mEditText;
    private EditText mEditEmail;

    private Report mReport = new Report();
    private LockerView loadingLocker;
    private TextView wantAnswerTv;
    private LinearLayout emailContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.fragment_feedback_message, null);

        // Navigation bar
        ActionBar actionBar = getActionBar(root);
        actionBar.showBackButton(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        actionBar.setTitleText(getString(R.string.settings_feedback));
        loadingLocker = (LockerView) root.findViewById(R.id.fbLoadingLocker);
        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);
        actionBar.showSendButton(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges(new Handler());
            }
        });
        Bundle extras = getActivity().getIntent().getExtras();
        int feedbackType = extras.getInt(INTENT_FEEDBACK_TYPE, UNKNOWN);
        switch (feedbackType) {
            case ERROR_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_error_message_internal);
                actionBar.setSubTitleText(getString(R.string.settings_error_message));
                break;
            case DEVELOPERS_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_ask_developer_internal);
                actionBar.setSubTitleText(getString(R.string.settings_ask_developer));
                break;
            case PAYMENT_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_payment_problems_internal);
                actionBar.setSubTitleText(getString(R.string.settings_payment_problems));
                break;
            case COOPERATION_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_cooperation_internal);
                actionBar.setSubTitleText(getString(R.string.settings_cooperation));
                break;
            case UNKNOWN:
                mReport.subject = getResources().getString(R.string.settings_feedback_internal);
                break;
        }

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
                if (TextUtils.isEmpty(before) && !TextUtils.isEmpty(after) || !TextUtils.isEmpty(before) && TextUtils.isEmpty(after)) {
                    mReport.body = after;
                    refreshSaveState();
                }
            }
        });

        initEmailViews(root, feedbackType);

        try {
            PackageInfo pInfo;
            pInfo = getActivity().getPackageManager().getPackageInfo(
                    getActivity().getPackageName(), 0);
            mReport.topface_version = pInfo.versionName;
            mReport.topface_versionCode = pInfo.versionCode;
        } catch (NameNotFoundException e) {
            Debug.error(e);
        }

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return root;
    }

    private void initEmailViews(View root, int feedbackType) {
        final TextView emailTitle = (TextView) root.findViewById(R.id.tvEmailTitle);
        mEditEmail = (EditText) root.findViewById(R.id.edEmail);
        final CheckBox emailSwitchLayout = (CheckBox) root.findViewById(R.id.loEmailSwitcher);
        mEditEmail.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditEmail.setText(Settings.getInstance().getSocialAccountEmail());
        emailContainer = (LinearLayout) root.findViewById(R.id.emailContainer);
        wantAnswerTv = (TextView) root.findViewById(R.id.wantAnswerTv);
        switch (feedbackType) {
            case DEVELOPERS_MESSAGE:
                emailSwitchLayout.setChecked(true);
                mReport.emailWanted = true;
                mEditEmail.setVisibility(View.VISIBLE);
                emailTitle.setVisibility(View.VISIBLE);
                break;
            case PAYMENT_MESSAGE:
                mReport.emailWanted = true;
                mEditEmail.setVisibility(View.VISIBLE);
//                emailTitle.setVisibility(View.VISIBLE);
                emailContainer.setVisibility(View.GONE);
                break;
            case ERROR_MESSAGE:
            case COOPERATION_MESSAGE:
            case UNKNOWN:
                emailSwitchLayout.setChecked(false);
                break;
        }

        emailSwitchLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
//                emailSwitchLayout.doSwitch();
                if (emailSwitchLayout.isChecked()) {
                    mReport.emailWanted = true;
                    mEditEmail.setVisibility(View.VISIBLE);
                    emailTitle.setVisibility(View.VISIBLE);
                } else {
                    mReport.emailWanted = false;
                    mEditEmail.setVisibility(View.GONE);
                    emailTitle.setVisibility(View.GONE);
                }
            }
        });

        wantAnswerTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                emailSwitchLayout.performClick();
            }
        });
    }

    @Override
    protected boolean hasChanges() {
        return !TextUtils.isEmpty(mReport.body);
    }

    @Override
    protected void saveChanges(Handler handler) {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
        String feedbackText = mEditText.getText().toString().trim();

        //Если текст сообщения пустой, то не отправляем сообщение
        if (TextUtils.isEmpty(feedbackText)) {
            return;
        }

        if (emailConfirmed()) {
            mReport.body = feedbackText;
            prepareRequestSend();
            FeedbackReport feedbackRequest = new FeedbackReport(getActivity().getApplicationContext());
            feedbackRequest.subject = mReport.getSubject();
            feedbackRequest.text = mReport.getBody();
            feedbackRequest.extra = mReport.getExtra();
            feedbackRequest.email = mReport.getEmail();
            feedbackRequest.callback(new ApiHandler() {

                @Override
                public void success(ApiResponse response) {
                    if (isAdded()) {
                        mReport.body = Static.EMPTY;
                        finishRequestSend();

                        mEditText.setText(Static.EMPTY);
                        Toast.makeText(App.getContext(),
                                R.string.settings_feedback_success_msg,
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }

                @Override
                public void fail(int codeError, ApiResponse response) {
                    finishRequestSend();
                    Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT).show();
                }
            }).exec();
        } else {
            Toast.makeText(App.getContext(), R.string.settings_invalid_email, Toast.LENGTH_LONG).show();
            mEditEmail.requestFocus();
        }
    }

    private boolean emailConfirmed() {
        if (mReport.emailWanted) {
            String email = mEditEmail.getText().toString();
            if (Utils.isValidEmail(email)) {
                mReport.email = email;
                Settings.getInstance().setSocialAccountEmail(mReport.email);
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    private void setBackground(int resId, ViewGroup frame) {
        ImageView background = (ImageView) frame.findViewById(R.id.ivEditBackground);
        background.setImageResource(resId);
    }

    private void setText(int titleResId, ViewGroup frame) {
        ((TextView) frame.findViewById(R.id.tvTitle)).setText(titleResId);
    }

    class Report {
        boolean emailWanted = false;
        String email;
        String subject;
        String body = Static.EMPTY;
        String topface_version = "unknown";
        int topface_versionCode = 0;
        String android_SDK = "API" + android.os.Build.VERSION.SDK_INT;
        String android_RELEASE = android.os.Build.VERSION.RELEASE;
        String android_CODENAME = android.os.Build.VERSION.CODENAME;
        String device = android.os.Build.DEVICE;
        String model = android.os.Build.MODEL;

        public String getSubject() {
            AuthToken authToken = AuthToken.getInstance();
            return "[" + Static.PLATFORM + "]" + subject + " {" + authToken.getSocialNet() + "_id=" + authToken.getUserId() + "}";
        }

        public String getBody() {
            return body;
        }

        public String getExtra() {
            StringBuilder strBuilder = new StringBuilder();

            if (emailWanted && email != null) {
                strBuilder.append("<p>Email for answer: ").append(email).append("</p>");
            }
            strBuilder.append("<p>Topface version: ").append(topface_version).append("/").append(topface_versionCode)
                    .append("</p>");
            strBuilder.append("<p>Device: ").append(device).append("/").append(model).append("</p>");
            strBuilder.append("<p>Device language: ").append(Locale.getDefault().getDisplayLanguage()).append("</p>");

            strBuilder.append("<p>Topface SSID: ").append(Ssid.get()).append("</p>");
            AuthToken authToken = AuthToken.getInstance();
            strBuilder.append("<p>Social net: ").append(authToken.getSocialNet()).append("</p>");
            strBuilder.append("<p>Social token: ").append(authToken.getTokenKey()).append("</p>");
            strBuilder.append("<p>Social id: ").append(authToken.getUserId()).append("</p>");

            strBuilder.append("<p>Android version: ").append(android_CODENAME).append("/");
            strBuilder.append(android_RELEASE).append("/").append(android_SDK).append("</p>");

            strBuilder.append("<p>Build type: ").append(Utils.getBuildType()).append(android_SDK).append("</p>");

            return strBuilder.toString();
        }

        public String getEmail() {
            return email;
        }
    }

    @Override
    protected void lockUi() {
        if (loadingLocker != null) {
            loadingLocker.setVisibility(View.VISIBLE);
            mEditText.setEnabled(false);
        }
    }

    @Override
    protected void unlockUi() {
        if (loadingLocker != null) {
            mEditText.setEnabled(true);
            loadingLocker.setVisibility(View.GONE);
        }
    }
}

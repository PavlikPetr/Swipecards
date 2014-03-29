package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.BuildConfig;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.requests.FeedbackReport;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.edit.AbstractEditFragment;
import com.topface.topface.utils.ClientUtils;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import java.util.List;
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
    private EditText mTransactionIdEditText;

    private Report mReport = new Report();
    private View mLoadingLocker;
    private int mFeedbackType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.fragment_feedback_message, null);
        if (root == null) return null;
        // Navigation bar
        mLoadingLocker = root.findViewById(R.id.fbLoadingLocker);
        // EditText
        root.findViewById(R.id.tvTitle).setVisibility(View.GONE);
        //Если  текущий язык приложения не русский или английский, то нужно показывать сообщение
        //о том, что лучше писать нам по русски или английски, поэтому проверяем тут локаль
        TextView incorrectLocaleTv = (TextView) root.findViewById(R.id.tvLocale);
        String language = Locale.getDefault().getLanguage();
        if (language.equals("en") || language.equals("ru")) {
            incorrectLocaleTv.setVisibility(View.GONE);
        }
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
        initTextViews(root, mFeedbackType);
        SettingsFeedbackMessageFragment.fillVersion(getActivity(), mReport);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return root;
    }

    public static void fillVersion(Context context, Report report) {
        if (context != null && report != null) {
            try {
                PackageInfo pInfo;
                PackageManager pManager = context.getPackageManager();
                if (pManager != null) {
                    pInfo = pManager.getPackageInfo(context.getPackageName(), 0);
                    report.topface_version = pInfo.versionName;
                    report.topface_versionCode = pInfo.versionCode;
                }
            } catch (NameNotFoundException e) {
                Debug.error(e);
            }
        }
    }

    @Override
    protected void restoreState() {
        super.restoreState();
        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            mFeedbackType = extras.getInt(INTENT_FEEDBACK_TYPE, UNKNOWN);
        }
        switch (mFeedbackType) {
            case ERROR_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_error_message_internal);
                break;
            case DEVELOPERS_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_ask_developer_internal);
                break;
            case PAYMENT_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_payment_problems_internal);
                break;
            case COOPERATION_MESSAGE:
                mReport.subject = getResources().getString(R.string.settings_cooperation_internal);
                break;
            case UNKNOWN:
                mReport.subject = getResources().getString(R.string.settings_feedback_internal);
                break;
        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_feedback);
    }

    protected String getSubtitle() {
        switch (mFeedbackType) {
            case ERROR_MESSAGE:
                return getString(R.string.settings_error_message);
            case DEVELOPERS_MESSAGE:
                return getString(R.string.settings_ask_developer);
            case PAYMENT_MESSAGE:
                return getString(R.string.settings_payment_problems);
            case COOPERATION_MESSAGE:
                return getString(R.string.settings_cooperation);
            default:
                return null;
        }
    }

    private void initTextViews(View root, int feedbackType) {
        mEditEmail = (EditText) root.findViewById(R.id.edEmail);
        mEditEmail.setInputType(InputType.TYPE_CLASS_TEXT);
        mEditEmail.setText(Settings.getInstance().getSocialAccountEmail());
        mTransactionIdEditText = (EditText) root.findViewById(R.id.edTransactionId);
        switch (feedbackType) {
            case DEVELOPERS_MESSAGE:
                break;
            case PAYMENT_MESSAGE:
                root.findViewById(R.id.tvTransactionIdTitle).setVisibility(View.VISIBLE);
                mTransactionIdEditText.setVisibility(View.VISIBLE);
                break;
            case ERROR_MESSAGE:
            case COOPERATION_MESSAGE:
            case UNKNOWN:
                break;
        }
    }

    @Override
    protected boolean hasChanges() {
        return !TextUtils.isEmpty(mReport.body);
    }

    @Override
    protected void saveChanges(Handler handler) {
        Utils.hideSoftKeyboard(getActivity(), mEditText);
        String feedbackText = Utils.getText(mEditText).trim();

        //Если текст сообщения пустой, то не отправляем сообщение
        if (TextUtils.isEmpty(feedbackText)) {
            Toast.makeText(App.getContext(), R.string.empty_fields, Toast.LENGTH_LONG).show();
            return;
        }

        if (emailConfirmed(Utils.getText(mEditEmail).trim())) {
            mReport.body = feedbackText;
            mReport.transactionId = Utils.getText(mTransactionIdEditText).trim();
            prepareRequestSend();
            FeedbackReport feedbackRequest = new FeedbackReport(getActivity(), mReport);
            feedbackRequest.callback(new ApiHandler() {

                @Override
                public void success(IApiResponse response) {
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
                public void fail(int codeError, IApiResponse response) {
                    finishRequestSend();
                    Toast.makeText(App.getContext(), R.string.general_data_error,
                            Toast.LENGTH_SHORT).show();
                }
            }).exec();
        } else {
            Toast.makeText(App.getContext(), R.string.settings_invalid_email, Toast.LENGTH_SHORT).show();
            mEditEmail.requestFocus();
        }
    }

    private boolean emailConfirmed(String email) {
        if (Utils.isValidEmail(email)) {
            mReport.email = email;
            Settings.getInstance().setSocialAccountEmail(mReport.email);
            return true;
        } else {
            return false;
        }
    }

    public static class Report {
        String email;
        List<String> userDeviceAccounts;
        String subject;
        String body = Static.EMPTY;
        String topface_version = "unknown";
        int topface_versionCode = 0;
        String android_SDK = "API " + android.os.Build.VERSION.SDK_INT;
        String android_RELEASE = android.os.Build.VERSION.RELEASE;
        String android_CODENAME = android.os.Build.VERSION.CODENAME;
        String device = android.os.Build.DEVICE;
        String model = android.os.Build.MODEL;
        String transactionId = null;

        private AuthToken authToken = AuthToken.getInstance();

        public Report() {
            userDeviceAccounts = ClientUtils.getClientAccounts();
        }

        public String getSubject() {
            return "[" + Static.PLATFORM + "]" + subject + " {" + authToken.getSocialNet() + "_id=" + authToken.getUserSocialId() + "}";
        }

        public String getBody() {
            return body;
        }

        public String getExtra() {
            StringBuilder strBuilder = new StringBuilder();

            strBuilder.append("<p>Email for answer: ").append(email).append(";</p>\n");
            strBuilder.append("<p>Device accounts: ");
            strBuilder.append(TextUtils.join(", ", userDeviceAccounts));
            strBuilder.append(";</p>\n");
            strBuilder.append("<p>Topface version: ").append(topface_version).append("/").append(topface_versionCode)
                    .append(";</p>\n");
            strBuilder.append("<p>Device: ").append(device).append("/").append(model).append(";</p>\n");
            strBuilder.append("<p>Device language: ").append(Locale.getDefault().getDisplayLanguage()).append(";</p>\n");

            strBuilder.append("<p>Topface SSID: ").append(Ssid.get()).append(";</p>\n");
            strBuilder.append("<p>Social net: ").append(authToken.getSocialNet()).append(";</p>\n");
            if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
                strBuilder.append("<p>Topface login: ").append(authToken.getLogin()).append(";</p>\n");
            } else {
                strBuilder.append("<p>Social token: ").append(authToken.getTokenKey()).append(";</p>\n");
            }

            strBuilder.append("<p>Social id: ").append(authToken.getUserSocialId()).append(";</p>\n");

            strBuilder.append("<p>Android version: ").append(android_CODENAME).append("/");
            strBuilder.append(android_RELEASE).append("/").append(android_SDK).append(";</p>\n");

            strBuilder.append("<p>Build type: ")
                    .append(BuildConfig.BILLING_TYPE.getClientType())
                    .append(android_SDK)
                    .append(";</p>\n");
            if (transactionId != null) {
                strBuilder.append("<p>Transaction Id: ").append(transactionId).append(";</p>\n");
            }

            return strBuilder.toString();
        }

        public String getEmail() {
            return email;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }

    @Override
    protected void lockUi() {
        if (mLoadingLocker != null) {
            mLoadingLocker.setVisibility(View.VISIBLE);
            mEditText.setEnabled(false);
        }
    }

    @Override
    protected void unlockUi() {
        if (mLoadingLocker != null) {
            mEditText.setEnabled(true);
            mLoadingLocker.setVisibility(View.GONE);
        }
    }

    @Override
    protected Integer getOptionsMenuRes() {
        return R.menu.actions_send;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                saveChanges(new Handler());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

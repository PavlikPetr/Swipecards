package com.topface.topface.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
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
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendFeedbackRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.edit.AbstractEditFragment;
import com.topface.topface.utils.ClientUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import java.util.List;
import java.util.Locale;

public class FeedbackMessageFragment extends AbstractEditFragment {

    public static final String INTENT_FEEDBACK_TYPE = "feedback_message_type";
    private EditText mEditText;
    private EditText mEditEmail;
    private EditText mTransactionIdEditText;
    private Report mReport = new Report();
    private View mLoadingLocker;
    private FeedbackType mFeedbackType;

    public static void fillVersion(Context context, Report report) {
        if (context != null && report != null) {
            report.topface_version = BuildConfig.VERSION_NAME;
            report.topface_versionCode = BuildConfig.VERSION_CODE;
        }
    }

    public static Fragment newInstance(FeedbackType feedbackType) {
        Fragment fragment = new FeedbackMessageFragment();
        Bundle args = new Bundle();
        args.putSerializable(INTENT_FEEDBACK_TYPE, feedbackType);
        fragment.setArguments(args);
        return fragment;
    }

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
        FeedbackMessageFragment.fillVersion(getActivity(), mReport);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        return root;
    }

    @Override
    protected void restoreState() {
        super.restoreState();
        Bundle extras = getArguments();
        if (extras != null) {
            mFeedbackType = (FeedbackType) extras.getSerializable(INTENT_FEEDBACK_TYPE);
            mFeedbackType = mFeedbackType == null ? FeedbackType.UNKNOWN : mFeedbackType;
            mReport.setType(mFeedbackType);
            mReport.subject = mFeedbackType.getTitle();
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
            case BAN:
                return getString(R.string.feedback_subject_ban);
            default:
                return null;
        }
    }

    private void initTextViews(View root, FeedbackType feedbackType) {
        initEmailInput(root);
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

    /**
     * инициализация поля ввода email
     * и заполнение его в зависимости от типа сети
     * если вход был через соц сеть - то email гугл-аккаунта
     * если через topface аккаунт - то его email
     *
     * @param root - view, где вложено поле ввода email
     */
    private void initEmailInput(View root) {
        mEditEmail = (EditText) root.findViewById(R.id.edEmail);
        mEditEmail.setInputType(InputType.TYPE_CLASS_TEXT);
        String email;
        if (AuthToken.getInstance().getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            email = AuthToken.getInstance().getLogin();
        } else {
            email = ClientUtils.getSocialAccountEmail();
        }

        mEditEmail.setText(email);
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
            SendFeedbackRequest feedbackRequest = new SendFeedbackRequest(getActivity(), mReport);
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
                    if (response.isCodeEqual(ErrorCodes.TOO_MANY_MESSAGES)) {
                        Toast.makeText(App.getContext(), R.string.ban_flood_detected,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(App.getContext(), R.string.general_data_error,
                                Toast.LENGTH_SHORT).show();
                    }
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
            App.getSessionConfig().setSocialAccountEmail(mReport.email);
            return true;
        } else {
            return false;
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

    public enum FeedbackType {
        UNKNOWN("mobile_none", R.string.settings_feedback_internal),
        ERROR_MESSAGE("mobile_error", R.string.settings_error_message_internal),
        DEVELOPERS_MESSAGE("mobile_question", R.string.settings_ask_developer_internal),
        PAYMENT_MESSAGE("mobile_payment_issue", R.string.settings_payment_problems_internal),
        COOPERATION_MESSAGE("mobile_cooperation", R.string.settings_cooperation_internal),
        BAN("mobile_ban", R.string.feedback_subject_ban_internal),
        LOW_RATE_MESSAGE("mobile_low_rate", R.string.settings_low_rate_internal);

        private final String mTypeTag;
        private final int mTitleRes;

        FeedbackType(String tag, @StringRes int title) {
            mTypeTag = tag;
            mTitleRes = title;
        }

        public String getTag() {
            return mTypeTag;
        }


        public String getTitle() {
            return App.getContext().getResources().getString(mTitleRes);
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
        FeedbackType type;

        private AuthToken authToken = AuthToken.getInstance();

        public Report() {
        }

        public Report(FeedbackType type) {
            setType(type);
        }

        public void setType(FeedbackType type) {
            this.type = type;
        }

        public String getSubject() {
            return "[" + Static.PLATFORM + "]" + subject + " {" + authToken.getSocialNet() + "_id=" + authToken.getUserSocialId() + "}";
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
        
        public String getExtra() {
            StringBuilder strBuilder = new StringBuilder();

            strBuilder.append("<p>Email for answer: ").append(email).append(";</p>\n");
            strBuilder.append("<p>Device accounts: ");
            strBuilder.append(TextUtils.join(", ", getUserDeviceAccounts()));
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

        public List<String> getUserDeviceAccounts() {
            if (userDeviceAccounts == null) {
                userDeviceAccounts = ClientUtils.getClientAccounts();
            }
            return userDeviceAccounts;
        }

        public FeedbackType getType() {
            return type;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}

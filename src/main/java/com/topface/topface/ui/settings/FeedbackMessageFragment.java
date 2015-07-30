package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class FeedbackMessageFragment extends AbstractEditFragment {

    public static final String INTENT_FEEDBACK_TYPE = "feedback_message_type";
    public static final String WALLET_HOLDER = "{{wallet}}";
    private static final String GOOGLE_WALLET_URL = "https://wallet.google.com";
    @Bind(R.id.edText)
    EditText mEditText;
    @Bind(R.id.edEmail)
    EditText mEditEmail;
    @Bind(R.id.fbLoadingLocker)
    View mLoadingLocker;
    @Bind(R.id.tvTransactionIdInfoLink)
    TextView mTransactionIdInfo;
    @Bind(R.id.tvLocale)
    TextView mIncorrectLocaleTv;
    private FeedbackType mFeedbackType;
    private String mFeedback = Static.EMPTY;
    private Report mReport = new Report();

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

    @OnTextChanged(value = R.id.edText, callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    void onBeforeFeedbackChanged(CharSequence text) {
        mFeedback = text.toString();
    }

    @OnTextChanged(value = R.id.edText, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onAfterFeedbackChanged(CharSequence text) {
        String after = text.toString();
        if (TextUtils.isEmpty(mFeedback) && !TextUtils.isEmpty(after) || !TextUtils.isEmpty(mFeedback) && TextUtils.isEmpty(after)) {
            mReport.body = after;
            refreshSaveState();
        }
    }

    @OnClick(R.id.sendFeedback)
    public void sendFeedbackClick() {
        saveChanges(new Handler());
    }

    @OnClick(R.id.tvTransactionIdInfoLink)
    public void googleWalletClick() {
        if (Utils.isIntentAvailable(getActivity(), Intent.ACTION_VIEW)) {
            Utils.goToUrl(getActivity(), GOOGLE_WALLET_URL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        getSupportActionBar().show();
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.fragment_feedback_message, null);
        if (root == null) return null;
        ButterKnife.bind(this, root);
        //Если  текущий язык приложения не русский или английский, то нужно показывать сообщение
        //о том, что лучше писать нам по русски или английски, поэтому проверяем тут локаль
        String language = Locale.getDefault().getLanguage();
        if (language.equals("en") || language.equals("ru")) {
            mIncorrectLocaleTv.setVisibility(View.GONE);
        }
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        initTextViews(mFeedbackType);
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

    private void initTextViews(FeedbackType feedbackType) {
        initEmailInput();
        switch (feedbackType) {
            case DEVELOPERS_MESSAGE:
                break;
            case PAYMENT_MESSAGE:
                mTransactionIdInfo.setText(createHelpMessage());
                mTransactionIdInfo.setVisibility(View.VISIBLE);
                break;
            case ERROR_MESSAGE:
            case COOPERATION_MESSAGE:
            case UNKNOWN:
                break;
        }
    }

    public SpannableString createHelpMessage() {
        String walletString = getString(R.string.google_wallet);
        String messageTemplate = getString(R.string.transaction_code_help).replace(WALLET_HOLDER, walletString);
        SpannableString helpSpannable = new SpannableString(messageTemplate);
        int startSpan = messageTemplate.indexOf(walletString);
        int endSpan = startSpan + walletString.length();
        helpSpannable.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.link_color)), startSpan, endSpan, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        helpSpannable.setSpan(new UnderlineSpan(), startSpan, endSpan, 0);
        return helpSpannable;
    }

    /**
     * инициализация поля ввода email
     * и заполнение его в зависимости от типа сети
     * если вход был через соц сеть - то email гугл-аккаунта
     * если через topface аккаунт - то его email
     */
    private void initEmailInput() {
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
            Utils.showToastNotification(R.string.empty_fields, Toast.LENGTH_LONG);
            return;
        }

        if (emailConfirmed(Utils.getText(mEditEmail).trim())) {
            mReport.body = feedbackText;
            prepareRequestSend();
            SendFeedbackRequest feedbackRequest = new SendFeedbackRequest(getActivity(), mReport);
            feedbackRequest.callback(new ApiHandler() {

                @Override
                public void success(IApiResponse response) {
                    if (isAdded()) {
                        mReport.body = Static.EMPTY;
                        finishRequestSend();

                        mEditText.setText(Static.EMPTY);
                        Utils.showToastNotification(R.string.settings_feedback_success_msg, Toast.LENGTH_SHORT);
                        getActivity().finish();
                    }
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    finishRequestSend();
                    if (response.isCodeEqual(ErrorCodes.TOO_MANY_MESSAGES)) {
                        Utils.showToastNotification(R.string.ban_flood_detected, Toast.LENGTH_SHORT);
                    } else {
                        Utils.showErrorMessage();
                    }
                }
            }).exec();
        } else {
            Utils.showToastNotification(R.string.settings_invalid_email, Toast.LENGTH_SHORT);
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
        FeedbackType type;

        private AuthToken authToken = AuthToken.getInstance();

        public Report() {
        }

        public Report(FeedbackType type) {
            setType(type);
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
                    .append(BuildConfig.MARKET_API_TYPE.getClientType())
                    .append(android_SDK)
                    .append(";</p>\n");
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

        public void setType(FeedbackType type) {
            this.type = type;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
}

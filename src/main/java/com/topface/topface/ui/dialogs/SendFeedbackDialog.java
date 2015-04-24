package com.topface.topface.ui.dialogs;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.utils.BackgroundThread;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendFeedbackRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.settings.FeedbackMessageFragment;
import com.topface.topface.utils.ClientUtils;
import com.topface.topface.utils.Utils;

/**
 * Created by kirussell on 30.12.13.
 * Dialog for user feedback with configurable title and feedback subject name
 * Use newInstance(int titleResId, String feedbackSubject) method to create dialog
 */
public class SendFeedbackDialog extends AbstractModalDialog implements View.OnClickListener {

    public static final String TAG = "SendFeedBackDialog";
    private static final String ARG_TITLE_RES_ID = "feedback_dialog_title_res_id";
    private static final String ARG_FEEDBACK_SUBJECT = "feedback_dialog_subject_res_id";
    private EditText mEdMessage;
    private String mSubject;

    public static SendFeedbackDialog newInstance(int titleResId, String feedbackSubject) {
        SendFeedbackDialog dialog = new SendFeedbackDialog();
        Bundle args = new Bundle();
        args.putInt(ARG_TITLE_RES_ID, titleResId);
        args.putString(ARG_FEEDBACK_SUBJECT, feedbackSubject);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Закрыть диалог можно
        setCancelable(true);
    }

    @Override
    protected void initContentViews(View root) {
        getDialog().setOnCancelListener(this);
        // init views
        TextView titleView = (TextView) root.findViewById(R.id.tvTitle);
        root.findViewById(R.id.btnSend).setOnClickListener(this);
        mEdMessage = (EditText) root.findViewById(R.id.edMessage);
        // restore arguments
        Bundle args = getArguments();
        if (args != null) {
            titleView.setText(args.getInt(ARG_TITLE_RES_ID));
            mSubject = args.getString(ARG_FEEDBACK_SUBJECT);
        }
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.dialog_input;
    }

    @Override
    protected void onCloseButtonClick(View v) {
        Utils.hideSoftKeyboard(getActivity(), mEdMessage);
        getDialog().cancel();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSend:
                Utils.hideSoftKeyboard(getActivity(), mEdMessage);
                final String message = Utils.getText(mEdMessage);
                final ApiHandler handler = new SimpleApiHandler() {
                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        if (response.isCodeEqual(ErrorCodes.TOO_MANY_MESSAGES)) {
                            Utils.showToastNotification(R.string.ban_flood_detected, Toast.LENGTH_SHORT);
                        } else {
                            Utils.showErrorMessage();
                        }
                    }
                };
                new BackgroundThread() {
                    @Override
                    public void execute() {
                        if (!TextUtils.isEmpty(message)) {
                            FeedbackMessageFragment.Report report = new FeedbackMessageFragment.Report(
                                    FeedbackMessageFragment.FeedbackType.LOW_RATE_MESSAGE
                            );
                            report.setSubject(mSubject);
                            report.setBody(message);
                            report.setEmail(ClientUtils.getSocialAccountEmail());
                            FeedbackMessageFragment.fillVersion(getActivity(), report);
                            SendFeedbackRequest feedbackRequest = new SendFeedbackRequest(getActivity(), report);
                            feedbackRequest.callback(handler).exec();
                        }
                    }
                };
                dismiss();
                break;
            default:
                break;
        }
    }
}

package com.topface.topface.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;

public class SettingsFeedbackFragment extends BaseFragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.fragment_feedback, null);

        // Init settings views
        initViews(root);

        return root;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_help);
    }

    private void initViews(View root) {
        ViewGroup frame;

        // FAQ header
        ViewGroup frame2 = (ViewGroup) root.findViewById(R.id.loFaqTitle);
        setText(R.string.settings_faq_title, frame2);

        // FAQ
        frame = (ViewGroup) root.findViewById(R.id.loFaq);
        if (!TextUtils.isEmpty(CacheProfile.getOptions().helpUrl)) {
            setBackground(R.drawable.edit_big_btn_selector, frame);
            setText(R.string.settings_faq, frame);
            frame.setOnClickListener(this);
        } else {
            frame.setVisibility(View.GONE);
            frame2.setVisibility(View.GONE);
        }

        // Error message
        frame = (ViewGroup) root.findViewById(R.id.loErrorMessage);
        setBackground(R.drawable.edit_big_btn_top_selector, frame);
        setText(R.string.settings_error_message, frame);
        frame.setOnClickListener(this);

        // Ask developers
        frame = (ViewGroup) root.findViewById(R.id.loAskDevelopers);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        setText(R.string.settings_ask_developer, frame);
        frame.setOnClickListener(this);

        // Payment's problems
        frame = (ViewGroup) root.findViewById(R.id.loPaymentProblem);
        setBackground(R.drawable.edit_big_btn_middle_selector, frame);
        setText(R.string.settings_payment_problems, frame);
        frame.setOnClickListener(this);

        // Cooperation
        frame = (ViewGroup) root.findViewById(R.id.loCooperation);
        setBackground(R.drawable.edit_big_btn_bottom_selector, frame);
        setText(R.string.settings_cooperation, frame);
        frame.setOnClickListener(this);
    }

    private void setBackground(int resId, ViewGroup frame) {
        ImageView background = (ImageView) frame.findViewById(R.id.ivEditBackground);
        background.setImageResource(resId);
    }

    private void setText(int titleId, ViewGroup frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(titleId);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        switch (v.getId()) {
            case R.id.loFaq:
                String helpUrl = CacheProfile.getOptions().helpUrl;
                //Ссылку на помощь показываем только в случае, если сервер нам ее прислал.
                if (!TextUtils.isEmpty(helpUrl)) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(helpUrl));
                    startActivity(intent);
                }
                break;
            case R.id.loErrorMessage:
                intent = SettingsContainerActivity.getFeedbackMessageIntent(
                        getActivity(),
                        FeedbackMessageFragment.FeedbackType.ERROR_MESSAGE
                );
                break;
            case R.id.loAskDevelopers:
                intent = SettingsContainerActivity.getFeedbackMessageIntent(
                        getActivity(),
                        FeedbackMessageFragment.FeedbackType.DEVELOPERS_MESSAGE
                );
                break;
            case R.id.loPaymentProblem:
                intent = SettingsContainerActivity.getFeedbackMessageIntent(
                        getActivity(),
                        FeedbackMessageFragment.FeedbackType.PAYMENT_MESSAGE
                );
                break;
            case R.id.loCooperation:
                intent = SettingsContainerActivity.getFeedbackMessageIntent(
                        getActivity(),
                        FeedbackMessageFragment.FeedbackType.COOPERATION_MESSAGE
                );
                break;
        }
        if (intent != null) {
            startActivityForResult(intent, SettingsContainerActivity.INTENT_SEND_FEEDBACK);
        }

    }
}

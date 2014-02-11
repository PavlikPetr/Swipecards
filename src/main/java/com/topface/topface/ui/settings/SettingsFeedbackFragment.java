package com.topface.topface.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;

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
        return getString(R.string.settings_feedback);
    }

    private void initViews(View root) {
        ViewGroup frame;

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
        Intent intent;
        switch (v.getId()) {
            case R.id.loErrorMessage:
                intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                intent.putExtra(SettingsFeedbackMessageFragment.INTENT_FEEDBACK_TYPE,
                        SettingsFeedbackMessageFragment.ERROR_MESSAGE);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_SEND_FEEDBACK);
                break;
            case R.id.loAskDevelopers:
                intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                intent.putExtra(SettingsFeedbackMessageFragment.INTENT_FEEDBACK_TYPE,
                        SettingsFeedbackMessageFragment.DEVELOPERS_MESSAGE);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_SEND_FEEDBACK);
                break;
            case R.id.loPaymentProblem:
                intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                intent.putExtra(SettingsFeedbackMessageFragment.INTENT_FEEDBACK_TYPE,
                        SettingsFeedbackMessageFragment.PAYMENT_MESSAGE);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_SEND_FEEDBACK);
                break;
            case R.id.loCooperation:
                intent = new Intent(getActivity().getApplicationContext(), SettingsContainerActivity.class);
                intent.putExtra(SettingsFeedbackMessageFragment.INTENT_FEEDBACK_TYPE,
                        SettingsFeedbackMessageFragment.COOPERATION_MESSAGE);
                startActivityForResult(intent, SettingsContainerActivity.INTENT_SEND_FEEDBACK);
                break;
        }

    }
}

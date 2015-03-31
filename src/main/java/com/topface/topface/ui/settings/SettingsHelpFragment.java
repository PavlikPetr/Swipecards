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

public class SettingsHelpFragment extends BaseFragment implements OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle saved) {
        super.onCreateView(inflater, container, saved);
        View root = inflater.inflate(R.layout.fragment_help, null);

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
        }
        if (intent != null) {
            startActivityForResult(intent, SettingsContainerActivity.INTENT_SEND_FEEDBACK);
        }

    }
}

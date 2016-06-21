package com.topface.topface.ui.dialogs;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.data.FullscreenSettings;
import com.topface.topface.databinding.OwnFullscreenLayoutBinding;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.AuthToken;

import org.jetbrains.annotations.Nullable;

/**
 * Попап для наших фулскринов
 * Created by tiberal on 17.06.16.
 */
public class OwnFullscreenPopup extends BaseDialog implements View.OnClickListener {

    public static final String FULLSCREEN_OPTIONS = "fullscreen_options";
    public static final String TAG = "OwnFullscreenPopup";
    private FullscreenSettings mFullscreenSettings;

    public static OwnFullscreenPopup newInstance(FullscreenSettings fullscreenSettings) {
        Bundle args = new Bundle();
        args.putParcelable(FULLSCREEN_OPTIONS, fullscreenSettings);
        OwnFullscreenPopup fragment = new OwnFullscreenPopup();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void parseArgs(@Nullable Bundle bundle) {
        if (bundle != null) {
            mFullscreenSettings = bundle.getParcelable(FULLSCREEN_OPTIONS);
        }
    }

    @Override
    protected void initViews(View root) {
        OwnFullscreenLayoutBinding binding = DataBindingUtil.bind(root);
        View bodyView = createBodyView();
        if (bodyView != null) {
            binding.content.addView(bodyView);
        }
        binding.setClick(this);
    }

    @Nullable
    private View createBodyView() {
        if (!mFullscreenSettings.isEmpty()) {
            switch (mFullscreenSettings.banner.type) {
                case FullscreenSettings.IMG:
                    View view = new ImageViewRemote(getContext().getApplicationContext());
                    view.setLayoutParams(new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
                    ((ImageViewRemote) view).setRemoteSrc(prepareUrl(mFullscreenSettings.banner.url));
                    return view;
                case FullscreenSettings.WEB:
                    WebView webView = new WebView(getContext().getApplicationContext());
                    webView.loadUrl(prepareUrl(mFullscreenSettings.banner.url));
                    return webView;
            }
        }
        return null;
    }

    private String prepareUrl(String url) {
        return url.replace(Utils.USER_ID, AuthToken.getInstance().getUserSocialId())
                .replace(Utils.SECRET_KEY, Ssid.get());
    }

    @Override
    protected int getDialogLayoutRes() {
        return R.layout.own_fullscreen_layout;
    }

    @Override
    protected int getDialogStyleResId() {
        return R.style.Theme_Topface_NoActionBar_DatingLockPopup;
    }

    @Override
    public void onClick(View v) {
        getDialog().cancel();
    }
}

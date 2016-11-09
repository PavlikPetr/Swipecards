package com.topface.topface.ui.fragments;

import android.os.Bundle;

import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;

/**
 * Created by ppetr on 10.01.16.
 * Фрагмент для загрузки и отображения web страницы интегратора
 */
public class IntegrationWebViewFragment extends WebViewFragment {

    public static final String INTEGRATION_FRAGMENT_TITLE = "integration_fragment_title";
    public static final String INTEGRATION_FRAGMENT_URL = "integration_fragment_url";

    private static final String PAGE_NAME = "integrationwebview";

    public static IntegrationWebViewFragment newInstance(String title, String url) {
        IntegrationWebViewFragment fragment = new IntegrationWebViewFragment();
        Bundle arguments = new Bundle();
        arguments.putString(INTEGRATION_FRAGMENT_TITLE, title);
        arguments.putString(INTEGRATION_FRAGMENT_URL, url);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public String getIntegrationUrl() {
        return getArguments().getString(INTEGRATION_FRAGMENT_URL);
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    public void onResume() {
        super.onResume();
        ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getArguments().getString(INTEGRATION_FRAGMENT_TITLE)));
    }

    @Override
    public boolean isNeedTitles() {
        return true;
    }
}

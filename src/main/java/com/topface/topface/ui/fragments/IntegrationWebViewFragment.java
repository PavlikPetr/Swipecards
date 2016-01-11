package com.topface.topface.ui.fragments;

import android.os.Bundle;

/**
 * Created by ppetr on 10.01.16.
 * Фрагмент для загрузки и отображения web страницы интегратора
 */
public class IntegrationWebViewFragment extends WebViewFragment {

    public static final String INTEGRATION_FRAGMENT_TITLE = "integration_fragment_title";
    public static final String INTEGRATION_FRAGMENT_URL = "integration_fragment_url";

    public static IntegrationWebViewFragment newInstance(String title, String url) {
        IntegrationWebViewFragment fragment = new IntegrationWebViewFragment();
        Bundle arguments = new Bundle();
        arguments.putString(INTEGRATION_FRAGMENT_TITLE, title);
        arguments.putString(INTEGRATION_FRAGMENT_URL, url);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    String getIntegrationUrl() {
        Bundle args = getArguments();
        return args.getString(INTEGRATION_FRAGMENT_URL);
    }

    @Override
    protected String getTitle() {
        Bundle args = getArguments();
        return args.getString(INTEGRATION_FRAGMENT_TITLE);
    }

    @Override
    boolean isNeedTitles() {
        return true;
    }
}

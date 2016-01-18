package com.topface.topface.ui;

import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.ui.fragments.EditorBannersFragment;

public class EditorBannersActivity extends CheckAuthActivity<EditorBannersFragment> {

    // Id для админки начиная со 101
    public static final int INTENT_EDITOR_BANNERS = 101;

    public static Intent getEditorBannersIntent() {
        Intent intent = new Intent(App.getContext(), EditorBannersActivity.class);
        intent.putExtra(App.INTENT_REQUEST_KEY, INTENT_EDITOR_BANNERS);
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return EditorBannersFragment.class.getSimpleName();
    }

    @Override
    protected EditorBannersFragment createFragment() {
        return new EditorBannersFragment();
    }
}

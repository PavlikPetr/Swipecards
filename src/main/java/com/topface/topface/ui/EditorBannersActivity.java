package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;

public class EditorBannersActivity extends CheckAuthActivity {

    // Id для админки начиная со 101
    public static final int INTENT_EDITOR_BANNERS = 101;

    public static Intent getEditorBannersIntent() {
        Intent intent = new Intent(App.getContext(), EditorBannersActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_EDITOR_BANNERS);
        return intent;
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ac_editor_banners);
    }
}

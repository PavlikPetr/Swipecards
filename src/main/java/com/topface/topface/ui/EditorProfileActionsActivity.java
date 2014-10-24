package com.topface.topface.ui;

import android.content.Intent;

import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;

public class EditorProfileActionsActivity extends SingleFragmentActivity {
    public static final int INTENT_EDITOR_PROFILE_ACTIONS = 9;

    public static Intent createIntent(int userId) {
        Intent intent = new Intent(App.getContext(), EditorProfileActionsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_EDITOR_PROFILE_ACTIONS);
        intent.putExtra(EditorProfileActionsFragment.USERID, userId);
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return EditorProfileActionsFragment.class.getSimpleName();
    }

    @Override
    protected EditorProfileActionsFragment createFragment() {
        return new EditorProfileActionsFragment();
    }
}

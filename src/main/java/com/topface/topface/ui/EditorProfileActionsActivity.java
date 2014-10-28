package com.topface.topface.ui;

import android.content.Intent;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.Static;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;

import org.json.JSONException;

public class EditorProfileActionsActivity extends SingleFragmentActivity {
    public static final int INTENT_EDITOR_PROFILE_ACTIONS = 9;

    public static Intent createIntent(int profileId, IApiResponse response) {
        Intent intent = new Intent(App.getContext(), EditorProfileActionsActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_EDITOR_PROFILE_ACTIONS);
        intent.putExtra(EditorProfileActionsFragment.USERID, profileId);
        try {
            intent.putExtra(EditorProfileActionsFragment.PROFILE_RESPONSE, response.toJson().toString());
        } catch (JSONException e) {
            Debug.error("Can't get profile data from json", e);
        }
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

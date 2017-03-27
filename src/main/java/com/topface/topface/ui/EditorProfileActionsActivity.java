package com.topface.topface.ui;

import android.content.Intent;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;

public class EditorProfileActionsActivity extends SingleFragmentActivity<EditorProfileActionsFragment, AcFragmentFrameBinding> {
    public static final int INTENT_EDITOR_PROFILE_ACTIONS = 9;

    public static Intent createIntent(int profileId, IApiResponse response) {
        Intent intent = new Intent(App.getContext(), EditorProfileActionsActivity.class);
        intent.putExtra(App.INTENT_REQUEST_KEY, INTENT_EDITOR_PROFILE_ACTIONS);
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

    @NotNull
    @Override
    public ToolbarViewBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbarInclude;
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }
}

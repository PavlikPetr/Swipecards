package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.topface.statistics.android.Slices;
import com.topface.statistics.generated.NonClassifiedStatisticsGeneratedStatistics;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Photo;
import com.topface.topface.databinding.AcFragmentFrameBinding;
import com.topface.topface.databinding.ToolbarViewBinding;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.fragments.EditorProfileActionsFragment;
import com.topface.topface.ui.fragments.profile.AbstractProfileFragment;
import com.topface.topface.ui.fragments.profile.UserProfileFragment;
import com.topface.topface.ui.views.toolbar.toolbar_custom_view.CustomToolbarViewModel;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.ui.views.toolbar.view_models.BaseToolbarViewModel;
import com.topface.topface.ui.views.toolbar.view_models.CustomTitleSubTitleToolbarViewModel;

import org.jetbrains.annotations.NotNull;

public class UserProfileActivity extends CheckAuthActivity<UserProfileFragment, AcFragmentFrameBinding> {

    public static final int INTENT_USER_PROFILE = 6;
    public static final String FROM = "from";
    public static final String INTENT_HIDE_CHAT_IN_OVERFLOw_MENU = "HiDeChAtInOvErFlOw";

    public static Intent createIntent(ApiResponse response, Photo photo, int userId, String itemId,
                                      boolean isChatAvailable, boolean isAddToFavoritesAvailable,
                                      String nameAndAge, String city, String from) {
        Intent intent = new Intent(App.getContext(), UserProfileActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_NAME_AND_AGE, nameAndAge);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, city);
        if (response != null) {
            intent.putExtra(EditorProfileActionsFragment.PROFILE_RESPONSE, response.toJson().toString());
        }
        intent.putExtra(AbstractProfileFragment.INTENT_UID, userId);
        intent.putExtra(AbstractProfileFragment.INTENT_IS_CHAT_AVAILABLE, isChatAvailable);
        intent.putExtra(AbstractProfileFragment.INTENT_IS_ADD_TO_FAVORITS_AVAILABLE, isAddToFavoritesAvailable);
        if (!TextUtils.isEmpty(itemId)) {
            intent.putExtra(AbstractProfileFragment.INTENT_ITEM_ID, itemId);
        }
        if (photo != null) {
            intent.putExtra(ChatFragment.INTENT_AVATAR, photo);
        }
        intent.putExtra(FROM, from);
        intent.putExtra(App.INTENT_REQUEST_KEY, INTENT_USER_PROFILE);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(FROM)) {
            NonClassifiedStatisticsGeneratedStatistics.sendNow_PROFILE_OPEN(new Slices().putSlice("plc", intent.getStringExtra(FROM)));
        }
    }

    @NotNull
    @Override
    public ToolbarViewBinding getToolbarBinding(@NotNull AcFragmentFrameBinding binding) {
        return binding.toolbarInclude;
    }

    @NotNull
    @Override
    protected BaseToolbarViewModel generateToolbarViewModel(@NotNull ToolbarViewBinding toolbar) {
        return new CustomTitleSubTitleToolbarViewModel(toolbar, this);
    }

    @Override
    public void setToolbarSettings(@NotNull ToolbarSettingsData settings) {
        if (getToolbarViewModel() instanceof CustomTitleSubTitleToolbarViewModel) {
            CustomToolbarViewModel customViewModel = ((CustomTitleSubTitleToolbarViewModel) getToolbarViewModel()).getExtraViewModel();
            customViewModel.getTitleVisibility().set(TextUtils.isEmpty(settings.getTitle()) ? View.GONE : View.VISIBLE);
            customViewModel.getSubTitleVisibility().set(TextUtils.isEmpty(settings.getSubtitle()) ? View.GONE : View.VISIBLE);
            Boolean isOnline = settings.isOnline();
            customViewModel.isOnline().set(isOnline != null && isOnline);
            if (settings.getTitle() != null) {
                customViewModel.getTitle().set(settings.getTitle());
            }
            if (settings.getSubtitle() != null) {
                customViewModel.getSubTitle().set(settings.getSubtitle());
            }
        }
    }

    @Override
    public int getLayout() {
        return R.layout.ac_fragment_frame;
    }

    @Override
    protected String getFragmentTag() {
        return UserProfileFragment.class.getSimpleName();
    }

    @Override
    protected UserProfileFragment createFragment() {
        return new UserProfileFragment();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public int getTabLayoutResId() {
        return R.id.toolbarInternalTabs;
    }
}

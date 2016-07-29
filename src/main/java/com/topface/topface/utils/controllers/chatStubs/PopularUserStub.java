package com.topface.topface.utils.controllers.chatStubs;

import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewStub;

import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.databinding.PopularUserBlockerBinding;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ppavlik on 26.07.16.
 * Draw view for popular user lock
 */

public class PopularUserStub extends BaseChatStub<PopularUserBlockerBinding, PopularUserStubViewModel> {

    private History mHistory;
    private String mPhotoUrl;

    public PopularUserStub(@NotNull ViewStub stub, @NotNull History msg, @NotNull String photoUrl, @NotNull View.OnClickListener onClick) {
        super(stub);
        mHistory = msg;
        mPhotoUrl = photoUrl;
        initViews();
        PopularUserBlockerBinding binding = getBinding();
        if (binding != null) {
            binding.setClick(onClick);
        }
    }

    @Override
    @LayoutRes
    int getViewLayout() {
        return R.layout.popular_user_blocker;
    }

    @Override
    @NotNull
    PopularUserStubViewModel createViewModel(PopularUserBlockerBinding binding) {
        return new PopularUserStubViewModel(binding, mHistory, mPhotoUrl);
    }

    public boolean updateData(@NotNull History msg, @NotNull String photoUrl) {
        PopularUserStubViewModel model = getViewModel();
        if (model != null) {
            model.setData(msg, photoUrl);
            return true;
        }
        return false;
    }
}

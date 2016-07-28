package com.topface.topface.utils.controllers.chatStubs;

import android.support.annotation.LayoutRes;
import android.view.ViewStub;

import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.data.Photo;
import com.topface.topface.databinding.MutualSympathyStubBinding;

import org.jetbrains.annotations.NotNull;

/**
 * Created by ppavlik on 26.07.16.
 * Draw view for popular user lock
 */

public class MutualSympathyStub extends BaseChatStub<MutualSympathyStubBinding, MutualSympathyStubViewModel> {

    private History mHistory;
    private Photo mPhoto;

    public MutualSympathyStub(@NotNull ViewStub stub, @NotNull History msg, @NotNull Photo photo) {
        super(stub);
        mHistory = msg;
        mPhoto = photo;
        initViews();
    }

    @Override
    @LayoutRes
    int getViewLayout() {
        return R.layout.mutual_sympathy_stub;
    }

    @Override
    @NotNull
    MutualSympathyStubViewModel createViewModel(MutualSympathyStubBinding binding) {
        return new MutualSympathyStubViewModel(binding, mHistory, mPhoto);
    }

    public boolean updateData(@NotNull History msg, @NotNull Photo photo) {
        MutualSympathyStubViewModel model = getViewModel();
        if (model != null) {
            model.setData(msg, photo);
            return true;
        }
        return false;
    }

}

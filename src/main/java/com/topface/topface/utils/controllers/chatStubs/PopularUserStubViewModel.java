package com.topface.topface.utils.controllers.chatStubs;

import com.topface.topface.data.History;
import com.topface.topface.data.Photo;
import com.topface.topface.databinding.PopularUserBlockerBinding;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;

public class PopularUserStubViewModel extends BaseViewModel<PopularUserBlockerBinding> {

    public PopularUserStubViewModel(@NotNull PopularUserBlockerBinding binding, @NotNull History msg, @NotNull Photo photo) {
        super(binding);
        setData(msg, photo);
    }

    @SuppressWarnings("ConstantConditions")
    public void setData(@NotNull History msg, @NotNull Photo photo) {
        PopularUserBlockerBinding binding = getBinding();
        binding.popularUserAvatar.setPhoto(photo);
        if (msg != null) {
            binding.popularUserLockText.setText(msg.blockText);
        }
    }
}

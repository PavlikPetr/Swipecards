package com.topface.topface.utils.controllers.chatStubs;

import android.content.res.Resources;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.data.Photo;
import com.topface.topface.databinding.MutualSympathyStubBinding;
import com.topface.topface.utils.Utils;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;

public class MutualSympathyStubViewModel extends BaseViewModel<MutualSympathyStubBinding> {

    private static final double ANGLE = 40;

    public MutualSympathyStubViewModel(@NotNull MutualSympathyStubBinding binding, @NotNull History msg, @NotNull Photo photo) {
        super(binding);
        setData(msg, photo);
    }

    @SuppressWarnings("ConstantConditions")
    public void setData(@NotNull History msg, @NotNull Photo photo) {
        MutualSympathyStubBinding binding = getBinding();
        binding.mutualSympathyAvatar.setPhoto(photo);
        if (msg != null) {
            binding.mutualSympathyMsg.setText(msg.blockText);
            binding.mutualSympathyDate.setText(msg.createdFormatted);
        }
        binding.mutualSympathyIc.setRemoteSrc(Utils.getLocalResUrl(R.drawable.ic_mutuality));
    }

    public final float calculatePaddingBottom() {
        Resources res = App.get().getResources();
        float radius = res.getDimension(R.dimen.mutual_sympathy_stub_avatar_size) / 2;
        float iconSize = res.getDimension(R.dimen.mutual_sympathy_icon_size);
        double pointX = radius + radius * Math.sin(Math.toRadians(ANGLE));
        return (float) (radius * 2 - pointX - iconSize / 2);
    }

    public final float calculatePaddingRight() {
        Resources res = App.get().getResources();
        float radius = res.getDimension(R.dimen.mutual_sympathy_stub_avatar_size) / 2;
        float iconSize = res.getDimension(R.dimen.mutual_sympathy_icon_size);
        double pointY = radius + radius * Math.cos(Math.toRadians(ANGLE));
        return (float) (radius * 2 - pointY - iconSize / 2);
    }
}

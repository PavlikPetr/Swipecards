package com.topface.topface.utils.controllers.chatStubs;

import android.content.res.Resources;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.History;
import com.topface.topface.databinding.MutualSympathyStubBinding;
import com.topface.topface.utils.Utils;
import com.topface.topface.viewModels.BaseViewModel;

import org.jetbrains.annotations.NotNull;

public class MutualSympathyStubViewModel extends BaseViewModel<MutualSympathyStubBinding> {

    private static final double ANGLE = 40;

    public MutualSympathyStubViewModel(@NotNull MutualSympathyStubBinding binding, @NotNull History msg, @NotNull String photoUrl) {
        super(binding);
        setData(msg, photoUrl);
    }

    @SuppressWarnings("ConstantConditions")
    public void setData(@NotNull History msg, @NotNull String photoUrl) {
        MutualSympathyStubBinding binding = getBinding();
        binding.mutualSympathyAvatar.setRemoteSrc(photoUrl);
        if (msg != null) {
            binding.mutualSympathyMsg.setText(msg.blockText);
            binding.mutualSympathyDate.setText(msg.createdFormatted);
        }
        binding.mutualSympathyIc.setRemoteSrc(Utils.getLocalResUrl(R.drawable.ic_mutuality));
    }

    public final float calculatePaddingBottom() {
        return getPadding(Math.sin(Math.toRadians(ANGLE)));
    }

    public final float calculatePaddingRight() {
        return getPadding(Math.cos(Math.toRadians(ANGLE)));
    }

    private float getPadding(double val) {
        Resources res = App.get().getResources();
        float radius = res.getDimension(R.dimen.mutual_sympathy_stub_avatar_size) / 2;
        float iconSize = res.getDimension(R.dimen.mutual_sympathy_icon_size);
        double pointX = radius + radius * val;
        return (float) (radius * 2 - pointX - iconSize / 2);
    }
}

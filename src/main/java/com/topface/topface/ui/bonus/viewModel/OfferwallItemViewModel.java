package com.topface.topface.ui.bonus.viewModel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import com.topface.topface.App;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.utils.Utils;

public class OfferwallItemViewModel {
    public final ObservableField<String> title = new ObservableField<>(Utils.EMPTY);
    public final ObservableField<String> description = new ObservableField<>(Utils.EMPTY);
    public final ObservableInt reward = new ObservableInt(0);
    public final ObservableField<String> iconUrl = new ObservableField<>(null);
    public final ObservableField<String> buttonText = new ObservableField<>(Utils.EMPTY);

    private static final String BUTTON_TEMPLATE = "+%d";

    public OfferwallItemViewModel(IOfferwallBaseModel data) {
        title.set(data.getTitle());
        description.set(data.getDescription());
        reward.set(data.getRewardValue());
        iconUrl.set(data.getIconUrl());
        buttonText.set(String.format(App.getCurrentLocale(), BUTTON_TEMPLATE, data.getRewardValue()));
    }
}
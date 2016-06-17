package com.topface.topface.ui.bonus.viewModel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;

import com.topface.topface.utils.Utils;

public class OfferwallItemViewModel {
    public final ObservableField<String> title = new ObservableField<>(Utils.EMPTY);
    public final ObservableField<String> description = new ObservableField<>(Utils.EMPTY);
    public final ObservableInt reward = new ObservableInt(0);
    public final ObservableField<String> iconUrl = new ObservableField<>(null);

    public OfferwallItemViewModel(){

    }
}
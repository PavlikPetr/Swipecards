package com.topface.topface.ui.bonus.viewModel;

import android.databinding.ObservableField;
import android.databinding.ObservableInt;
import android.view.View;

import com.topface.topface.utils.Utils;

public class BonusFragmentViewModel {
    public final ObservableInt emptyViewVisibility = new ObservableInt(View.GONE);
    public final ObservableField<String> emptyViewText = new ObservableField<>(Utils.EMPTY);
    public final ObservableInt emptyViewIcon = new ObservableInt(0);
    public final ObservableInt offersVisibility = new ObservableInt(View.GONE);
}

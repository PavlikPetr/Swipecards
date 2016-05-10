package com.topface.topface.viewModels;

import android.databinding.ObservableField;

import com.topface.topface.data.leftMenu.LeftMenuHeaderData;
import com.topface.topface.utils.Utils;

/**
 * Created by ppavlik on 05.05.16.
 */
public class LeftMenuHeaderViewModel {

    public ObservableField<String> text = new ObservableField<>(Utils.EMPTY);

    public LeftMenuHeaderViewModel(LeftMenuHeaderData data) {
        text.set(data.text);
    }
}

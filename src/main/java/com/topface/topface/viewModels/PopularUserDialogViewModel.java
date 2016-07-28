package com.topface.topface.viewModels;

import android.databinding.ObservableField;
import android.os.Bundle;
import android.view.View;

import com.topface.topface.databinding.PopularUserDialogBinding;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;


public class PopularUserDialogViewModel extends BaseViewModel<PopularUserDialogBinding> {

    public static final String DIALOG_TITLE_ARG = "DIALOG_TITLE_ARG";
    public static final String BLOCK_TEXT_ARG = "BLOCK_TEXT_ARG";

    public final ObservableField<String> title = new ObservableField<>();
    public final ObservableField<String> message = new ObservableField<>();

    public PopularUserDialogViewModel(@NotNull PopularUserDialogBinding binding, Bundle bundle, @NotNull View.OnClickListener onBtnClick) {
        super(binding, bundle);
        if (bundle != null) {
            title.set(bundle.containsKey(DIALOG_TITLE_ARG) ? bundle.getString(DIALOG_TITLE_ARG) : Utils.EMPTY);
            message.set(bundle.containsKey(BLOCK_TEXT_ARG) ? bundle.getString(BLOCK_TEXT_ARG) : Utils.EMPTY);
        }
        getBinding().setClick(onBtnClick);
    }
}

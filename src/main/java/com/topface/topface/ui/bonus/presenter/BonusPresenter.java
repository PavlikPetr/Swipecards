package com.topface.topface.ui.bonus.presenter;

import com.topface.topface.ui.bonus.view.IBonusView;

import org.jetbrains.annotations.NotNull;

public class BonusPresenter implements IBonusPresenter {

    private IBonusView mIBonusView;

    @Override
    public void bindView(@NotNull IBonusView iBonusView) {
        mIBonusView = iBonusView;
    }

    @Override
    public void unbindView() {
        mIBonusView = null;
    }
}

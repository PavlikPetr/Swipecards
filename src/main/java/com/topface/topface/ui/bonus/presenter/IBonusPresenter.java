package com.topface.topface.ui.bonus.presenter;


import com.topface.topface.ui.bonus.view.IBonusView;

public interface IBonusPresenter {
    void bindView(IBonusView iBonusView);
    void unbindView();
    void loadOfferwalls();

}

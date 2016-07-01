package com.topface.topface.ui.bonus.presenter;


import com.topface.topface.ui.bonus.view.IBonusView;
import com.topface.topface.ui.bonus.viewModel.BonusFragmentViewModel;

public interface IBonusPresenter {
    void bindView(IBonusView iBonusView);

    void unbindView();

    void loadOfferwalls();

    void setViewModel(BonusFragmentViewModel viewModel);
}

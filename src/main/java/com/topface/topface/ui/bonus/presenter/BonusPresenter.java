package com.topface.topface.ui.bonus.presenter;

import com.fyber.Fyber;
import com.topface.topface.App;
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

    @Override
    public void loadOfferwalls() {
        if(App.get().getOptions().offerwallsSettings.isEnable()){
            if(mIBonusView!=null){
                mIBonusView.setProgressState(true);
                mIBonusView.showEmptyViewVisibility(false);
                mIBonusView.showOfferwallsVisibility(false);
            }
        }else{
            if(mIBonusView!=null){
                mIBonusView.showEmptyViewVisibility(true);
                mIBonusView.showOfferwallsVisibility(false);
                mIBonusView.setProgressState(false);
            }
        }
    }

    private void getOfferwalls(){

    }

    private void initFyber(){
        Fyber.with()
    }
}

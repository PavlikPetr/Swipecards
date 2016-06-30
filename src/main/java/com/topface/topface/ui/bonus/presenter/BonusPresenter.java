package com.topface.topface.ui.bonus.presenter;

import com.topface.topface.App;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.ui.bonus.view.IBonusView;
import com.topface.topface.ui.external_libs.offers.Fyber.FyberOffersRequest;
import com.topface.topface.ui.external_libs.offers.Fyber.FyberOffersResponse;
import com.topface.topface.ui.external_libs.offers.Fyber.FyberOfferwallModel;
import com.topface.topface.ui.external_libs.offers.IronSource.IronSourceOffersRequest;
import com.topface.topface.ui.external_libs.offers.IronSource.IronSourceOffersResponse;
import com.topface.topface.ui.external_libs.offers.IronSource.IronSourceOfferwallModel;
import com.topface.topface.utils.RxUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;


public class BonusPresenter implements IBonusPresenter {

    private IBonusView mIBonusView;
    private ArrayList<IOfferwallBaseModel> mOffers = new ArrayList<>();

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
        if (App.get().getOptions().offerwallsSettings.isEnable()) {
            if (mIBonusView != null) {
                mIBonusView.setProgressState(true);
                mIBonusView.showEmptyViewVisibility(false);
                mIBonusView.showOfferwallsVisibility(false);
            }
            getOfferwalls();
        } else {
            showEmptyView();
        }
    }

    private void getOfferwalls() {
        new FyberOffersRequest().getRequestObservable()
                .reduce(new ArrayList<IOfferwallBaseModel>(),
                        new Func2<ArrayList<IOfferwallBaseModel>, FyberOffersResponse, ArrayList<IOfferwallBaseModel>>() {
                            @Override
                            public ArrayList<IOfferwallBaseModel> call(ArrayList<IOfferwallBaseModel> iOfferwallBaseModels,
                                                                       FyberOffersResponse fyberOffersResponse) {
                                ArrayList<IOfferwallBaseModel> res = new ArrayList<>();
                                ArrayList<FyberOfferwallModel> initialList = fyberOffersResponse != null ?
                                        fyberOffersResponse.getOffers() : new ArrayList<FyberOfferwallModel>();
                                if (initialList != null) {
                                    for (FyberOfferwallModel item : initialList) {
                                        res.add(item);
                                    }
                                }
                                return res;
                            }
                        })
                .mergeWith(new IronSourceOffersRequest().getRequestObservable()
                        .reduce(new ArrayList<IOfferwallBaseModel>(),
                                new Func2<ArrayList<IOfferwallBaseModel>, IronSourceOffersResponse, ArrayList<IOfferwallBaseModel>>() {
                                    @Override
                                    public ArrayList<IOfferwallBaseModel> call(ArrayList<IOfferwallBaseModel> iOfferwallBaseModels,
                                                                               IronSourceOffersResponse ironSourceOffersResponse) {
                                        ArrayList<IOfferwallBaseModel> res = new ArrayList<>();
                                        ArrayList<IronSourceOfferwallModel> initialList = ironSourceOffersResponse != null
                                                && ironSourceOffersResponse.getResponse() != null
                                                ? ironSourceOffersResponse.getResponse().getOffers()
                                                : new ArrayList<IronSourceOfferwallModel>();
                                        for (IronSourceOfferwallModel item : initialList) {
                                            res.add(item);
                                        }
                                        return res;
                                    }
                                }))
                .filter(new Func1<ArrayList<IOfferwallBaseModel>, Boolean>() {
                    @Override
                    public Boolean call(ArrayList<IOfferwallBaseModel> iOfferwallBaseModels) {
                        return !iOfferwallBaseModels.isEmpty();
                    }
                })
                .compose(RxUtils.<ArrayList<IOfferwallBaseModel>>applySchedulers())
                .subscribe(new Action1<ArrayList<IOfferwallBaseModel>>() {
                    @Override
                    public void call(ArrayList<IOfferwallBaseModel> iOfferwallBaseModels) {
                        mOffers.addAll(iOfferwallBaseModels);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showEmptyView();
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        showOffers();
                    }
                });
    }

    private void showEmptyView() {
        if (mIBonusView != null) {
            mIBonusView.showEmptyViewVisibility(true);
            mIBonusView.showOfferwallsVisibility(false);
            mIBonusView.setProgressState(false);
        }
    }

    private void showOffers() {
        if (mOffers.isEmpty()) {
            showEmptyView();
        } else {
            if (mIBonusView != null) {
                mIBonusView.showOffers(mOffers);
                mIBonusView.showEmptyViewVisibility(false);
                mIBonusView.showOfferwallsVisibility(true);
                mIBonusView.setProgressState(false);
            }
        }
    }
}

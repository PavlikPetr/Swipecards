package com.topface.topface.ui.bonus.presenter;

import android.view.View;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.state.EventBus;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.ui.bonus.models.OfferwallsSettings;
import com.topface.topface.ui.bonus.view.IBonusView;
import com.topface.topface.ui.bonus.viewModel.BonusFragmentViewModel;
import com.topface.topface.ui.external_libs.offers.OffersModels;
import com.topface.topface.ui.external_libs.offers.OffersUtils;
import com.topface.topface.utils.ListUtils;
import com.topface.topface.utils.RxUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

public class BonusPresenter implements IBonusPresenter {

    private static final int OFFERS_DELAY = 500; //увеличиваем время показа лоадера

    @Inject
    EventBus mEvenBus;
    private IBonusView mIBonusView;
    private ArrayList<IOfferwallBaseModel> mOffers = new ArrayList<>();
    private BonusFragmentViewModel mViewModel;
    private Subscription mSubscription;

    public BonusPresenter() {
        App.get().inject(this);
    }

    @Override
    public void bindView(@NotNull IBonusView iBonusView) {
        mIBonusView = iBonusView;
    }

    @Override
    public void unbindView() {
        mIBonusView = null;
        RxUtils.safeUnsubscribe(mSubscription);
    }

    private void showLoader() {
        if (mViewModel != null) {
            mViewModel.offersVisibility.set(View.GONE);
            mViewModel.emptyViewVisibility.set(View.VISIBLE);
            mViewModel.emptyViewText.set(App.getContext().getResources().getString(R.string.offers_loading_text));
            mViewModel.emptyViewIcon.set(R.drawable.ill_positive);
        }
    }

    private void showEmptyView() {
        if (mViewModel != null) {
            mViewModel.offersVisibility.set(View.GONE);
            mViewModel.emptyViewVisibility.set(View.VISIBLE);
            mViewModel.emptyViewText.set(App.getContext().getResources().getString(R.string.offers_empty_text));
            mViewModel.emptyViewIcon.set(R.drawable.ill_sorrow);
        }
    }

    private void showOffersList() {
        if (mViewModel != null) {
            mViewModel.offersVisibility.set(View.VISIBLE);
            mViewModel.emptyViewVisibility.set(View.GONE);
        }
    }

    @Override
    public void loadOfferwalls() {
        if (App.get().getOptions().offerwallsSettings.isEnable()) {
            showLoader();
            getOfferwalls();
        } else {
            showEmptyView();
        }
    }

    @Override
    public void setViewModel(BonusFragmentViewModel viewModel) {
        mViewModel = viewModel;
    }

    private Observable<ArrayList<IOfferwallBaseModel>>[] getObservables() {
        OfferwallsSettings settings = App.get().getOptions().offerwallsSettings;
        List<String> offersType = settings.getOfferwallsList();
        List<Observable<ArrayList<IOfferwallBaseModel>>> observables = new ArrayList<>();
        Debug.showChunkedLogDebug("Offerwalls",
                "Available offerwalls type: " + (ListUtils.isNotEmpty(offersType)
                        ? Arrays.toString(offersType.toArray(new String[offersType.size()]))
                        : "no one"));
        for (Iterator<String> it = offersType.iterator(); it.hasNext(); ) {
            String item = it.next();
            Observable<ArrayList<IOfferwallBaseModel>> obs = OffersUtils.getOffersObservableByType(item);
            Debug.showChunkedLogDebug("Offerwalls", item + (obs != null ? " available" : " not available"));
            if (obs != null) {
                observables.add(obs);
            } else {
                it.remove();
            }
        }
        sendOfferwallOpenedBroadcast(offersType);
        return observables.toArray(new Observable[observables.size()]);
    }

    private void getOfferwalls() {
        mSubscription = Observable.merge(getObservables())
                .filter(new Func1<ArrayList<IOfferwallBaseModel>, Boolean>() {
                    @Override
                    public Boolean call(ArrayList<IOfferwallBaseModel> iOfferwallBaseModels) {
                        boolean isEmpty = iOfferwallBaseModels.isEmpty();
                        Debug.showChunkedLogDebug("Offerwalls",
                                (isEmpty
                                        ? "offerwalls list is empty"
                                        : iOfferwallBaseModels.get(0).getOfferwallsType() + " has " + iOfferwallBaseModels.size() + " offerwalls"));
                        return !isEmpty;
                    }
                }).collect(new Func0<ArrayList<IOfferwallBaseModel>>() {
                    @Override
                    public ArrayList<IOfferwallBaseModel> call() {
                        return new ArrayList<>();
                    }
                }, new Action2<ArrayList<IOfferwallBaseModel>, ArrayList<IOfferwallBaseModel>>() {
                    @Override
                    public void call(ArrayList<IOfferwallBaseModel> result, ArrayList<IOfferwallBaseModel> iOfferwallBaseModels) {
                        result.addAll(iOfferwallBaseModels);
                    }
                })
                .map(new Func1<ArrayList<IOfferwallBaseModel>, ArrayList<IOfferwallBaseModel>>() {
                    @Override
                    public ArrayList<IOfferwallBaseModel> call(ArrayList<IOfferwallBaseModel> iOfferwallBaseModels) {
                        Collections.sort(iOfferwallBaseModels, new Comparator<IOfferwallBaseModel>() {
                            @Override
                            public int compare(IOfferwallBaseModel lhs, IOfferwallBaseModel rhs) {
                                return Integer.valueOf(rhs.getRewardValue()).compareTo(lhs.getRewardValue());
                            }
                        });
                        return iOfferwallBaseModels;
                    }
                })
                .delay(OFFERS_DELAY, TimeUnit.MILLISECONDS)
                .compose(RxUtils.<ArrayList<IOfferwallBaseModel>>applySchedulers())
                .subscribe(new Observer<ArrayList<IOfferwallBaseModel>>() {
                    @Override
                    public void onCompleted() {
                        showOffers();
                    }

                    @Override
                    public void onError(Throwable e) {
                        showEmptyView();
                    }

                    @Override
                    public void onNext(ArrayList<IOfferwallBaseModel> iOfferwallBaseModels) {
                        mOffers.addAll(iOfferwallBaseModels);
                    }
                });
    }

    private void showOffers() {
        boolean isEmpty = mOffers.isEmpty();
        Debug.showChunkedLogDebug("Offerwalls", isEmpty ? "nothing to show" : "try show " + mOffers.size() + " offers");
        if (isEmpty) {
            showEmptyView();
        } else {
            if (mIBonusView != null) {
                mIBonusView.showOffers(mOffers);
            }
            showOffersList();
        }
    }

    private void sendOfferwallOpenedBroadcast(List<String> availableOfferwalls) {
        if (ListUtils.isNotEmpty(availableOfferwalls)) {
            mEvenBus.setData(new OffersModels.OfferOpened(availableOfferwalls.toArray(new String[availableOfferwalls.size()])));
        }
    }
}

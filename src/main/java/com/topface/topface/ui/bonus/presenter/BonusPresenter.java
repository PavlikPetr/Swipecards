package com.topface.topface.ui.bonus.presenter;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.state.EventBus;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.ui.bonus.models.OfferwallsSettings;
import com.topface.topface.ui.bonus.view.IBonusView;
import com.topface.topface.ui.bonus.viewModel.BonusFragmentViewModel;
import com.topface.topface.ui.external_libs.offers.OffersUtils;
import com.topface.topface.utils.ListUtils;
import com.topface.topface.utils.RxUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;

import static com.topface.topface.ui.bonus.view.BonusFragment.OFFERWALL_NAME;
import static com.topface.topface.ui.bonus.view.BonusFragment.OFFERWALL_OPENED;

public class BonusPresenter implements IBonusPresenter {

    private static final int OFFERS_DELAY = 500; //увеличиваем время показа лоадера

    @Inject
    EventBus mEvenBus;

    private IBonusView mIBonusView;
    private ArrayList<IOfferwallBaseModel> mOffers = new ArrayList<>();
    private BonusFragmentViewModel mViewModel;
    private Subscription mSubscription;

    public BonusPresenter(){
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
        for (String s : offersType) {
            Observable<ArrayList<IOfferwallBaseModel>> obs = OffersUtils.getOffersObservableByType(s);
            if (obs != null) {
                observables.add(obs);
            } else {
                offersType.remove(s);
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
                        return !iOfferwallBaseModels.isEmpty();
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
        if (mOffers.isEmpty()) {
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
//            mEvenBus.setData(new OffersModels.OfferOpened(availableOfferwalls.toArray()));
            Intent intent = new Intent(OFFERWALL_OPENED);
            intent.putStringArrayListExtra(OFFERWALL_NAME, new ArrayList<>(availableOfferwalls));
            LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
        }
    }
}

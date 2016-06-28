package com.topface.topface.state;

import com.topface.framework.utils.Debug;
import com.topface.topface.utils.RxUtils;
import com.topface.topface.utils.controllers.startactions.IStartAction;
import com.topface.topface.utils.controllers.startactions.OnNextActionListener;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * Рассадник попапов, хранит в себе все очереди, которые были запущены на разных экрнах
 * Created by tiberal on 02.03.16.
 */
public class PopupHive {

    public static final int AC_PRIORITY_HIGH = 3;
    public static final int AC_PRIORITY_NORMAL = 2;
    public static final int AC_PRIORITY_LOW = 1;

    private ConcurrentHashMap<Class, PopupSequencedHolder> mSequenceHolderMap = new ConcurrentHashMap<>();
    private Subscription mSequencedSubscription;
    private boolean mAlreadyShown = false;

    public void execPopupRush(Class clazz, boolean isStateChanged) {
        final PopupSequencedHolder holder = mSequenceHolderMap.get(clazz);
        if (holder != null && (!holder.mIsExecuted || isStateChanged) && !mAlreadyShown) {
            Debug.log("PopupHive start rush");
            mSequencedSubscription = holder.mActionObservable
                    .map(new Func1<IStartAction, IStartAction>() {
                        @Override
                        public IStartAction call(IStartAction iStartAction) {
                            iStartAction.callInBackground();
                            return iStartAction;
                        }
                    })
                    .subscribe(new Action1<IStartAction>() {
                        @Override
                        public void call(IStartAction iStartAction) {
                            Debug.log("PopupHive " + iStartAction.getActionName() + " started " + "isApplicable " + iStartAction.isApplicable());
                            iStartAction.callOnUi();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Debug.log("PopupHive error " + throwable);
                            throwable.printStackTrace();
                        }
                    }, new Action0() {
                        @Override
                        public void call() {
                            Debug.log("PopupHive OK ");
                            mAlreadyShown = true;
                        }
                    });
        }
    }

    public boolean isSequencedExecuted(Class clazz) {
        PopupSequencedHolder popupSequencedHolder = mSequenceHolderMap.get(clazz);
        return popupSequencedHolder != null && popupSequencedHolder.mIsExecuted;
    }

    public void releaseHive() {
        RxUtils.safeUnsubscribe(mSequencedSubscription);
    }

    public void registerPopupSequence(List<IStartAction> startActionList, Class aClass, boolean isNeedResetOldSequence) {
        if (isNeedResetOldSequence) {
            mSequenceHolderMap.put(aClass, new PopupSequencedHolder(startActionList));
        }
        if (mSequenceHolderMap.containsKey(aClass)) {
            mSequenceHolderMap.put(aClass, mSequenceHolderMap.get(aClass).updateActionsList(startActionList));
        } else {
            mSequenceHolderMap.put(aClass, new PopupSequencedHolder(startActionList));
        }
    }

    public boolean containSequence(Class aClass) {
        return mSequenceHolderMap.containsKey(aClass);
    }

    public void clear() {
        mSequenceHolderMap.clear();
    }

    private static class PopupSequencedHolder {

        private int mLastShownPopupPosition = 0;
        private List<IStartAction> mStartActionList;
        private boolean mIsExecuted;
        private OnNextActionListener mListener = new OnNextActionListener() {
            @Override
            public void onNextAction() {
                mLastShownPopupPosition++;
                emmitAction();
            }
        };
        private Observable<IStartAction> mActionObservable;
        private Subscriber<? super IStartAction> mSubscriber;

        public PopupSequencedHolder(List<IStartAction> startActionList) {
            mStartActionList = startActionList;
            mActionObservable = createListenerObservable();
        }

        @NotNull
        public PopupSequencedHolder updateActionsList(List<IStartAction> newActionList) {
            mStartActionList = newActionList;
            return this;
        }

        @Nullable
        private IStartAction getApplicableAction() {
            IStartAction action;
            for (int i = mLastShownPopupPosition; i < mStartActionList.size(); i++) {
                action = mStartActionList.get(i);
                if (action.isApplicable()) {
                    mLastShownPopupPosition = i;
                    action.setStartActionCallback(mListener);
                    return action;
                }
            }
            return null;
        }

        private void emmitAction() {
            if (mSubscriber != null && !mSubscriber.isUnsubscribed()) {
                IStartAction action = getApplicableAction();
                if (action != null) {
                    mSubscriber.onNext(action);
                } else {
                    mSubscriber.onCompleted();
                }
            }
        }

        @NotNull
        private Observable<IStartAction> createListenerObservable() {
            return Observable.create(new Observable.OnSubscribe<IStartAction>() {
                @Override
                public void call(Subscriber<? super IStartAction> subscriber) {
                    mSubscriber = subscriber;
                    mIsExecuted = true;
                    emmitAction();
                }
            });
        }
    }
}

package com.topface.topface.requests.transport.scruffy;

import com.koushikdutta.async.http.WebSocket;
import com.topface.framework.utils.Debug;
import com.topface.topface.utils.RxUtils;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * Прослоечка для пинга сервера
 * Created by tiberal on 28.05.16.
 */

public class PingPonger extends Subscriber<Long> implements WebSocket.PongCallback {

    public static final long PING_TIME = 30000;
    private static final long MAX_ATTEMPTS_COUNT = 3;

    private final IRequestManagerInteractor mRequestMangerInteractor;
    private boolean mIsPingSended = false;
    private byte mAttemptsСounter = 0;
    private Subscription mPingPongerSubscription;

    public PingPonger(IRequestManagerInteractor requestManagerInteractor) {
        mRequestMangerInteractor = requestManagerInteractor;
        mPingPongerSubscription = Observable.interval(0, PING_TIME, TimeUnit.MILLISECONDS).subscribe(this);
    }

    @Override
    public void onPongReceived(String s) {
        mIsPingSended = false;
        mAttemptsСounter = 0;
        mRequestMangerInteractor.pong();
    }

    @Override
    public void onCompleted() {
        RxUtils.safeUnsubscribe(mPingPongerSubscription);
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }

    @Override
    public void onNext(Long aLong) {
        if (mAttemptsСounter >= MAX_ATTEMPTS_COUNT) {
            mAttemptsСounter = 0;
            mRequestMangerInteractor.reconnect();
            return;
        }
        if (!mIsPingSended) {
            mIsPingSended = true;
            mRequestMangerInteractor.ping();
        } else {
            Debug.log("Scruffy:: PONG FAIL " + mAttemptsСounter);
            mAttemptsСounter++;
        }
    }

    public interface IRequestManagerInteractor {

        void ping();

        void pong();

        void reconnect();
    }

}

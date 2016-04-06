package com.topface.topface.utils.social;

import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.ok.android.sdk.Odnoklassniki;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by ppavlik on 05.04.16.
 * parrent for all request to OK API
 */
public abstract class OkRequest {

    public List<OkRequestListener> mListeners;
    private Odnoklassniki mOdnoklassniki;

    public OkRequest(@NotNull Odnoklassniki ok) {
        mOdnoklassniki = ok;
    }

    @NotNull
    private List<OkRequestListener> getListeners() {
        if (mListeners == null) {
            mListeners = new ArrayList<>();
        }
        return mListeners;
    }

    public OkRequest setUserListener(OkRequestListener listener) {
        getListeners().add(listener);
        return this;
    }

    abstract protected String getRequest(Odnoklassniki ok) throws IOException;

    abstract protected void getObservable(Observable<String> observable);

    private Observable<String> prepareObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String res = Utils.EMPTY;
                try {
                    res = getRequest(mOdnoklassniki);
                } catch (IOException e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
                subscriber.onNext(res);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                ;
    }

    public void exec() {
        getObservable(prepareObservable());
    }

    public void callFail() {
        for (OkRequestListener listener : getListeners()) {
            if (listener != null) {
                listener.onFail();
            }
        }
        onDestroy();
    }

    public void callSuccess(@Nullable OkUserData data) {
        for (OkRequestListener listener : getListeners()) {
            if (listener != null) {
                listener.onSuccess(data);
            }
        }
        onDestroy();
    }

    private void onDestroy() {
        getListeners().clear();
    }

    public interface OkRequestListener {

        void onSuccess(@Nullable OkUserData data);

        void onFail();

    }
}

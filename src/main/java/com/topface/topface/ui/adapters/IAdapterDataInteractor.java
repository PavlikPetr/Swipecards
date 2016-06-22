package com.topface.topface.ui.adapters;

import android.os.Bundle;

import java.util.ArrayList;

import rx.Observable;

/**
 * Created by tiberal on 05.05.16.
 */
interface IAdapterDataInteractor<D> {

    void addData(ArrayList<D> data);

    ArrayList<D> getData();

    Observable<Bundle> getUpdaterObservable();
}

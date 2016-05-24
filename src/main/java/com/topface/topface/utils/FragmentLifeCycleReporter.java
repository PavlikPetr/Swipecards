package com.topface.topface.utils;

import com.topface.topface.data.FragmentLifreCycleData;

import static com.topface.topface.data.FragmentLifreCycleData.*;

/**
 * Created by ppavlik on 13.05.16.
 * LifeCycle reporter for activities/fragments
 */
public class FragmentLifeCycleReporter extends LifeCycleReporter<FragmentLifreCycleData> {
    private String mClassName;

    public FragmentLifeCycleReporter(String className) {
        mClassName = className;
    }

    public FragmentLifreCycleData getData(int state) {
        return new FragmentLifreCycleData(mClassName, state);
    }

    public void onDestroyView() {
        emitLifeCycle(getData(DESTROY_VIEW));
    }

    public void onAttach() {
        emitLifeCycle(getData(ATTACH));
    }

    public void onCreate() {
        emitLifeCycle(getData(CREATE));
    }

    public void onCreateView() {
        emitLifeCycle(getData(CREATE_VIEW));
    }

    public void onViewCreated() {
        emitLifeCycle(getData(VIEW_CREATED));
    }

    public void onStart() {
        emitLifeCycle(getData(START));
    }

    public void onResume() {
        emitLifeCycle(getData(RESUME));
    }

    public void onSaveInstanceState() {
        emitLifeCycle(getData(SAVE_INSTANCE_STATE));
    }

    public void onPause() {
        emitLifeCycle(getData(PAUSE));
    }

    public void onStop() {
        emitLifeCycle(getData(STOP));
    }

    public void onDestroy() {
        emitLifeCycle(getData(DESTROY));
    }

    public void onDetach() {
        emitLifeCycle(getData(DETACH));
    }
}

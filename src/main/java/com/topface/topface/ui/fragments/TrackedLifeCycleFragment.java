package com.topface.topface.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.utils.FragmentLifeCycleReporter;

/**
 * Created by ppavlik on 13.05.16.
 * Observe fragment lifecycle
 */
public class TrackedLifeCycleFragment extends Fragment {

    private FragmentLifeCycleReporter mLifeCycleReporter = new FragmentLifeCycleReporter(getClass().getName());

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        //
        mLifeCycleReporter.onDestroyView();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        //
        mLifeCycleReporter.onAttach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLifeCycleReporter.onCreate();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //
        mLifeCycleReporter.onCreateView();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //
        mLifeCycleReporter.onViewCreated();
    }

    @Override
    public void onStart() {
        super.onStart();
        mLifeCycleReporter.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mLifeCycleReporter.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //
        mLifeCycleReporter.onSaveInstanceState();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLifeCycleReporter.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mLifeCycleReporter.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLifeCycleReporter.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //
        mLifeCycleReporter.onDetach();
    }
}

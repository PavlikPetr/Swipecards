package com.topface.topface.ui.debug;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;

/**
 * Тестовый ырагмент для поиска утечек. Удобно юзать при поиске ликов во вьюпейджере
 * Created by tiberal on 27.05.16.
 */
public class EmptyFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.empty, container, false);
    }
}

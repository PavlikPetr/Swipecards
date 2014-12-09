package com.topface.topface.ui.adapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.ViewAnimator;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

public class ChatListAnimatedAdapter extends AnimationAdapter {
    private static final String TRANSLATION_Y = "translationY";

    public ChatListAnimatedAdapter(@NonNull BaseAdapter baseAdapter) {
        super(baseAdapter);
    }

    @Override
    public void setListViewWrapper(@NonNull ListViewWrapper listViewWrapper) {
        super.setListViewWrapper(listViewWrapper);
        ViewAnimator viewAnimator = getViewAnimator();
        viewAnimator.setAnimationDelayMillis(0);
        viewAnimator.setAnimationDurationMillis(100);
        viewAnimator.setInitialDelayMillis(0);
    }

    @NonNull
    @Override
    public Animator[] getAnimators(@NonNull ViewGroup viewGroup, @NonNull View view) {
        Animator animator = ObjectAnimator.ofFloat(view, TRANSLATION_Y, viewGroup.getMeasuredHeight() >> 1, 0);

        return new Animator[]{animator};
    }
}

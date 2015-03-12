package com.topface.topface.ui.adapters;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.topface.topface.App;

/**
 * Адовый костыль! Если в чате один итем или ни одного, нужно увелисить количество сообщения на 1,
 * для того, чтобы при перехде на новую строку в футере чата(input поле) анимация не отражалась снова.
 * Это происходит из-за перерисовки листа, и  неверного расчета условий анимации в методе
 * animateViewIfNecessary класса AnimationAdapter в библиотеке ListViewAnimation
 * Created by onikitin on 11.03.15.
 */
public class HackBaseAdapterDecorator extends BaseAdapter {

    private BaseAdapter mBaseAdapter;
    private View mGoneView;
    private boolean mIsRegistred;
    private static final String TRANSLATION_Y = "translationY";
    private boolean mNeedAnimate = true;


    public HackBaseAdapterDecorator(BaseAdapter baseAdapter) {
        mBaseAdapter = baseAdapter;
        if (mBaseAdapter.getCount() < 2) {
            mBaseAdapter.registerDataSetObserver(mDataSetObserver);
            mIsRegistred = true;
        }
    }

    //Обсервер для того, чтобы детектить добавление данных в декорируемый адаптер
    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }
    };

    //хакаем, только если в чате 1 итем
    private boolean needHack() {
        return mBaseAdapter.getCount() == 1;
    }

    //накручиваем позицию для исправления бага
    @Override
    public int getCount() {
        return needHack() ? mBaseAdapter.getCount() + 1 : mBaseAdapter.getCount();
    }

    @Override
    public Object getItem(int position) {
        return mBaseAdapter.getItem(position);
    }


    @Override
    public long getItemId(int position) {
        return mBaseAdapter.getItemId(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (needHack() && position == 1) {
            mGoneView = new View(App.getContext());
            return mGoneView;
        } else if (convertView == mGoneView) {
            convertView = null;
            if (mIsRegistred) {
                //отписываемся от оповещений о добавлении данных
                mIsRegistred = false;
                mBaseAdapter.unregisterDataSetObserver(mDataSetObserver);
            }
        }
        View view = mBaseAdapter.getView(position, convertView, parent);
        if (position == 1 && mBaseAdapter.getCount() == 2 || mNeedAnimate) {
            mNeedAnimate = false;
            Animator animator = ObjectAnimator.ofFloat(view, TRANSLATION_Y, parent.getMeasuredHeight() >> 1, 0)
                    .setDuration(100);
            animator.start();
        }
        return view;
    }

}

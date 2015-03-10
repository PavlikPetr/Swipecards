package com.topface.topface.ui.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.topface.topface.data.History;

/**
 * Адовый костыль! Если в чате один итем или ни одного, нужно увелисить количество сообщения на 1,
 * для того, чтобы при перехде на новую строку в футере чата(input поле) анимация не отражалась снова.
 * Это происходит из-за перерисовки листа, и  неверного расчета условий анимации в методе
 * animateViewIfNecessary класса AnimationAdapter в библиотеке ListViewAnimation
 * Created by onikitin on 06.03.15.
 */
public class HackChatListAdapter extends ChatListAdapter {

    private boolean mOverPosition = false;
    private View mGoneView;
    private static final String TRANSLATION_Y = "translationY";

    public HackChatListAdapter(Context context, FeedList<History> data, Updater updateCallback) {
        super(context, data, updateCallback);
    }

    @Override
    public History getItem(int position) {
        if (mOverPosition) {
            //уменьшаем на 1 позицию, чтобы соответствовать позиции выданной getCount
            position = position - 1;
            if (position < 0) {
                return new History();
            } else {
                return getData().get(position);
            }
        }
        return getData().get(getPosition(position));
    }

    @Override
    protected View getContentView(int position, View convertView, ViewGroup viewGroup) {
        View view = super.getContentView(position, convertView, viewGroup);
        History item = getItem(position);
        if (TextUtils.isEmpty(item.text)) {
            //прячем вьюху, чтобы не показывать пустой облачко сообщения
            view.setVisibility(View.GONE);
            mGoneView = view;
        }
        if (position == 1 && getData().size() == 2) {
            /*
            при отображении первого сообщения было добавлено две вьюхи, одна с контентом,
            вторая пустая(нужня для работы аниматора). Пустая вьюха была аимирована и скрыта.
            после добавления в нее контента, вьюху нужно анимировать заново.
            */
            Animator animator = ObjectAnimator.ofFloat(view, TRANSLATION_Y, viewGroup.getMeasuredHeight() >> 1, 0)
                    .setDuration(100);
            animator.start();
        }
        return view;
    }

    @Override
    public int getCount() {
        if (getData().size() == 1) {
            mOverPosition = true;
            return getData().size() + 1;

        } else {
            mOverPosition = false;
            return getData().size();
        }
    }

    @Override
    protected void addSentMessage(History item) {
        if (mOverPosition) {
            //Отображаем спрятанную в getView вьюху
            mGoneView.setVisibility(View.VISIBLE);
            mOverPosition = false;
        }
        super.addSentMessage(item);
    }

}

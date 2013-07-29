package com.topface.topface.ui.fragments.closing;

import android.support.v4.app.FragmentTransaction;
import com.topface.topface.data.FeedUser;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SkipAllClosedRequest;
import com.topface.topface.requests.handlers.SimpleApiHandler;
import com.topface.topface.ui.fragments.OnQuickMessageSentListener;
import com.topface.topface.ui.fragments.QuickMessageFragment;
import com.topface.topface.ui.fragments.ViewUsersListFragment;

/**
 * Базовый фрагмент экранов запираний
 */
abstract public class ClosingFragment extends ViewUsersListFragment<FeedUser> {

    public static final int CHAT_CLOSE_DELAY_MILLIS = 1500;

    public void showChat() {
        QuickMessageFragment fragment = QuickMessageFragment.newInstance(getCurrentUser().id, getChatListener());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(android.R.id.content, fragment, fragment.getClass().getName());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    protected void skipAllRequests(int type) {
        SkipAllClosedRequest skipAllRequest = new SkipAllClosedRequest(type, getActivity());
        skipAllRequest.callback(new SimpleApiHandler() {
            @Override
            public void always(ApiResponse response) {
                refreshActionBarTitles(getView());
            }
        });
        registerRequest(skipAllRequest);
        skipAllRequest.exec();
        onUsersProcessed();
    }

    private OnQuickMessageSentListener getChatListener() {
        return new OnQuickMessageSentListener() {
            @Override
            public void onMessageSent(String message, final QuickMessageFragment fragment) {
                //Закрываем чат с задержкой и переключаем пользователя
                ClosingFragment.this.getView().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        closeFragment(fragment);
                        showNextUser();
                    }
                }, CHAT_CLOSE_DELAY_MILLIS);
            }

            @Override
            public void onCancel(QuickMessageFragment fragment) {
                closeFragment(fragment);
            }

            private void closeFragment(QuickMessageFragment fragment) {
                FragmentTransaction transaction = ClosingFragment.this.getFragmentManager().beginTransaction();
                transaction.remove(fragment);
                transaction.commit();
            }
        };
    }
}

package com.topface.topface;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.ui.views.SpriteTile;
import com.topface.topface.utils.Debug;

/**
 * Диалог, показываемый при ошибке отправки запроса и предлагающий его повторить
 */
public class RetryDialog extends AlertDialog {
    private BroadcastReceiver mReciever;
    private Context mContext;
    private ApiRequest mRequest;

    public RetryDialog(Context context, ApiRequest request) {
        super(context);
        mContext = context;
        mRequest = request;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retry_dialog_layout);

        ImageView satelite = (ImageView) findViewById(R.id.sat);
        Animation anim = new RotateAnimation(0f, 358f,Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(3000);
        anim.setInterpolator(new LinearInterpolator());
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                Debug.log("ANIMATION:: start");
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Debug.log("ANIMATION:: end");
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                Debug.log("ANIMATION:: repeat");
            }
        });
//        satelite.setAnimation(anim);
        satelite.startAnimation(anim);

        mReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, 0) != ConnectionChangeReceiver.CONNECTION_OFFLINE) {
                    mRequest.exec();
                    cancel();
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReciever, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReciever);
    }
}

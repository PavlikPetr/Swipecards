package com.topface.topface;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.IApiRequest;

/**
 * Диалог, показываемый при ошибке отправки запроса и предлагающий его повторить
 */
public class RetryDialog extends AlertDialog {
    private String mMessage;
    private BroadcastReceiver mReciever;
    private Context mContext;
    private IApiRequest mRequest;

    private static boolean isShowing = false;

    public RetryDialog(Context context, IApiRequest request) {
        super(context);
        mContext = context;
        mRequest = request;
    }

    public RetryDialog(String message, Context context, IApiRequest request) {
        this(context, request);
        mMessage = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.retry_dialog_layout);
        ((TextView) findViewById(R.id.retryDialogMessage)).setText(mMessage);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ImageView satelite = (ImageView) findViewById(R.id.sat);
        Animation anim = new RotateAnimation(0f, 358f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);

        anim.setRepeatCount(Animation.INFINITE);
        anim.setDuration(3000);
        anim.setInterpolator(new LinearInterpolator());
        satelite.startAnimation(anim);

        mReciever = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int connectionType = intent.getIntExtra(ConnectionChangeReceiver.CONNECTION_TYPE, 0);

                if (ConnectionChangeReceiver.ConnectionType.valueOf(connectionType) != ConnectionChangeReceiver.ConnectionType.CONNECTION_OFFLINE) {
                    mRequest.exec();
                    if (isShowing()) {
                        try {
                            cancel();
                        } catch (Exception e) {
                            Debug.error(e);
                        }
                    }
                }
            }
        };
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReciever, new IntentFilter(RetryRequestReceiver.RETRY_INTENT));
    }

    @Override
    public void show() {
        if (!isShowing) {
            super.show();
            isShowing = true;
        }
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isShowing = false;
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mReciever);
    }
}

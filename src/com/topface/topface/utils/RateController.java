package com.topface.topface.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.topface.topface.R;
import com.topface.topface.billing.BuyingActivity;
import com.topface.topface.data.Rate;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.MessageRequest;
import com.topface.topface.requests.RateRequest;
import com.topface.topface.ui.NavigationActivity;

public class RateController {
    private Activity mContext;
    private Dialog mCommentDialog;
    private EditText mCommentText;
    private InputMethodManager mInputManager;
    private OnRateControllerListener mOnRateControllerListener;

    public interface OnRateControllerListener {
        public void successRate();

        public void failRate();
    }

    public RateController(final Activity context) {
        mContext = context;
        mCommentDialog = new Dialog(context);
        mCommentDialog.setTitle(R.string.chat_comment);
        mCommentDialog.setContentView(R.layout.popup_comment);
        mCommentDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mCommentDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if(NavigationActivity.mThis != null)
                ((NavigationActivity)context).onDialogCancel();
            }
        });
        mCommentText = (EditText) mCommentDialog.findViewById(R.id.etPopupComment);
        mInputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    public void onRate(final int userId, final int rate) {
        if (rate < 10) {
            sendRate(userId, rate);
            return;
        }
        if (rate == 10 && CacheProfile.money <= 0) {
            mContext.startActivity(new Intent(mContext, BuyingActivity.class));
            return;
        }
        ((Button) mCommentDialog.findViewById(R.id.btnPopupCommentSend)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = mCommentText.getText().toString();
                if (comment.equals(""))
                    return;

                MessageRequest messageRequest = new MessageRequest(mContext);
                messageRequest.message = comment;
                messageRequest.userid = userId;

                messageRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                        mContext.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                sendRate(userId, rate);
                                mCommentDialog.cancel();
                                mCommentText.setText("");
                                mInputManager.hideSoftInputFromWindow(mCommentText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
                            }
                        });

                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                        mCommentDialog.cancel();
                        sendRate(userId, rate);
                    }
                }).exec();


            }
        });
        mCommentDialog.show();
    }

    private void sendRate(final int userid, final int rate) {
        RateRequest doRate = new RateRequest(mContext);
        doRate.userid = userid;
        doRate.rate = rate;
        doRate.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Rate rate = Rate.parse(response);
                CacheProfile.power = rate.power;
                CacheProfile.money = rate.money;
                CacheProfile.average_rate = rate.average;
                if (mOnRateControllerListener != null) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOnRateControllerListener.successRate();
                        }
                    });
                }
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
                if (mOnRateControllerListener != null) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOnRateControllerListener.failRate();
                        }
                    });
                }
            }
        }).exec();
    }

    public void onRate(final int userId, final int rate, final int mutualId) {
        if (rate < 10) {
            sendRate(userId, rate, mutualId);
            return;
        }
        if (rate == 10 && CacheProfile.money <= 0) {
            mContext.startActivity(new Intent(mContext, BuyingActivity.class));
            return;
        }
        ((Button) mCommentDialog.findViewById(R.id.btnPopupCommentSend)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String comment = mCommentText.getText().toString();
                if (comment.equals(""))
                    return;

                MessageRequest messageRequest = new MessageRequest(mContext);
                messageRequest.message = comment;
                messageRequest.userid = userId;
                messageRequest.callback(new ApiHandler() {
                    @Override
                    public void success(ApiResponse response) {
                    }

                    @Override
                    public void fail(int codeError, ApiResponse response) {
                    }
                }).exec();


                sendRate(userId, rate);
                mCommentDialog.cancel();
                mCommentText.setText("");
                mInputManager.hideSoftInputFromWindow(mCommentText.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        mCommentDialog.show();
    }

    private void sendRate(final int userid, final int rate, final int mutualId) {
        RateRequest doRate = new RateRequest(mContext);
        doRate.userid = userid;
        doRate.rate = rate;
        doRate.mutualid = mutualId;
        doRate.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) {
                Rate rate = Rate.parse(response);
                CacheProfile.power = rate.power;
                CacheProfile.money = rate.money;
                CacheProfile.average_rate = rate.average;
                if (mOnRateControllerListener != null) {
                    mContext.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mOnRateControllerListener.successRate();
                        }
                    });
                }
            }

            @Override
            public void fail(int codeError, ApiResponse response) {
            }
        }).exec();
    }

    public void setOnRateControllerListener(OnRateControllerListener onRateControllerListener) {
        mOnRateControllerListener = onRateControllerListener;
    }
}

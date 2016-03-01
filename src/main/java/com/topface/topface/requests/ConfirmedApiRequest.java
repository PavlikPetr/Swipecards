package com.topface.topface.requests;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;

import com.topface.topface.App;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.ui.dialogs.ConfirmEmailDialog;
import com.topface.topface.utils.social.AuthToken;

/**
 * Это тип запроса к API, который разрешен только подтвержденным пользователям
 */
abstract public class ConfirmedApiRequest extends ApiRequest {
    public static final String CONFIGRM_EMAIL_DIALOG_TAG = "configrm_email_dialog_tag";
    private boolean mBlockUnconfirmed;
    private Context mContext;

    public ConfirmedApiRequest(Context context, boolean blockUnconfirmed) {
        super(context);
        mContext = context;
        mBlockUnconfirmed = blockUnconfirmed;
    }

    @Override
    public void exec() {
        String socialNet = AuthToken.getInstance().getSocialNet();
        if (!isTopfaceProfile(socialNet) || !isNeedBlock()) {
            super.exec();
        } else {
            showConfirmDialog(context);
            handleFail(ErrorCodes.UNCONFIRMED_LOGIN_ACTION, "Need confirm email");
        }
    }

    public static void showConfirmDialog(Context context) {
        if (context instanceof FragmentActivity) {
            ConfirmEmailDialog.newInstance().show(
                    ((FragmentActivity) context).getSupportFragmentManager(),
                    CONFIGRM_EMAIL_DIALOG_TAG
            );
        }
    }

    private boolean isNeedBlock() {
        return mBlockUnconfirmed && !App.from(mContext).getProfile().emailConfirmed;
    }

    private boolean isTopfaceProfile(String socialNet) {
        return TextUtils.equals(socialNet, AuthToken.SN_TOPFACE);
    }
}

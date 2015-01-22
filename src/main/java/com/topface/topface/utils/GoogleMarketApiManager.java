package com.topface.topface.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.topface.framework.utils.Debug;
import com.topface.topface.R;

public class GoogleMarketApiManager extends BaseMarketApiManager {

    private Context mContext;
    private boolean mIsServicesAvailable;
    private int mResultCode;
    private String mTitleText = "";
    private String mButtonText = "";
    private boolean misButtonVisible = false;
    private boolean misTitleVisible = false;

    public GoogleMarketApiManager(Context context) {
        mContext = context;
        setContext(context);
        setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onButtonClick();
            }
        });
        onResume();
    }

    private int getPlayServicesStatusCode() {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
    }

    private void decryptingErrorCode() {
        switch (mResultCode) {
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                setParamServiceVersionUpdateRequired();
                break;
            case ConnectionResult.SERVICE_DISABLED:
                setParamServiceDisabled();
                break;
            case ConnectionResult.SERVICE_MISSING:
                setParamServiceMissing();
                break;
            case ConnectionResult.SUCCESS:
                setParamServiceSuccess();
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                setParamSignInAccount();
                break;
            case ConnectionResult.INVALID_ACCOUNT:
                setParamInvalidAccount();
                break;
            default:
                setParamUnavailableServices();
                break;
        }
    }

    @Override
    public void onResume() {
        mResultCode = getPlayServicesStatusCode();
        mIsServicesAvailable = mResultCode == ConnectionResult.SUCCESS ? true : false;
        decryptingErrorCode();
    }

    @Override
    public View getView() {
        getTitle().setText(mTitleText);
        getButton().setText(mButtonText);
        getButton().setVisibility(misButtonVisible ? View.VISIBLE : View.GONE);
        getTitle().setVisibility(misTitleVisible ? View.VISIBLE : View.GONE);
        return getCurrentView();
    }

    private void setParamServiceDisabled() {
        mTitleText = mContext.getString(R.string.google_service_disabled_title);
        mButtonText = mContext.getString(R.string.google_service_disabled_button);
        misButtonVisible = true;
        misTitleVisible = true;
    }

    private void setParamServiceMissing() {
        mTitleText = mContext.getString(R.string.google_service_missing_title);
        mButtonText = mContext.getString(R.string.google_service_missing_button);
        misButtonVisible = true;
        misTitleVisible = true;
    }

    private void setParamServiceVersionUpdateRequired() {
        mTitleText = mContext.getString(R.string.google_service_version_update_required_title);
        mButtonText = mContext.getString(R.string.google_service_version_update_required_button);
        misButtonVisible = true;
        misTitleVisible = true;
    }

    private void setParamServiceSuccess() {
        misButtonVisible = false;
        misTitleVisible = false;
    }

    private void setParamInvalidAccount() {
        mTitleText = mContext.getString(R.string.google_invalid_account_title);
        misButtonVisible = false;
        misTitleVisible = true;
    }

    private void setParamSignInAccount() {
        mTitleText = mContext.getString(R.string.google_sign_in_account_title);
        mButtonText = mContext.getString(R.string.google_sign_in_account_button);
        misButtonVisible = true;
        misTitleVisible = true;
    }

    private void setParamUnavailableServices() {
        mTitleText = mContext.getString(R.string.google_unavailable_services_title);
        misButtonVisible = false;
        misTitleVisible = true;
    }

    @Override
    public boolean isServicesAvailable() {
        return mIsServicesAvailable;
    }

    @Override
    public int getResultCode() {
        return mResultCode;
    }

    @Override
    public void onButtonClick() {
        switch (mResultCode) {
            case ConnectionResult.SIGN_IN_REQUIRED:
                Intent addAccountIntent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                addAccountIntent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
                mContext.startActivity(addAccountIntent);
                break;
            default:
                PendingIntent pendingIntent = GooglePlayServicesUtil.getErrorPendingIntent(mResultCode, mContext, 0);
                if (pendingIntent != null) {
                    try {
                        pendingIntent.send();
                    } catch (PendingIntent.CanceledException e) {
                        Debug.error("PendingIntent send: " + e);
                    }
                }
                break;
        }
    }

    @Override
    public String getButtonText() {
        return mButtonText;
    }

    @Override
    public boolean isButtonVisible() {
        return misButtonVisible;
    }

    @Override
    public String getMessage() {
        return mTitleText;
    }

}


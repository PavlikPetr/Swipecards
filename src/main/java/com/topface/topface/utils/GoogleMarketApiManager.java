package com.topface.topface.utils;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;

public class GoogleMarketApiManager extends BaseMarketApiManager {

    private boolean mIsServicesAvailable;
    private int mResultCode;
    private int mTitleId;
    private int mButtonId;
    private boolean mIsButtonVisible = false;
    private boolean mIsTitleVisible = false;

    public GoogleMarketApiManager() {
        checkServices();
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
        checkServices();
    }

    private void checkServices() {
        // check google account, because isGooglePlayServicesAvailable return SUCCESS when user unsigned
        mResultCode = isGoogleAccountExists() ? GooglePlayServicesUtil.isGooglePlayServicesAvailable(App.getContext()) : ConnectionResult.SIGN_IN_REQUIRED;
        mIsServicesAvailable = mResultCode == ConnectionResult.SUCCESS;
        decryptingErrorCode();
    }

    private boolean isGoogleAccountExists() {
        AccountManager manager = AccountManager.get(App.getContext());
        Account[] accounts = manager.getAccountsByType("com.google");
        return accounts != null && accounts.length > 0;
    }

    private void setParamServiceDisabled() {
        mTitleId = R.string.google_service_disabled_title;
        mButtonId = R.string.google_service_disabled_button;
        mIsButtonVisible = true;
        mIsTitleVisible = true;
    }

    private void setParamServiceMissing() {
        mTitleId = R.string.google_service_missing_title;
        mButtonId = R.string.google_service_missing_button;
        mIsButtonVisible = true;
        mIsTitleVisible = true;
    }

    private void setParamServiceVersionUpdateRequired() {
        mTitleId = R.string.google_service_version_update_required_title;
        mButtonId = R.string.google_service_version_update_required_button;
        mIsButtonVisible = true;
        mIsTitleVisible = true;
    }

    private void setParamServiceSuccess() {
        mIsButtonVisible = false;
        mIsTitleVisible = false;
    }

    private void setParamInvalidAccount() {
        mTitleId = R.string.google_invalid_account_title;
        mIsButtonVisible = false;
        mIsTitleVisible = true;
    }

    private void setParamSignInAccount() {
        mTitleId = R.string.google_sign_in_account_title;
        mButtonId = R.string.google_sign_in_account_button;
        mIsButtonVisible = true;
        mIsTitleVisible = true;
    }

    private void setParamUnavailableServices() {
        mTitleId = R.string.google_unavailable_services_title;
        mIsButtonVisible = false;
        mIsTitleVisible = true;
    }

    @Override
    public boolean isMarketApiAvailable() {
        return mIsServicesAvailable;
    }

    @Override
    public boolean isMarketApiSupportByUs() {
        return true;
    }

    @Override
    public int getResultCode() {
        return mResultCode;
    }

    @Override
    public void onProblemResolve(Context context) {
        switch (mResultCode) {
            case ConnectionResult.SIGN_IN_REQUIRED:
                Intent addAccountIntent = new Intent(android.provider.Settings.ACTION_ADD_ACCOUNT)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    addAccountIntent.putExtra(Settings.EXTRA_ACCOUNT_TYPES, new String[]{"com.google"});
                }
                context.startActivity(addAccountIntent);
                break;
            default:
                PendingIntent pendingIntent = GooglePlayServicesUtil.getErrorPendingIntent(mResultCode, context, 0);
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
    public int getButtonTextId() {
        return mButtonId;
    }

    @Override
    public boolean isButtonVisible() {
        return mIsButtonVisible;
    }

    @Override
    public boolean isTitleVisible() {
        return mIsTitleVisible;
    }

    @Override
    public int getTitleTextId() {
        return mTitleId;
    }

}


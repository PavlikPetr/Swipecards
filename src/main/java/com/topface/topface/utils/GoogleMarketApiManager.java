package com.topface.topface.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.topface.topface.R;

public class GoogleMarketApiManager extends BaseMarketApiManager {

    private Context mContext;
    private boolean mIsServicesAvailable;
    private int mResultCode;

    public GoogleMarketApiManager(Context context) {
        mContext = context;
        setContext(context);
        setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                e.printStackTrace();
                            }
                        }
                        break;
                }


            }
        });
        onResume();
    }

    private int getPlayServicesStatusCode() {
        int resCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        mIsServicesAvailable = resCode == ConnectionResult.SUCCESS ? true : false;
        return resCode;
    }

    private void createViewServiceDisabled() {
        getTitle().setText(mContext.getString(R.string.google_service_disabled_title));
        getButton().setText(R.string.google_service_disabled_button);
    }

    @Override
    public void onResume() {
        mResultCode = getPlayServicesStatusCode();
    }

    @Override
    public View getView() {
        switch (mResultCode) {
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                createViewServiceVersionUpdateRequired();
                break;
            case ConnectionResult.SERVICE_DISABLED:
                createViewServiceDisabled();
                break;
            case ConnectionResult.SERVICE_MISSING:
                createViewServiceMissing();
                break;
            case ConnectionResult.SUCCESS:
                createViewServiceSuccess();
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                createViewSignInAccount();
                break;
            case ConnectionResult.INVALID_ACCOUNT:
                createViewInvalidAccount();
                break;
            default:
                createViewUnavailableServices();
                break;
        }
        return getCurrentView();
    }

    private void createViewServiceMissing() {
        getTitle().setText(mContext.getString(R.string.google_service_missing_title));
        getButton().setText(R.string.google_service_missing_button);
    }

    private void createViewServiceVersionUpdateRequired() {
        getTitle().setText(mContext.getString(R.string.google_service_version_update_required_title));
        getButton().setText(R.string.google_service_version_update_required_button);
    }

    @Override
    public boolean isServicesAvailable() {
        return mIsServicesAvailable;
    }

    private void createViewServiceSuccess() {
        getTitle().setVisibility(View.GONE);
        getButton().setVisibility(View.GONE);
    }

    private void createViewInvalidAccount() {
        getTitle().setText(mContext.getString(R.string.google_invalid_account_title));
        getButton().setVisibility(View.GONE);
    }

    private void createViewSignInAccount() {
        getTitle().setText(mContext.getString(R.string.google_sign_in_account_title));
        getButton().setText(R.string.google_sign_in_account_button);
    }

    private void createViewUnavailableServices() {
        getTitle().setText(mContext.getString(R.string.google_unavailable_services_title));
        getButton().setVisibility(View.GONE);
    }

    @Override
    public String getMessage() {
        if (getTitle() != null) {
            return getTitle().getText().toString();
        }
        return null;
    }

    @Override
    public int getResultCode() {
        return mResultCode;
    }

}


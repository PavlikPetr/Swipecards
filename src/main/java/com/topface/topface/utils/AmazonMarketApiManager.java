package com.topface.topface.utils;

import android.content.Context;
import android.view.View;

import com.topface.topface.R;

public class AmazonMarketApiManager extends BaseMarketApiManager {

    private Context mContext;
    private boolean mIsServicesAvailable;

    public AmazonMarketApiManager(Context context) {
        mContext = context;
        setContext(context);
        mIsServicesAvailable = false;
        onResume();
    }


    @Override
    public void onResume() {
        createViewUnavailableServices();
    }

    @Override
    public View getView() {
        return getCurrentView();
    }

    @Override
    public boolean isServicesAvailable() {
        return mIsServicesAvailable;
    }

    private void createViewUnavailableServices() {
        getTitle().setText(mContext.getString(R.string.amazon_unavailable_services_title));
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
        return 0;
    }

}


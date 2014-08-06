package com.topface.topface.ui;

import android.content.Context;
import android.content.Intent;

import com.topface.topface.requests.ComplainRequest;
import com.topface.topface.ui.fragments.ComplainsMessageFragment;

public class ComplainsMessageActivity extends CheckAuthActivity<ComplainsMessageFragment> {

    public static Intent createIntent(Context context, int uid, ComplainRequest.ClassNames className, ComplainRequest.TypesNames typeName) {
        Intent intent = new Intent(context, ComplainsMessageActivity.class);
        intent.putExtra(ComplainsMessageFragment.CLASS_NAME, className);
        intent.putExtra(ComplainsMessageFragment.TYPE_NAME, typeName);
        intent.putExtra(ComplainsMessageFragment.USER_ID, uid);
        return intent;
    }

    public static Intent createIntent(Context context, int uid, String feedId, ComplainRequest.ClassNames className, ComplainRequest.TypesNames typeName) {
        Intent intent = createIntent(context, uid, className, typeName);
        if (feedId != null) {
            intent.putExtra(ComplainsMessageFragment.FEED_ID, feedId);
        }
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return ComplainsMessageFragment.class.getSimpleName();
    }

    @Override
    protected ComplainsMessageFragment createFragment() {
        return new ComplainsMessageFragment();
    }
}

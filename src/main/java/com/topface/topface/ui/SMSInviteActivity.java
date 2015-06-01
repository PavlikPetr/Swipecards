package com.topface.topface.ui;

import android.app.Activity;
import android.content.Intent;

import com.topface.topface.Static;
import com.topface.topface.ui.fragments.SmsInviteFragment;

public class SMSInviteActivity extends CheckAuthActivity<SmsInviteFragment> {

    public static final int INTENT_CONTACTS = 8;

    public static Intent createIntent(Activity context) {
        Intent intent = new Intent(context, SMSInviteActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, INTENT_CONTACTS);
        return intent;
    }

    @Override
    protected String getFragmentTag() {
        return SmsInviteFragment.class.getSimpleName();
    }

    @Override
    protected SmsInviteFragment createFragment() {
        return new SmsInviteFragment();
    }
}

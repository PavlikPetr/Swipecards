package com.topface.topface.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.topface.topface.R;

public class ChatActivity extends CustomTitlesBaseFragmentActivity implements IUserOnlineListener {

    private View mOnlineIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_chat);
    }

    @Override
    protected void initCustomActionBarView(View mCustomView) {
        mOnlineIcon = mCustomView.findViewById(R.id.online);
    }

    @Override
    protected int getActionBarCustomViewResId() {
        return R.layout.actionbar_container_title_view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getSupportFragmentManager().findFragmentById(R.id.chat_fragment).onOptionsItemSelected(item);
                if (isTaskRoot()) {
                    Intent i = new Intent(this, NavigationActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    onBackPressed();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setUserOnline(boolean online) {
        if (mOnlineIcon != null) {
            mOnlineIcon.setVisibility(online ? View.VISIBLE : View.GONE);
        }
    }
}

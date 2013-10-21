package com.topface.topface.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.View;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.utils.actionbar.IActionBarTitleSetter;

/**
 * Created by kirussell on 26.09.13.
 * Extend this Activity to use custom actionbar titles
 * Its important:
 * - use R.id.title for title's TextView,
 * - use R.id.subtitle for subtitle's TextView,
 */
public abstract class CustomTitlesBaseFragmentActivity extends BaseFragmentActivity {

    IActionBarTitleSetter mActioBarTitleSetter;
    private View mCustomView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        //Странный глюк на некоторых устройствах (воспроизводится например на HTC One V),
        // из-за которого показывается лоадер в ActionBar
        // этот метод можно использовать только после setContent
        setSupportProgressBarIndeterminateVisibility(false);
    }

    @Override
    protected void initActionBar(ActionBar actionBar) {
        super.initActionBar(actionBar);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowCustomEnabled(true);
            mCustomView = getLayoutInflater().inflate(getActionBarCustomViewResId(), null);
            actionBar.setCustomView(mCustomView);
            mActioBarTitleSetter = new CustomActionBarTitleSetter(
                    (TextView) mCustomView.findViewById(R.id.title),
                    (TextView) mCustomView.findViewById(R.id.subtitle)
            );
            initCustomActionBarView(mCustomView);
        }
    }

    protected abstract void initCustomActionBarView(View mCustomView);

    /**
     * Specify custom title layout through abstract method
     * @return resourse id of layout for titles
     */
    protected abstract int getActionBarCustomViewResId();

    /**
     * Can get custom view of actiobar (between logo and menuItem buttons)
     * Custom view for actionbar was inflated onCreate
     * @return custom view objects
     */
    protected View getCustomActionBarView() {
        return mCustomView;
    }

    /**
     * Getting title setter delegate of actionbar whether it custom view or standart actionbar
     * @return delegate of actionbar to set titles
     */
    public IActionBarTitleSetter getActionBarTitleSetterDelegate() {
        return mActioBarTitleSetter;
    }

    /**
     * Action bar title setter for custom action bar view
     */
    protected static class CustomActionBarTitleSetter implements IActionBarTitleSetter {

        private TextView mTitleView;
        private TextView mSubtitleView;

        protected CustomActionBarTitleSetter(TextView titleView, TextView subtitleView) {
            mTitleView = titleView;
            mSubtitleView = subtitleView;
        }

        @Override
        public void setActionBarTitles(String title, String subtitle) {
            setTitle(title);
            setSubtitle(subtitle);
        }

        @Override
        public void setActionBarTitles(int title, int subtitle) {
            setTitle(title);
            setSubtitle(subtitle);
        }

        @Override
        public void setActionBarTitles(String title, int subtitle) {
            setTitle(title);
            setSubtitle(subtitle);
        }

        @Override
        public void setActionBarTitles(int title, String subtitle) {
            setTitle(title);
            setSubtitle(subtitle);
        }

        private void setTitle(String title) {
            if (title == null) {
                mTitleView.setVisibility(View.GONE);
            } else {
                mTitleView.setVisibility(View.VISIBLE);
                mTitleView.setText(title);
            }
        }

        private void setTitle(int title) {
            if (mTitleView.getVisibility() == View.GONE) {
                mTitleView.setVisibility(View.VISIBLE);
            }
            mTitleView.setText(title);
        }

        private void setSubtitle(String subtitle) {
            if (subtitle == null) {
                mSubtitleView.setVisibility(View.GONE);
            } else {
                mSubtitleView.setVisibility(View.VISIBLE);
                mSubtitleView.setText(subtitle);
            }
        }

        private void setSubtitle(int subtitle) {
            if (mSubtitleView.getVisibility() == View.GONE) {
                mSubtitleView.setVisibility(View.VISIBLE);
            }
            mSubtitleView.setText(subtitle);
        }
    }
}

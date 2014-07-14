package com.topface.topface.ui;

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
    protected void initActionBar(ActionBar actionBar) {
        int actionBarResId = getActionBarCustomViewResId();
        if (actionBarResId != 0) {
            super.initActionBar(actionBar);
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setDisplayShowCustomEnabled(true);
                mCustomView = getLayoutInflater().inflate(actionBarResId, null);
                actionBar.setCustomView(mCustomView);
                mActioBarTitleSetter = new CustomActionBarTitleSetter(
                        (TextView) mCustomView.findViewById(R.id.title),
                        (TextView) mCustomView.findViewById(R.id.subtitle)
                );
                initCustomActionBarView(mCustomView);
            }
        }
    }

    protected abstract void initCustomActionBarView(View mCustomView);

    /**
     * Specify custom title layout through abstract method
     *
     * @return resourse id of layout for titles
     */
    protected abstract int getActionBarCustomViewResId();

    /**
     * Can get custom view of actiobar (between logo and menuItem buttons)
     * Custom view for actionbar was inflated onCreate
     *
     * @return custom view objects
     */
    protected View getCustomActionBarView() {
        return mCustomView;
    }

    /**
     * Getting title setter delegate of actionbar whether it custom view or standart actionbar
     *
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
        private boolean mNoTitle;
        private boolean mNoSubtitle;

        protected CustomActionBarTitleSetter(TextView titleView, TextView subtitleView) {
            mTitleView = titleView;
            mSubtitleView = subtitleView;
            mNoTitle = (mTitleView == null);
            mNoSubtitle = (mSubtitleView == null);
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
            if (mNoTitle) return;
            if (title == null) {
                mTitleView.setVisibility(View.GONE);
            } else {
                mTitleView.setVisibility(View.VISIBLE);
                mTitleView.setText(title);
            }
        }

        private void setTitle(int title) {
            if (mNoTitle) return;
            if (mTitleView.getVisibility() == View.GONE) {
                mTitleView.setVisibility(View.VISIBLE);
            }
            mTitleView.setText(title);
        }

        private void setSubtitle(String subtitle) {
            if (mNoSubtitle) return;
            if (subtitle == null) {
                mSubtitleView.setVisibility(View.GONE);
            } else {
                mSubtitleView.setVisibility(View.VISIBLE);
                mSubtitleView.setText(subtitle);
            }
        }

        private void setSubtitle(int subtitle) {
            if (mNoSubtitle) return;
            if (mSubtitleView.getVisibility() == View.GONE) {
                mSubtitleView.setVisibility(View.VISIBLE);
            }
            mSubtitleView.setText(subtitle);
        }
    }
}

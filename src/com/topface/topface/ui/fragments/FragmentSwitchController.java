package com.topface.topface.ui.fragments;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.*;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.Scroller;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.feed.*;
import com.topface.topface.ui.views.ImageSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;

public class FragmentSwitchController extends ViewGroup {

    private int mOpenDX;
    private int mFullOpenDX;
    private int mWidth;
    private int mAnimation;
    private int mCurrentFragmentId;
    private Scroller mScroller;
    private FragmentManager mFragmentManager;
    private FragmentSwitchListener mFragmentSwitchListener;
    private boolean mAutoScrolling = false;
    public static int EXPANDING_PERCENT = 30;
    private BaseFragment mCurrentFragment;
    private FrameLayout mExtraFrame;
    private Fragment mCurrentExtraFragment;

    public static final int EXPAND = 1;
    public static final int EXPAND_FULL = 2;
    public static final int COLLAPSE = 3;
    public static final int COLLAPSE_FULL = 4;

    /*
    *   interface FragmentSwitchListener
    */
    public interface FragmentSwitchListener {
        public void afterClosing();

        public void beforeExpanding();

        public void afterOpening();

        public void onExtraFrameOpen();
    }

    public FragmentSwitchController(Context context, AttributeSet attrs) {
        super(context, attrs);
//        mCurrentFragmentId = BaseFragment.F_PROFILE;
        Interpolator mPrixingInterpolator = new Interpolator() {
            public float getInterpolation(float t) {
                return (t - 1) * (t - 1) * (t - 1) * (t - 1) * (t - 1) + 1.0f;
            }
        };
        mScroller = new Scroller(context, mPrixingInterpolator);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mMinimumVelocity = 10 * configuration.getScaledMinimumFlingVelocity();
        mVelocitySlop = configuration.getScaledMinimumFlingVelocity();
        //noinspection deprecation
        mTouchSlop = configuration.getScaledTouchSlop();
        mAnimation = COLLAPSE;
    }

    public void setFragmentManager(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
    }

    public void setFragmentSwitchListener(FragmentSwitchListener fragmentSwitchListener) {
        mFragmentSwitchListener = fragmentSwitchListener;
    }

    public int getAnimationState() {
        return mAnimation;
    }

    public void showFragmentWithAnimation(int fragmentId) {
        if (mScroller.isFinished()) {
            if (fragmentId != mCurrentFragmentId) {
                mCurrentFragmentId = fragmentId;
                snapToScreen(EXPAND_FULL);
            } else {
                closeMenu();
            }
        }
    }

    public void showFragment(int fragmentId) {
        if (mScroller.isFinished()) {
            if (fragmentId != mCurrentFragmentId) {
                mCurrentFragmentId = fragmentId;
                switchFragment();
            } else {
                closeMenu();
            }
        }
    }

    private void switchFragment() {
        Fragment oldFragment = mFragmentManager.findFragmentById(R.id.fragment_container);

        BaseFragment newFragment = (BaseFragment) mFragmentManager.findFragmentByTag(getTagById(mCurrentFragmentId));
        //Если не нашли в FragmentManager уже существующего инстанса, то создаем новый
        if (newFragment == null) {
            newFragment = getFragmentNewInstanceById(mCurrentFragmentId);
        }

        if (oldFragment == null || newFragment != oldFragment) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();

            transaction.replace(R.id.fragment_container, newFragment, getTagById(mCurrentFragmentId));
            transaction.commit();
            mCurrentFragment = newFragment;
        }
        closeExtraFragment();
    }

    private String getTagById(int id) {
        return "fragment_switch_controller_" + id;
    }

    public void switchExtraFragment(Fragment fragment) {
        if (mExtraFrame == null) {
            mExtraFrame = (FrameLayout) this.findViewById(R.id.fragment_extra_container);
        }
        mExtraFrame.setVisibility(View.VISIBLE);

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_extra_container, fragment);
        if (mCurrentExtraFragment != null) {
            transaction.remove(mCurrentExtraFragment);
        }
        transaction.commit();
        mCurrentExtraFragment = fragment;

        mFragmentSwitchListener.onExtraFrameOpen();
        mCurrentFragmentId = BaseFragment.F_UNKNOWN;
    }

    public void closeExtraFragment() {
        if (mExtraFrame != null) mExtraFrame.setVisibility(View.GONE);
        if (mCurrentExtraFragment != null) {
            if (mCurrentExtraFragment instanceof BaseFragment) {
                ((BaseFragment) mCurrentExtraFragment).clearContent();
                mFragmentManager.beginTransaction().remove(mCurrentExtraFragment).commit();
            }
            mCurrentExtraFragment = null;
        }
    }

    public boolean isExtraFrameShown() {
        return (mExtraFrame.getVisibility() == View.VISIBLE);
    }

    public BaseFragment getCurrentFragment() {
        return mCurrentFragment;
    }

    public Fragment getCurrentExtraFragment() {
        return mCurrentExtraFragment;
    }

    private BaseFragment getFragmentNewInstanceById(int id) {
        BaseFragment fragment;
        switch (id) {
            case BaseFragment.F_VIP_PROFILE:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE,
                        VipBuyFragment.class.getName());
                break;
            case BaseFragment.F_PROFILE:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE);
                break;
            case BaseFragment.F_DATING:
                fragment = new DatingFragment();
                break;
            case BaseFragment.F_LIKES:
                fragment = new LikesFragment();
                break;
            case BaseFragment.F_MUTUAL:
                fragment = new MutualFragment();
                break;
            case BaseFragment.F_DIALOGS:
                fragment = new DialogsFragment();
                break;
            case BaseFragment.F_BOOKMARKS:
                fragment = new BookmarksFragment();
                break;
            case BaseFragment.F_FANS:
                fragment = new FansFragment();
                break;
            case BaseFragment.F_TOPS:
                fragment = new TopsFragment();
                break;
            case BaseFragment.F_VISITORS:
                fragment = new VisitorsFragment();
                break;
            case BaseFragment.F_SETTINGS:
                fragment = new SettingsFragment();
                break;
            default:
                fragment = ProfileFragment.newInstance(CacheProfile.uid, ProfileFragment.TYPE_MY_PROFILE);
                break;
        }
        return fragment;
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        try {
            // shadow
            getChildAt(0).measure(getChildAt(0).getMeasuredWidth(), heightMeasureSpec);
            // fragments
            getChildAt(1).measure(widthMeasureSpec, heightMeasureSpec);
        } catch (Exception e) {
            Debug.error(e);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        getChildAt(0).layout(-getChildAt(0).getMeasuredWidth(), 0, 0, getChildAt(0).getMeasuredHeight());
        getChildAt(1).layout(0, 0, getChildAt(1).getMeasuredWidth(), getChildAt(1).getMeasuredHeight());

        mWidth = getChildAt(1).getWidth();
        int mClosedDX = mWidth / 100 * EXPANDING_PERCENT;
        mOpenDX = mWidth - mClosedDX;
        mFullOpenDX = mWidth - mOpenDX;

        if (mExtraFrame == null) {
            mExtraFrame = (FrameLayout) this.findViewById(R.id.fragment_extra_container);
        }
    }

    private void snapToScreen(int typeAnimation) {
        mAnimation = typeAnimation;
        setScrollingCacheEnabled(true);
        mAutoScrolling = true;
        switch (typeAnimation) {
            case EXPAND:
                mFragmentSwitchListener.beforeExpanding();
                mScroller.startScroll(getLeftBound(), 0, -getRightBound(), 0, 500);
                break;
            case COLLAPSE:
                mScroller.startScroll(-getRightBound(), 0, getRightBound(), 0, 500);
                break;
            case EXPAND_FULL:
                mScroller.startScroll(-getRightBound(), 0, -(mFullOpenDX), 0, 500);
                break;
            case COLLAPSE_FULL:
                mScroller.startScroll(-mWidth, 0, mWidth, 0, 500);
                break;
            default:
                break;
        }
        invalidate();
    }

    @Override
    public void computeScroll() {
        int mScrollX = mScroller.getCurrX();
        if (!mScroller.isFinished()) {
            if (mScroller.computeScrollOffset()) {
                scrollTo(mScrollX, getScrollY());
                return;
            }
        }
        endScrollAnimation();
    }

    public void endScrollAnimation() {
        if (mAutoScrolling) {
            mScroller.abortAnimation();
            int oldX = getScrollX();
            int x = mScroller.getCurrX();
            if (oldX != x) {
                scrollTo(x, 0);
            }

            mAutoScrolling = false;
            if (mAnimation == COLLAPSE || mAnimation == COLLAPSE_FULL) {
                mFragmentSwitchListener.afterClosing();
            }

            if (mAnimation == EXPAND || mAnimation == EXPAND_FULL) {
                mFragmentSwitchListener.afterOpening();
            }

            if (mAnimation == EXPAND_FULL) {
                fullExpanding();
            }
        }
        setScrollingCacheEnabled(false);
    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        invalidate();
    }

    public void setScrollingCacheEnabled(boolean enabled) {
        if(getChildCount() > 0) {
            getChildAt(0).setDrawingCacheEnabled(enabled);
            getChildAt(1).setDrawingCacheEnabled(enabled);
        }
    }

    private void fullExpanding() {
        switchFragment();
        snapToScreen(COLLAPSE_FULL);
    }

    private int getLeftBound() {
        return 0;
    }

    private int getRightBound() {
        return mOpenDX;
    }

    public void openMenu() {
        snapToScreen(EXPAND);
        mFragmentSwitchListener.beforeExpanding();
    }

    public void closeMenu() {
        snapToScreen(COLLAPSE);
    }

    // sliding part
    float mLastMotionX;
    float mLastMotionY;
    float mTouchSlop;
    boolean mIsDragging = false;
    private VelocityTracker mVelocityTracker;
    private float mMaximumVelocity;
    private int mMinimumVelocity;
    private float mVelocitySlop;
    private boolean mActionDownOnBezier = false;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mAutoScrolling) {
            return false;
        }

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mLastMotionY = y;
                mActionDownOnBezier = !(!inBezierThreshold(x) && (mAnimation == COLLAPSE || mAnimation == COLLAPSE_FULL));
                break;
            case MotionEvent.ACTION_MOVE:
                if (mAnimation == EXPAND) {
                    startDragging(x);
                    break;
                }

                float dx = x - mLastMotionX;
                float xDiff = Math.abs(dx);
                float dy = y - mLastMotionY;
                float yDiff = Math.abs(dy);

                if (canScroll(getChildAt(1), false, (int) dx, (int) x, (int) y)) {
                    return false;
                }

                if (!mActionDownOnBezier) {
                    return false;
                }

                if (xDiff > mTouchSlop && xDiff > yDiff) {
                    startDragging(x);
                    if (mAnimation == COLLAPSE || mAnimation == COLLAPSE_FULL) {
                        mFragmentSwitchListener.beforeExpanding();
                    }
                } else {
                    stopDragging();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                stopDragging();
                break;
            case MotionEvent.ACTION_UP:
                if (mAnimation == EXPAND) {
                    completeDragging(0);
                    Debug.log("interseptTouch:true");
                    return true;
                }
                stopDragging();
                break;
        }

        return mIsDragging;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        if (!mIsDragging) {
            return false;
        } else {
            mVelocityTracker.addMovement(event);
        }

        int action = event.getAction();
        float x = event.getX();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastMotionX = x;
                mActionDownOnBezier = !(!inBezierThreshold(x) && (mAnimation == COLLAPSE || mAnimation == COLLAPSE_FULL));
                mVelocityTracker.addMovement(event);
                break;
            case MotionEvent.ACTION_MOVE:
                if (mIsDragging) {
                    float dx = mLastMotionX - x;
                    mLastMotionX = x;

                    final float oldXScroll = getScrollX();
                    float newXScroll = oldXScroll + dx;

                    if (-newXScroll < getLeftBound()) {
                        newXScroll = -getLeftBound();
                    } else if (-newXScroll > getRightBound()) {
                        newXScroll = -getRightBound();
                    }

                    scrollTo((int) newXScroll, getScrollY());
                }
                break;
            case MotionEvent.ACTION_UP:
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                int initialVelocity = (int) velocityTracker.getXVelocity();
                completeDragging(initialVelocity);
                break;
            case MotionEvent.ACTION_CANCEL:
                stopDragging();
                break;
        }

        return true;
    }

    private void startDragging(float x) {
        mIsDragging = true;
        mLastMotionX = x;
        setScrollingCacheEnabled(true);
    }

    private void stopDragging() {
        mIsDragging = false;
        setScrollingCacheEnabled(false);
    }


    private void completeDragging(int velocity) {
        mIsDragging = false;
        mAutoScrolling = true;
        int dx;
        int duration;

        if (Math.abs(velocity) < mVelocitySlop)
            velocity = 0;

        int scrollingVelocityThreshold = 2000;
        float scrollingDistanceThreshold = 0;
        if (velocity > 0) {
            // right - expect EXPAND
            dx = -getRightBound() - getScrollX();

            if (velocity >= scrollingVelocityThreshold) {
                //EXPAND because user slides insanely
                mAnimation = EXPAND;
                duration = Math.abs(1000 * dx / scrollingVelocityThreshold);
            } else {
                if (velocity < mMinimumVelocity) {
                    velocity = mMinimumVelocity;
                }
                //EXPAND with normal velocity, but check "distance threshold"
                if (-getScrollX() - getLeftBound() > scrollingDistanceThreshold) {
                    mAnimation = EXPAND;
                } else {
                    mAnimation = COLLAPSE;
                    dx = -getScrollX() - getLeftBound();
                }
                duration = Math.abs(1000 * dx / velocity);
            }
            mScroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY(), duration);
        } else if (velocity < 0) {
            // left - expect COLLAPSE
            dx = -getScrollX() - getLeftBound();

            if (-velocity >= scrollingVelocityThreshold) {
                duration = Math.abs(1000 * dx / scrollingVelocityThreshold);
            } else {
                if (-velocity < mMinimumVelocity) {
                    velocity = mMinimumVelocity;
                }
                duration = Math.abs(1000 * dx / velocity);
            }

            mAnimation = COLLAPSE;
            mScroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY(), duration);
        } else {
            if (-getScrollX() > scrollingDistanceThreshold) {
                if (mAnimation == EXPAND) {
                    mAnimation = COLLAPSE;
                    dx = -getScrollX() - getLeftBound();
                } else {
                    mAnimation = EXPAND;
                    dx = -getRightBound() - getScrollX();
                }
            } else {
                mAnimation = COLLAPSE;
                dx = -getScrollX() - getLeftBound();
            }

            duration = Math.abs(1000 * dx / mMinimumVelocity);

            mScroller.startScroll(getScrollX(), getScrollY(), dx, getScrollY(), duration);
        }
        mScroller.computeScrollOffset();
        invalidate();
    }

    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        if (v instanceof ViewGroup) {
            final ViewGroup group = (ViewGroup) v;
            if (group.isShown()) {
                final int scrollX = v.getScrollX();
                final int scrollY = v.getScrollY();
                final int count = group.getChildCount();
                for (int i = count - 1; i >= 0; i--) {
                    final View child = group.getChildAt(i);
                    if (x + scrollX >= child.getLeft()
                            && x + scrollX < child.getRight()
                            && y + scrollY >= child.getTop()
                            && y + scrollY < child.getBottom()
                            && canScroll(child, true, dx, x + scrollX - child.getLeft(), y + scrollY
                            - child.getTop())) {
                        return true;
                    }
                }
            }
        }

        boolean result;

        //Данная проверка нужна, т.к. метод доступен в API >= 14, в ImageSwitcher мы его эмулируем
        if (v instanceof com.topface.topface.ui.views.ImageSwitcher) {
            //noinspection RedundantCast
            result = ((ImageSwitcher) v).canScrollHorizontally(-dx);
        } else {
            result = ViewCompat.canScrollHorizontally(v, -dx);
        }

        return checkV && result;
    }

    protected boolean inBezierThreshold(float x) {
        return x < getContext().getResources().getDimensionPixelSize(R.dimen.bezier_threshold);
    }


}

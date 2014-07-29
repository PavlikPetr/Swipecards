package android.support.v4.app;

import android.os.Bundle;
import android.view.ViewGroup;

/**
 * Workaround to use custom classes. See android issue here
 * https://code.google.com/p/android/issues/detail?id=37484
 */
public abstract class HackyFragmentStatePagerAdapter extends FragmentStatePagerAdapter {

    public HackyFragmentStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment f = (Fragment) super.instantiateItem(container, position);
        Bundle savedFragmentState = f.mSavedFragmentState;
        if (savedFragmentState != null) {
            savedFragmentState.setClassLoader(((Object) f).getClass().getClassLoader());
        }
        return f;
    }
}

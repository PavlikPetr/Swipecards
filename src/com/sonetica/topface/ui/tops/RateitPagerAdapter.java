package com.sonetica.topface.ui.tops;

import java.util.ArrayList;
import java.util.List;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class RateitPagerAdapter extends FragmentPagerAdapter {
  // Data
  private final ArrayList<Fragment> mFragments;
  //---------------------------------------------------------------------------
  public RateitPagerAdapter(FragmentManager fm, ArrayList<Fragment> fragments) {
    super(fm);
    mFragments = fragments;
  }
  //---------------------------------------------------------------------------
  @Override
  public Fragment getItem(int position) {
    return mFragments.get(position);
  }
  //---------------------------------------------------------------------------
  @Override
  public int getCount() {
    return mFragments.size();
  }
  //---------------------------------------------------------------------------
}

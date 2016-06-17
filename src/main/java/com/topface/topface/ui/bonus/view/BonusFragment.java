package com.topface.topface.ui.bonus.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.topface.R;
import com.topface.topface.databinding.FragmentBonus1Binding;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.fragments.BaseFragment;

@FlurryOpenEvent(name = BonusFragment.PAGE_NAME)
public class BonusFragment extends BaseFragment implements IBonusView {
    public static final String PAGE_NAME = "bonus";

    private FragmentBonus1Binding mBinding;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bonus1, null);
        mBinding = DataBindingUtil.bind(root);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void setProgressState(boolean isVisible) {
        if (mBinding != null) {
            mBinding.prsOfferwalls.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showEmptyViewVisibility(boolean isVisible) {
        if (mBinding != null) {
            mBinding.emptyOfferwalls.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showOfferwallsVisibility(boolean isVisible) {
        if (mBinding != null) {
            mBinding.rvOfferwalls.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }
}

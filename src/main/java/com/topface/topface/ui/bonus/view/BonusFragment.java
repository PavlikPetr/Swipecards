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
import com.topface.topface.ui.bonus.presenter.BonusPresenter;
import com.topface.topface.ui.bonus.presenter.IBonusPresenter;
import com.topface.topface.ui.fragments.BaseFragment;

import static com.topface.topface.ui.fragments.BonusFragment.NEED_SHOW_TITLE;

@FlurryOpenEvent(name = BonusFragment.PAGE_NAME)
public class BonusFragment extends BaseFragment implements IBonusView {
    public static final String PAGE_NAME = "bonus";

    private FragmentBonus1Binding mBinding;
    private IBonusPresenter mPresenter;

    public static BonusFragment newInstance(boolean needShowTitle) {
        BonusFragment fragment = new BonusFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(NEED_SHOW_TITLE, needShowTitle);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected void setNeedTitles(boolean needTitles) {
        super.setNeedTitles(getArguments().getBoolean(NEED_SHOW_TITLE));
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.general_bonus);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPresenter = new BonusPresenter();
        View root = inflater.inflate(R.layout.fragment_bonus1, null);
        mBinding = DataBindingUtil.bind(root);
        mPresenter.bindView(this);
        mPresenter.loadOfferwalls();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unbindView();
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

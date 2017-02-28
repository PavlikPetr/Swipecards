package com.topface.topface.ui.bonus.view;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.databinding.FragmentBonusBinding;
import com.topface.topface.mvp.IPresenterFactory;
import com.topface.topface.mvp.PresenterCache;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.adapters.ItemEventListener;
import com.topface.topface.ui.bonus.models.IOfferwallBaseModel;
import com.topface.topface.ui.bonus.presenter.BonusPresenter;
import com.topface.topface.ui.bonus.presenter.IBonusPresenter;
import com.topface.topface.ui.bonus.viewModel.BonusFragmentViewModel;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.toolbar.utils.ToolbarManager;
import com.topface.topface.ui.views.toolbar.utils.ToolbarSettingsData;
import com.topface.topface.utils.Utils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@FlurryOpenEvent(name = BonusFragment.PAGE_NAME)
public class BonusFragment extends BaseFragment implements IBonusView {
    public static final String NEED_SHOW_TITLE = "need_show_title";
    public static final String PAGE_NAME = "bonus";
    public static final String TAG = BonusFragment.class.getSimpleName();

    private FragmentBonusBinding mBinding;
    private IBonusPresenter mPresenter;
    private OfferwallsAdapter mAdapter;
    private PresenterCache mPresenterCache;

    public static BonusFragment newInstance(boolean needShowTitle) {
        BonusFragment fragment = new BonusFragment();
        Bundle arguments = new Bundle();
        arguments.putBoolean(NEED_SHOW_TITLE, needShowTitle);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected String getScreenName() {
        return PAGE_NAME;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPresenterCache = App.getAppComponent().presenterCache();
        mPresenter = mPresenterCache.getPresenter(TAG, new IPresenterFactory<IBonusPresenter>() {
            @NotNull
            @Override
            public IBonusPresenter createPresenter() {
                return new BonusPresenter();
            }
        });
        mBinding = DataBindingUtil.bind(inflater.inflate(R.layout.fragment_bonus, null));
        BonusFragmentViewModel viewModel = new BonusFragmentViewModel();
        mPresenter.setViewModel(viewModel);
        mBinding.setViewModel(viewModel);
        mBinding.rvOfferwalls.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
        mBinding.rvOfferwalls.setAdapter(getAdapter());
        mPresenter.bindView(this);
        mPresenter.loadOfferwalls();
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        Bundle arg = getArguments();
        if (arg != null && arg.getBoolean(NEED_SHOW_TITLE)) {
            ToolbarManager.INSTANCE.setToolbarSettings(new ToolbarSettingsData(getString(R.string.general_bonus)));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.unbindView();
    }

    @Override
    public void showOffers(ArrayList<IOfferwallBaseModel> offers) {
        OfferwallsAdapter adapter = getAdapter();
        adapter.clearData();
        adapter.addData(offers);
    }

    private OfferwallsAdapter getAdapter() {
        if (mAdapter == null) {
            mAdapter = new OfferwallsAdapter();
            mAdapter.setOnItemClickListener(new ItemEventListener.OnRecyclerViewItemClickListener<IOfferwallBaseModel>() {
                @Override
                public void itemClick(View view, int itemPosition, IOfferwallBaseModel data) {
                    Debug.showChunkedLogDebug("Offerwalls", "try open " + data.getOfferwallsType() + " url " + data.getLink());
                    Utils.goToUrl(getActivity(), data.getLink());
                }
            });
        }
        return mAdapter;
    }
}

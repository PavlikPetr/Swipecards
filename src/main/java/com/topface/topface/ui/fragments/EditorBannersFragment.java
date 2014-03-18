package com.topface.topface.ui.fragments;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.ui.blocks.BannerBlock;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.ads.BannersConfig;

public class EditorBannersFragment extends BaseFragment implements View.OnClickListener {

    private ViewGroup mConfigContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_editor_banners, null);
        initHeader();
        mConfigContainer = (ViewGroup) root.findViewById(R.id.loBannersConfigurationsContainer);
        initConfigContainer();
        root.findViewById(R.id.btnSaveSettings).setOnClickListener(this);
        root.findViewById(R.id.btnResetSettings).setOnClickListener(this);
        initOnStartLoadingControls(root);
        return root;
    }

    private void initOnStartLoadingControls(View root) {
        final BannersConfig config = App.getBannerConfig();
        final CheckBox checkBoxOnStart = ((CheckBox) root.findViewById(R.id.cbOnStart));
        checkBoxOnStart.setChecked(config.needLoadOnStart());
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                config.setLoadOnStart(checkBoxOnStart.isChecked());
            }
        };
        checkBoxOnStart.setOnClickListener(listener);
    }

    private void initConfigContainer() {
        if (mConfigContainer != null) {
            for (String pageName : CacheProfile.getOptions().pages.keySet()) {
                Options.Page page = CacheProfile.getOptions().pages.get(pageName);
                if (page != null) {
                    PageConfigurator configurator = new PageConfigurator(getActivity());
                    configurator.setPage(page);
                    mConfigContainer.addView(configurator);
                }
            }
        }
    }

    private void clearConfigContainer() {
        if (mConfigContainer != null) {
            mConfigContainer.removeAllViews();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Editor.isEditor()) {
            getActivity().finish();
        }
    }

    private void initHeader() {
    }

    private void showCompleteMessage() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.editor_fragment_complete), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnSaveSettings:
                App.getBannerConfig().saveBannersSettings();
                showCompleteMessage();
                break;
            case R.id.btnResetSettings:
                App.getBannerConfig().resetBannersSettings();
                clearConfigContainer();
                initConfigContainer();
                showCompleteMessage();
                break;
            default:
                break;
        }
    }

    private class PageConfigurator extends LinearLayout {
        private Options.Page mPage;

        private TextView mTitleText;
        private ViewGroup mSpinnersContainer;
        private Spinner mFloatTypeSpinner;
        private Spinner mBannerTypeSpinner;

        private LayoutParams mCompressedParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0);

        private LayoutParams mExpandedParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 150);
        private boolean mExpanded = false;

        public PageConfigurator(Context context) {
            this(context, null);
        }

        public PageConfigurator(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View root = inflater.inflate(R.layout.editor_banner_configurator, this, true);
            initTitleText(root);
            initSpinnersContainer(root);
            initFloatTypeSpinner(root);
            initBannerTypeSpinner(root);

            if (android.os.Build.VERSION.SDK_INT >= 16) {
                setLayoutTransition(new LayoutTransition());
                LayoutTransition transition = getLayoutTransition();
                transition.enableTransitionType(LayoutTransition.CHANGING);
            }
        }

        private void initTitleText(View root) {
            mTitleText = (TextView) root.findViewById(R.id.tvTitle);
            mTitleText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSpinnersContainer != null) {
                        mSpinnersContainer.setLayoutParams(mExpanded ? mCompressedParams : mExpandedParams);
                        mExpanded = !mExpanded;
                        requestLayout();
                    }
                }
            });
        }

        private void initSpinnersContainer(View root) {
            mSpinnersContainer = (ViewGroup) root.findViewById(R.id.loSpinners);
            mSpinnersContainer.setLayoutParams(mCompressedParams);
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                mSpinnersContainer.setLayoutTransition(new LayoutTransition());
                LayoutTransition transition = mSpinnersContainer.getLayoutTransition();
                transition.enableTransitionType(LayoutTransition.CHANGING);
            }
        }

        private void initBannerTypeSpinner(View root) {
            mBannerTypeSpinner = (Spinner) root.findViewById(R.id.spEditBannerType);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    BannerBlock.BANNERS
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBannerTypeSpinner.setAdapter(adapter);
            mBannerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    getPage().banner = BannerBlock.BANNERS[position];
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        private void initFloatTypeSpinner(View root) {
            mFloatTypeSpinner = (Spinner) root.findViewById(R.id.spEditFloatType);
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                    getActivity(),
                    android.R.layout.simple_spinner_item,
                    FloatBlock.FLOAT_TYPES
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mFloatTypeSpinner.setAdapter(adapter);
            mFloatTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    getPage().floatType = FloatBlock.FLOAT_TYPES[position];
                    mBannerTypeSpinner.setVisibility(FloatBlock.FLOAT_TYPES[position].equals(FloatBlock.FLOAT_TYPE_BANNER) ? View.VISIBLE : View.GONE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        private Options.Page getPage() {
            return mPage;
        }

        public void setPage(Options.Page page) {
            mPage = page;
            mTitleText.setText(page.name);
            mPage = page;
            for (int i = 0; i < FloatBlock.FLOAT_TYPES.length; i++) {
                if (FloatBlock.FLOAT_TYPES[i].equals(mPage.floatType)) {
                    mFloatTypeSpinner.setSelection(i);
                    mBannerTypeSpinner.setVisibility(FloatBlock.FLOAT_TYPES[i].equals(FloatBlock.FLOAT_TYPE_BANNER) ? View.VISIBLE : View.GONE);
                }
            }
            if (mPage.name.equals(Options.PAGE_GAG) || mPage.name.equals(Options.PAGE_START)) {
                mFloatTypeSpinner.setVisibility(View.GONE);
            }

            for (int i = 0; i < BannerBlock.BANNERS.length; i++) {
                if (BannerBlock.BANNERS[i].equals(mPage.banner)) {
                    mBannerTypeSpinner.setSelection(i);
                }
            }

        }
    }

    @Override
    protected String getTitle() {
        return getString(R.string.editor_configure_banners);
    }
}

package com.topface.topface.ui.fragments;

import android.animation.LayoutTransition;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.R;
import com.topface.topface.data.Options;
import com.topface.topface.ui.blocks.FloatBlock;
import com.topface.topface.utils.ActionBar;

public class EditorBannersFragment extends BaseFragment{

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_editor_banners, null);

        initHeader(root);

        ViewGroup configContainer = (ViewGroup) root.findViewById(R.id.loBannersConfigurationsContainer);
        for (Options.Page page : FloatBlock.getActivityMap().values()) {
            PageConfigurator configurator = new PageConfigurator(getActivity());
            configurator.setPage(page);
            configContainer.addView(configurator);
        }

        return root;
    }

    private void initHeader(View root) {
        ActionBar actionBar = new ActionBar(root);
        actionBar.setTitleText(getString(R.string.editor_configure_banners));
        actionBar.showBackButton(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
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
            super(context,attrs);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View root = inflater.inflate(R.layout.editor_banner_configurator, this, true);
            initTitleText(root);
            initSpinnersContainer(root);
            initFloatTypeSpinner(root);
            initBannerTypeSpinner(root);

            if (android.os.Build.VERSION.SDK_INT >= 16){
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
            if (android.os.Build.VERSION.SDK_INT >= 16){
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
                    new String[]{
                            Options.BANNER_TOPFACE,
                            Options.BANNER_ADFONIC,
                            Options.BANNER_ADMOB,
                            Options.BANNER_WAPSTART,
                            Options.BANNER_ADWIRED,
                            Options.BANNER_MADNET,
                            Options.BANNER_BEGUN,
                            Options.BANNER_MOPUB,
                            Options.BANNER_INNERACTIVE,
                            Options.BANNER_MOBCLIX,
                            Options.BANNER_GAG,
                    }
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mBannerTypeSpinner.setAdapter(adapter);
            mBannerTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //TODO select float Type for page
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
                    new String[]{
                            Options.FLOAT_TYPE_BANNER,
                            Options.FLOAT_TYPE_LEADERS,
                            Options.FLOAT_TYPE_NONE
                    }
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mFloatTypeSpinner.setAdapter(adapter);
            mFloatTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    //TODO select float Type for page
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }

        public void setPage(Options.Page page) {
            mPage = page;
            mTitleText.setText(page.name);
            mPage = page;
        }
    }
}

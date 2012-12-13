package com.topface.topface.ui.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.utils.CacheProfile;

public class VipBuyFragment extends BaseFragment {

    EditSwitcher mInvisSwitcher;
    EditSwitcher mBgSwitcher;

    ProgressBar mInvisLoadBar;

    // В этот метод потом можно будет передать аргументы,
    // чтобы потом установить их с помощью setArguments();
    public static VipBuyFragment newInstance() {
        return new VipBuyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root;
        if (CacheProfile.premium) {
            root = inflater.inflate(R.layout.fragment_edit_premium, null);
            initEditVipViews(root);
        } else {
            root = inflater.inflate(R.layout.fragment_buy_premium, null);
            initBuyVipViews(root);
        }
        return root;
    }

    private void initBuyVipViews(View root) {
        RelativeLayout btnBuyMonth = (RelativeLayout) root.findViewById(R.id.fbpBuyingMonth);
        btnBuyMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyPremium();
            }
        });

        RelativeLayout btnBuyYear = (RelativeLayout) root.findViewById(R.id.fbpBuyingYear);
        btnBuyYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyPremium();
            }
        });
    }

    private void initEditVipViews(View root) {
        Button editVip = (Button) root.findViewById(R.id.fepVipEdit);
        editVip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPremium();
            }
        });

        RelativeLayout invisLayout =
                initEditItem(root,
                        R.id.fepInvis,
                        R.drawable.edit_big_btn_top_selector,
                        R.drawable.ic_vip_invisible_min,
                        getString(R.string.vip_invis),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setVisibility();
                            }
                        });
        mInvisSwitcher = new EditSwitcher(invisLayout);
        mInvisLoadBar = (ProgressBar) invisLayout.findViewById(R.id.vsiLoadBar);
        mInvisSwitcher.setChecked(CacheProfile.invisible);

//  Здесь работа с переключателем отображения VIP бэкграунда в элементах ленты,
//  так как пока решили его не использовать, из основного layouta он был удален.
//  View.gone ему нельзя было сделать, так как он был подключен с помощью include
//
//        RelativeLayout bgSwitchLayout =
//                initEditItem(root,
//                    R.id.fepMsgsBG,
//                    R.drawable.edit_big_btn_bottom_selector,
//                    R.drawable.ic_vip_message_bg_min,
//                    getString(R.string.vip_messages_bg),
//                    new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//
//                        }
//                    }
//                    );
//        mBgSwitcher = new EditSwitcher(bgSwitchLayout);
//        mBgSwitcher.setChecked(false);

        initEditItem(root,
                R.id.fepBlackList,
                R.drawable.edit_big_btn_top_selector,
                R.drawable.ic_vip_blacklist_min,
                getString(R.string.vip_black_list),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToBlackList();
                    }
                });
        initEditItem(root,
                R.id.fepProfileBG,
                R.drawable.edit_big_btn_bottom_selector,
                R.drawable.ic_vip_profile_bg,
                getString(R.string.vip_profile_bg),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        goToBgPick();
                    }
                });
    }

    private RelativeLayout initEditItem(View root, int ID, int bgId, int bgLeftId, String text, View.OnClickListener listener) {
        RelativeLayout layout = initLayouts(root, ID, bgId, bgLeftId, text);
        layout.setOnClickListener(listener);
        return layout;
    }

    private RelativeLayout initLayouts(View root, int ID, int bgId, int bgLeftId, String text) {
        RelativeLayout layout = (RelativeLayout) root.findViewById(ID);

        TextView layoutText = (TextView) layout.findViewById(R.id.tvTitle);
        layoutText.setText(text);
        layout.setBackgroundResource(bgId);
        layoutText.setCompoundDrawablesWithIntrinsicBounds(bgLeftId, 0, 0, 0);
        return layout;
    }

    private void buyPremium() {
    }

    private void editPremium() {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.default_market_link)));
        startActivity(i);
    }

    private void setVisibility() {
        mInvisSwitcher.doSwitch();
        mInvisSwitcher.setVisibility(View.GONE);
        mInvisLoadBar.setVisibility(View.VISIBLE);

        SettingsRequest request = new SettingsRequest(getActivity());
        request.invisible = mInvisSwitcher.isChecked();
        registerRequest(request);

        request.callback(new ApiHandler() {
            @Override
            public void success(ApiResponse response) throws NullPointerException {
                if (mInvisLoadBar != null && getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            CacheProfile.invisible = mInvisSwitcher.isChecked();
                            mInvisLoadBar.setVisibility(View.GONE);
                            mInvisSwitcher.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void fail(int codeError, ApiResponse response) throws NullPointerException {
                if (mInvisSwitcher != null && getActivity() != null) {
                    if (CacheProfile.invisible != mInvisSwitcher.isChecked()) {
                        //TODO: Нужно как-то оповещать пользователя, что не получилось
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mInvisSwitcher.doSwitch();
                                mInvisLoadBar.setVisibility(View.GONE);
                                mInvisSwitcher.setVisibility(View.VISIBLE);
                            }
                        });

                    }
                }
            }
        }).exec();
    }

    private void goToBlackList() {
    }

    private void goToBgPick() {
        Intent intent = new Intent(getActivity(), EditContainerActivity.class);
        intent.putExtra(Static.INTENT_REQUEST_KEY, EditContainerActivity.INTENT_EDIT_BACKGROUND);
        startActivity(intent);
    }
}

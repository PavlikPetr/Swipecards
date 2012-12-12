package com.topface.topface.ui.fragments;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.ui.edit.EditSwitcher;

public class VipBuyFragment extends BaseFragment {

    EditSwitcher mInvisSwitcher;
    EditSwitcher mBgSwitcher;

    public static VipBuyFragment newInstance() {
        return new VipBuyFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_premium, null);
//        initBuyVipViews(root);
        initEditVipViews(root);
        return root;
    }

    private void initBuyVipViews(View root) {
        RelativeLayout btnBuy1 = (RelativeLayout)root.findViewById(R.id.fbpBuying1);
        btnBuy1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyPremium();
            }
        });

        RelativeLayout btnBuy7 = (RelativeLayout)root.findViewById(R.id.fbpBuying7);
        btnBuy7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyPremium();
            }
        });

        RelativeLayout btnBuy30 = (RelativeLayout)root.findViewById(R.id.fbpBuying30);
        btnBuy30.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buyPremium();
            }
        });
    }

    private void initEditVipViews(View root) {
        TextView fepVipRest = (TextView)root.findViewById(R.id.fepVipRest);
        fepVipRest.setText(String.format(getString(R.string.vip_rest,19,"дней"))); //сделать плюральную форму штоле

        Button editVip = (Button)root.findViewById(R.id.fepVipEdit);
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
                            new View.OnClickListener(){
                                @Override
                                public void onClick(View v) {
                                    mInvisSwitcher.doSwitch();
                                }
                            });
        mInvisSwitcher = new EditSwitcher(invisLayout);
        mInvisSwitcher.setChecked(false);

        RelativeLayout bgSwitchLayout =
                initEditItem(root,
                    R.id.fepMsgsBG,
                    R.drawable.edit_big_btn_bottom_selector,
                    R.drawable.ic_vip_message_bg_min,
                    getString(R.string.vip_messages_bg),
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mBgSwitcher.doSwitch();
                        }
                    }
                    );
        mBgSwitcher = new EditSwitcher(bgSwitchLayout);
        mBgSwitcher.setChecked(false);

        initEditItem(root,
                R.id.fepBlackList,
                R.drawable.edit_big_btn_top_selector,
                R.drawable.ic_vip_blacklist_min,
                getString(R.string.vip_black_list),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

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

                    }
                });
    }

    private RelativeLayout initEditItem(View root, int ID, int bgId, int bgLeftId, String text, View.OnClickListener listener) {
        RelativeLayout layout = initLayouts(root, ID, bgId, bgLeftId, text);
        layout.setOnClickListener(listener);
        return layout;
    }

    private RelativeLayout initLayouts(View root, int ID, int bgId, int bgLeftId, String text) {
        RelativeLayout layout = (RelativeLayout)root.findViewById(ID);

        TextView layoutText = (TextView)layout.findViewById(R.id.tvTitle);
        layoutText.setText(text);
        layoutText.setBackgroundResource(bgId);
        layoutText.setCompoundDrawablesWithIntrinsicBounds(bgLeftId, 0, 0, 0);
        return layout;
    }

    public void buyPremium() {}
    public void editPremium() {}
}

package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.Utils;

public class RecoverPwdFragment extends BaseFragment{

    private Button mBtnRecover;
    private EditText mEdEmail;
    private ProgressBar mProgressBar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_recover_pwd, null);

        getActivity().findViewById(R.id.loNavigationBar).setVisibility(View.GONE);

        initViews(root);

        return root;
    }

    private void initViews(View root) {
        initEditViews(root);
        initButtonViews(root);
        initOtherViews(root);
    }

    private void initOtherViews(View root) {
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsRecoverSending);
    }

    private void initButtonViews(View root) {
        mBtnRecover = (Button) root.findViewById(R.id.btnRecover);
        mBtnRecover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mBtnRecover.setEnabled(false);

        root.findViewById(R.id.tvBackToMainAuth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
    }

    private void initEditViews(View root) {
        mEdEmail = (EditText) root.findViewById(R.id.edEmail);
        mEdEmail.addTextChangedListener(new TextWatcher() {
            String before = Static.EMPTY;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
                String after = s.toString();
                if (!before.equals(after)) {
                    mBtnRecover.setEnabled(Utils.isValidEmail(after));
                }
            }
        });
    }

    private void showButtons() {
        if (mBtnRecover != null && mProgressBar != null) {
            mBtnRecover.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void hideButtons() {
        if (mBtnRecover != null && mProgressBar != null) {
            mBtnRecover.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }
}

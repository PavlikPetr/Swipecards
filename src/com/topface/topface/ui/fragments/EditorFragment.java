package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.utils.ActionBar;

/**
 * Фрагмент админки. Доступен только для редакторов.
 */
public class EditorFragment extends BaseFragment implements View.OnClickListener {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootLayout = inflater.inflate(R.layout.fragment_editor, null);
        rootLayout.findViewById(R.id.EditorRefreshProfile).setOnClickListener(this);
        initNavigationBar(rootLayout);
        return rootLayout;
    }

    private void initNavigationBar(View view) {
        ActionBar mActionBar = getActionBar(view);
        mActionBar.showHomeButton((View.OnClickListener) getActivity());
        mActionBar.setTitleText(getString(R.string.editor_menu_admin));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.EditorRefreshProfile:
                App.sendProfileAndOptionsRequests();
                break;
        }
    }
}

package com.topface.topface.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import com.topface.topface.R;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.FormItem;

public class ProfileFormFragment extends BaseFragment {

    private ProfileFormListAdapter mProfileFormListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfileFormListAdapter = new ProfileFormListAdapter(getActivity().getApplicationContext());
        mProfileFormListAdapter.setOnFillListener(mOnFillClickListener);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        ListView formListView = (ListView) root.findViewById(R.id.fragmentFormList);
        formListView.setAdapter(mProfileFormListAdapter);

        View titleLayout = root.findViewById(R.id.usedTitle);
        titleLayout.setVisibility(View.GONE);
        (root.findViewById(R.id.ivDivider)).setVisibility(View.GONE);

        return root;
    }

    @Override
    public void onResume() {
        mProfileFormListAdapter.notifyDataSetChanged();
        super.onResume();
    }

    View.OnClickListener mOnFillClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Object formItem = view.getTag();
            if (formItem instanceof FormItem) {
                FormItem item = (FormItem) formItem;

                if (item.type == FormItem.STATUS) {
                    Intent intent = new Intent(getActivity().getApplicationContext(), EditContainerActivity.class);
                    startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_STATUS);
                } else if (item.dataId == FormItem.NO_RESOURCE_ID) {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            EditContainerActivity.class);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, item.titleId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, item.dataId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, item.value);
                    startActivityForResult(intent,
                            EditContainerActivity.INTENT_EDIT_INPUT_FORM_ITEM);
                } else {
                    Intent intent = new Intent(getActivity().getApplicationContext(),
                            EditContainerActivity.class);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, item.titleId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, item.dataId);
                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, item.value);
                    startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FORM_ITEM);
                }
            }
        }
    };
}

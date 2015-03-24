package com.topface.topface.ui.fragments.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.dialogs.EditFormItemsSelectorDialog;
import com.topface.topface.ui.edit.EditContainerActivity;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import static com.topface.topface.ui.dialogs.AbstractSelectorDialog.EditingFinishedListener;

public class ProfileFormFragment extends ProfileInnerFragment {

    private static final String POSITION = "POSITION";

    private ListView mFormListView;
    private FragmentManager fm;

    private EditingFinishedListener<FormItem> mFormEditedListener = new EditingFinishedListener<FormItem>() {
        @Override
        public void onEditingFinished(final FormItem data) {
            for (final FormItem form: CacheProfile.forms) {
                if (form.titleId == data.titleId) {
                    if (form.dataId != data.dataId) {
                        FormInfo info = new FormInfo(App.getContext(), CacheProfile.sex, Profile.TYPE_OWN_PROFILE);
                        ApiRequest request = info.getFormRequest(data);
                        registerRequest(request);
                        form.isEditing = true;
                        mProfileFormListAdapter.notifyDataSetChanged();
                        info.getFormRequest(data).callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                form.copy(data);
                                Intent intent = new Intent(CacheProfile.PROFILE_UPDATE_ACTION);
                                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                Toast.makeText(App.getContext(), R.string.general_data_error, Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void always(IApiResponse response) {
                                super.always(response);
                                form.isEditing = false;
                                mProfileFormListAdapter.notifyDataSetChanged();
                            }
                        }).exec();
                    }
                    break;
                }
            }
        }
    };

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
                    if (item.getLimitInterface() != null) {
                        intent.putExtra(EditContainerActivity.INTENT_FORM_LIMIT_VALUE, item.getLimitInterface().getLimit());
                    }
                    startActivityForResult(intent,
                            EditContainerActivity.INTENT_EDIT_INPUT_FORM_ITEM);
                } else {
//                    Intent intent = new Intent(getActivity().getApplicationContext(),
//                            EditContainerActivity.class);
//                    intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, item.titleId);
//                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, item.dataId);
//                    intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, item.value);
//                    startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FORM_ITEM);
                    if (fm != null) {
                        EditFormItemsSelectorDialog.newInstance(item.title, item, mFormEditedListener).
                                show(fm, EditFormItemsSelectorDialog.class.getName());
                    }
                }
            }
        }
    };
    private ProfileFormListAdapter mProfileFormListAdapter;
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mProfileFormListAdapter != null) {
                mProfileFormListAdapter.refillData();
                mProfileFormListAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProfileFormListAdapter = new ProfileFormListAdapter(getActivity());
        mProfileFormListAdapter.setOnFillListener(mOnFillClickListener);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        fm = getChildFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_form, container, false);
        mFormListView = (ListView) root.findViewById(R.id.fragmentFormList);
        mFormListView.setAdapter(mProfileFormListAdapter);

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedInstanceState != null) {
            mFormListView.setSelection(savedInstanceState.getInt(POSITION, 0));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(POSITION, mFormListView.getFirstVisiblePosition());
    }
}

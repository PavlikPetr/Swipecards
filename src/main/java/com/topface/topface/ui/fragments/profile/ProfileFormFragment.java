package com.topface.topface.ui.fragments.profile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.topface.framework.JsonUtils;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.City;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.ui.OwnGiftsActivity;
import com.topface.topface.ui.dialogs.EditFormItemsEditDialog;
import com.topface.topface.ui.dialogs.EditTextFormDialog;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.topface.topface.ui.dialogs.BaseEditDialog.EditingFinishedListener;

public class ProfileFormFragment extends AbstractFormFragment {

    private FragmentManager mFragmentManager;

    private List<Integer> mMainFormTypes = new ArrayList<>(Arrays.asList(
            new Integer[]{FormItem.AGE, FormItem.CITY, FormItem.NAME, FormItem.SEX, FormItem.STATUS}));

    private EditingFinishedListener<FormItem> mFormEditedListener = new EditingFinishedListener<FormItem>() {
        @Override
        public void onEditingFinished(final FormItem data) {
            for (final FormItem form : mProfileFormListAdapter.getFormItems()) {
                if (form.type == data.type && form.titleId == data.titleId) {
                    if (form.dataId != data.dataId ||
                            data.dataId == FormItem.NO_RESOURCE_ID && !TextUtils.equals(form.value, data.value)) {
                        FormInfo info = new FormInfo(App.getContext(), CacheProfile.sex, Profile.TYPE_OWN_PROFILE);
                        boolean isSettingsRequest = mMainFormTypes.contains(data.type);
                        ApiRequest request = isSettingsRequest ?
                                getSettingsRequest(data) : info.getFormRequest(data);
                        registerRequest(request);
                        form.isEditing = true;
                        mProfileFormListAdapter.notifyDataSetChanged();
                        request.callback(new ApiHandler() {
                            @Override
                            public void success(IApiResponse response) {
                                form.copy(data);
                                Intent intent = new Intent(CacheProfile.PROFILE_UPDATE_ACTION);
                                LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                Utils.showToastNotification(R.string.general_data_error, Toast.LENGTH_SHORT);
                            }

                            @Override
                            public void always(IApiResponse response) {
                                super.always(response);
                                form.isEditing = false;
                                mProfileFormListAdapter.notifyDataSetChanged();
                            }
                        });
                        if (isSettingsRequest) {
                            new ParallelApiRequest(getActivity())
                                    .addRequest(request)
                                    .addRequest(App.getProfileRequest(ProfileRequest.P_ALL))
                                    .setFrom(getClass().getSimpleName())
                                    .exec();
                        } else {
                            request.exec();
                        }
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

                if (item.type == FormItem.CITY) {
                    Intent intent = new Intent(getActivity(), CitySearchActivity.class);
                    intent.putExtra(Static.INTENT_REQUEST_KEY, CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
                    Fragment parent = getParentFragment();
                    if (parent != null) {
                        parent.startActivityForResult(intent, CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
                    } else {
                        startActivityForResult(intent, CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
                    }
                } else if (item.dataId == FormItem.NO_RESOURCE_ID && item.type != FormItem.SEX) {
                    if (mFragmentManager != null) {
                        EditTextFormDialog.newInstance(item.getTitle(), item, mFormEditedListener).
                                show(mFragmentManager, EditTextFormDialog.class.getName());
                    }
                } else {
                    if (mFragmentManager != null) {
                        EditFormItemsEditDialog.newInstance(item.getTitle(), item, mFormEditedListener).
                                show(mFragmentManager, EditFormItemsEditDialog.class.getName());
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
                mProfileFormListAdapter.setUserData(CacheProfile.getStatus(), CacheProfile.forms);
                mProfileFormListAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        mFragmentManager = getChildFragmentManager();
    }

    @Override
    protected AbstractFormListAdapter createFormAdapter(Context context) {
        mProfileFormListAdapter = new ProfileFormListAdapter(context);
        mProfileFormListAdapter.setOnEditListener(mOnFillClickListener);
        return mProfileFormListAdapter;
    }

    @Override
    protected void onGiftsClick() {
        Activity activity = getActivity();
        Intent intent = new Intent(activity, OwnGiftsActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY &&
                resultCode == Activity.RESULT_OK) {
            City city = data.getParcelableExtra(CitySearchActivity.INTENT_CITY);
            mFormEditedListener.onEditingFinished(
                    new FormItem(R.string.general_city, JsonUtils.toJson(city), FormItem.CITY));
        }
    }

    private SettingsRequest getSettingsRequest(FormItem formItem) {
        SettingsRequest settingsRequest = new SettingsRequest(getActivity());
        String value = formItem.value;
        switch (formItem.type) {
            case FormItem.NAME:
                settingsRequest.name = value;
                break;
            case FormItem.AGE:
                if (TextUtils.isDigitsOnly(value) && !TextUtils.isEmpty(value)) {
                    settingsRequest.age = Integer.valueOf(value);
                }
                break;
            case FormItem.SEX:
                settingsRequest.sex = formItem.dataId;
                break;
            case FormItem.STATUS:
                settingsRequest.status = value;
                break;
            case FormItem.CITY:
                settingsRequest.cityid = JsonUtils.fromJson(value, City.class).id;
                break;
        }
        return settingsRequest;
    }
}

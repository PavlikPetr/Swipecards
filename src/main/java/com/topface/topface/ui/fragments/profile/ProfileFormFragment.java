package com.topface.topface.ui.fragments.profile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.City;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.ParallelApiRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.state.EventBus;
import com.topface.topface.statistics.FlurryOpenEvent;
import com.topface.topface.ui.OwnGiftsActivity;
import com.topface.topface.ui.dialogs.CitySearchPopup;
import com.topface.topface.ui.dialogs.EditFormItemsEditDialog;
import com.topface.topface.ui.dialogs.EditTextFormDialog;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.RxUtils;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Subscription;
import rx.functions.Action1;

import static com.topface.topface.ui.dialogs.BaseEditDialog.EditingFinishedListener;

@FlurryOpenEvent(name = ProfileFormFragment.PAGE_NAME)
public class ProfileFormFragment extends AbstractFormFragment {

    public static final String PAGE_NAME = "profile.form";

    @Inject
    EventBus mEventBus;
    private Subscription mCitySubscription;
    private FragmentManager mFragmentManager;

    private List<Integer> mMainFormTypes = new ArrayList<>(Arrays.asList(
            new Integer[]{FormItem.AGE, FormItem.CITY, FormItem.NAME, FormItem.SEX, FormItem.STATUS}));

    private EditingFinishedListener<FormItem> mFormEditedListener = new EditingFinishedListener<FormItem>() {
        @Override
        public void onEditingFinished(final FormItem data) {
            if (mProfileFormListAdapter == null) {
                return;
            }
            for (final FormItem form : mProfileFormListAdapter.getFormItems()) {
                if (form.type == data.type && form.titleId == data.titleId) {
                    if (form.dataId != data.dataId ||
                            data.dataId == FormItem.NO_RESOURCE_ID && !TextUtils.equals(form.value, data.value)) {
                        FormInfo info = new FormInfo(App.getContext(), App.get().getProfile().sex, Profile.TYPE_OWN_PROFILE);
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
                            new ParallelApiRequest(App.getContext())
                                    .addRequest(request)
                                    .addRequest(App.getProfileRequest())
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

    @Override
    public boolean isTrackable() {
        return false;
    }

    ListView.OnItemClickListener mOnFillClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parentView, View view, int position, long id) {
            View valueView = view.findViewById(R.id.tvTitle);
            if (valueView != null && valueView.getTag() instanceof FormItem) {
                FormItem item = (FormItem) valueView.getTag();

                if (item.type == FormItem.CITY) {
                    CitySearchPopup.getInstance().show(getActivity().getSupportFragmentManager(), CitySearchPopup.TAG);
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
            if (mProfileFormListAdapter != null && isAdded()) {
                mProfileFormListAdapter.setUserData(CacheProfile.getStatus(), App.get().getProfile().forms);
                mProfileFormListAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.get().inject(this);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mUpdateReceiver, new IntentFilter(CacheProfile.PROFILE_UPDATE_ACTION));
        mFragmentManager = getActivity().getSupportFragmentManager();
        mCitySubscription = mEventBus.getObservable(City.class).subscribe(new Action1<City>() {
            @Override
            public void call(City city) {
                City profileCity = App.get().getProfile().city;
                if (city != null && profileCity != null && !profileCity.equals(city)) {
                    UserConfig config = App.getUserConfig();
                    config.setUserCityChanged(true);
                    config.saveConfig();
                    mFormEditedListener.onEditingFinished(
                            new FormItem(R.string.general_city, JsonUtils.toJson(city), FormItem.CITY));
                }
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Debug.error("City change observable failed", throwable);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mFormEditedListener = null;
        mFragmentManager = null;
        mOnFillClickListener = null;
        mProfileFormListAdapter = null;
    }

    @Override
    protected AbstractFormListAdapter createFormAdapter(Context context) {
        mProfileFormListAdapter = new ProfileFormListAdapter(context);
        return mProfileFormListAdapter;
    }

    @Override
    protected void onGiftsClick() {
        Intent intent = new Intent(getActivity().getApplicationContext(), OwnGiftsActivity.class);
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RxUtils.safeUnsubscribe(mCitySubscription);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mUpdateReceiver);
    }

    private SettingsRequest getSettingsRequest(FormItem formItem) {
        SettingsRequest settingsRequest = new SettingsRequest(App.getContext());
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getList().setOnItemClickListener(mOnFillClickListener);
    }
}

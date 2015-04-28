package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.receivers.ConnectionChangeReceiver;
import com.topface.topface.requests.AuthRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.EditorBannersActivity;
import com.topface.topface.ui.UserProfileActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.gcmutils.GCMUtils;
import com.topface.topface.utils.notifications.UserNotification;
import com.topface.topface.utils.notifications.UserNotificationManager;
import com.topface.topface.utils.offerwalls.OfferwallsManager;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import static com.topface.topface.receivers.TestNotificationsReceiver.ACTION_CANCEL_TEST_NETWORK_ERRORS;
import static com.topface.topface.receivers.TestNotificationsReceiver.ACTION_TEST_NETWORK_ERRORS_OFF;
import static com.topface.topface.receivers.TestNotificationsReceiver.ACTION_TEST_NETWORK_ERRORS_ON;
import static com.topface.topface.receivers.TestNotificationsReceiver.createBroadcastPendingIntent;
import static com.topface.topface.utils.notifications.UserNotificationManager.getInstance;

/**
 * Фрагмент админки. Доступен только для редакторов.
 */
public class EditorFragment extends BaseFragment implements View.OnClickListener {
    public static final String API_STAGE_TF = "https://api-%s.stage.tf/";
    private static final int NETWORK_ERROR_NOTIFICATION_ID = 800;
    private Spinner mApiUrl;
    private Spinner mOfferwallTypeChoose;
    private AppConfig mAppConfig;
    private Spinner mDebugModeSpinner;
    private Spinner mEditorModeSpinner;
    private SparseArray<CharSequence> mApiUrlsMap;
    private boolean mConfigInited = false;
    private EditSwitcher switcher;
    private long standard_timeout;
    private EditSwitcher switcherTestNetwork;
    private CheckBox mCustomApiCheckBox;
    private EditText mCustomApi;
    private Spinner mConnectionTypeChoose;
    private CheckBox mConnectionCheckbox;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAppConfig = App.getAppConfig();

        mApiUrlsMap = new SparseArray<>();
        mApiUrlsMap.put(0, Static.API_URL);
        mApiUrlsMap.put(1, Static.API_ALPHA_URL);
        mApiUrlsMap.put(2, Static.API_BETA_URL);
        mApiUrlsMap.put(3, Static.API_GAMMA_URL);
        mApiUrlsMap.put(4, Static.API_DELTA_URL);
        mApiUrlsMap.put(5, Static.API_500_ERROR_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_editor, null);
        // buttons
        root.findViewById(R.id.EditorRefreshProfile).setOnClickListener(this);
        root.findViewById(R.id.EditorClearSearchCache).setOnClickListener(this);
        root.findViewById(R.id.EditorConfigureBanners).setOnClickListener(this);
        root.findViewById(R.id.EditorResetSettings).setOnClickListener(this);
        root.findViewById(R.id.EditorSaveSettings).setOnClickListener(this);
        root.findViewById(R.id.EditorClearAirMessages).setOnClickListener(this);
        root.findViewById(R.id.EditorSendGCMToken).setOnClickListener(this);
        root.findViewById(R.id.EditorSendAuth).setOnClickListener(this);
        mCustomApi = (EditText) root.findViewById(R.id.EditorLogin);
        mCustomApi.setText(mAppConfig.getStageLogin());

        mCustomApiCheckBox = (CheckBox) root.findViewById(R.id.EditorApiCheckbox);
        mCustomApiCheckBox.setOnClickListener(this);
        mCustomApiCheckBox.setChecked(mAppConfig.getStageChecked());
        // попап приглашений
        ViewGroup switcherView = (ViewGroup) root.findViewById(R.id.loPopupSwitcher);
        ((TextView) switcherView.findViewWithTag("tvTitle")).setText("Показывать попап приглашений");
        switcherView.setOnClickListener(this);
        switcher = new EditSwitcher(switcherView);
        // ошиюки соединения
        ViewGroup testNetworkSwitcherView = (ViewGroup) root.findViewById(R.id.loTestNetworkSwitcher);
        ((TextView) testNetworkSwitcherView.findViewWithTag("tvTitle")).setText("Режим ошибок соединения");
        testNetworkSwitcherView.setOnClickListener(this);
        switcherTestNetwork = new EditSwitcher(testNetworkSwitcherView);

        standard_timeout = CacheProfile.getOptions().popup_timeout;

        initNavigationBar();
        initApiUrl(root);
        initDebugMode(root);
        initProfileId(root);
        initEditorMode(root);
        initUserInfo(root);
        initOfferwall(root);
        initConnections(root);
        //После инита всех элементов заполняем их значениями по умолчанию
        setConfigValues();
        mConfigInited = true;
        return root;
    }

    private void initConnections(View root) {
        final AppConfig config = App.getAppConfig();
        mConnectionTypeChoose = (Spinner) root.findViewById(R.id.spConnection);
        mConnectionCheckbox = (CheckBox) root.findViewById(R.id.debugConnectionEnabled);
        mConnectionCheckbox.setOnClickListener(this);
        mConnectionCheckbox.setChecked(config.getDebugConnectionChecked());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                new String[]{"Offline", "3G", "Edge", "WiFi"});

        mConnectionTypeChoose.setAdapter(adapter);

        int connectionType = mConnectionCheckbox.isChecked() ? config.getDebugConnection() : ConnectionChangeReceiver.getConnectionType().getInt();
        mConnectionTypeChoose.setSelection(connectionType);

        mConnectionCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    config.setDebugConnection(mConnectionTypeChoose.getSelectedItemPosition());
                }
                config.setDebugConnectionChecked(isChecked);
            }
        });

        mConnectionTypeChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mConnectionCheckbox.isChecked()) {
                    config.setDebugConnection(position);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initOfferwall(View root) {
        mOfferwallTypeChoose = (Spinner) root.findViewById(R.id.spOfferwall);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                OfferwallsManager.OFFERWALLS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mOfferwallTypeChoose.setAdapter(adapter);
        mOfferwallTypeChoose.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mConfigInited) {
                    CacheProfile.getOptions().offerwall = OfferwallsManager.OFFERWALLS[position];
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initProfileId(View rootLayout) {
        Button goProfile = (Button) rootLayout.findViewById(R.id.btnGoProfile);
        final EditText profileId = (EditText) rootLayout.findViewById(R.id.profile_id);
        goProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getActivity().startActivity(
                            UserProfileActivity.createIntent(
                                    Integer.parseInt(profileId.getText().toString()),
                                    getActivity()
                            )
                    );
                } catch (Exception e) {
                    Debug.error(e);
                }
            }
        });
    }

    private void initEditorMode(View rootLayout) {
        mEditorModeSpinner = (Spinner) rootLayout.findViewById(R.id.EditorMode);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.editor_mode_titles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mEditorModeSpinner.setAdapter(adapter);
        mEditorModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mAppConfig.setEditorMode(position);
                //Обновляем данные класса редактора
                Editor.setConfig(mAppConfig);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initDebugMode(View rootLayout) {
        mDebugModeSpinner = (Spinner) rootLayout.findViewById(R.id.EditorDebug);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.editor_debug_titles,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mDebugModeSpinner.setAdapter(adapter);
        mDebugModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //Записываем данные в конфиг
                mAppConfig.setDebugMode(position);
                //Обновляем данные в классе дебага, дабы не лазить каждый раз в конфиг
                Debug.setDebugMode(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initUserInfo(View rootLayout) {
        setInfoText(rootLayout, R.id.EditorInfoName, CacheProfile.first_name);
        setInfoText(rootLayout, R.id.EditorInfoEditorStatus,
                CacheProfile.isEditor() ?
                        getString(R.string.general_yes) :
                        getString(R.string.general_no)
        );
        setInfoText(rootLayout, R.id.EditorInfoSsid, Ssid.get());
        AuthToken authToken = AuthToken.getInstance();
        setInfoText(rootLayout, R.id.EditorInfoToken, authToken.getTokenKey());
        setInfoText(rootLayout, R.id.EditorInfoId, Integer.toString(CacheProfile.uid));
        setInfoText(
                rootLayout,
                R.id.EditorInfoSocialNetwork,
                Utils.getSocialNetworkLink(authToken.getSocialNet(), authToken.getUserSocialId())
        );
    }

    private void setInfoText(View rootLayout, int fieldId, String text) {
        TextView textView = (TextView) rootLayout.findViewById(fieldId);
        textView.setText(textView.getText() + " " + text);
    }

    private void initApiUrl(View rootLayout) {
        mApiUrl = (Spinner) rootLayout.findViewById(R.id.EditorApiUrl);

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (mConfigInited) {
                    saveApiUrl();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

        mCustomApi.addTextChangedListener(watcher);

        //Создаем стандартный адаптер
        @SuppressWarnings("unchecked") ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                Utils.sparsArrayToArrayList(mApiUrlsMap)
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mApiUrl.setAdapter(adapter);
        mApiUrl.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (mConfigInited) {
                    saveApiUrl();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void saveApiUrl() {
        try {
            String customApi = mCustomApi.getText().toString();
            if (!TextUtils.isEmpty(customApi)) {
                mAppConfig.setStageLogin(customApi, mCustomApiCheckBox.isChecked());
            }
            if (!mCustomApiCheckBox.isChecked()) {
                mAppConfig.setApiUrl(
                        (String) mApiUrl.getSelectedItem()
                );
            } else {
                if (!TextUtils.isEmpty(customApi)) {
                    mAppConfig.setApiUrl(String.format(API_STAGE_TF, customApi.trim()));
                }
            }
            showCompleteMessage();
            mAppConfig.saveConfig();
        } catch (Exception e) {
            showError();
        }
    }

    private void initNavigationBar() {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.EditorRefreshProfile:
                App.sendProfileAndOptionsRequests();
                showCompleteMessage();
                break;
            case R.id.EditorClearSearchCache:
                new SearchCacheManager().clearCache();
                showCompleteMessage();
                break;
            case R.id.EditorConfigureBanners:
                getActivity().startActivity(EditorBannersActivity.getEditorBannersIntent());
                break;
            case R.id.EditorResetSettings:
                mConfigInited = false;
                mAppConfig.resetAndSaveConfig();
                setConfigValues();
                mConfigInited = true;
                showCompleteMessage();
                break;
            case R.id.EditorSaveSettings:
                mAppConfig.saveConfig();
                showCompleteMessage();
                break;
            case R.id.loPopupSwitcher:
                switcher.doSwitch();
                if (CacheProfile.canInvite) {
                    CacheProfile.getOptions().popup_timeout = standard_timeout;
                } else {
                    CacheProfile.getOptions().popup_timeout = 1;
                }
                CacheProfile.canInvite = switcher.isChecked();

                break;
            case R.id.loTestNetworkSwitcher:
                switcherTestNetwork.doSwitch();
                UserNotificationManager notificationManager = getInstance();
                if (switcherTestNetwork.isChecked()) {
                    mAppConfig.setTestNetwork(true);
                    UserNotification.NotificationAction[] actions = new UserNotification.NotificationAction[]{
                            new UserNotification.NotificationAction(0, getString(R.string.enable),
                                    createBroadcastPendingIntent(ACTION_TEST_NETWORK_ERRORS_ON)),
                            new UserNotification.NotificationAction(0, getString(R.string.disable),
                                    createBroadcastPendingIntent(ACTION_TEST_NETWORK_ERRORS_OFF)),
                            new UserNotification.NotificationAction(R.drawable.ic_close_dialog, getString(R.string.cancel),
                                    createBroadcastPendingIntent(ACTION_CANCEL_TEST_NETWORK_ERRORS,
                                            NETWORK_ERROR_NOTIFICATION_ID)
                            ),
                    };
                    notificationManager.showNotificationWithActions(
                            "Network Errors", "all requests will be returning errors", null,
                            true,
                            actions,
                            NETWORK_ERROR_NOTIFICATION_ID
                    );
                } else {
                    mAppConfig.setTestNetwork(false);
                    notificationManager.cancelNotification(NETWORK_ERROR_NOTIFICATION_ID);
                }
                break;
            case R.id.EditorClearAirMessages:
                CacheProfile.getOptions().premiumMessages.clearPopupShowTime();
                break;
            case R.id.EditorSendGCMToken:
                new GCMUtils(getActivity()).registerGCM("");
                break;
            case R.id.EditorSendAuth:
                new AuthRequest(AuthToken.getInstance().getTokenInfo(), getActivity()).callback(new ApiHandler() {
                    @Override
                    public void success(IApiResponse response) {
                        Utils.showToastNotification(R.string.general_ready, Toast.LENGTH_LONG);
                        AuthorizationManager.saveAuthInfo(response);
                    }

                    @Override
                    public void fail(int codeError, IApiResponse response) {
                        Utils.showToastNotification(R.string.general_error, Toast.LENGTH_LONG);
                    }
                }).exec();
                break;
            case R.id.EditorApiCheckbox:
                saveApiUrl();
                break;
            default:
                showError();
        }
    }

    private void showCompleteMessage() {
        Utils.showToastNotification(getActivity().getString(R.string.editor_fragment_complete), Toast.LENGTH_SHORT);
    }

    private void showError() {
        Utils.showToastNotification(getActivity().getString(R.string.editor_fragment_error), Toast.LENGTH_SHORT);
    }

    private void setConfigValues() {
        mApiUrl.setSelection(mApiUrlsMap.indexOfValue(mAppConfig.getApiDomain()));
        mEditorModeSpinner.setSelection(mAppConfig.getEditorMode());
        mDebugModeSpinner.setSelection(mAppConfig.getDebugMode());
        switcherTestNetwork.setChecked(mAppConfig.getTestNetwork());
        switcher.setChecked(CacheProfile.canInvite);
        mOfferwallTypeChoose.setSelection(getOfferwallIndexInArray(CacheProfile.getOptions().offerwall));
    }

    @Override
    protected String getTitle() {
        return getString(R.string.editor_menu_admin);
    }

    private int getOfferwallIndexInArray(String offerwall) {
        for (int i = 0; i < OfferwallsManager.OFFERWALLS.length; i++) {
            if (offerwall.equals(OfferwallsManager.OFFERWALLS[i]))
                return i;
        }
        return -1;
    }
}

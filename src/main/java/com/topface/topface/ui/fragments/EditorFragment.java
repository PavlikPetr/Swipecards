package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Ssid;
import com.topface.topface.Static;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.edit.EditSwitcher;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.Editor;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.cache.SearchCacheManager;
import com.topface.topface.utils.config.AppConfig;
import com.topface.topface.utils.social.AuthToken;

/**
 * Фрагмент админки. Доступен только для редакторов.
 */
public class EditorFragment extends BaseFragment implements View.OnClickListener {
    private Spinner mApiUrl;
    private EditText mApiVersion;
    private EditText mApiRevision;
    private AppConfig mConfig;
    private Spinner mDebugModeSpinner;
    private Spinner mEditorModeSpinner;
    private SparseArray<CharSequence> mApiUrlsMap;
    private boolean mConfigInited = false;
    private EditSwitcher switcher;
    private long standard_timeout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConfig = App.getConfig();

        mApiUrlsMap = new SparseArray<CharSequence>();
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
        View rootLayout = inflater.inflate(R.layout.fragment_editor, null);
        rootLayout.findViewById(R.id.EditorRefreshProfile).setOnClickListener(this);
        rootLayout.findViewById(R.id.EditorClearSearchCache).setOnClickListener(this);
        rootLayout.findViewById(R.id.EditorConfigureBanners).setOnClickListener(this);
        rootLayout.findViewById(R.id.EditorResetSettings).setOnClickListener(this);
        rootLayout.findViewById(R.id.EditorSaveSettings).setOnClickListener(this);
        rootLayout.findViewById(R.id.EditorClearAirMessages).setOnClickListener(this);

        ViewGroup switcherView = (ViewGroup) rootLayout.findViewById(R.id.loPopupSwitcher);
        switcherView.setOnClickListener(this);
        switcher = new EditSwitcher(switcherView);
        switcher.setChecked(CacheProfile.canInvite);

        standard_timeout = CacheProfile.getOptions().popup_timeout;

        initNavigationBar();
        initApiUrl(rootLayout);
        initDebugMode(rootLayout);
        initEditorMode(rootLayout);
        initUserInfo(rootLayout);
        //После инита всех элементов заполняем их значениями по умолчанию
        setConfigValues();
        mConfigInited = true;
        return rootLayout;
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
                mConfig.setEditorMode(position);
                //Обновляем данные класса редактора
                Editor.setConfig(mConfig);
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
                mConfig.setDebugMode(position);
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
                Utils.getSocialNetworkLink(authToken.getSocialNet(), authToken.getUserId())
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

        mApiVersion = (EditText) rootLayout.findViewById(R.id.EditorApiVersion);
        mApiVersion.addTextChangedListener(watcher);
        mApiRevision = (EditText) rootLayout.findViewById(R.id.EditorApiRevision);
        mApiRevision.addTextChangedListener(watcher);

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
            Editable version = mApiVersion.getText();
            Editable revision = mApiRevision.getText();
            if (version != null && revision != null) {
                mConfig.setApiUrl(
                        (String) mApiUrl.getSelectedItem(),
                        Integer.parseInt(version.toString()),
                        revision.toString()
                );
                showCompleteMessage();
            } else {
                showError();
            }
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
                getActivity().startActivity(ContainerActivity.getNewIntent(ContainerActivity.INTENT_EDITOR_BANNERS));
                break;
            case R.id.EditorResetSettings:
                mConfigInited = false;
                mConfig.resetToDefault();
                setConfigValues();
                mConfigInited = true;
                showCompleteMessage();
                break;
            case R.id.EditorSaveSettings:
                mConfig.saveConfig();
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
            case R.id.EditorClearAirMessages:
                CacheProfile.getOptions().premiumMessages.clearPopupShowTime();
                break;
            default:
                showError();
        }
    }

    private void showCompleteMessage() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.editor_fragment_complete), Toast.LENGTH_SHORT).show();
    }

    private void showError() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.editor_fragment_error), Toast.LENGTH_SHORT).show();
    }

    private void setConfigValues() {
        mApiVersion.setText(Integer.toString(mConfig.getApiVersion()));
        mApiRevision.setText(mConfig.getApiRevision());
        mApiUrl.setSelection(mApiUrlsMap.indexOfValue(mConfig.getApiDomain()));
        mEditorModeSpinner.setSelection(mConfig.getEditorMode());
        mDebugModeSpinner.setSelection(mConfig.getDebugMode());
    }

    @Override
    protected String getTitle() {
        return getString(R.string.editor_menu_admin);
    }
}

package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiRequest;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.Utils;

public class EditFormItemInputFragment extends AbstractEditFragment {

    private static final String ARG_TAG_TITLE_ID = "titleId";
    private static final String ARG_TAG_DATA = "data";
    private int mTitleId;
    private String mData;
    private String mInputData = "";
    private Profile mProfile;
    private FormInfo mFormInfo;

    private EditText mEditText;

    public EditFormItemInputFragment() {
        super();
    }

    public static EditFormItemInputFragment newInstance(int titleId, String data) {
        EditFormItemInputFragment fragment = new EditFormItemInputFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TAG_TITLE_ID, titleId);
        args.putString(ARG_TAG_DATA, data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected void restoreState() {
        mTitleId = getArguments().getInt(ARG_TAG_TITLE_ID);
        mData = getArguments().getString(ARG_TAG_DATA);
        mProfile = CacheProfile.getProfile();
        mFormInfo = new FormInfo(getActivity(), mProfile.sex, mProfile.getType());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_edit_input, null, false);

        // TextEdit
        ((TextView) root.findViewById(R.id.tvTitle)).setText(mFormInfo.getFormTitle(mTitleId));
        mEditText = (EditText) root.findViewById(R.id.edText);
        mEditText.setInputType(mFormInfo.getInputType(mTitleId));
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(mFormInfo.getMaxCharacters(mTitleId));
        mEditText.setFilters(FilterArray);
        if (mData != null) {
            mEditText.append(mData);
        }

        mInputData = mData;
        mEditText.setOnEditorActionListener(getOnDoneListener());
        mEditText.addTextChangedListener(new TextWatcher() {

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
                    mInputData = after;
                    refreshSaveState();
                }
            }
        });

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        return root;
    }

    @Override
    protected boolean hasChanges() {
//        boolean result = true;
//        int value = -1;
//        try {
//            value = Integer.parseInt(mInputData);
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//        if (mTitleId==R.array.form_main_height && mTitleId == R.array.form_main_weight && value==-1) {
//            result = false;
//        }
//        if (){
//            result = false;
//        }
        return !TextUtils.equals(mData, mInputData);
    }

    private boolean isCheckNumeric() {
        return (mTitleId == R.array.form_main_height || mTitleId == R.array.form_main_weight);
    }

    private boolean isValueZero() {
        if (isCheckNumeric()) {
            if (mInputData.length()==0){
                return false;
            }
            int value = -1;
            try {
                value = Integer.parseInt(mInputData);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            if (value > 0) {
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    @Override
    protected void saveChanges(final Handler handler) {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

        if (hasChanges()) {
            if (isCheckNumeric()&&isValueZero()) {
                    mEditText.setText("");
            }else {
                for (int i = 0; i < CacheProfile.forms.size(); i++) {
                    if (CacheProfile.forms.get(i).titleId == mTitleId) {
                        final FormItem item = CacheProfile.forms.get(i);
                        FormItem newItem;
                        mInputData = Utils.getText(mEditText).trim();
                        newItem = new FormItem(item.titleId, mInputData, FormItem.DATA);

                        mFormInfo.fillFormItem(newItem);

                        prepareRequestSend();
                        ApiRequest request = mFormInfo.getFormRequest(newItem);
                        registerRequest(request);
                        request.callback(new ApiHandler() {

                            @Override
                            public void success(IApiResponse response) {
                                item.value = TextUtils.isEmpty(mInputData) ? null : mInputData;
                                mFormInfo.fillFormItem(item);
                                getActivity().setResult(Activity.RESULT_OK);
                                mData = mInputData;
                                finishRequestSend();
                                if (handler == null) getActivity().finish();
                                else handler.sendEmptyMessage(0);
                                LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(CacheProfile.PROFILE_UPDATE_ACTION));
                            }

                            @Override
                            public void fail(int codeError, IApiResponse response) {
                                if (isCheckNumeric()) {
                                    warnEditingFailedHeightWeight(handler);
                                } else {
                                    warnEditingFailed(handler);
                                }
                            }
                        }).exec();
                        break;
                    }
                }
            }
        } else {
            if (handler == null) getActivity().finish();
            else handler.sendEmptyMessage(0);
        }
    }

    @Override
    protected void lockUi() {
        mEditText.setEnabled(false);
    }

    @Override
    protected void unlockUi() {
        mEditText.setEnabled(true);
    }

    @Override
    protected void refreshSaveState() {
        super.refreshSaveState();

    }

    @Override
    protected void prepareRequestSend() {
        super.prepareRequestSend();

    }

    @Override
    protected void finishRequestSend() {
        super.finishRequestSend();

    }

    @Override
    protected String getTitle() {
        return getString(R.string.edit_title);
    }

    @Override
    protected String getSubtitle() {
        return mFormInfo.getFormTitle(mTitleId);
    }
}

package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Profile;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.QuestionaryRequest;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormInfo;
import com.topface.topface.utils.FormItem;

public class EditFormItemInputFragment extends AbstractEditFragment {

    private int mTitleId;
    private String mData;
    private String mInputData;
    private Profile mProfile;
    private FormInfo mFormInfo;

    private EditText mEditText;

    public EditFormItemInputFragment() {
    }

    public EditFormItemInputFragment(int titleId, String data) {
        mTitleId = titleId;
        mData = data;
        mProfile = CacheProfile.getProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFormInfo = new FormInfo(getActivity().getApplicationContext(), mProfile);

        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.item_edit_form_input, null, false);

        // Navigation bar
        ((TextView) getActivity().findViewById(R.id.tvNavigationTitle))
                .setText(R.string.edit_title);
        TextView subTitle = (TextView) getActivity().findViewById(R.id.tvNavigationSubtitle);
        subTitle.setVisibility(View.VISIBLE);
        subTitle.setText(R.string.edit_bg_photo);

        ((Button) getActivity().findViewById(R.id.btnNavigationHome)).setVisibility(View.GONE);
        mBackButton = (Button) getActivity().findViewById(R.id.btnNavigationBackWithText);
        mBackButton.setVisibility(View.VISIBLE);
        mBackButton.setText(R.string.navigation_edit);
        mBackButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        mSaveButton = (Button) getActivity().findViewById(R.id.btnNavigationRightWithText);
        mSaveButton.setText(getResources().getString(R.string.navigation_save));
        mSaveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveChanges(null);
            }
        });

        mRightPrsBar = (ProgressBar) getActivity().findViewById(R.id.prsNavigationRight);

        // TextEdit
        ((TextView) root.findViewById(R.id.tvTitle)).setText(mFormInfo.getFormTitle(mTitleId));
        mEditText = (EditText) root.findViewById(R.id.edText);
        mEditText.setText(mData);
        mEditText.setInputType(mFormInfo.getInputType(mTitleId));
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
        return !mData.equals(mInputData);
    }

    @Override
    protected void saveChanges(Handler handler) {
        InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);

        if (hasChanges()) {
            for (int i = 0; i < CacheProfile.forms.size(); i++) {
                if (CacheProfile.forms.get(i).titleId == mTitleId) {
                    final FormItem item = CacheProfile.forms.get(i);
                    FormItem newItem;
                    mInputData = mEditText.getText().toString();
                    newItem = new FormItem(item.titleId, mInputData, FormItem.DATA);

                    mFormInfo.fillFormItem(newItem);

                    prepareRequestSend();
                    QuestionaryRequest request = mFormInfo.getFormRequest(newItem);
                    registerRequest(request);
                    request.callback(new ApiHandler() {

                        @Override
                        public void success(ApiResponse response) throws NullPointerException {
                            item.value = mInputData;
                            mFormInfo.fillFormItem(item);
                            getActivity().setResult(Activity.RESULT_OK);
                            mData = mInputData;
                            finishRequestSend();
                        }

                        @Override
                        public void fail(int codeError, ApiResponse response)
                                throws NullPointerException {
                            getActivity().setResult(Activity.RESULT_CANCELED);
                            finishRequestSend();
                        }
                    }).exec();
                    break;
                }
            }
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
}

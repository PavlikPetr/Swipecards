package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.data.Register;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.RegisterRequest;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.DateUtils;
import com.topface.topface.utils.EasyTracker;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.social.STAuthMails;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;
import butterknife.OnItemSelected;

public class RegistrationFragment extends BaseFragment {

    public static final String INTENT_LOGIN = "registration_login";
    public static final String INTENT_PASSWORD = "registration_password";
    public static final String INTENT_USER_ID = "registration_user_id";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String NAME = "name";
    public static final String SEX = "sex";
    public static final String BIRTHDAY = "birthday";

    private static final int START_SHIFT = 33;

    private Date mBirthday;
    private int mSex;
    private Timer mTimer = new Timer();

    @Bind(R.id.ivShowPassword)
    ImageButton mShowPassword;
    @Bind(R.id.etMail)
    EditText mEdEmail;
    @Bind(R.id.edPassword)
    EditText mEdPassword;
    @Bind(R.id.etName)
    EditText mEdName;

    @Bind(R.id.spnSex)
    Spinner mSpnSex;

    @OnItemSelected(R.id.spnSex)
    public void sexSelected(int position) {
        mSex = position;
    }

    @Bind(R.id.tvRedAlert)
    TextView mRedAlertView;

    @OnEditorAction(R.id.etName)
    public boolean nameActionListener(int actionId) {
        boolean handled = false;
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            if (mBirthdayText != null) {
                handled = true;
                mBirthdayText.performClick();
            }
        }
        return handled;
    }

    @Bind(R.id.btnStartChat)
    Button mBtnRegister;

    @OnClick(R.id.btnStartChat)
    public void startChatClick() {
        removeRedAlert();
        hideButtons();
        setEditing(false);
        Utils.hideSoftKeyboard(getActivity(), mEdEmail, mEdName);
        sendRegistrationRequest();
    }

    @Bind(R.id.tvBirthday)
    TextView mBirthdayText;

    @OnClick(R.id.tvBirthday)
    public void birthdayClick() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -START_SHIFT);
        DatePickerFragment datePicker = DatePickerFragment.newInstance(c.get(Calendar.YEAR)
                , c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        datePicker.setOnDateSetListener(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                final Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -Static.MIN_AGE);
                long maxDate = c.getTimeInMillis();

                c.add(Calendar.YEAR, -(Static.MAX_AGE - Static.MIN_AGE));
                long minDate = c.getTimeInMillis();

                if (DatePickerFragment.isValidDate(year, monthOfYear, dayOfMonth, minDate, maxDate)) {
                    Date date = DateUtils.getDate(year, monthOfYear, dayOfMonth);
                    String dateStr = DateFormat.getDateFormat(getActivity()).format(date);
                    mBirthdayText.setText(dateStr);
                    mBirthday = date;
                }
            }
        });
        datePicker.show(getChildFragmentManager(), DatePickerFragment.TAG);
    }

    public static RegistrationFragment getInstance() {
        return new RegistrationFragment();
    }

    @Override
    protected String getTitle() {
        return getActivity().getString(R.string.create_account);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EMAIL, mEdEmail.getText().toString());
        outState.putString(PASSWORD, mEdPassword.getText().toString());
        outState.putString(NAME, mEdName.getText().toString());
        outState.putInt(SEX, mSex);
        outState.putString(BIRTHDAY, mBirthday.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected int getStatusBarColor() {
        return R.color.status_bar_dark_gray_color;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setNeedTitles(true);
        View root = inflater.inflate(R.layout.topface_registration, null);
        ButterKnife.bind(this, root);
        initViews();
        if (savedInstanceState != null) {
            mEdEmail.setText(savedInstanceState.getString(EMAIL));
            mEdPassword.setText(savedInstanceState.getString(PASSWORD));
            mEdName.setText(savedInstanceState.getString(NAME));
            mSex = savedInstanceState.getInt(SEX);
            mBirthdayText.setText(savedInstanceState.getString(BIRTHDAY));
        }
        return root;
    }

    private void initViews() {
        mShowPassword.setOnClickListener(new TopfaceAuthFragment.HidePasswordController(mShowPassword, mEdPassword));
        initEditTextViews();
        initSexChangeSpinner();
    }

    private void initSexChangeSpinner() {
        ArrayList<String> data = new ArrayList<>();
        data.add(getActivity().getString(R.string.im_girl));
        data.add(getActivity().getString(R.string.im_boy));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), R.layout.sex_choise_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnSex.setAdapter(adapter);
        mSpnSex.setPrompt(getActivity().getString(R.string.u_sex));
    }

    private void initEditTextViews() {
        mEdEmail.setText(getArguments().getString(RecoverPwdFragment.ARG_EMAIL));
    }

    private void sendRegistrationRequest() {
        final String emailLogin = Utils.getText(mEdEmail).trim();
        final String name = Utils.getText(mEdName).trim();
        final String password = Utils.getText(mEdPassword);

        setEditing(false);
        if (TextUtils.isEmpty(emailLogin) || TextUtils.isEmpty(name) || mSex == -1 || mBirthday == null
                || password.trim().length() == 0) {
            redAlert(R.string.empty_fields);
            showButtons();
            setEditing(true);
        } else {
            RegisterRequest request = new RegisterRequest(getActivity().getApplicationContext(), emailLogin, password, name,
                    DateUtils.getSeconds(mBirthday), mSex);
            registerRequest(request);
            request.callback(new DataApiHandler<Register>() {

                @Override
                protected void success(Register data, IApiResponse response) {
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_LOGIN, emailLogin);
                    intent.putExtra(INTENT_PASSWORD, password);
                    intent.putExtra(INTENT_USER_ID, data.getUserId());

                    //Запоминаем email после регистрации, что бы помочь при логине
                    STAuthMails.addEmail(emailLogin);

                    EasyTracker.sendEvent(
                            "Registration",
                            "SubmitRegister",
                            mEdPassword.getVisibility() == View.VISIBLE ? "PasswordEntered" : "PasswordGenerated",
                            1L
                    );

                    CacheProfile.onRegistration(getActivity().getApplicationContext());
                    getActivity().setResult(Activity.RESULT_OK, intent);
                    getActivity().finish();
                }

                @Override
                protected Register parseResponse(ApiResponse response) {
                    return new Register(response);
                }

                @Override
                public void fail(int codeError, IApiResponse response) {
                    switch (codeError) {
                        case ErrorCodes.INCORRECT_LOGIN:
                            redAlert(R.string.incorrect_email);
                            break;
                        case ErrorCodes.USER_ALREADY_REGISTERED:
                            redAlert(R.string.email_already_registered);
                            break;
                        case ErrorCodes.MISSING_REQUIRE_PARAMETER:
                            redAlert(R.string.empty_fields);
                            break;
                        case ErrorCodes.INCORRECT_PASSWORD:
                            redAlert(R.string.wrong_password_format);
                            break;
                        case ErrorCodes.INCORRECT_VALUE:
                        default:
                            Activity activity = getActivity();
                            if (activity != null) {
                                Toast.makeText(activity, R.string.general_error_try_again_later, Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }

                @Override
                public void always(IApiResponse response) {
                    showButtons();
                    setEditing(true);
                }
            });
            request.exec();
        }
    }

    private void redAlert(String text) {
        if (text != null) {
            mRedAlertView.setText(text);
        }
        mRedAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                R.anim.slide_down_fade_in));
        mRedAlertView.setVisibility(View.VISIBLE);
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isAdded()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeRedAlert();
                        }
                    });
                }
            }
        }, Static.RED_ALERT_APPEARANCE_TIME);
    }

    private void redAlert(int resId) {
        redAlert(getString(resId));
    }

    private void removeRedAlert() {
        if (mRedAlertView.getVisibility() == View.VISIBLE) {
            if (isAdded()) {
                mRedAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity(),
                        android.R.anim.fade_out));
            }
            mRedAlertView.setVisibility(View.INVISIBLE);
        }
    }

    private void showButtons() {
        if (mBtnRegister != null) {
            mBtnRegister.setVisibility(View.VISIBLE);
        }
    }

    private void hideButtons() {
        if (mBtnRegister != null) {
            mBtnRegister.setVisibility(View.INVISIBLE);
        }
    }

    private void setEditing(boolean enable) {
        mEdEmail.setEnabled(enable);
        mEdName.setEnabled(enable);
        mEdPassword.setEnabled(enable);
        mBirthdayText.setEnabled(enable);
    }
}

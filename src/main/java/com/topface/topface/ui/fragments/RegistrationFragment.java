package com.topface.topface.ui.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.google.analytics.tracking.android.EasyTracker;
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
import com.topface.topface.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class RegistrationFragment extends BaseFragment implements DatePickerDialog.OnDateSetListener {

    public static final String INTENT_LOGIN = "registration_login";
    public static final String INTENT_PASSWORD = "registration_password";
    public static final String INTENT_USER_ID = "registration_user_id";

    private static final int START_SHIFT = 33;

    private EditText mEdEmail;
    private EditText mEdName;
    private EditText mEdPassword;
    private TextView mBirthdayText;
    private SexController mSexController;
    private TextView mRedAlertView;
    private ProgressBar mProgressBar;
    private Button mBtnRegister;

    private Date mBirthday;
    private int mYear;
    private int mMonthOfYear;
    private int mDayOfMonth;
    private Timer mTimer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedTitles(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_create_account, null);

        initViews(root);
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -START_SHIFT);
        mYear = c.get(Calendar.YEAR);
        mMonthOfYear = c.get(Calendar.MONTH);
        mDayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSexController != null) mSexController.switchSex(Static.BOY);
    }

    private void initViews(View root) {
        initEditTextViews(root);
        initBirthdayViews(root);
        initSexViews(root);
        initButtons(root);
        initOtherViews(root);
    }

    private void initOtherViews(View root) {
        mRedAlertView = (TextView) root.findViewById(R.id.tvRedAlert);
        mProgressBar = (ProgressBar) root.findViewById(R.id.prsRegistrationSending);
    }

    private void initEditTextViews(View root) {
        mEdEmail = (EditText) root.findViewById(R.id.edEmail);
        mEdName = (EditText) root.findViewById(R.id.edName);
        mEdPassword = (EditText) root.findViewById(R.id.edPassword);
    }

    private void initBirthdayViews(View root) {
        View birthday = root.findViewById(R.id.loBirthday);
        mBirthdayText = (TextView) birthday.findViewById(R.id.tvBirthday);
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment datePicker = DatePickerFragment.newInstance(mYear, mMonthOfYear, mDayOfMonth);
                datePicker.setOnDateSetListener(RegistrationFragment.this);
                datePicker.show(getChildFragmentManager(), DatePickerFragment.TAG);
            }
        });
    }

    private void initSexViews(View root) {
        mSexController = new SexController(getActivity().getApplicationContext(), root.findViewById(R.id.loSex));
    }

    private void initButtons(final View root) {
        mBtnRegister = (Button) root.findViewById(R.id.btnRegister);
        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeRedAlert();
                hideButtons();
                Utils.hideSoftKeyboard(getActivity(), mEdEmail, mEdName);
                sendRegistrationRequest();
            }
        });

        root.findViewById(R.id.tvBackToMainAuth).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.hideSoftKeyboard(getActivity(), mEdName, mEdEmail);
                getActivity().finish();
            }
        });
    }

    private void sendRegistrationRequest() {
        final String email = mEdEmail.getText().toString();
        final String name = mEdName.getText().toString();
        final String password = mEdPassword.getText().toString();
        int sex = mSexController.getSex();

        if (TextUtils.isEmpty(email.trim()) || TextUtils.isEmpty(name.trim()) || sex == -1 || mBirthday == null
                || password.trim().length() == 0) {
            redAlert(R.string.empty_fields);
            showButtons();
        } else {
            RegisterRequest request = new RegisterRequest(getActivity().getApplicationContext(), email, password, name,
                    DateUtils.getSeconds(mBirthday), sex);
            registerRequest(request);
            request.callback(new DataApiHandler<Register>() {

                @Override
                protected void success(Register data, IApiResponse response) {
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_LOGIN, email);
                    intent.putExtra(INTENT_PASSWORD, password);
                    intent.putExtra(INTENT_USER_ID, data.getUserId());

                    EasyTracker.getTracker().sendEvent(
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
                        case ErrorCodes.INCORRECT_VALUE:
                        default:
                            break;
                    }
                }

                @Override
                public void always(IApiResponse response) {
                    showButtons();
                }
            });
            request.exec();
        }
    }

    private void redAlert(String text) {
        if (text != null) {
            mRedAlertView.setText(text);
        }
        mRedAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
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
            mRedAlertView.setAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                    android.R.anim.fade_out));
            mRedAlertView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        final Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, -Static.MIN_AGE);
        long maxDate = c.getTimeInMillis();

        c.add(Calendar.YEAR, -(Static.MAX_AGE - Static.MIN_AGE));
        long minDate = c.getTimeInMillis();

        if (DatePickerFragment.isValidDate(year, monthOfYear, dayOfMonth, minDate, maxDate)) {
            Date date = DateUtils.getDate(year, monthOfYear, dayOfMonth);
            mYear = year;
            mMonthOfYear = monthOfYear;
            mDayOfMonth = dayOfMonth;
            String dateStr = DateFormat.getDateFormat(getActivity().getApplicationContext()).format(date);
            mBirthdayText.setText(dateStr);
            mBirthday = date;
        }
    }

    private void showButtons() {
        if (mBtnRegister != null && mProgressBar != null) {
            mBtnRegister.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void hideButtons() {
        if (mBtnRegister != null && mProgressBar != null) {
            mBtnRegister.setVisibility(View.INVISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    private static class SexController {

        private Context mContext;

        private TextView mBoy;
        private TextView mGirl;
        private int mSex;

        public SexController(Context context, View sexViewsRoot) {
            mBoy = (TextView) sexViewsRoot.findViewById(R.id.tvBoy);
            mBoy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchSex(Static.BOY);
                }
            });

            mGirl = (TextView) sexViewsRoot.findViewById(R.id.tvGirl);
            mGirl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchSex(Static.GIRL);
                }
            });

            mSex = -1;
            mContext = context;
        }

        public void switchSex(int sex) {
            mSex = sex;
            switch (mSex) {
                case Static.BOY:
                    mBoy.setCompoundDrawablesWithIntrinsicBounds(
                            mContext.getResources().getDrawable(R.drawable.ic_sex_male), null, null, null);
                    mGirl.setCompoundDrawablesWithIntrinsicBounds(
                            mContext.getResources().getDrawable(R.drawable.ic_sex), null, null, null);
                    break;
                case Static.GIRL:
                    mBoy.setCompoundDrawablesWithIntrinsicBounds(
                            mContext.getResources().getDrawable(R.drawable.ic_sex), null, null, null);
                    mGirl.setCompoundDrawablesWithIntrinsicBounds(
                            mContext.getResources().getDrawable(R.drawable.ic_sex_female), null, null, null);
                    break;
            }
        }

        public int getSex() {
            return mSex;
        }

    }
}

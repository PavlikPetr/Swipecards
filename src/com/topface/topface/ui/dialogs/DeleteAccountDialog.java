package com.topface.topface.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileDelete;
import com.topface.topface.requests.handlers.ApiHandler;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.Settings;
import com.topface.topface.utils.social.AuthToken;
import com.topface.topface.utils.social.AuthorizationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DeleteAccountDialog extends DialogFragment implements View.OnClickListener {

    public static final String TAG = "com.topface.topface.ui.dialogs.DeleteAccountDialog_TAG";
    private EditText mExtraMessage;
    private Spinner mSpinner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.dialog_delete_account, container, false);

        setTransparentBackground();
        getDialog().setCanceledOnTouchOutside(false);

        root.findViewById(R.id.btnClose).setOnClickListener(this);
        root.findViewById(R.id.btnCancel).setOnClickListener(this);
        root.findViewById(R.id.btnOk).setOnClickListener(this);
        ((ImageViewRemote) root.findViewById(R.id.ivAvatar)).setPhoto(CacheProfile.photo);
        ((TextView) root.findViewById(R.id.tvProfile)).setText(CacheProfile.getUserNameAgeString());

        TextView socialAccountText = ((TextView) root.findViewById(R.id.tvAccount));
        setSocialAccountText(socialAccountText);

        ((TextView) root.findViewById(R.id.tvWarningText)).setText(R.string.delete_account_warning);

        mExtraMessage = (EditText) root.findViewById(R.id.edReasonMessage);
        mExtraMessage.setVisibility(View.VISIBLE);

        mSpinner = (Spinner) root.findViewById(R.id.spReason);
        final String[] reasons = getResources().getStringArray(R.array.delete_account_reasons);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                reasons
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        // last reason points to extra message
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < parent.getCount() - 1) {
                    mExtraMessage.setVisibility(View.GONE);
                } else {
                    mExtraMessage.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mSpinner.setSelection(reasons.length - 1);
            }
        });
        mSpinner.setSelection(reasons.length - 1);

        return root;
    }

    private void setSocialAccountText(final TextView textView) {
        Settings.getInstance().getSocialAccountName(textView);
        Settings.getInstance().getSocialAccountIcon(textView);
    }

    private void setTransparentBackground() {
        ColorDrawable color = new ColorDrawable(Color.BLACK);
        color.setAlpha(175);
        getDialog().getWindow().setBackgroundDrawable(color);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
            case R.id.btnCancel:
                closeDialog();
                break;
            case R.id.btnOk:
                boolean checked = false;
                if (mExtraMessage.getVisibility() == View.VISIBLE) {
                    if (mExtraMessage.getText().toString().trim().isEmpty()) {
                        Toast.makeText(getActivity(), R.string.empty_fields, Toast.LENGTH_LONG).show();
                        break;
                    }
                }

                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.settings_delete_account)
                        .setMessage(R.string.delete_account_are_you_sure)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progress = new ProgressDialog(getActivity());
                                progress.setTitle(R.string.settings_delete_account);
                                progress.setMessage(getActivity().getResources().getString(R.string.settings_delete_account));
                                progress.show();

                                ProfileDelete request = new ProfileDelete(mSpinner.getSelectedItemPosition() + 1,
                                        mExtraMessage.getText().toString().trim(), getActivity());
                                request.callback(new ApiHandler() {
                                    @Override
                                    public void success(ApiResponse response) {
                                        if (response.isCompleted()) {
                                            DeleteAccountDialog.saveDeletedAccountToken();
                                            AuthorizationManager.logout(getActivity());
                                        } else {
                                            fail(response.getResultCode(), response);
                                        }
                                    }

                                    @Override
                                    public void fail(int codeError, ApiResponse response) {
                                        Toast.makeText(getActivity(), R.string.delete_account_error, Toast.LENGTH_SHORT)
                                                .show();
                                    }

                                    @Override
                                    public void always(ApiResponse response) {
                                        super.always(response);
                                        progress.dismiss();
                                    }
                                }).exec();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                closeDialog();
                            }
                        }).show();
                break;
        }
    }

    private static void saveDeletedAccountToken() {
        SharedPreferences preferences = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        AuthToken authToken = AuthToken.getInstance();
        String deletedId = authToken.getUserId();
        String prefKey = Static.EMPTY;
        if (authToken.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_FB_IDS;
        } else if (authToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_VK_IDS;
        } else if (authToken.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_TF_IDS;
        }
        // add new token
        String ids = preferences.getString(prefKey, Static.EMPTY);
        List<String> arrIds = new ArrayList<String>(Arrays.asList(ids.split(",")));
        arrIds.add(deletedId);
        ids = TextUtils.join(",", arrIds);
        // save new tokens' list to preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(prefKey, ids).commit();
    }

    public static void removeDeletedAccountToken(AuthToken token) {
        SharedPreferences preferences = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        String userId = token.getUserId();
        String prefKey = Static.EMPTY;
        if (token.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_FB_IDS;
        } else if (token.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_VK_IDS;
        } else if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_TF_IDS;
        }
        // add new token
        String ids = preferences.getString(prefKey, Static.EMPTY);
        List<String> arrIds = new ArrayList<String>(Arrays.asList(ids.split(",")));
        String delete = null;
        for (String item : arrIds) {
            if (item.equals(userId)) {
                delete = item;
            }
        }
        if (arrIds.size() > 1) {
            if (delete != null) arrIds.remove(delete);
            ids = TextUtils.join(",", arrIds);
        } else {
            if (delete != null) ids = Static.EMPTY;
        }
        // save new tokens' list to preferences
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(prefKey, ids).commit();
    }

    public static boolean hasDeltedAccountToken(AuthToken token) {
        SharedPreferences preferences = App.getContext().getSharedPreferences(Static.PREFERENCES_TAG_PROFILE, Context.MODE_PRIVATE);
        String userId = token.getUserId();
        String prefKey = Static.EMPTY;
        if (token.getSocialNet().equals(AuthToken.SN_FACEBOOK)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_FB_IDS;
        } else if (token.getSocialNet().equals(AuthToken.SN_VKONTAKTE)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_VK_IDS;
        } else if (token.getSocialNet().equals(AuthToken.SN_TOPFACE)) {
            prefKey = Static.PREFERENCES_DELETED_ACCOUNTS_TF_IDS;
        }
        String ids = preferences.getString(prefKey, Static.EMPTY);
        for (String item : ids.split(",")) {
            if (item.equals(userId)) {
                return true;
            }
        }
        return false;
    }

    private void closeDialog() {
        final Dialog dialog = getDialog();
        if (dialog != null) dialog.dismiss();
    }

    public static DeleteAccountDialog newInstance() {
        DeleteAccountDialog dialog = new DeleteAccountDialog();
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Theme_Topface);
        return dialog;
    }
}

package com.topface.topface.ui.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.topface.framework.utils.Debug;
import com.topface.topface.R;
import com.topface.topface.data.Leader;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.ContainerActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.fragments.BaseFragment.FragmentId;
import com.topface.topface.ui.fragments.ChatFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.EasyTracker;

import org.json.JSONException;
import org.json.JSONObject;

public class LeadersDialog extends AbstractModalDialog {

    private Leader user;

    public static LeadersDialog newInstance(Leader user) {
        LeadersDialog dialog = new LeadersDialog();
        Bundle args = new Bundle();
        try {
            args.putString("user", user.toJson().toString());
        } catch (JSONException e) {
            Debug.error(e);
        }
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    protected void initContentViews(View root) {
        Bundle arguments = getArguments();
        String userJsonString = arguments.getString("user");
        try {
            JSONObject object = new JSONObject(userJsonString);
            user = new Leader(object);
            ImageViewRemote photo = (ImageViewRemote) root.findViewById(R.id.leaderPhoto);
            photo.setPhoto(user.photo);
            TextView name = (TextView) root.findViewById(R.id.leaderName);
            name.setText(user.getNameAndAge());
            TextView status = (TextView) root.findViewById(R.id.leaderStatus);
            status.setText(user.getStatus());
            TextView city = (TextView) root.findViewById(R.id.leaderCity);
            city.setText(user.city.name);
            // установка иконки онлайн
            name.setCompoundDrawablesWithIntrinsicBounds(
                    user.online ? R.drawable.ico_online : 0,
                    0, 0, 0
            );
            final Dialog dialog = getDialog();
            Button profile = (Button) root.findViewById(R.id.leaderProfile);
            profile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (user.id == CacheProfile.uid) {
                        ((NavigationActivity) getActivity()).showFragment(FragmentId.F_PROFILE);
                        dialog.dismiss();
                    } else {
                        startActivity(ContainerActivity.getProfileIntent(user.id, LeadersDialog.class, getActivity()));
                    }
                }
            });
            Button message = (Button) root.findViewById(R.id.leaderMessage);
            message.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openChat();
                }
            });
            message.setVisibility((CacheProfile.premium || !CacheProfile.getOptions().block_chat_not_mutual) ? View.VISIBLE : View.GONE);
        } catch (JSONException e) {
            Debug.error(e);
        }
    }

    @Override
    protected int getContentLayoutResId() {
        return R.layout.leaders_dialog;
    }

    @Override
    protected void onCloseButtonClick(View v) {
        getDialog().dismiss();
    }

    private void openChat() {
        Intent intent = new Intent(getActivity(), ContainerActivity.class);
        intent.putExtra(ChatFragment.INTENT_USER_ID, user.id);
        intent.putExtra(ChatFragment.INTENT_USER_NAME, user.first_name);
        intent.putExtra(ChatFragment.INTENT_USER_SEX, user.sex);
        intent.putExtra(ChatFragment.INTENT_USER_AGE, user.age);
        intent.putExtra(ChatFragment.INTENT_USER_CITY, user.city.name);
        intent.putExtra(BaseFragmentActivity.INTENT_PREV_ENTITY, ((Object) this).getClass().getSimpleName());
        getActivity().startActivityForResult(intent, ContainerActivity.INTENT_CHAT_FRAGMENT);
        EasyTracker.sendEvent("Leaders", "Dialog", "Chat", 1L);
    }
}

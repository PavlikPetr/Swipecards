package com.topface.topface.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.topface.topface.R;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.SMSInviteActivity;
import com.topface.topface.utils.ContactsProvider;

import java.util.ArrayList;

public class SMSInviteFragment extends BaseFragment {
    ListView contactsView;
    private ArrayList<ContactsProvider.Contact> data;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_sms_invite, container, false);
        getSupportActionBar().setTitle(R.string.sms_invite_subtitle);
        contactsView = (ListView) root.findViewById(R.id.contactsList);
        contactsView.addHeaderView(getActivity().getLayoutInflater().inflate(R.layout.header_sms_invite, null));
        //Получаем список контактов из аргументов. Если он не пришел, закрываем фрагмент.
        Bundle extras = getArguments();
        if (extras != null) {
            data = extras.getParcelableArrayList(SMSInviteActivity.CONTACTS_DATA);
        }

        if (data == null) {
            ((BaseFragmentActivity) getActivity()).close(this, false);
        }
        return root;
    }

}

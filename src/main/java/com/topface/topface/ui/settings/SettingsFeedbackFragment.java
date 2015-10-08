package com.topface.topface.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.ui.fragments.BaseFragment;

public class SettingsFeedbackFragment extends BaseFragment implements AdapterView.OnItemClickListener {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_feedback_list, null);
        ListView listView = ((ListView) root.findViewById(R.id.feedback_list));
        listView.setOnItemClickListener(this);
        listView.setAdapter(new FeedbackAdapter(getActivity()));
        return root;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_feedback);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = null;
        switch (position) {
            case 0:
                intent = SettingsContainerActivity.getFeedbackMessageIntent(
                        getActivity(),
                        FeedbackMessageFragment.FeedbackType.ERROR_MESSAGE
                );
                break;
            case 1:
                intent = SettingsContainerActivity.getFeedbackMessageIntent(
                        getActivity(),
                        FeedbackMessageFragment.FeedbackType.DEVELOPERS_MESSAGE
                );
                break;
            case 2:
                intent = SettingsContainerActivity.getFeedbackMessageIntent(
                        getActivity(),
                        FeedbackMessageFragment.FeedbackType.PAYMENT_MESSAGE
                );
                break;
        }
        if (intent != null) {
            startActivityForResult(intent, SettingsContainerActivity.INTENT_SEND_FEEDBACK);
        }
    }

    private static class FeedbackAdapter extends BaseAdapter {

        private String[] items;
        private LayoutInflater mInflater;

        public FeedbackAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            items = App.getContext().getResources().getStringArray(R.array.feedback_items);
        }

        @Override
        public int getCount() {
            return items.length;
        }

        @Override
        public Object getItem(int position) {
            return items[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.feedback_list_item, parent, false);
            }
            ((TextView) convertView.findViewById(R.id.feedback_message))
                    .setText(items[position]);
            return convertView;

        }
    }

}

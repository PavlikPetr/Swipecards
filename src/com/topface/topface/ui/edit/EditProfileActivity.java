package com.topface.topface.ui.edit;

import android.app.Activity;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import com.topface.topface.GCMUtils;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.requests.ApiHandler;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.ProfileRequest;
import com.topface.topface.requests.SettingsRequest;
import com.topface.topface.ui.BaseFragmentActivity;
import com.topface.topface.ui.CitySearchActivity;
import com.topface.topface.ui.NavigationActivity;
import com.topface.topface.ui.edit.EditProfileItem.Type;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.ui.views.ImageViewRemote;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.FormItem;
import com.topface.topface.utils.http.ProfileBackgrounds;

import java.util.LinkedList;

public class EditProfileActivity extends BaseFragmentActivity implements OnClickListener {

    private ListView mEditsListView;
    private EditsAdapter mAdapter;
    private LinkedList<EditProfileItem> mEditItems;
    private TextView mEditName;
    private TextView mEditAge;
    private ImageView mEditSex;
    private Button mEditCity;
    private ImageViewRemote mProfilePhoto;
    private TextView editProfileMsg;

    private boolean hasStartedFromAuthActivity;


    private BroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ac_edit_profile);

        // Navigation bar
        ((TextView) findViewById(R.id.tvNavigationTitle)).setText(R.string.edit_title);
        findViewById(R.id.btnNavigationHome).setVisibility(View.GONE);
        Button btnBackToProfile = (Button) findViewById(R.id.btnNavigationBackWithText);
        btnBackToProfile.setText(R.string.general_profile);
        btnBackToProfile.setVisibility(View.VISIBLE);
        btnBackToProfile.setOnClickListener(this);

        hasStartedFromAuthActivity = getIntent().getBooleanExtra(NavigationActivity.FROM_AUTH, false);

        // ListView
        mEditItems = new LinkedList<EditProfileItem>();
        initEditItems();

        mEditsListView = (ListView) findViewById(R.id.lvEdits);

        // Header
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup header = (ViewGroup) inflater.inflate(R.layout.item_edit_profile_header, mEditsListView, false);

        ViewGroup profileNameLayout = (ViewGroup) header.findViewById(R.id.loProfileName);
        profileNameLayout.findViewById(R.id.ivNameEditBackground).setOnClickListener(this);
        mEditName = (TextView) profileNameLayout.findViewById(R.id.tvName);
        mEditName.setText(CacheProfile.first_name + ", ");
        mEditAge = (TextView) profileNameLayout.findViewById(R.id.tvAge);
        mEditAge.setText(Integer.toString(CacheProfile.age));
        mEditSex = (ImageView) profileNameLayout.findViewById(R.id.ivSexIcon);
        mEditSex.setImageResource(CacheProfile.sex == Static.BOY ?
                R.drawable.ico_boy :
                R.drawable.ico_girl);


        mEditCity = (Button) header.findViewById(R.id.btnEditCity);
        mEditCity.setText(CacheProfile.city_name);
        mEditCity.setOnClickListener(this);

        mEditsListView.addHeaderView(header);
        mAdapter = new EditsAdapter(getApplicationContext(), mEditItems);
        mEditsListView.setAdapter(mAdapter);

        mProfilePhoto = (ImageViewRemote) header.findViewById(R.id.ivProfilePhoto);
        mProfilePhoto.setOnClickListener(this);
        mProfilePhoto.setPhoto(CacheProfile.photo);
        if (hasStartedFromAuthActivity) {
            editProfileMsg = (TextView) findViewById(R.id.EditProfileMessage);
            editProfileMsg.setVisibility(View.VISIBLE);
        }

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateViews();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver, new IntentFilter(ProfileRequest.PROFILE_UPDATE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    private void updateViews() {
//        initEditItems();
        mAdapter.notifyDataSetChanged();
        mEditName.setText(CacheProfile.first_name + ", ");
        mEditAge.setText(Integer.toString(CacheProfile.age));
        mEditSex.setImageResource(CacheProfile.sex == Static.BOY ?
                R.drawable.ico_boy :
                R.drawable.ico_girl);
        mEditCity.setText(CacheProfile.city_name);
        mProfilePhoto.setPhoto(CacheProfile.photo);
    }

    private void initEditItems() {
        mEditItems.add((new EditStatus()).setType(Type.TOP));
        mEditItems.add((new EditBackPhoto()).setType(Type.MIDDLE));
        mEditItems.add((new EditPhotos()).setType(Type.BOTTOM));

        // edit form items
        FormItem prevFormItem = null;
        for (int i = 0; i < CacheProfile.forms.size(); i++) {
            FormItem formItem = CacheProfile.forms.get(i);
            EditProfileItem item = null;

            // set text info
            if (formItem.type == FormItem.HEADER) {
                item = (new EditHeader()).setText(formItem.title);
            } else if (formItem.type == FormItem.DATA) {
                item = (new EditForm()).setFormItem(formItem);
            } else if (formItem.type == FormItem.DIVIDER) {
                continue;
            } else if (formItem.type == FormItem.STATUS) {
                continue;
            }

            // set position type info
            if (item != null) {
                if (prevFormItem != null && prevFormItem.type == FormItem.HEADER) {
                    item.setType(Type.TOP);
                } else if (i + 1 < CacheProfile.forms.size()) {
                    int type = CacheProfile.forms.get(i + 1).type;
                    if (type == FormItem.HEADER || type == FormItem.DIVIDER) {
                        item.setType(Type.BOTTOM);
                    }
                } else if (i == CacheProfile.forms.size() - 1) {
                    item.setType(Type.BOTTOM);
                } else {
                    item.setType(Type.MIDDLE);
                }
            }

            mEditItems.add(item);

            prevFormItem = formItem;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ivNameEditBackground:
                startActivityForResult(new Intent(getApplicationContext(), EditContainerActivity.class),
                        EditContainerActivity.INTENT_EDIT_NAME_AGE);
                break;
            case R.id.btnEditCity:
                startActivityForResult(new Intent(getApplicationContext(), CitySearchActivity.class),
                        CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY);
                break;
            case R.id.btnNavigationBackWithText:
                if (hasStartedFromAuthActivity) {
                    Intent intent = new Intent(this, NavigationActivity.class);
                    intent.putExtra(GCMUtils.NEXT_INTENT, BaseFragment.F_VIP_PROFILE);
                    SharedPreferences preferences = getSharedPreferences(Static.PREFERENCES_TAG_SHARED, Context.MODE_PRIVATE);
                    preferences.edit().putBoolean(Static.PREFERENCES_TAG_NEED_EDIT, false).commit();
                    startActivity(intent);
                }
                finish();
                break;
            case R.id.ivProfilePhoto:
                startActivityForResult(new Intent(getApplicationContext(), EditContainerActivity.class),
                        EditContainerActivity.INTENT_EDIT_PROFILE_PHOTO);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case EditContainerActivity.INTENT_EDIT_NAME_AGE:
                    mEditName.setText(CacheProfile.first_name + ", ");
                    mEditAge.setText(Integer.toString(CacheProfile.age));
                    mEditSex.setImageResource(CacheProfile.sex == Static.BOY ?
                            R.drawable.ico_boy :
                            R.drawable.ico_girl);
                    break;
                case EditContainerActivity.INTENT_EDIT_STATUS:
                    mAdapter.notifyDataSetChanged();
                    break;
                case EditContainerActivity.INTENT_EDIT_BACKGROUND:
                    mAdapter.notifyDataSetChanged();
                    break;
                case EditContainerActivity.INTENT_EDIT_FORM_ITEM:
                    mAdapter.notifyDataSetChanged();
                    break;
                case EditContainerActivity.INTENT_EDIT_INPUT_FORM_ITEM:
                    mAdapter.notifyDataSetChanged();
                    break;
                case EditContainerActivity.INTENT_EDIT_PROFILE_PHOTO:
                    mProfilePhoto.setPhoto(CacheProfile.photo);
                    mAdapter.notifyDataSetChanged();
                    break;
                case EditContainerActivity.INTENT_EDIT_ALBUM:
                    mProfilePhoto.setPhoto(CacheProfile.photo);
                    mAdapter.notifyDataSetChanged();
                    break;
                case CitySearchActivity.INTENT_CITY_SEARCH_ACTIVITY:
                    Bundle extras = data.getExtras();
                    final String city_name = extras.getString(CitySearchActivity.INTENT_CITY_NAME);
                    final String city_full = extras.getString(CitySearchActivity.INTENT_CITY_FULL_NAME);
                    final int city_id = extras.getInt(CitySearchActivity.INTENT_CITY_ID);

                    SettingsRequest request = new SettingsRequest(this);
                    request.cityid = city_id;
                    sendBroadcast(new Intent().setAction("com.topface.receivers.ConnectionChangeReceiver"));
                    request.callback(new ApiHandler() {

                        @Override
                        public void success(ApiResponse response) {
                            CacheProfile.city_id = city_id;
                            CacheProfile.city_name = city_name;
                            CacheProfile.city_full = city_full;
                            mEditCity.setText(CacheProfile.city_name);
                        }

                        @Override
                        public void fail(int codeError, ApiResponse response) {
                            Toast.makeText(EditProfileActivity.this, getString(R.string.general_data_error),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }).exec();
                default:
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    class EditsAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private LinkedList<EditProfileItem> mData;

        private int T_HEADER = 0;
        private int T_EDIT_ITEM = 1;
        private int T_COUNT = 2;

        public EditsAdapter(Context context, LinkedList<EditProfileItem> data) {
            mInflater = LayoutInflater.from(context);
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public EditProfileItem getItem(int position) {
            return mData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            if (getItem(position) instanceof EditHeader) {
                return T_HEADER;
            }
            return T_EDIT_ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return T_COUNT;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final EditProfileItem item = getItem(position);
            int type = getItemViewType(position);

            // get holder
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(item.getLayoutResId(), null, false);

                holder.mTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                holder.mText = (TextView) convertView.findViewById(R.id.tvText);
                holder.mBackground = (ImageView) convertView.findViewById(R.id.ivEditBackground);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // set background image
            switch (item.getType()) {
                case TOP:
                    holder.mBackground.setImageDrawable(getResources().getDrawable(
                            R.drawable.edit_big_btn_top_selector));
                    break;
                case MIDDLE:
                    holder.mBackground.setImageDrawable(getResources().getDrawable(
                            R.drawable.edit_big_btn_middle_selector));
                    break;
                case BOTTOM:
                    holder.mBackground.setImageDrawable(getResources().getDrawable(
                            R.drawable.edit_big_btn_bottom_selector));
                    break;
                case NONE:
                    break;
            }


            // set text
            if (type == T_HEADER) {
                if (!item.getTitle().equals(Static.EMPTY)) {
                    holder.mTitle.setText(item.getTitle());
                    holder.mTitle.setVisibility(View.VISIBLE);
                } else {
                    holder.mTitle.setVisibility(View.GONE);
                }
            } else {
                holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                holder.mText.setVisibility(View.GONE);
                if (item instanceof EditStatus) {
                    holder.mTitle.setText(item.getTitle());
                } else if (item instanceof EditBackPhoto) {
                    holder.mTitle.setText(item.getTitle());
                    holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
                } else if (item instanceof EditPhotos) {
                    holder.mTitle.setText(item.getTitle());
                    holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(item.getIcon(), null, null, null);
                } else if (item instanceof EditInterests) {
                    holder.mTitle.setText(item.getTitle());
                } else if (item instanceof EditForm) {
                    holder.mTitle.setText(item.getTitle());
                    holder.mText.setVisibility(View.VISIBLE);
                    holder.mText.setText(item.getText());
                }

                convertView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        item.onClick();
                    }
                });
            }

            if (position == getCount() - 1) {
                convertView.setPadding(0, 0, 0, 10);
            } else {
                convertView.setPadding(0, 0, 0, 0);
            }


            return convertView;
        }

        class ViewHolder {
            TextView mTitle;
            TextView mText;
            ImageView mBackground;
        }

    }

    class EditStatus extends EditProfileItem {

        @Override
        public String getTitle() {
            if (CacheProfile.status.trim().length() != 0) {
                return getString(R.string.edit_refresh_status);
            }
            return CacheProfile.status;
        }

        @Override
        void onClick() {
            Intent intent = new Intent(getApplicationContext(), EditContainerActivity.class);
            startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_STATUS);
        }
    }

    class EditBackPhoto extends EditProfileItem {

        @Override
        public String getTitle() {
            return getResources().getString(R.string.edit_bg_photo);
        }

        @Override
        public Drawable getIcon() {
            Bitmap original = BitmapFactory.decodeResource(getResources(),
                    ProfileBackgrounds.getBackgroundResource(getApplicationContext(),
                            CacheProfile.background_id));
            int w = getResources().getDrawable(R.drawable.edit_icon_photo).getIntrinsicWidth();
            int h = getResources().getDrawable(R.drawable.edit_icon_photo).getIntrinsicHeight();

            return new BitmapDrawable(
                    getResources(),
                    Bitmap.createScaledBitmap(original, w, h, true)
            );
        }

        @Override
        void onClick() {
            startActivityForResult(new Intent(getApplicationContext(), EditContainerActivity.class),
                    EditContainerActivity.INTENT_EDIT_BACKGROUND);
        }
    }

    class EditPhotos extends EditProfileItem {

        @Override
        public String getTitle() {
            int quantity = 0;
            if (CacheProfile.photos != null) {
                quantity = CacheProfile.photos.size();
            }
            return quantity + " " + getResources().getString(R.string.edit_album_photos);
        }

        @Override
        public Drawable getIcon() {
            return getResources().getDrawable(R.drawable.edit_icon_photo);
        }

        @Override
        void onClick() {
            Intent intent = new Intent(getApplicationContext(), EditContainerActivity.class);
            startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_ALBUM);
        }
    }

    class EditInterests extends EditProfileItem {

        @Override
        public String getTitle() {
            return 0 + " " + getResources().getString(R.string.edit_interests);
            // TODO interests number
        }

        @Override
        void onClick() {
            // TODO
        }
    }

    class EditForm extends EditProfileItem {

        private FormItem mFormItem;

        @Override
        public String getTitle() {
            return mFormItem.title;
        }

        @Override
        public String getText() {
            return mFormItem.value;
        }

        public EditForm setFormItem(FormItem item) {
            mFormItem = item;
            return this;
        }

        @Override
        void onClick() {
            Intent intent = new Intent(getApplicationContext(), EditContainerActivity.class);
            intent.putExtra(EditContainerActivity.INTENT_FORM_TITLE_ID, mFormItem.titleId);
            intent.putExtra(EditContainerActivity.INTENT_FORM_DATA, mFormItem.value);
            if (mFormItem.dataId == FormItem.NO_RESOURCE_ID) {
                startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_INPUT_FORM_ITEM);
            } else {
                intent.putExtra(EditContainerActivity.INTENT_FORM_DATA_ID, mFormItem.dataId);
                startActivityForResult(intent, EditContainerActivity.INTENT_EDIT_FORM_ITEM);
            }
        }
    }

    class EditHeader extends EditProfileItem {

        private String mTitle = "";

        @Override
        public String getTitle() {
            return mTitle;
        }

        @Override
        void onClick() {
        }

        @Override
        public int getLayoutResId() {
            return R.layout.item_edit_profile_form_header;
        }

        @Override
        public Type getType() {
            return Type.NONE;
        }

        public EditProfileItem setText(String text) {
            mTitle = text;
            return this;
        }

        public EditProfileItem setText(int resId) {
            mTitle = getResources().getString(resId);
            return this;
        }
    }
}
package com.topface.topface.ui.settings;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.topface.framework.JsonUtils;
import com.topface.framework.utils.Debug;
import com.topface.topface.App;
import com.topface.topface.R;
import com.topface.topface.data.Profile;
import com.topface.topface.data.SendMailNotificationResponse;
import com.topface.topface.requests.ApiResponse;
import com.topface.topface.requests.DataApiHandler;
import com.topface.topface.requests.IApiResponse;
import com.topface.topface.requests.SendMailNotificationsRequest;
import com.topface.topface.ui.dialogs.NotificationEditDialog;
import com.topface.topface.ui.fragments.BaseFragment;
import com.topface.topface.utils.CacheProfile;
import com.topface.topface.utils.MarketApiManager;
import com.topface.topface.utils.Utils;
import com.topface.topface.utils.config.UserConfig;
import com.topface.topface.utils.notifications.UserNotificationManager;

import static com.topface.topface.ui.dialogs.BaseEditDialog.EditingFinishedListener;

/**
 * Notifications settings
 */
public class SettingsNotificationsFragment extends BaseFragment implements View.OnClickListener {
    public static final int REQUEST_CODE_RINGTONE = 333;

    private MarketApiManager mMarketApiManager;
    private UserConfig mUserConfig = App.getUserConfig();
    private String mSavingText = App.getContext().getString(R.string.saving_in_progress);

    private View mLoLikes;
    private View mLoMutual;
    private View mLoChat;
    private View mLoGuests;
    private CheckBox mLoVibration;
    private CheckBox mLoLED;
    private View mLoMelody;

    private TextView mMelodyName;

    private EditingFinishedListener<Profile.TopfaceNotifications> mEditingFinishedListener =
            new EditingFinishedListener<Profile.TopfaceNotifications>() {
                @Override
                public void onEditingFinished(Profile.TopfaceNotifications notification) {
                    if (hasChanges(notification)) {
                        updateNotificationSettings(notification);
                    }
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_notifications, null);

        mMarketApiManager = new MarketApiManager();

        mLoLikes = view.findViewById(R.id.notification_sympathies);
        mLoMutual = view.findViewById(R.id.notification_mutuals);
        mLoChat = view.findViewById(R.id.notification_messages);
        mLoGuests = view.findViewById(R.id.notification_guests);
        mLoVibration = (CheckBox) view.findViewById(R.id.notification_vibro);
        mLoLED = (CheckBox) view.findViewById(R.id.notification_led);
        mLoMelody = view.findViewById(R.id.notification_melody);

        mMelodyName = (TextView) mLoMelody.findViewWithTag("tvText");

        mLoLikes.setOnClickListener(this);
        mLoMutual.setOnClickListener(this);
        mLoChat.setOnClickListener(this);
        mLoGuests.setOnClickListener(this);
        mLoVibration.setOnClickListener(this);
        mLoLED.setOnClickListener(this);
        mLoMelody.setOnClickListener(this);

        setTitle(R.string.settings_likes, mLoLikes);
        setTitle(R.string.settings_mutual, mLoMutual);
        setTitle(R.string.settings_messages, mLoChat);
        setTitle(R.string.settings_guests, mLoGuests);

        if (CacheProfile.notifications != null) {
            setText(CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES), mLoLikes);
            setText(CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY), mLoMutual);
            setText(CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE), mLoChat);
            setText(CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR), mLoGuests);
        }

        setTitle(R.string.settings_vibration, mLoVibration);
        mLoVibration.setChecked(App.getUserConfig().isVibrationEnabled());
        mLoVibration.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserConfig.setGCMVibrationEnabled(isChecked);
                mUserConfig.saveConfig();
                Debug.log(mUserConfig, "UserConfig changed");

                // Send empty vibro notification to demonstrate
                if (isChecked) {
                    UserNotificationManager.getInstance().showSimpleNotification(
                            new NotificationCompat.Builder(getActivity()).setDefaults(Notification.
                                    DEFAULT_VIBRATE).build()
                    );
                }
            }
        });

        setTitle(R.string.settings_led, mLoLED);
        mLoLED.setChecked(mUserConfig.isLEDEnabled());
        mLoLED.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUserConfig.setLEDEnabled(isChecked);
                mUserConfig.saveConfig();
                Debug.log(App.getUserConfig(), "UserConfig changed");
            }
        });

        setTitle(R.string.settings_melody, mLoMelody);
        setRingtonNameByUri(mUserConfig.getGCMRingtone());

        setNotificationState();

        return view;
    }

    public SendMailNotificationsRequest getMailNotificationRequest(Profile.TopfaceNotifications notification, Context context) {
        SendMailNotificationsRequest request = getMailNotificationRequest(context);

        if (request != null) {
            switch (notification.type) {
                case CacheProfile.NOTIFICATIONS_LIKES:
                    request.mailSympathy = notification.mail;
                    request.apnsSympathy = notification.apns;
                    break;
                case CacheProfile.NOTIFICATIONS_MESSAGE:
                    request.mailChat = notification.mail;
                    request.apnsChat = notification.apns;
                    break;
                case CacheProfile.NOTIFICATIONS_SYMPATHY:
                    request.mailMutual = notification.mail;
                    request.apnsMutual = notification.apns;
                    break;
                case CacheProfile.NOTIFICATIONS_VISITOR:
                    request.mailGuests = notification.mail;
                    request.apnsVisitors = notification.apns;
                    break;
            }
        }
        return request;
    }

    private void setTitle(int titleId, View frame) {
        ((TextView) frame.findViewWithTag("tvTitle")).setText(titleId);
    }

    private void setText(String text, View frame) {
        TextView textView = (TextView) frame.findViewWithTag("tvText");
        if (TextUtils.isEmpty(text)) {
            textView.setVisibility(View.GONE);
        } else {
            textView.setText(text);
            textView.setVisibility(View.VISIBLE);
        }
    }

    private void setText(Profile.TopfaceNotifications notification, View frame) {
        StringBuilder textBuilder = new StringBuilder();
        Context context = App.getContext();
        if (notification.apns) {
            textBuilder.append(context.getString(R.string.on_phone));
        }
        if (notification.mail) {
            if (notification.apns) {
                textBuilder.append(", ").append(context.getString(R.string.on_mail).toLowerCase());
            } else {
                textBuilder.append(context.getString(R.string.on_mail));
            }
        }
        setText(textBuilder.toString(), frame);
    }

    private void setNotificationState() {
        boolean isMarketApiAvailable = mMarketApiManager.isMarketApiAvailable();
        if (!CacheProfile.email && !isMarketApiAvailable) {
            mMelodyName.setVisibility(View.GONE);
            setNotificationVisibility(View.GONE);
        } else {
            setNotificationVisibility(View.VISIBLE);
            mMelodyName.setVisibility(View.VISIBLE);
        }
        if (isMarketApiAvailable) {
            setNotificationSettingsVisibility(View.VISIBLE);
        } else {
            setNotificationSettingsVisibility(View.GONE);
        }
    }

    private void setNotificationVisibility(int visibility) {
        mLoLikes.setVisibility(visibility);
        mLoMutual.setVisibility(visibility);
        mLoChat.setVisibility(visibility);
        mLoGuests.setVisibility(visibility);
    }

    private void setNotificationSettingsVisibility(int visibility) {
        mLoVibration.setVisibility(visibility);
        mLoLED.setVisibility(visibility);
        mLoMelody.setVisibility(visibility);
    }

    @Override
    protected String getTitle() {
        return getString(R.string.notifications);
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getFragmentManager();
        Resources res = App.getContext().getResources();
        switch (v.getId()) {
            case R.id.notification_sympathies:
                NotificationEditDialog.newInstance(res.getString(R.string.receive_sympathy_notification),
                        CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES),
                        mEditingFinishedListener).show(fm, NotificationEditDialog.class.getName());
                break;
            case R.id.notification_mutuals:
                NotificationEditDialog.newInstance(res.getString(R.string.receive_mutual_notification),
                        CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY),
                        mEditingFinishedListener).show(fm, NotificationEditDialog.class.getName());
                break;
            case R.id.notification_messages:
                NotificationEditDialog.newInstance(res.getString(R.string.receive_message_notification),
                        CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE),
                        mEditingFinishedListener).show(fm, NotificationEditDialog.class.getName());
                break;
            case R.id.notification_guests:
                NotificationEditDialog.newInstance(res.getString(R.string.receive_guest_notification),
                        CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR),
                        mEditingFinishedListener).show(fm, NotificationEditDialog.class.getName());
                break;
            case R.id.notification_melody:
                Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.settings_melody));
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, App.getUserConfig().getGCMRingtone());
                startActivityForResult(intent, REQUEST_CODE_RINGTONE);
                break;
        }
    }

    private void updateNotificationSettings(final Profile.TopfaceNotifications notification) {
        final View view = getViewByNotificationType(notification.type);
        if (view != null) {
            view.setEnabled(false);
            setText(mSavingText, view);
        }
        getMailNotificationRequest(notification, App.getContext()).callback(new DataApiHandler<SendMailNotificationResponse>() {
            @Override
            public void fail(int codeError, IApiResponse response) {
                if (getView() != null) {
                    setText(CacheProfile.notifications.get(notification.type), view);
                    Utils.showToastNotification(R.string.general_data_error, Toast.LENGTH_SHORT);
                }
            }

            @Override
            protected void success(SendMailNotificationResponse data, IApiResponse response) {
                if (data.saved) {
                    CacheProfile.notifications.put(notification.type, notification);
                    CacheProfile.sendUpdateProfileBroadcast();
                    if (getView() != null) {
                        setText(notification, view);
                    }
                }
            }

            @Override
            protected SendMailNotificationResponse parseResponse(ApiResponse response) {
                return JsonUtils.fromJson(response.toString(), SendMailNotificationResponse.class);
            }

            @Override
            public void always(IApiResponse response) {
                super.always(response);
                if (view != null) {
                    view.setEnabled(true);
                }
            }
        }).exec();
    }

    private View getViewByNotificationType(int type) {
        switch (type) {
            case CacheProfile.NOTIFICATIONS_MESSAGE:
                return mLoChat;
            case CacheProfile.NOTIFICATIONS_SYMPATHY:
                return mLoMutual;
            case CacheProfile.NOTIFICATIONS_LIKES:
                return mLoLikes;
            case CacheProfile.NOTIFICATIONS_VISITOR:
                return mLoGuests;
            default:
                return null;
        }
    }

    private boolean hasChanges(Profile.TopfaceNotifications notification) {
        if (CacheProfile.notifications != null) {
            Profile.TopfaceNotifications cachedNotification = CacheProfile.notifications.get(notification.type);
            return cachedNotification.mail != notification.mail || cachedNotification.apns != notification.apns;
        }
        return false;
    }

    private void setRingtonNameByUri(Uri uri) {
        String ringtoneName = getActivity().getString(R.string.silent_ringtone);
        if (uri != null) {
            Cursor mCursor = null;
            try {
                mCursor = getActivity().getContentResolver().query(uri, null, null, null, null);

                if (mCursor != null && mCursor.moveToFirst()) {
                    if (mCursor.getColumnIndex("title") >= 0) {
                        ringtoneName = mCursor.getString(mCursor.getColumnIndex("title"));
                    } else {
                        uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        ringtoneName = getString(R.string.default_ringtone);
                    }
                }
            } catch (Exception ex) {
                Debug.error(ex);
            } finally {
                if (mCursor != null) mCursor.close();
            }
        }
        mMelodyName.setText(ringtoneName);
        App.getUserConfig().setGCMRingtone(uri == null ? UserConfig.SILENT : uri.toString());
        App.getUserConfig().saveConfig();
        Debug.log(App.getUserConfig(), "UserConfig changed");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RINGTONE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                setRingtonNameByUri(uri);
            }
        }
    }

    /**
     * Creates SendMailNotificationRequest if possible
     * may return null, when profile information still not cached
     *
     * @return new SendMailNotificationRequest
     */
    public SendMailNotificationsRequest getMailNotificationRequest(Context context) {
        SendMailNotificationsRequest request = new SendMailNotificationsRequest(context);
        if (CacheProfile.notifications != null) {
            try {
                request.mailSympathy = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).mail;
                request.mailMutual = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).mail;
                request.mailChat = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).mail;
                request.mailGuests = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).mail;
            } catch (Exception e) {
                Debug.error(e);
            }

            try {
                request.apnsSympathy = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_LIKES).apns;
                request.apnsMutual = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_SYMPATHY).apns;
                request.apnsChat = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_MESSAGE).apns;
                request.apnsVisitors = CacheProfile.notifications.get(CacheProfile.NOTIFICATIONS_VISITOR).apns;
            } catch (Exception e) {
                Debug.error(e);
            }
            return request;
        } else {
            return null;
        }

    }
}

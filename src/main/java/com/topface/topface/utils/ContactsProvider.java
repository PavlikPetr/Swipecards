package com.topface.topface.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.topface.framework.utils.BackgroundThread;

import java.util.ArrayList;

public class ContactsProvider {

    private static final int EMAIL = 1;
    private static final int PHONE = 2;
    private static final int EMAIL_AND_PHONE = 3;

    private final Context ctx;
    private ArrayList<Contact> contacts;

    public ContactsProvider(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        contacts = new ArrayList<>();
    }

    private void getContacts(final int limit, final int offset, final GetContactsHandler handler, final int dataType) {
        new BackgroundThread() {
            @Override
            public void execute() {
                getContactsAsync(limit, offset, dataType, handler);
            }
        };
    }

    public void getContactsWithEmails(final int limit, final int offset, final GetContactsHandler handler) {
        getContacts(limit, offset, handler, EMAIL);
    }

    public void getContactsWithPhones(final int limit, final int offset, final GetContactsHandler handler) {
        getContacts(limit, offset, handler, PHONE);
    }

    @SuppressWarnings("unused")
    public void getContacts(final int limit, final int offset, final GetContactsHandler handler) {
        getContacts(limit, offset, handler, EMAIL_AND_PHONE);
    }

    private void getContactsAsync(int limit, int offset, int dataType, GetContactsHandler handler) {
        ContentResolver cr = getContentResolver();
        String limitCondition = (limit == -1) ? "" : " LIMIT " + limit + " offset " + offset;
        String selection = dataType == PHONE ? ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0" : null;
        Cursor cur = cr.query(
                ContactsContract.Contacts.CONTENT_URI, null, selection, null,
                ContactsContract.Contacts._ID + limitCondition
        );
        if (cur == null) {
            return;
        }
        if (cur.getCount() > 0) {
            while (!cur.isLast()) {
                cur.moveToNext();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String email = null, phone = null;
                // email
                if ((dataType & EMAIL) == EMAIL) {
                    email = getEmail(cr, id);
                }
                // phone
                if ((dataType & PHONE) == PHONE) {
                    phone = getPhone(cr, id);
                }
                // adds non empty contact
                if (!TextUtils.isEmpty(email) || !TextUtils.isEmpty(phone)) {
                    contacts.add(new Contact(id, name, email, phone));
                }
            }
        }
        cur.close();

        handler.sendContacts(contacts);
    }

    private String getEmail(ContentResolver cr, String id) {
        String email = null;
        Cursor emailCur = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? ",
                new String[]{String.valueOf(id)},
                null
        );
        if (emailCur != null) {
            if (emailCur.getCount() > 0) {
                emailCur.moveToFirst();
                email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            emailCur.close();
        }
        return email;
    }

    private String getPhone(ContentResolver cr, String id) {
        String phone = null;
        Cursor phoneCursor = cr.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                new String[]{String.valueOf(id)},
                null
        );
        if (phoneCursor != null) {
            if (phoneCursor.getCount() > 0) {
                phoneCursor.moveToNext();
                phone = getContactFromCursor(phoneCursor);
                while (!phoneCursor.isLast()) {
                    phoneCursor.moveToNext();
                    int isPrimary = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY));
                    if (isPrimary > 0) {
                        phone = getContactFromCursor(phoneCursor);
                        break;
                    }
                }
            }
            phoneCursor.close();
        }
        return phone;
    }

    private String getContactFromCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }

    private ContentResolver getContentResolver() {
        return ctx.getContentResolver();
    }

    public static class Contact implements Parcelable {
        private String id;
        private String name;
        private String email;
        private String phone;
        private boolean isChecked = true;
        private int status;

        public Contact(String id, String name, String email, String phone) {
            this(id, name, email, phone, 2);
        }

        public Contact(String id, String name, String email, String phone, int status) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.status = status;
        }

        private Contact(Parcel in) {
            id = in.readString();
            name = in.readString();
            email = in.readString();
            phone = in.readString();
            isChecked = in.readInt() == 1;
            status = in.readInt();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Uri getPhoto() {
            Uri person = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
                    Long.parseLong(id));
            return Uri.withAppendedPath(person, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        }

        public boolean hasEmail() {
            return !TextUtils.isEmpty(email);
        }

        @SuppressWarnings("unused")
        public boolean hasPhone() {
            return !TextUtils.isEmpty(phone);
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public boolean isChecked() {
            return isChecked;
        }

        public static final Creator<Contact> CREATOR = new Creator<Contact>() {
            @Override
            public Contact createFromParcel(Parcel source) {
                return new Contact(source);
            }

            @Override
            public Contact[] newArray(int size) {
                return new Contact[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeString(name);
            dest.writeString(email);
            dest.writeString(phone);
            dest.writeInt(isChecked ? 1 : 0);
            dest.writeInt(status);
        }
    }

    public static abstract class GetContactsHandler extends Handler {

        private ArrayList<Contact> mContacts;

        public GetContactsHandler() {
        }

        protected void sendContacts(ArrayList<Contact> contacts) {
            mContacts = contacts;
            sendMessage(new Message());
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            onContactsReceived(mContacts);
        }

        public abstract void onContactsReceived(ArrayList<Contact> contacts);
    }

}
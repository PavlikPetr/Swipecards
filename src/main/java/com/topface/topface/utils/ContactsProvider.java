package com.topface.topface.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract;

import java.util.ArrayList;

public class ContactsProvider {

    private final Context ctx;
    private ArrayList<Contact> contacts;
    private GetContactsListener listener;

    public ContactsProvider(Context ctx) {
        this.ctx = ctx;
        contacts = new ArrayList<Contact>();
    }

    public void getContacts(final int limit, final int offset, GetContactsListener listener) {
        this.listener = listener;
        new BackgroundThread() {
            @Override
            public void execute() {
                getContactsAsync(limit, offset);
            }
        };
    }

    //Что за странная хрень с циклом???
    @SuppressWarnings("LoopStatementThatDoesntLoop")
    private void getContactsAsync(int limit, int offset) {
        ContentResolver cr = getContentResolver();

        String limitCondition = (limit == -1) ? "" : " LIMIT " + limit + " offset " + offset;
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, " _id" + limitCondition);
        if (cur == null) {
            return;
        }
        if (cur.getCount() > 0) {
            while (!cur.isLast()) {
                cur.moveToNext();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String email = "";
                Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? ", new String[]{String.valueOf(id)}, null);
                if (emailCur.isLast()) {
                    emailCur.moveToNext();
                    email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                }
                emailCur.close();
                if (!email.equals("")) {
                    contacts.add(new Contact(name, email, true));
                } else {
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{String.valueOf(id)}, null);
                    if (phoneCursor.getCount() > 0) {
                        phoneCursor.moveToNext();
                        Contact contact = new Contact(name, getContactFromCursor(phoneCursor), false);
                        while (!phoneCursor.isLast()) {
                            phoneCursor.moveToNext();
                            int isPrimary = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY));
                            if (isPrimary > 0) {
                                String phone = getContactFromCursor(phoneCursor);
                                contact = new Contact(name, phone, false);
                                break;
                            }
                        }
                        contacts.add(contact);
                    }
                    phoneCursor.close();
                }
            }

        }
        cur.close();

        listener.onContactsReceived(contacts);
    }

    private String getContactFromCursor(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }

    private ContentResolver getContentResolver() {
        return ctx.getContentResolver();
    }

    public static class Contact implements Parcelable {
        private String name;
        private String phone;
        private boolean email;
        private boolean isChecked = true;

        public Contact(String name, String phone, boolean isMail) {
            this.name = name;
            this.phone = phone;
            email = isMail;
        }

        private Contact(Parcel in) {
            name = in.readString();
            phone = in.readString();
            email = Boolean.getBoolean(in.readString());
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public boolean isEmail() {
            return email;
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
            dest.writeString(name);
            dest.writeString(phone);
            dest.writeString(Boolean.toString(email));
        }

    }

    public interface GetContactsListener {
        public void onContactsReceived(ArrayList<Contact> contacts);
    }

}

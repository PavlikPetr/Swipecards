package com.topface.topface.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.StringBuilderPrinter;

import java.util.LinkedList;

public class ContactsProvider {

    private final Context ctx;
    private LinkedList<Contact> contacts;
    private GetContactsListener listener;

    public ContactsProvider(Context ctx) {
        this.ctx = ctx;
        contacts = new LinkedList<Contact>();
    }

    public void getContacts(final int limit, final int offset, GetContactsListener listener) {
        this.listener = listener;
        new Thread(new Runnable() {
            @Override
            public void run() {
                getContactsAsync(limit, offset);
            }
        }).start();
    }

    private void getContactsAsync(int limit, int offset) {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "  _id LIMIT " + limit + " offset " + offset);
        if (cur.getCount() > 0) {
            while (!cur.isLast()) {
                cur.moveToNext();
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String email = "";
                Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ? ", new String[]{String.valueOf(id)},null);
                while (emailCur.isLast()) {
                    emailCur.moveToNext();
                    email = emailCur.getString( emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    break;
                }
                emailCur.close();
                if (!email.equals("")) {
                    contacts.add(new Contact(name, email));
                } else {
                    Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{String.valueOf(id)}, null);
                    if (phoneCursor.getCount() > 0) {
                        phoneCursor.moveToNext();
                        Contact contact = new Contact(name, getContactFromCursor(phoneCursor));
                        while (!phoneCursor.isLast()) {
                            phoneCursor.moveToNext();
                            int isPrimary = phoneCursor.getInt( phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.IS_SUPER_PRIMARY) );
                            if(isPrimary > 0) {
                                String phone = getContactFromCursor(phoneCursor);
                                contact = new Contact(name, phone);
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
        return cursor.getString( cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
    }

    private ContentResolver getContentResolver () {
        return  ctx.getContentResolver();
    }

    public static class Contact {
        private String name;
        private String phone;
        private boolean isChecked = true;

        public Contact(String name, String phone) {
            this.name = name;
            this.phone = phone;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public void setChecked(boolean checked) {
            isChecked = checked;
        }

        public boolean isChecked() {
            return isChecked;
        }
    }

    public interface GetContactsListener {
        public void onContactsReceived(LinkedList<Contact> contacts);
    }

}

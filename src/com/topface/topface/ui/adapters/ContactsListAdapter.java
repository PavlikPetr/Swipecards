package com.topface.topface.ui.adapters;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.topface.topface.R;
import com.topface.topface.requests.InviteRequest;
import com.topface.topface.utils.TriggersList;

import java.io.InputStream;

public class ContactsListAdapter extends CursorAdapter {
    private LayoutInflater mInflater;
    private TriggersList<Long, InviteRequest.Recipient> mTriggersList;

    public static class ViewHolder {
        ImageView avatar;
        TextView name;
        TextView phone;
        ImageView checkbox;
        public long contactId;
        public InviteRequest.Recipient recipient;
    }

    public ContactsListAdapter(Context context, Cursor c, TriggersList<Long, InviteRequest.Recipient> triggers) {
        super(context, c);
        mInflater = LayoutInflater.from(context);
        mTriggersList = triggers;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View v = mInflater.inflate(R.layout.item_invite, viewGroup, false);
        bindView(v, context, cursor);
        return v;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder == null) {
            holder = new ViewHolder();
            holder.name = (TextView) view.findViewById(R.id.contactName);
            holder.phone = (TextView) view.findViewById(R.id.contactPhone);
            holder.avatar = (ImageView) view.findViewById(R.id.contactAvatar);
            holder.checkbox = (ImageView) view.findViewById(R.id.contactCheckbox);
        }

        holder.contactId = cursor.getLong(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));


        String name = cursor.getString(
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        );
        String phone = cursor.getString(
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        );

        //Храним данные о пользователя для дальнейшего использования
        holder.recipient = new InviteRequest.Recipient(name, phone);

        //Сохраняем holder
        view.setTag(holder);

        //Имя
        holder.name.setText(name);
        //Телефон
        holder.phone.setText(phone);


        //Фото контакта
        holder.avatar.setImageBitmap(loadContactPhoto(
                context.getContentResolver(),
                holder.contactId
        ));


        //Состояние чекбокса пункта
        holder.checkbox.setImageResource(
                mTriggersList.isOn(holder.contactId) ?
                        android.R.drawable.checkbox_on_background :
                        android.R.drawable.checkbox_off_background
        );

    }

    public static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null) {
            return null;
        }
        return BitmapFactory.decodeStream(input);
    }
}

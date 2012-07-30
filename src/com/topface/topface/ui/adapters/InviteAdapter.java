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
import com.topface.topface.ui.views.RoundedImageView;
import com.topface.topface.utils.AvatarManager;
import com.topface.topface.utils.TriggersList;
import com.topface.topface.utils.Utils;

import java.io.InputStream;

public class InviteAdapter extends CursorAdapter {
    private LayoutInflater mInflater;
    private TriggersList<Long, InviteRequest.Recipient> mTriggersList;

    public static class ViewHolder {
        RoundedImageView avatar;
        TextView name;
        TextView phone;
        ImageView checkbox;
        public long contactId;
        public InviteRequest.Recipient recipient;
    }

    public InviteAdapter(Context context, Cursor c, TriggersList<Long, InviteRequest.Recipient> triggers) {
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
            holder.avatar = (RoundedImageView) view.findViewById(R.id.contactAvatar);
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
        Bitmap avatarBitmap = loadContactPhoto(
                context.getContentResolver(),
                holder.contactId
        );
        if (avatarBitmap != null) {
            int avatarSize = Math.min(avatarBitmap.getWidth(), avatarBitmap.getHeight());
            avatarBitmap = Utils.getRoundedCornerBitmap(
                    avatarBitmap,
                    avatarSize,
                    avatarSize,
                    AvatarManager.AVATAR_ROUND_RADIUS
            );
        }
        holder.avatar.setImageBitmap(avatarBitmap);


        //Состояние чекбокса пункта
        holder.checkbox.setImageResource(
                mTriggersList.isOn(holder.contactId) ?
                        R.drawable.invite_checkbox_on:
                        R.drawable.invite_checkbox_off
        );

    }

    /**
     * Возвращает фотографию контакта по его id
     *
     * @param cr ContentResolver из которого получаем фото
     * @param id контакта
     * @return Bitmap c фото контакта
     */
    public static Bitmap loadContactPhoto(ContentResolver cr, long  id) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
        if (input == null) {
            return null;
        }
        return BitmapFactory.decodeStream(input);
    }
}

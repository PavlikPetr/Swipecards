package com.topface.topface.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import com.topface.topface.R;
import com.topface.topface.Static;
import com.topface.topface.utils.http.Http;
import com.topface.topface.utils.social.AuthToken;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;

public class Socium {
    // Data
    private Context mContext;
    private AuthToken mToken;

    //---------------------------------------------------------------------------
    public Socium(Context context) {
        mContext = context;
        mToken = new AuthToken(mContext);
    }

    //---------------------------------------------------------------------------
    public String[] uploadPhoto(Uri uri) {
        if (mToken.getSocialNet().equals(AuthToken.SN_VKONTAKTE))
            return uploadPhotoVK(uri);
        if (mToken.getSocialNet().equals(AuthToken.SN_FACEBOOK))
            return uploadPhotoFB(uri);
        else
            return null;
    }

    //---------------------------------------------------------------------------
    public String[] uploadPhotoVK(Uri uri) {
        String[] result = new String[3];
        try {
            StringBuilder request = new StringBuilder("https://api.vk.com/method/photos.getAlbums?");
            request.append("uid=" + mToken.getUserId());
            request.append("&access_token=" + mToken.getTokenKey());

            int albumId = 0;
            String albumName = mContext.getString(R.string.general_vk_album_name);
            // запрос альбомов
            String response = Http.httpPostRequest(request.toString(), null);
            JSONObject jsonResult = null;
            jsonResult = new JSONObject(response);
            // получили список альбомов
            JSONArray albumsList = jsonResult.getJSONArray("response");
            if (albumsList != null && albumsList.length() > 0) {
                for (int i = 0; i < albumsList.length(); i++) {
                    JSONObject obj = albumsList.getJSONObject(i);
                    if (obj.getString("title").equals(albumName)) {
                        albumId = obj.getInt("aid"); // нашли нужный
                        break;
                    }
                }
            }

            // создаем новый
            if (albumId == 0) {
                request = new StringBuilder("https://api.vk.com/method/photos.createAlbum?");
                request.append("title=" + URLEncoder.encode(albumName));
                request.append("&access_token=" + mToken.getTokenKey());
                response = Http.httpPostRequest(request.toString(), null);
                jsonResult = new JSONObject(response);
                JSONObject obj = jsonResult.getJSONObject("response");
                albumId = obj.getInt("aid");
            }

            // uploading
            request = new StringBuilder("https://api.vk.com/method/photos.getUploadServer?");
            request.append("aid=" + albumId);
            request.append("&access_token=" + mToken.getTokenKey());
            response = Http.httpPostRequest(request.toString(), null);
            jsonResult = new JSONObject(response);
            JSONObject obj = jsonResult.getJSONObject("response");
            String url = obj.getString("upload_url");

            // отправка
            // получаем размеры изображения 
            InputStream is = mContext.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options); // чтение размеров
            is.close();
            int width = options.outWidth, height = options.outHeight;
            int scale = 1;
            if (width > Static.PHOTO_WIDTH && height > Static.PHOTO_HEIGHT) // определили степень уменьшения
                while (true) {
                    if ((width / 2) < Static.PHOTO_WIDTH && (height / 2) < Static.PHOTO_HEIGHT)
                        break;
                    width /= 2;
                    height /= 2;
                    scale *= 2;
                }
            options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inTempStorage = new byte[64 * 1024];
            options.inPurgeable = true;
            // подгрузка изображения
            is = mContext.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            // если горизонтальная - переварачиваем
            int or = getImageOrientation(mContext, uri);
            if (or > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(or);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();

            // загрузка фото
            //response = Http.httpPostDataRequest(url,null,mContext.getContentResolver().openInputStream(uri));
            response = Http.httpPostDataRequest(url, null, data);

            jsonResult = new JSONObject(response);

            String photosList = jsonResult.getString("photos_list");
            String hash = jsonResult.getString("hash");
            String server = jsonResult.getString("server");

            request = new StringBuilder("https://api.vk.com/method/photos.save?");
            request.append("aid=" + albumId);
            request.append("&server=" + server);
            request.append("&photos_list=" + photosList);
            request.append("&hash=" + hash);
            request.append("&access_token=" + mToken.getTokenKey());

            response = Http.httpPostRequest(request.toString(), null);
            jsonResult = new JSONObject(response);
            JSONArray links = jsonResult.getJSONArray("response");
            JSONObject link = links.getJSONObject(0); // повесить проверки 

            result[0] = link.getString("src_big");
            result[1] = link.getString("src");
            result[2] = link.getString("src_small");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //---------------------------------------------------------------------------
    public String[] uploadPhotoFB(Uri uri) {
        String[] result = new String[3];
        try {

            // отправка
            // получаем размеры изображения 
            InputStream is = mContext.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(is, null, options); // чтение размеров
            is.close();
            int width = options.outWidth, height = options.outHeight;
            int scale = 1;
            if (width > Static.PHOTO_WIDTH && height > Static.PHOTO_HEIGHT) // определили степень уменьшения
                while (true) {
                    if ((width / 2) < Static.PHOTO_WIDTH && (height / 2) < Static.PHOTO_HEIGHT)
                        break;
                    width /= 2;
                    height /= 2;
                    scale *= 2;
                }
            options = new BitmapFactory.Options();
            options.inSampleSize = scale;
            options.inTempStorage = new byte[64 * 1024];
            options.inPurgeable = true;
            // подгрузка изображения
            is = mContext.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            // если горизонтальная - переварачиваем
            int or = getImageOrientation(mContext, uri);
            if (or > 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(or);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
            }

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(CompressFormat.JPEG, 100, bos);
            byte[] data = bos.toByteArray();

            // загрузка фото
            StringBuilder request = new StringBuilder("https://graph.facebook.com/me/photos?access_token=");
            request.append("&access_token=" + mToken.getTokenKey());
            //String response = Http.httpPostDataRequest(request.toString(),null,mContext.getContentResolver().openInputStream(uri));
            String response = Http.httpPostDataRequest(request.toString(), null, data);

            JSONObject jsonResult = new JSONObject(response);
            long id = jsonResult.getLong("id");

            request = new StringBuilder("https://graph.facebook.com/");
            request.append(id + "?");
            request.append("access_token=" + mToken.getTokenKey());

            response = Http.httpGetRequest(request.toString());
            jsonResult = new JSONObject(response);
            JSONArray images = jsonResult.getJSONArray("images");

            if (images.length() < 2)
                return null;

            result[0] = images.getJSONObject(0).getString("source"); // big
            result[1] = images.getJSONObject(4).getString("source"); // medium
            result[2] = images.getJSONObject(1).getString("source"); // small

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //---------------------------------------------------------------------------
    public static int getImageOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);
        if (cursor.getCount() != 1)
            return -1;
        cursor.moveToFirst();

        return cursor.getInt(0);
    }
    //---------------------------------------------------------------------------
}

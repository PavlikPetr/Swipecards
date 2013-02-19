package com.topface.topface.requests;

import android.content.Context;
import android.net.Uri;
import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.Ssid;
import com.topface.topface.utils.Base64;
import com.topface.topface.utils.http.HttpUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public class PhotoAddRequest extends ApiRequest {
    public static final String SERVICE_NAME = "photoAdd";

    //Данные для конструирования HTTP запроса
    public static final String BOUNDARY = "FAfsadkfn23412034aHJSAdnk";
    public static final String PHOTO_ADD_CONTENT_TYPE = "multipart/mixed; boundary=" + BOUNDARY;
    public static final String LINE_END = "\r\n";
    public static final String TWO_HH = "--";

    private Uri mUri = null;

    public PhotoAddRequest(Uri uri, Context context) {
        super(context);
        mUri = uri;
    }

    @Override
    public int sendRequest() throws IOException {
        HttpURLConnection connection = getConnection();
        //Непосредственно перед отправкой запроса устанавливаем новый SSID
        setSsid(Ssid.get());
        //Ставим, если это нужно, ревизию (только на тестовых платформах)
        setRevisionHeader(connection);

        //Отправляем наш  POST запрос
        writeDatatToConnection(toPostData(), HttpUtils.getOutputStream(connection));

        return connection.getResponseCode();
    }

    private void writeDatatToConnection(String postParams, OutputStream stream) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(stream));
        dos.writeBytes(LINE_END + TWO_HH + BOUNDARY + LINE_END);
        //Это составной запрос
        dos.writeBytes("Content-Disposition: mixed" + LINE_END);
        //Первая часть обычный json с данными запроса
        dos.writeBytes("Content-Type: application/json" + LINE_END + LINE_END);
        //Записываем оснонвые данные запроса
        dos.writeBytes(postParams + LINE_END);
        dos.writeBytes(TWO_HH + BOUNDARY + LINE_END);
        dos.writeBytes("Content-Disposition: mixed" + LINE_END);
        //Вторая часть это Картинка в формате Base64
        dos.writeBytes("Content-Type: image/jpeg" + LINE_END);
        //Мы отправляем картинку в виде строки Base64, о чем сообщаем в заголовке
        dos.writeBytes("Content-Transfer-Encoding: base64" + LINE_END + LINE_END);
        //Открываем InputStream к файлу и пропуская через Base64.InputStream для кодирования картинку в виде Base64
        Base64.encodeFromInputToOutputStream(getInputStream(), dos);

        dos.writeBytes(
                LINE_END +
                        TWO_HH + BOUNDARY + TWO_HH +
                        LINE_END
        );

        dos.flush();
        dos.close();
    }

    private InputStream getInputStream() throws IOException {
        InputStream stream;

        if (mUri == null) {
            stream = null;
        } else if (isInternetUri(mUri)) {
            stream = new URL(mUri.toString()).openStream();
        } else {
            stream = getContext().getContentResolver().openInputStream(mUri);
        }

        return stream;
    }

    /**
     * Проверяет, ссылается ли данный Uri на ресурс в интернете
     *
     * @param uri ресурса
     * @return является ресурс ссылкой на файл в интернете
     */
    private boolean isInternetUri(Uri uri) {
        return Arrays.asList(
                "http",
                "https",
                "ftp"
        ).contains(uri.getScheme());
    }

    @Override
    protected JSONObject getRequestData() throws JSONException {
        return null;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME;
    }

    @Override
    public void exec() {
        if (mUri != null) {
            super.exec();
            EasyTracker.getTracker().trackEvent("Profile", "PhotoAdd", "", 1L);
        } else {
            handleFail(ApiResponse.MISSING_REQUIRE_PARAMETER, "Need set photo Uri");
        }
    }

    @Override
    public HttpURLConnection openConnection() throws IOException {
        //Если открываем новое подключение, то старое закрываем
        if (mURLConnection != null) {
            mURLConnection.disconnect();
        }

        mURLConnection = HttpUtils.openPostConnection(getApiUrl(), PHOTO_ADD_CONTENT_TYPE);
        return mURLConnection;
    }

}

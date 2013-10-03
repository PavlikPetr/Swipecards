package com.topface.topface.requests;

import android.content.Context;
import android.net.Uri;

import com.google.analytics.tracking.android.EasyTracker;
import com.topface.topface.requests.handlers.ErrorCodes;
import com.topface.topface.utils.Base64;
import com.topface.topface.utils.BitmapUtils;
import com.topface.topface.utils.Debug;
import com.topface.topface.utils.http.ConnectionManager;
import com.topface.topface.utils.http.HttpUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

public class PhotoAddRequest extends ApiRequest {
    public static final String SERVICE_NAME = "photo.add";

    //Данные для конструирования HTTP запроса
    public static final String BOUNDARY = "FAfsadkfn23412034aHJSAdnk";
    public static final String PHOTO_ADD_CONTENT_TYPE = "multipart/mixed; boundary=" + BOUNDARY;
    public static final String LINE_END = "\r\n";
    public static final String TWO_HH = "--";
    public static final String HTTP_REQUEST_CLOSE_DATA = LINE_END + TWO_HH + BOUNDARY + TWO_HH + LINE_END;

    private Uri mUri = null;

    public PhotoAddRequest(Uri uri, Context context) {
        super(context);
        mUri = uri;
    }

    @Override
    protected boolean writeData(HttpURLConnection connection) throws IOException {
        //Формируем базовую часть запроса (Заголовки, json данные)
        String headers = getHeaders();
        //Переводим в байты
        byte[] headersBytes = headers.getBytes();
        //Это просто закрывающие данные запроса с boundary и переносами строк
        byte[] endBytes = HTTP_REQUEST_CLOSE_DATA.getBytes();
        //Открываем InputStream к файлу который будем отправлять
        InputStream inputStream = BitmapUtils.getInputStream(getContext(), mUri);
        //Считаем длинну файла в виде строки base64
        Debug.log("File size: " + inputStream.available());
        int fileSize = (int) Math.ceil(inputStream.available() * 4 / 3);
        Debug.log("Base64 size: " + fileSize);
        //Дополнительные символы
        int padding = (fileSize % 4) == 0 ? 0 : 4 - (fileSize % 4);
        Debug.log("Base64 padding:  " + padding);
        fileSize += padding;
        Debug.log("Base64 file size: " + fileSize);
        //Считаем общую длинну получившегося запроса
        int contentLength = headersBytes.length + endBytes.length + fileSize;

        if (contentLength > 0) {
            //Отправляем наш  POST запрос
            writeRequest(
                    headersBytes,
                    endBytes,
                    inputStream,
                    HttpUtils.getOutputStream(contentLength, connection)
            );

            Debug.logJson(
                    ConnectionManager.TAG,
                    "REQUEST >>> " + mApiUrl + " rev:" + getRevNum(),
                    headers
            );
            return true;
        } else {
            return false;
        }
    }

    private void writeRequest(byte[] headersBytes, byte[] endBytes, InputStream inputStream, OutputStream outputStream) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(
                outputStream
        ));
        dos.write(headersBytes);
        Base64.encodeFromInputToOutputStream(inputStream, dos);
        dos.write(endBytes);
        dos.flush();
        dos.close();
        outputStream.close();
    }


    private String getHeaders() {
        return LINE_END + TWO_HH + BOUNDARY + LINE_END +
                //Это составной запрос
                "Content-Disposition: mixed" + LINE_END +
                //Первая часть обычный json с данными запроса
                "Content-Type: application/json" + LINE_END + LINE_END +
                //Записываем оснонвые данные запроса (json с нужными для API данными)
                toPostData() + LINE_END +
                TWO_HH + BOUNDARY + LINE_END +
                "Content-Disposition: mixed" + LINE_END +
                //Вторая часть это Картинка в формате Base64
                "Content-Type: image/jpeg" + LINE_END +
                //Мы отправляем картинку в виде строки Base64, о чем сообщаем в заголовке
                "Content-Transfer-Encoding: base64" + LINE_END + LINE_END;
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
            EasyTracker.getTracker().sendEvent("Profile", "PhotoAdd", "", 1L);
        } else {
            handleFail(ErrorCodes.MISSING_REQUIRE_PARAMETER, "Need set photo Uri");
        }
    }

    @Override
    protected String getContentType() {
        return PHOTO_ADD_CONTENT_TYPE;
    }
}

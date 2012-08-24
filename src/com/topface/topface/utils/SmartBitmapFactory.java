package com.topface.topface.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import com.topface.topface.R;
import com.topface.topface.utils.http.ConnectionManager;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.protocol.BasicHttpContext;

import java.io.IOException;
import java.io.InputStream;

/**
 * Класс облегчающий работу при создании битмапов
 * Главное предназначение это безоапасное
 */
public class SmartBitmapFactory {

    public static final int BITMAP_ERROR_RESOURCE = R.drawable.im_photo_error;

    private static SmartBitmapFactory mInstance;

    private SmartBitmapFactory() {
        super();
    }

    public static SmartBitmapFactory getInstance() {
        if (mInstance == null) {
            mInstance = new SmartBitmapFactory();
        }
        return mInstance;
    }

    public void setBitmapByUrl(final String url, final ImageView iv,
                               final int height, final int width,
                               final boolean clip, final BitmapHandler handler, int threadPriority) {
        Thread t = new Thread() {
            @Override
            public void run() {
                Debug.log("Try load image: " + url);
                int maxSize = Math.max(height, width);
                Bitmap bitmap = getBitmapByUrl(url, maxSize);
                //Обрезаем bitmap под размер imageView
                if (clip) {
                    bitmap = Utils.clipping(bitmap, width, height);
                }
                sendHandlerMessage(bitmap, handler);

                //Если у нас в результате получился bitmap, то устанавливаем его в ImageView
                final Bitmap finalBitmap = bitmap;
                iv.post(new Runnable() {
                    @Override
                    public void run() {
                        Debug.log("Runnable!!");
                        if (finalBitmap != null) {
                            iv.setImageBitmap(finalBitmap);
                        }
                        else {
                            iv.setImageResource(BITMAP_ERROR_RESOURCE);
                        }

                    }
                });
            }
        };
        //Мы используем максимальный приоретет, т.к. этот метод не используется для предзагрузки
        t.setPriority(threadPriority);
        t.start();
    }

    public void setBitmapByUrl(String url, final ImageView iv) {
        setBitmapByUrl(url, iv, null);
    }

    public void setBitmapByUrl(String url, final ImageView iv, BitmapHandler handler) {
        int maxSize = Device.getMaxDisplaySize();
        setBitmapByUrl(url, iv, maxSize, maxSize, false, handler, Thread.MAX_PRIORITY);
    }

    public void setBitmapByUrl(String url, final ImageView iv, BitmapHandler handler, int threadPriority) {
            int maxSize = Device.getMaxDisplaySize();
            setBitmapByUrl(url, iv, maxSize, maxSize, false, handler, threadPriority);
        }

    public void loadBitmapByUrl(final String url,
                               final int height, final int width,
                               final boolean clip, final BitmapHandler handler, int threadPriority) {
        Thread t = new Thread() {
            @Override
            public void run() {
                int maxSize = Math.max(height, width);
                Bitmap bitmap = getBitmapByUrl(url, maxSize);
                //Обрезаем bitmap под размер imageView
                if (clip) {
                    bitmap = Utils.clipping(bitmap, width, height);
                }
                Debug.log("Preload image: " + url);
                sendHandlerMessage(bitmap, handler);

            }
        };
        t.setPriority(threadPriority);
        t.start();
    }

    public void loadBitmapByUrl(final String url, final BitmapHandler handler) {
        int maxSize = Device.getMaxDisplaySize();
        //Мы используем минимальный приоретет, т.к. этот метод нужен для предзагрузки
        loadBitmapByUrl(url, maxSize, maxSize, false, handler, Thread.MIN_PRIORITY);
    }


    public void loadBitmapByUrl(final String url, final BitmapHandler handler, int threadPriority) {
        int maxSize = Device.getMaxDisplaySize();
        loadBitmapByUrl(url, maxSize, maxSize, false, handler, threadPriority);
    }

    private void sendHandlerMessage(Bitmap bitmap, BitmapHandler handler) {
        if (handler != null) {
            Message msg = new Message();
            msg.obj = bitmap;
            handler.sendMessage(msg);
        }
    }



    private Bitmap clipBitmap(Bitmap rawBitmap, int bitmapWidth, int bitmapHeight) {
        if (rawBitmap == null || bitmapWidth <= 0 || bitmapHeight <= 0)
            return null;

        // Исходный размер загруженного изображения
        int width = rawBitmap.getWidth();
        int height = rawBitmap.getHeight();

        // буль, длинная фото или высокая
        boolean leg = (width >= height);


        // коффициент сжатия фотографии
        float ratio = Math.max(bitmapWidth / width, bitmapHeight / height);

        // на получение оригинального размера по ширине или высоте
        if (ratio == 0) ratio = 1;

        // матрица сжатия
        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);

        // сжатие изображения
        Bitmap scaledBitmap = Bitmap.createBitmap(rawBitmap, 0, 0, width, height, matrix, true);

        // вырезаем необходимый размер
        Bitmap clippedBitmap;
        if (leg) {
            // у горизонтальной, вырезаем по центру
            int offset_x = (scaledBitmap.getWidth() - bitmapWidth) / 2;
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, offset_x, 0, bitmapWidth, bitmapHeight, null, false);
        } else {
            // у вертикальной режим с верху
            clippedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, bitmapWidth, bitmapHeight, null, false);
        }

        return clippedBitmap;
    }

    /**
     * Возвращает bitmap изображения по его url
     *
     * @param url адрес картинки для создания из нее битмапа
     * @param maxSize максимальный размер создаваемого битмапа,
     *                если исходное изображение меньше, оно не будет уменьшено
     *                Если maxSize = 0, вернется полноразмерный bitmap
     * @return битмап созданный из скачанного изображения
     */
    private Bitmap getBitmapByUrl(String url, int maxSize) {
        if (url == null) return null;
        Bitmap bitmap = null;
        HttpGet httpGet = new HttpGet(url);
        try {
            BasicHttpContext localContext = new BasicHttpContext();
            HttpResponse response = ConnectionManager.getInstance().getHttpClient().execute(httpGet, localContext);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (isBadStatusCode(statusCode)) {
                Debug.log("Bitmap loading wrong status:: " + statusCode);
                return null;
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                BufferedHttpEntity bufferedHttpEntity = new BufferedHttpEntity(entity);
                //Используем FluashedInputStream для фикса бага загрузки на плохом интернете на старых версиях android
                Http.FlushedInputStream fis = new Http.FlushedInputStream(
                        bufferedHttpEntity.getContent()
                );

                if (maxSize > 0) {
                    //Если передан максимальный необходимый размер битмапа, то для экономии оперативки,
                    //мы создаем уже уменьшенный битмап, не загружая в память его полную версию
                    BitmapFactory.Options options = getBitmapOptionWithScale(bufferedHttpEntity, maxSize);
                    bitmap = BitmapFactory.decodeStream(fis, null, options);
                }
                else {
                    bitmap = BitmapFactory.decodeStream(fis);
                }


                fis.close();
            }
        } catch (Exception e) {
            if (!httpGet.isAborted()) httpGet.abort();
            Debug.log("Bitmap loading error::" + e.getMessage());
        }

        return bitmap;
    }

    /**
     * Проверяем на то, что http код при запросе картинки не ошибка
     *
     * @param statusCode http код ответа
     * @return является ли валидным код ответа
     */
    private boolean isBadStatusCode(int statusCode) {
        return statusCode == 403 || statusCode == 404 || statusCode >= 500;
    }

    /**
     * Считывает характеристики изображения перед созданием битмапа и расчитывает во сколько раз его следует уменьшить
     *
     * @param bufferedHttpEntity Буферизованное HttpEntity для получения InputStream
     * @param maxSize максимальный необходимый размер битмапа
     * @return объект опций для BitmapFactory с заданым коэфицентом уменьшения исходного битмапа
     * @throws IOException
     */
    private BitmapFactory.Options getBitmapOptionWithScale(BufferedHttpEntity bufferedHttpEntity, int maxSize) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        InputStream is = bufferedHttpEntity.getContent();
        //Опция, сообщающая что не нужно грузить изображение в память, а только считать его данные
        options.inJustDecodeBounds = true;
        //Не загружая битмап, просто загружаем его характеристики
        BitmapFactory.decodeStream(is, null, options);
        //Получаем коэффицент, во сколько раз необходимо уменьшить битмап перед загрузкой
        options.inSampleSize = getBitmapScale(options, maxSize);
        //Используем тот же объект опций, что бы повторно его не создавать, переключая на режим
        options.inJustDecodeBounds = false;
        //Закрываем отработавший поток, из которого мы получили размеры изображения
        is.close();

        return options;
    }

    /**
     * Возвращает делитель, во сколько раз уменьшить размер изображения при создании битмапа
     *
     * @param options   InputStrem к изображению, для того, что бы получить его размеры, не загружая его в память
     * @param size размер до которого нужно уменьшить
     * @return делитель размера битмапа
     */
    public int getBitmapScale(BitmapFactory.Options options, int size) {
        //1 по умолчанию, значит что битмап нет необходимости уменьшать
        int scale = 1;

        //Определяем во сколько раз нужно уменьшить изображение для создания битмапа
        if (options.outHeight > size || options.outWidth > size) {
            scale = (int) Math.pow(2,
                    (int) Math.round(
                            Math.log(
                                    size /
                                    (double) Math.max(options.outHeight, options.outWidth)
                            ) /
                            Math.log(0.5)
                    )
            );
        }
        Debug.log("Bitmap " + options.outHeight + " x " + options.outWidth + " scaled x" + scale + " for screen " + size);

        return scale;
    }

    public static abstract class BitmapHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            handleBitmap((Bitmap) msg.obj);
        }

        abstract public void handleBitmap(Bitmap bitmap);
    }
}

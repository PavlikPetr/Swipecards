package com.topface.framework.imageloader;

/**
 * Created by kirussell on 24.06.2014.
 * Interface for ImageLoader interaction with photos
 */
public interface IPhoto {

    /**
     * Этот флаг нужен для того, чтобы ставить пустые фото в поиске, которые, будут подгружаться после запроса альбома
     *
     * @return true if photo is not downloaded yet
     */
    boolean isFake();

    /**
     * Picks link which is corresponded to given place sizes
     *
     * @param height place height
     * @param width  place width
     * @return url to download image
     */
    String getSuitableLink(int height, int width);

    /**
     * Picks link which suits to given size
     *
     * @param sizeString server defined sizes
     * @return url to download image
     */
    @SuppressWarnings("unused")
    String getSuitableLink(String sizeString);

    String getDefaultLink();
}

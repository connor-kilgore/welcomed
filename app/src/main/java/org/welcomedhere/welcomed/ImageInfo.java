package org.welcomedhere.welcomed;

import java.io.Serializable;

public class ImageInfo implements Serializable {
    public String name;
    public int size;
    ImageOpertion op;

    enum ImageOpertion
    {
        SEND_PROFILE_PICTURE,
        GET_PROFILE_PICTURE,
        SEND_REVIEW_PICTURE,
        GET_REVIEW_PICTURE,
        SEND_REPORT_PICTURE,
        GET_REPORT_PICTURE
        }

    public ImageInfo(String name, int size)
    {
        this.name = name;
        this.size = size;
    }
}

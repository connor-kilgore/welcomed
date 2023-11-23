package org.welcomedhere.welcomed;

import android.media.ExifInterface;
import android.net.Uri;

import java.io.IOException;
import java.io.Serializable;

public class ImageInfo implements Serializable {
    public String path;
    public Uri photoUri;
    public ImageOpertion op;

    public enum ImageOpertion
    {
        SEND_PROFILE_PICTURE,
        GET_PROFILE_PICTURE,
        SEND_REVIEW_PICTURE,
        GET_REVIEW_PICTURE,
        SEND_REPORT_PICTURE,
        GET_REPORT_PICTURE
    }

    public ImageInfo(String path, Uri photoUri)
    {
        this.path = path;
        this.photoUri = photoUri;
    }

    public static int getCameraPhotoOrientation(String imagePath) {
        int rotate = 0;
        try {
            ExifInterface exif  = null;
            try {
                exif = new ExifInterface(imagePath);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotate = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotate = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotate = 90;
                    break;
                default:
                    rotate = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotate;
    }
}

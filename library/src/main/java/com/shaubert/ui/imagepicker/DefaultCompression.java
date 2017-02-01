package com.shaubert.ui.imagepicker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.util.Log;

import java.io.*;

public class DefaultCompression implements Compression {

    private static final String TAG = DefaultCompression.class.getSimpleName();

    @Override
    public boolean compressImage(File source, File destination, CompressionOptions compressionOptions) {
        return compressImage(source, destination, compressionOptions.targetWidth,
                compressionOptions.targetHeight, compressionOptions.maxFileSize);
    }

    private boolean compressImage(File source, File destination, int targetWidth, int targetHeight, long maxSize) {
        InputStream boundsStream = null;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            boundsStream = new FileInputStream(source);
            BitmapFactory.decodeStream(boundsStream, null, options);
            int w = options.outWidth;
            int h = options.outHeight;
            if (w > 0 && h > 0) {
                options.inSampleSize = ImageSizeUtils.computeImageSampleSize(new ImageSize(w, h),
                        new ImageSize(targetWidth, targetHeight), true);
            }
            options.inJustDecodeBounds = false;
            is = new FileInputStream(source);
            Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
            int quality = 100;
            long currentSize;
            do {
                quality -= 10;
                fos = new FileOutputStream(destination);
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
                fos.close();
                currentSize = destination.length();
            } while (quality > 50
                    && currentSize > maxSize
                    && maxSize > 0);
            bitmap.recycle();
            copyExif(source, destination);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "failed to create compressed image", e);
        } finally {
            if (boundsStream != null) {
                try {
                    boundsStream.close();
                } catch (IOException ignored) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
        return false;
    }

    public static void copyExif(File source, File destination) {
        try {
            ExifInterface oldExif = new ExifInterface(source.getAbsolutePath());
            String exifOrientation = oldExif.getAttribute(ExifInterface.TAG_ORIENTATION);
            ExifInterface newExif = new ExifInterface(destination.getAbsolutePath());
            if (exifOrientation != null) {
                newExif.setAttribute(ExifInterface.TAG_ORIENTATION, exifOrientation);
            }
            newExif.saveAttributes();
        } catch (Exception ex) {
            Log.d(TAG, "failed to copy exif", ex);
        }
    }

}

package com.shaubert.ui.imagepicker;

import android.opengl.GLES10;

import javax.microedition.khronos.opengles.GL10;

/**
 * @author Sergey Tarasevich (nostra13[at]gmail[dot]com)
 */
final class ImageSizeUtils {

    private static final int DEFAULT_MAX_BITMAP_DIMENSION = 2048;

    private static ImageSize maxBitmapSize;

    static {
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        int maxBitmapDimension = Math.max(maxTextureSize[0], DEFAULT_MAX_BITMAP_DIMENSION);
        maxBitmapSize = new ImageSize(maxBitmapDimension, maxBitmapDimension);
    }

    private ImageSizeUtils() {
    }

    /**
     * Computes sample size for downscaling image size (<b>srcSize</b>) to view size (<b>targetSize</b>). This sample
     * size is used during
     * {@linkplain BitmapFactory#decodeStream(java.io.InputStream, android.graphics.Rect, android.graphics.BitmapFactory.Options)
     * decoding image} to bitmap.<br />
     * <br />
     * <b>Examples:</b><br />
     * <p/>
     * <pre>
     * srcSize(100x100), targetSize(10x10), powerOf2Scale = true -> sampleSize = 8
     * srcSize(100x100), targetSize(10x10), powerOf2Scale = false -> sampleSize = 10
     *
     * srcSize(100x100), targetSize(20x40), viewScaleType = FIT_INSIDE -> sampleSize = 5
     * srcSize(100x100), targetSize(20x40), viewScaleType = CROP       -> sampleSize = 2
     * </pre>
     * <p/>
     * <br />
     * The sample size is the number of pixels in either dimension that correspond to a single pixel in the decoded
     * bitmap. For example, inSampleSize == 4 returns an image that is 1/4 the width/height of the original, and 1/16
     * the number of pixels. Any value <= 1 is treated the same as 1.
     *
     * @param srcSize       Original (image) size
     * @param targetSize    Target (view) size
     * @param viewScaleType {@linkplain ViewScaleType Scale type} for placing image in view
     * @param powerOf2Scale <i>true</i> - if sample size be a power of 2 (1, 2, 4, 8, ...)
     * @return Computed sample size
     */
    public static int computeImageSampleSize(ImageSize srcSize, ImageSize targetSize, boolean powerOf2Scale) {
        final int srcWidth = srcSize.getWidth();
        final int srcHeight = srcSize.getHeight();
        final int targetWidth = targetSize.getWidth();
        final int targetHeight = targetSize.getHeight();

        int scale = 1;

        if (powerOf2Scale) {
            final int halfWidth = srcWidth / 2;
            final int halfHeight = srcHeight / 2;
            while ((halfWidth / scale) > targetWidth || (halfHeight / scale) > targetHeight) { // ||
                scale *= 2;
            }
        } else {
            scale = Math.max(srcWidth / targetWidth, srcHeight / targetHeight); // max
        }

        if (scale < 1) {
            scale = 1;
        }
        scale = considerMaxTextureSize(srcWidth, srcHeight, scale, powerOf2Scale);

        return scale;
    }

    private static int considerMaxTextureSize(int srcWidth, int srcHeight, int scale, boolean powerOf2) {
        final int maxWidth = maxBitmapSize.getWidth();
        final int maxHeight = maxBitmapSize.getHeight();
        while ((srcWidth / scale) > maxWidth || (srcHeight / scale) > maxHeight) {
            if (powerOf2) {
                scale *= 2;
            } else {
                scale++;
            }
        }
        return scale;
    }

}
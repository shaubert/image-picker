package com.shaubert.ui.imagepicker;

import android.app.Activity;

public class ImagePickerConfig {

    private static boolean intialized;

    private static ImageLoader imageLoader;
    private static Class<? extends Activity> openImageActivityClass;
    private static Class<? extends Activity> cropImageActivityClass;

    public static class Setup {
        private ImageLoader imageLoader;
        private Class<? extends Activity> openImageActivityClass;
        private Class<? extends Activity> cropImageActivityClass;

        private Setup() {
        }

        public Setup imageLoader(ImageLoader imageLoader) {
            this.imageLoader = imageLoader;
            return this;
        }

        public Setup openImageActivityClass(Class<? extends Activity> openImageActivityClass) {
            this.openImageActivityClass = openImageActivityClass;
            return this;
        }

        public Setup cropImageActivityClass(Class<? extends Activity> cropImageActivityClass) {
            this.cropImageActivityClass = cropImageActivityClass;
            return this;
        }

        public void apply() {
            if (imageLoader == null) {
                throw new NullPointerException("Please specify image loader");
            }
            if (openImageActivityClass == null) {
                openImageActivityClass = ImageViewActivity.class;
            }
            if (cropImageActivityClass == null) {
                cropImageActivityClass = CropImageActivity.class;
            }

            ImagePickerConfig.setup(this);
        }
    }

    public static Setup setup() {
        return new Setup();
    }

    private static synchronized void setup(Setup setup) {
        ImagePickerConfig.imageLoader = setup.imageLoader;
        ImagePickerConfig.openImageActivityClass = setup.openImageActivityClass;
        ImagePickerConfig.cropImageActivityClass = setup.cropImageActivityClass;

        ImagePickerConfig.intialized = true;
    }

    public static synchronized Class<? extends Activity> getCropImageActivityClass() {
        throwIfNotInitialized();
        return cropImageActivityClass;
    }

    public static synchronized Class<? extends Activity> getOpenImageActivityClass() {
        throwIfNotInitialized();
        return openImageActivityClass;
    }

    public static synchronized ImageLoader getImageLoader() {
        throwIfNotInitialized();
        return imageLoader;
    }

    private static void throwIfNotInitialized() {
        if (!ImagePickerConfig.intialized) {
            throw new IllegalStateException("You need to setup ImagePickerConfig before usage of ImagePicker");
        }
    }
}

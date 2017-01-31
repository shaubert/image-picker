package com.shaubert.ui.imagepicker;

import com.shaubert.ui.imagepicker.nostra.ViewScaleType;

public class CompressionOptions {
    public final int maxFileSize;
    public final int targetWidth;
    public final int targetHeight;
    public final ViewScaleType targetScaleType;

    CompressionOptions(Builder builder) {
        maxFileSize = builder.maxFileSize;
        targetWidth = builder.targetWidth;
        targetHeight = builder.targetHeight;
        targetScaleType = builder.targetScaleType;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private int maxFileSize = ImagePickerController.DEFAULT_MAX_FILE_SIZE;
        private int targetWidth = ImagePickerController.DEFAULT_TARGET_IMAGE_WIDTH;
        private int targetHeight = ImagePickerController.DEFAULT_TARGET_IMAGE_HEIGHT;
        private ViewScaleType targetScaleType = ImagePickerController.DEFAULT_TARGET_SCALE_TYPE;

        private Builder() {
        }

        public Builder maxFileSize(int maxFileSize) {
            this.maxFileSize = maxFileSize;
            return this;
        }

        public Builder targetWidth(int targetWidth) {
            this.targetWidth = targetWidth;
            return this;
        }

        public Builder targetHeight(int targetHeight) {
            this.targetHeight = targetHeight;
            return this;
        }

        public Builder targetScaleType(ViewScaleType targetScaleType) {
            this.targetScaleType = targetScaleType;
            return this;
        }

        public CompressionOptions build() {
            return new CompressionOptions(this);
        }
    }
}

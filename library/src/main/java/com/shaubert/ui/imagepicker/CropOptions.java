package com.shaubert.ui.imagepicker;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class CropOptions implements Parcelable {
    private final Uri inUri;
    private final Uri outUri;
    private final int minWidth;
    private final int minHeight;
    private final int maxWidth;
    private final int maxHeight;
    private final int aspectX;
    private final int aspectY;
    private final Bitmap.CompressFormat compressFormat;
    private final int compressQuality;
    private final int bottomInfoLayout;

    private CropOptions(Builder builder) {
        inUri = builder.inUri;
        outUri = builder.outUri;
        minWidth = builder.minWidth;
        minHeight = builder.minHeight;
        maxWidth = builder.maxWidth;
        maxHeight = builder.maxHeight;
        aspectX = builder.aspectX;
        aspectY = builder.aspectY;
        compressFormat = builder.compressFormat;
        compressQuality = builder.compressQuality;
        bottomInfoLayout = builder.bottomInfoLayout;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public Uri getInUri() {
        return inUri;
    }

    public Uri getOutUri() {
        return outUri;
    }

    public int getMinWidth() {
        return minWidth;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getAspectX() {
        return aspectX;
    }

    public int getAspectY() {
        return aspectY;
    }

    public Bitmap.CompressFormat getCompressFormat() {
        return compressFormat;
    }

    public int getCompressQuality() {
        return compressQuality;
    }

    public int getBottomInfoLayout() {
        return bottomInfoLayout;
    }

    public static final class Builder {
        private Uri inUri;
        private Uri outUri;
        private int minWidth;
        private int minHeight;
        private int maxWidth;
        private int maxHeight;
        private int aspectX;
        private int aspectY;
        private Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
        private int compressQuality = 90;
        private int bottomInfoLayout;

        private Builder() {
        }

        Builder inUri(Uri inUri) {
            this.inUri = inUri;
            return this;
        }

        Builder outUri(Uri outUri) {
            this.outUri = outUri;
            return this;
        }

        public Builder minWidth(int minWidth) {
            this.minWidth = minWidth;
            return this;
        }

        public Builder minHeight(int minHeight) {
            this.minHeight = minHeight;
            return this;
        }

        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder maxHeight(int maxHeight) {
            this.maxHeight = maxHeight;
            return this;
        }

        public Builder aspectX(int aspectX) {
            this.aspectX = aspectX;
            return this;
        }

        public Builder aspectY(int aspectY) {
            this.aspectY = aspectY;
            return this;
        }

        public Builder compressFormat(Bitmap.CompressFormat compressFormat) {
            this.compressFormat = compressFormat;
            return this;
        }

        public Builder compressQuality(int compressQuality) {
            this.compressQuality = compressQuality;
            return this;
        }

        public Builder bottomInfoLayout(int bottomInfoLayout) {
            this.bottomInfoLayout = bottomInfoLayout;
            return this;
        }

        public Bitmap.CompressFormat getCompressFormat() {
            return compressFormat;
        }

        public CropOptions build() {
            if (outUri == null) throw new NullPointerException("you have to specify outUri");
            if (inUri == null) throw new NullPointerException("you have to specify inUri");
            if (compressFormat == null)  {
                compressFormat = Bitmap.CompressFormat.JPEG;
                compressQuality = 90;
            }

            return new CropOptions(this);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.inUri, 0);
        dest.writeParcelable(this.outUri, 0);
        dest.writeInt(this.minWidth);
        dest.writeInt(this.minHeight);
        dest.writeInt(this.maxWidth);
        dest.writeInt(this.maxHeight);
        dest.writeInt(this.aspectX);
        dest.writeInt(this.aspectY);
        Enums.toParcel(this.compressFormat, dest);
        dest.writeInt(this.compressQuality);
        dest.writeInt(this.bottomInfoLayout);
    }

    private CropOptions(Parcel in) {
        this.inUri = in.readParcelable(Uri.class.getClassLoader());
        this.outUri = in.readParcelable(Uri.class.getClassLoader());
        this.minWidth = in.readInt();
        this.minHeight = in.readInt();
        this.maxWidth = in.readInt();
        this.maxHeight = in.readInt();
        this.aspectX = in.readInt();
        this.aspectY = in.readInt();
        this.compressFormat = Enums.fromParcel(Bitmap.CompressFormat.class, in);
        this.compressQuality = in.readInt();
        this.bottomInfoLayout = in.readInt();
    }

    public static final Creator<CropOptions> CREATOR = new Creator<CropOptions>() {
        public CropOptions createFromParcel(Parcel source) {
            return new CropOptions(source);
        }

        public CropOptions[] newArray(int size) {
            return new CropOptions[size];
        }
    };
}


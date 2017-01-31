package com.shaubert.ui.imagepicker;

import android.os.Bundle;
import android.os.Parcel;
import android.text.TextUtils;

class Enums {

    public static <T extends Enum<T>> void toBundle(T enumVal, Bundle bundle, String key) {
        if (enumVal != null) {
            bundle.putString(key, enumVal.name());
        }
    }

    public static <T extends Enum<T>> T fromBundle(Class<T> enumClass, Bundle bundle, String key) {
        if (bundle != null && bundle.containsKey(key)) {
            return Enum.valueOf(enumClass, bundle.getString(key));
        }
        return null;
    }

    public static <T extends Enum<T>> void toParcel(T enumVal, Parcel parcel) {
        if (enumVal == null) {
            parcel.writeString(null);
        } else {
            parcel.writeString(enumVal.name());
        }
    }

    public static <T extends Enum<T>> T fromParcel(Class<T> enumClass, Parcel parcel) {
        String val = parcel.readString();
        if (TextUtils.isEmpty(val)) {
            return null;
        } else {
            return Enum.valueOf(enumClass, val);
        }
    }

}

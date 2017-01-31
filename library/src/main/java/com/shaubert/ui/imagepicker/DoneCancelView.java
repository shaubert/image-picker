package com.shaubert.ui.imagepicker;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

public class DoneCancelView extends LinearLayout {

    private View doneButton;
    private View cancelButton;

    private Callback callback;

    public DoneCancelView(Context context) {
        super(context);
        init();
    }

    public DoneCancelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DoneCancelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DoneCancelView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.sh_image_picker_done_cancel_view, this);
        doneButton = findViewById(R.id.sh_image_picker_ok_button);
        cancelButton = findViewById(R.id.sh_image_picker_cancel_button);

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onDone();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (callback != null) {
                    callback.onCancel();
                }
            }
        });
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        boolean onDone();
        boolean onCancel();
    }

}
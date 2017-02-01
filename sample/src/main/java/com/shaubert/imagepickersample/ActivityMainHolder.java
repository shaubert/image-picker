package com.shaubert.imagepickersample;

import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;

public class ActivityMainHolder {
    private ImageView image3;
    private ProgressBar image3Progress;
    private View image3Error;
    private Button load3Button;
    private ImageView image4;
    private Button load4Button;
    private Button load5Button;
    private ImageView image6;
    private Button load6Button;

    public ActivityMainHolder(Activity activity) {
        image3 = (ImageView) activity.findViewById(R.id.image_3);
        image3Progress = (ProgressBar) activity.findViewById(R.id.progress_image_3);
        image3Error = activity.findViewById(R.id.error_image_3);
        load3Button = (Button) activity.findViewById(R.id.load_3_button);
        image4 = (ImageView) activity.findViewById(R.id.image_4);
        load4Button = (Button) activity.findViewById(R.id.load_4_button);
        load5Button = (Button) activity.findViewById(R.id.load_5_button);
        image6 = (ImageView) activity.findViewById(R.id.image_6);
        load6Button = (Button) activity.findViewById(R.id.load_6_button);
    }

    public ImageView getImage3() {
        return image3;
    }

    public Button getLoad4Button() {
        return load4Button;
    }

    public ImageView getImage4() {
        return image4;
    }

    public Button getLoad3Button() {
        return load3Button;
    }

    public ProgressBar getImage3Progress() {
        return image3Progress;
    }

    public View getImage3Error() {
        return image3Error;
    }

    public Button getLoad5Button() {
        return load5Button;
    }

    public ImageView getImage6() {
        return image6;
    }

    public Button getLoad6Button() {
        return load6Button;
    }

}

package com.shaubert.ui.imagepicker;

public interface EditActionsPresenter {

    void bindTo(ImagePickerController controller);

    void showEditImageDialog();

    void showAddImageDialog();

    void showEditNotLoadedImageDialog();

}

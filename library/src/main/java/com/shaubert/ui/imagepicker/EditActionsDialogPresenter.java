package com.shaubert.ui.imagepicker;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.ArrayAdapter;

import androidx.fragment.app.FragmentManager;

import com.shaubert.ui.dialogs.ListDialogManager;

public class EditActionsDialogPresenter implements EditActionsPresenter {

    private final Context context;
    private final FragmentManager fragmentManager;

    private ListDialogManager editImageDialog;
    private ListDialogManager editNotLoadedImageDialog;
    private ListDialogManager addImageDialog;
    private ImagePickerController controller;

    public EditActionsDialogPresenter(Context context, FragmentManager fragmentManager, String tag) {
        this.context = context;
        this.fragmentManager = fragmentManager;
        EditAction[] editActions = new EditAction[]{
                new EditAction(context.getString(R.string.sh_image_picker_image_edit_dialog_option_open), new Runnable() {
                    @Override
                    public void run() {
                        showImageFullScreen();
                    }
                }),
                new EditAction(context.getString(R.string.sh_image_picker_image_edit_dialog_option_take_photo), new Runnable() {
                    @Override
                    public void run() {
                        onTakePhotoClicked();
                    }
                }),
                new EditAction(context.getString(R.string.sh_image_picker_image_edit_dialog_option_choose_picture), new Runnable() {
                    @Override
                    public void run() {
                        onPickPictureClicked();
                    }
                }),
                new EditAction(context.getString(R.string.sh_image_picker_image_edit_dialog_option_remove), new Runnable() {
                    @Override
                    public void run() {
                        onRemoveImageClicked();
                    }
                })
        };

        String[] names = new String[editActions.length];
        int i = 0;
        for (EditAction editAction : editActions) {
            names[i++] = editAction.name;
        }

        editImageDialog = createImageOptionsDialog(names, editActions, tag + "-edit-image-dialog");
        editNotLoadedImageDialog = createImageOptionsDialog(
                new String[] {names[1], names[2], names[3]},
                new EditAction[] {editActions[1], editActions[2], editActions[3]},
                tag + "-edit-not-loaded-image-dialog"
        );
        addImageDialog = createImageOptionsDialog(
                new String[] {names[1], names[2]},
                new EditAction[] {editActions[1], editActions[2]},
                tag +"-add-image-dialog"
        );
    }

    private void onRemoveImageClicked() {
        if (controller != null) {
            controller.onRemoveImageClicked();
        }
    }

    private void onPickPictureClicked() {
        if (controller != null) {
            controller.onPickPictureClicked();
        }
    }

    private void onTakePhotoClicked() {
        if (controller != null) {
            controller.onTakePhotoClicked();
        }
    }

    private void showImageFullScreen() {
        if (controller != null) {
            controller.showImageFullScreen();
        }
    }

    @Override
    public void bindTo(ImagePickerController controller) {
        this.controller = controller;
    }

    private ListDialogManager createImageOptionsDialog(String[] labels, final EditAction[] actions, String tag) {
        final ListDialogManager dialog = new ListDialogManager(fragmentManager, tag);
        ArrayAdapter<String> dialogAdapter = new ArrayAdapter<>(context,
                R.layout.sh_image_picker_select_dialog_item, labels);
        dialog.setListAdapter(dialogAdapter);
        dialog.setOnItemClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                actions[which].action.run();
                dialog.hideDialog();
            }
        });
        dialog.setCancellable(true);
        dialog.setCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                controller.onActionsDialogCancelled();
            }
        });

        return dialog;
    }

    @Override
    public void showEditImageDialog() {
        editImageDialog.showDialog();
    }

    @Override
    public void showAddImageDialog() {
        addImageDialog.showDialog();
    }

    @Override
    public void showEditNotLoadedImageDialog() {
        editNotLoadedImageDialog.showDialog();
    }

    private static class EditAction {
        String name;
        Runnable action;

        private EditAction(String name, Runnable action) {
            this.name = name;
            this.action = action;
        }
    }
}


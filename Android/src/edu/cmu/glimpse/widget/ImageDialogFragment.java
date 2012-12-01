package edu.cmu.glimpse.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import edu.cmu.glimpse.activities.R;

public class ImageDialogFragment extends DialogFragment {
    private Bitmap mBitmap;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View imageDialog = inflater.inflate(R.layout.image_dialog, null);
        ImageView imageView = (ImageView) imageDialog.findViewById(R.id.image_dialog_image);
        if (mBitmap != null) {
            imageView.setImageBitmap(mBitmap);
        }

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(imageDialog)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getDialog().cancel();
                    }
                })
                .setCancelable(false);
        return builder.create();
    }

    public void setImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }
}

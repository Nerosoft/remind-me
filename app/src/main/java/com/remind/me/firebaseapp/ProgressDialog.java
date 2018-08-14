package com.remind.me.firebaseapp;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by nero on 05/08/18.
 */

public class ProgressDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.progress_dialog,null);
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        builder.setView(view);
        builder.setCancelable(false);
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){

                }
                return true;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);



        return dialog;
    }



}

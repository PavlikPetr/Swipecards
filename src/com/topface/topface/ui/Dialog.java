package com.topface.topface.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Dialog {
  public static void messageBox(Activity context,String text) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setMessage(text);
    builder.setCancelable(false);
    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int id) {
        dialog.cancel();
      }
    });
    builder.create().show();
  }
}

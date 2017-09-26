package com.ioannisgk.evsharingapp.utils;


import android.app.AlertDialog;
import android.content.Context;


public class Settings {

    // Show dialogue box message

    public static void showDialogBox(String title, String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message).setNegativeButton("Retry", null).create().show();
    }

    public static void showDialogBoxSuccess(String title, String message, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message).setPositiveButton("OK", null).create().show();
    }
}
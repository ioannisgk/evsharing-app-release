package com.ioannisgk.evsharingapp.utils;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ioannisgk.evsharingapp.R;
import com.ioannisgk.evsharingapp.RegisterActivity;
import com.ioannisgk.evsharingapp.RequestActivity;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Settings {

    public static void showToast(Context context, String message) {

        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    // Validate input data from the registration form

    public static boolean validateRegisterData(String theName, String theUsername, String thePassword, String theDate, Context context) {
        int year = 0, currentYear = 0;

        // Extract year string and convert it to int variable

        if ((theDate.length() == 8) || (theDate.length() == 9) || (theDate.length() == 10)) {
            String theYear = theDate.substring(theDate.length() - 4);
            year = Integer.parseInt(theYear);
            currentYear = Calendar.getInstance().get(Calendar.YEAR);
        }

        if ((theName.isEmpty()) || (theUsername.isEmpty()) || (thePassword.isEmpty())) {

            showDialogBox("Register error", "Empty fields detected", context);
            return false;

        } else if ((theName.length() < 6) || (theUsername.length() < 6) || (thePassword.length() < 6)) {

            showDialogBox("Register error", "Minimum number of chars is 6", context);
            return false;

        } else if ((theUsername.length() > 45) || (thePassword.length() > 45)) {

            showDialogBox("Register error", "Maximum number of chars is 45", context);
            return false;

        } else if (((currentYear - year) <= 18) || ((currentYear - year) >100)) {

            showDialogBox("Register error", "You must be 19-100 years old to use this service", context);
            return false;

        } else {

            // Use regex to check if the username is a valid email

            Pattern pattern = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,4}");
            Matcher mat = pattern.matcher(theUsername);

            if (!mat.matches()) {

                showDialogBox("Register error", "Username is not a valid email", context);
                return false;
            }
        }
        return true;
    }

    // Validate input data from the profile form

    public static boolean validateProfileData(String theName, String theDate, Context context) {
        int year = 0, currentYear = 0;

        // Extract year string and convert it to int variable

        if ((theDate.length() == 8) || (theDate.length() == 9) || (theDate.length() == 10)) {
            String theYear = theDate.substring(theDate.length() - 4);
            year = Integer.parseInt(theYear);
            currentYear = Calendar.getInstance().get(Calendar.YEAR);
        }

        if (theName.isEmpty()) {

            showDialogBox("Register error", "Empty fields detected", context);
            return false;

        } else if (theName.length() < 6) {

            showDialogBox("Register error", "Minimum number of chars is 6", context);
            return false;

        } else if (((currentYear - year) <= 18) || ((currentYear - year) >100)) {

            showDialogBox("Register error", "You must be 19-100 years old to use this service", context);
            return false;

        }
        return true;
    }

    // Validate input data from the request form

    public static boolean validateRequestData(int startStationID, int finishStationID, String theTime, Context context) {

        if (startStationID == finishStationID) {

            showDialogBox("Request error", "Start and destination stations can not be the same", context);
            return false;

        } else if (theTime.isEmpty()) {

            showDialogBox("Request error", "Empty fields detected", context);
            return false;

        } else if (Integer.parseInt(theTime.substring(0,2)) < 6) {

            showDialogBox("Request error", "Earliest time is 06:00", context);
            return false;

        } else if (Integer.parseInt(theTime.substring(0,2)) > 22) {

            showDialogBox("Request error", "Latest time is 22:00", context);
            return false;

        }
        return true;
    }

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
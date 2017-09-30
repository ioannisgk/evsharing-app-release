package com.ioannisgk.evsharingapp.utils;


import android.app.AlertDialog;
import android.content.Context;

import com.ioannisgk.evsharingapp.RegisterActivity;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Settings {

    // Validate input data from the registration form

    public static boolean validate(String theName, String theUsername, String thePassword, String theDate, Context context) {
        int year = 0, currentYear = 0;

        // Extract year string and convert it to int variable

        System.out.println("DATE: " + theDate);

        if ((theDate.length() == 8) || (theDate.length() == 9) || (theDate.length() == 10)) {
            String theYear = theDate.substring(theDate.length() - 4);
            year = Integer.parseInt(theYear);
            currentYear = Calendar.getInstance().get(Calendar.YEAR);

            System.out.println("YEAR ENTERED: " + year + "CURRENT YEAR: " + currentYear);
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
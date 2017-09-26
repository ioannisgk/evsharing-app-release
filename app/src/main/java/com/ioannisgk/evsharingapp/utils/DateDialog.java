package com.ioannisgk.evsharingapp.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;

public class DateDialog extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    EditText textDate;

    public DateDialog(View view) {
        textDate = (EditText)view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use current date as the default date in the dialog

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    // Show the selected date in the text box

    public void onDateSet(DatePicker view, int year, int month, int day) {
        String date = day + "/" + (month + 1) + "/" + year;
        textDate.setText(date);
    }
}
package com.ioannisgk.evsharingapp.utils;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimeDialog extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    EditText textTime;

    public TimeDialog(View view) {
        textTime = (EditText)view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use current time as the default time in the dialog

        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of DatePickerDialog
        return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
    }

    // Show the selected date in the text box

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        boolean isDoubleDigit = (minute > 9 && minute < 100) || (minute < -9 && minute > -100);
        String time = "";

        if (isDoubleDigit == true) {
            time = hourOfDay + ":" + minute;
        } else {
            time = hourOfDay + ":0" + minute;
        }
        textTime.setText(time);
    }
}
package com.ioannisgk.evsharingapp.utils;

import android.database.sqlite.SQLiteDatabase;

import com.ioannisgk.evsharingapp.entities.User;

import java.io.File;

public class Global {

    public static String ipAddress;
    public static int portNumber;

    public static User currentUser;
    public static SQLiteDatabase myDB;
    public static File databaseFile;

}
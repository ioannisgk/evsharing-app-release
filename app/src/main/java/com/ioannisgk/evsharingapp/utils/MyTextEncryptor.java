package com.ioannisgk.evsharingapp.utils;

import org.jasypt.util.text.BasicTextEncryptor;

// We use jasypt for encryption/decryption of user passwords

public class MyTextEncryptor {

    // Class attribute
    private BasicTextEncryptor textEncryptor = new BasicTextEncryptor();

    // Class constructor
    public MyTextEncryptor() {
        textEncryptor.setPassword("evsharingPassEncryptor");
    }

    // Method to encrypt a password

    public String encryptPassword(String plainPassword) {

        return textEncryptor.encrypt(plainPassword);
    }

    // Method to decrypt a password

    public String decryptPassword(String encryptedPassword) {

        return textEncryptor.decrypt(encryptedPassword);
    }
}
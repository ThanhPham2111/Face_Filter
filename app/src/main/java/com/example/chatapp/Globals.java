package com.example.chatapp;

import java.nio.charset.StandardCharsets;

public class Globals {

    //User
    public static String USER_EMAIL = null;
    //Message Sender
    public static String MESSAGE_SENDER = "temporary";
    //Database Conversation Table
    public static final String CONVERSATION_TABLE = "Conversation";
    public static final String C_COLUMN_NAME = "person_name";
    public static final String C_COLUMN_LAST_MESSAGE = "last_message";
    public static final String C_COLUMN_TIMESTAMP = "timestamp";
    public static final String C_COLUMN_MESSAGE_TYPE = "message_type";
    //Database Message Table
    public static final String MESSAGE_TABLE = "Message";
    public static final String M_COLUMN_USERNAME = "username";
    public static final String M_COLUMN_DETAIL = "detail";
    public static final String M_COLUMN_TIME = "time";
    public static final String M_COLUMN_IS_SENDER = "is_sender";
    public static final String M_COLUMN_C_ID = "c_id";     //conversation id
    public static final String M_COLUMN_CONTENT_TYPE = "content_type";
    public static final String M_COLUMN_MEDIA_PAYLOAD = "media_payload";
    public static final String M_COLUMN_MEDIA_DURATION = "media_duration";

    //database
    public static IChatInterface dao;

    //firebase persistance enabled
    public static Boolean isPersistenceEnabled = false;
    //firebase constants
    public static String Full_Name = "full_name";
    public static final String Email_Extension = "@chatapp.com";
    public static final String CHAT_DB = "ChatDb";
    // functions
    //global function that takes phone number as input and check it is correct or not
    public static boolean verifyPhoneNumber(String phoneNumber){
        return !formatPhoneNumber(phoneNumber).equals("-1");
    }

    public static String formatPhoneNumber(String phoneNumber){
        if(phoneNumber == null){
            return "-1";
        }
        phoneNumber = phoneNumber.replaceAll("\\s", "");
        // Keep only digits so users can input spaces or separators.
        phoneNumber = phoneNumber.replaceAll("[^0-9]", "");

        // Accept local forms: 0xxxxxxxxx (10 digits) or legacy 0xxxxxxxxxx (11 digits).
        if(phoneNumber.startsWith("0") && (phoneNumber.length() == 10 || phoneNumber.length() == 11)){
            return phoneNumber;
        }

        // Accept +84/84 form and normalize to local 0xxxxxxxxx.
        if(phoneNumber.startsWith("84") && phoneNumber.length() == 11){
            return "0" + phoneNumber.substring(2);
        }

        return "-1";
    }
}

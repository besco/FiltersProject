package com.sanai.filters_v_01.utils;

import com.sanai.filters_v_01.consts.AllConsts;

import org.json.JSONException;
import org.json.JSONObject;

public class UserInfo {

    private static String UName ;
    private static String Email;
    private static String Phone;
    private static String Date ;

    public UserInfo(String jsonAboutUser) {
        try {
            JSONObject jObj = new JSONObject(jsonAboutUser);
            UName = jObj.getString(AllConsts.TAG_NAME) ;
            Email = jObj.getString(AllConsts.TAG_EMAIL);
            Phone = jObj.getString(AllConsts.TAG_PHONE);
            Date  = jObj.getString(AllConsts.TAG_CRDT) ;
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static String getUName() {
        return UName;
    }

    public static void setUName(String UName) {
        UserInfo.UName = UName;
    }

    public static String getEmail() {
        return Email;
    }

    public static void setEmail(String email) {
        Email = email;
    }

    public static String getPhone() {
        return Phone;
    }

    public static void setPhone(String phone) {
        Phone = phone;
    }

    public static String getDate() {
        return Date;
    }

    public static void setDate(String date) {
        Date = date;
    }
}

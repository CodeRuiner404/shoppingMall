package com.ibei.mall.util;

import org.apache.tomcat.util.codec.binary.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils {
    public static String getMD5Str(String strValue) throws NoSuchAlgorithmException {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return Base64.encodeBase64String(md5.digest((strValue+Constant.SALT).getBytes()));

    }

    public static void main(String[] args) {
        try {
            System.out.println(getMD5Str("1234"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}

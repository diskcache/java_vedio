package com.example.util;

import java.math.BigInteger;
import java.security.MessageDigest;

public class Md5 {
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(input.getBytes());
            String hash = new BigInteger(1, md.digest()).toString(16);// 16是表示转换为16进制数
            return hash;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

package com.campus.errand.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Util {

    private static final String SALT = "campus_errand_salt_2024";

    public static String encrypt(String password) {
        if (password == null || password.isEmpty()) {
            return null;
        }
        
        String saltedPassword = password + SALT;
        
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(saltedPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    public static boolean verify(String password, String encryptedPassword) {
        if (password == null || encryptedPassword == null) {
            return false;
        }
        return encrypt(password).equals(encryptedPassword);
    }
}

package me.teixayo.bytetalk.backend.security;

public class StringUtils {

    public static boolean isValidName(String name) {
        return name != null && name.matches("^[A-Za-z][A-Za-z0-9__-]{3,19}$");
    }
    public static boolean isValidPassword(String password) {
        return password !=null && password.matches("^[\\S]{8,19}$");
    }
}

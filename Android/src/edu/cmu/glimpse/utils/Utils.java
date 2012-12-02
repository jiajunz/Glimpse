package edu.cmu.glimpse.utils;

public class Utils {

    public static boolean stringEquals(String a, String b) {
        if (a == null) {
            return b == null;
        } else {
            return a.equals(b);
        }
    }
}

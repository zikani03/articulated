package me.zikani.labs.articulated;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Utils {
    private Utils() {}

    /**
     * Digest instance for computing hash of a text
     */
    static MessageDigest messageDigest;
    public static String sha1(String text) {
        if (messageDigest == null) {
            try {
                messageDigest = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException e) {
                // This shouldn't happen
                throw new RuntimeException(e);
            }
        }
        return Hex.encode(messageDigest.digest(text.getBytes()));
    }
}

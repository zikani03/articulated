package me.zikani.labs.articulated;

public final class Hex {
    private Hex() {}

    private static final char[] HEX = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String encode(byte[] bytes) {
        char[] chars = new char[bytes.length*2];
        int i = 0;
        for(byte b: bytes) {
            int bb = b & 0xFF;
            chars[i*2] = HEX[bb >>> 4];
            chars[i*2 + 1] = HEX[bb & 0x0F];
            i++;
        }
        return new String(chars);
    }
}
package auth;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import static org.apache.commons.codec.digest.DigestUtils.sha512;

public class SecurityUtils {

    private static  byte[] concat(byte[] first, byte[]... others) {
        int length = first.length;
        for (int i = 0; i < others.length; i++) {
            length += others[i].length;
        }

        final byte[] concatenation = Arrays.copyOf(first, length);
        for (int i = 0, j = first.length; i < others.length; j += others[i++].length) {
            System.arraycopy(others[i], 0, concatenation, j, others[i].length);
        }

        return concatenation;
    }

    public static byte[] stringToBytes(String string) {
        return string.getBytes(StandardCharsets.UTF_8);
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String stringToBase64(String string) {
        return bytesToBase64(stringToBytes(string));
    }

    public static String bytesToBase64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static byte[] base64ToBytes(String string) {
        return Base64.getUrlDecoder().decode(string);
    }

    public static String base64ToString(String string) {
        return bytesToString(base64ToBytes(string));
    }

    public static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String hashPassword(String password) {
        return bytesToBase64(sha256(password));
    }

    public static byte[] sha256(String input) {
        return sha256(stringToBytes(input));
    }

    /**
     * INSECURE
     * Implements the simple MAC schema <code>mac(M, K) = hash(K + M + K)</code>
     * where hash = sha512
     *
     * @param message the message M
     * @param secret the secret K
     *
     * @return a byte array representing a MAC for the message
     */
    public static byte[] simpleMAC(byte[] message, byte[] secret) {
        return sha512(concat(secret,  message, secret));
    }

    // Insecure
    public static byte[] simpleMAC(String message, String secret) {
        return simpleMAC(
                stringToBytes(message),
                stringToBytes(secret)
        );
    }

    /**
     * Simplifyed, i.e. non standard => INSECURE
     * Implements the simple MAC schema <code>mac(M, K) = hash(K + hash(K + M))</code>
     * where hash = sha512
     *
     * @param message the message M
     * @param secret the secret K
     *
     * @return a byte array representing a MAC for the message
     */
    public static byte[] simpleHMAC(byte[] message, byte[] secret) {
        return sha512(concat(secret, sha512(concat(secret, message))));
    }

    public static byte[] simpleHMAC(String message, String secret) {
        return simpleHMAC(
                stringToBytes(message),
                stringToBytes(secret)
        );
    }

    public static byte[] hmac256(byte[] message, byte[] secret) {
        return HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_256, secret).doFinal(message);
    }

    public static byte[] hmac256(String message, String secret) {
        return hmac256(
                stringToBytes(message),
                stringToBytes(secret)
        );
    }

    public static byte[] hmac512(byte[] message, byte[] secret) {
        return HmacUtils.getInitializedMac(HmacAlgorithms.HMAC_SHA_512, secret).doFinal(message);
    }

    public static byte[] hmac512(String message, String secret) {
        return hmac512(
                stringToBytes(message),
                stringToBytes(secret)
        );
    }
}

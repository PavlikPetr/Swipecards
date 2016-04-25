package com.topface.topface.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class EncryptMethods {

    private static final String ENCRYPT_METHOD_AES = "AES";
    private static final String ENCRYPT_KEY = "topface_user_id_"; // 16 bytes of key (we can use 128/192/256 bits key length only)
    private static final String SEPARATOR = "_";

    /**
     * Encrypt user id with AES
     *
     * @param uid          user id
     * @param defaultValue default value for false case
     * @return encrypted user id or default value
     */
    public String encryptUid(int uid, String defaultValue) {
        try {
            return getBytesString(encrypt(getKey(), String.valueOf(uid).getBytes()), defaultValue);
        } catch (Exception e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    /**
     * get 128 bit key for all encrypt/decrypt operation
     *
     * @return 16 bytes of key
     */
    public byte[] getKey() {
        return ENCRYPT_KEY.getBytes();
    }

    private String getBytesString(byte[] bytes, String defaultValue) {
        if (bytes.length == 0) {
            return defaultValue;
        } else {
            String res = Utils.EMPTY;
            for (byte symbol : bytes) {
                res = res + SEPARATOR + symbol;
            }
            return res.replaceFirst(SEPARATOR, Utils.EMPTY);
        }
    }

    /**
     * Encrypt data with users key
     *
     * @param key  key for encrypt operation
     * @param data bytes array of users data
     * @return bytes array of encrypted data
     * @throws Exception
     */
    public byte[] encrypt(byte[] key, byte[] data) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, ENCRYPT_METHOD_AES);
        Cipher cipher = Cipher.getInstance(ENCRYPT_METHOD_AES);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(data);
    }

    /**
     * Decrypt encrypted data
     *
     * @param key       key for decrypt (it should be similar to data which is encrypted with)
     * @param encrypted encrypted data
     * @return bytes array of decrypted data
     * @throws Exception
     */
    public byte[] decrypt(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, ENCRYPT_METHOD_AES);
        Cipher cipher = Cipher.getInstance(ENCRYPT_METHOD_AES);
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(encrypted);
    }
}

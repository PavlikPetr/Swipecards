package com.topface.topface.unittests.utils;

import com.topface.topface.utils.EncryptMethods;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by ppavlik on 25.04.16.
 * test encrypt/decrypt methods
 * encryptUid/decryptUid can be used to process current uid value
 */
public class EncryptDecryptTest {

    private static final int[] UID_ARRAY = new int[]{50663698};
    private static final String[] ENCRYPTED_UID = new String[]{"-53_26_44_115_82_-34_-57_-51_73_-47_-92_-116_-64_35_-57_-102"};

    private static final String SPLIT = "_";
    private static final String DEFAULT_VALUE = "";

    private static final int ITERATION_COUNT = 1000;
    private static final int MIN_UID_VALUE = 10000000;

    @Test
    public void testEncruptDecrypt() {
        Random random = new Random(MIN_UID_VALUE);
        int i = 0;
        while (i < ITERATION_COUNT) {
            ++i;
            int uid = Math.abs(random.nextInt());
            String encrypted = new EncryptMethods().encryptUid(uid, DEFAULT_VALUE);
            assertFalse("Error while encrypting uid " + uid, DEFAULT_VALUE.equals(encrypted));
            String decrypted = getDecryptedUid(encrypted);
            assertFalse("Error while decrypting uid " + uid, DEFAULT_VALUE.equals(decrypted));
            assertFalse("uid and decrypted data is not equals! Uid = " + uid + " decrypted = " + decrypted, !decrypted.equals(String.valueOf(uid)));
        }
        assertTrue(true);
    }

    @Test
    public void encryptUid() {
        for (int item : UID_ARRAY) {
            String res = new EncryptMethods().encryptUid(item, DEFAULT_VALUE);
            System.out.println("uid = " + item + " encrypted = " + res);
        }
    }

    @Test
    public void decryptUid() {
        for (String item : ENCRYPTED_UID) {
            System.out.println("uid = " + getDecryptedUid(item) + " encrypted = " + item);
        }
    }

    private String getDecryptedUid(String encryptedUid) {
        String result = DEFAULT_VALUE;
        String[] array = encryptedUid.split(SPLIT);
        if (array.length > 0) {
            byte[] encryptedArray = new byte[array.length];
            for (int i = 0; i < array.length; i++) {
                encryptedArray[i] = Byte.valueOf(array[i]);
            }
            EncryptMethods encryptMethods = new EncryptMethods();
            try {
                result = new String(encryptMethods.decrypt(encryptMethods.getKey(), encryptedArray), "UTF-8");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}

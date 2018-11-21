package cryptography;

import java.util.Base64;
import javax.crypto.Cipher;
import java.lang.Exception;
import java.security.Key;

public class SecuredRSAUsage {

    static int RSA_KEY_LENGTH = 3072;
    static String ALGORITHM_NAME = "RSA";
    static String PADDING_SCHEME = "OAEPWITHSHA-512ANDMGF1PADDING";
    static String MODE_OF_OPERATION = "ECB"; // This essentially means none behind the scene

    /**
     * Attempts to encrypt the {@code message} with an asymmetric encryption
     * @param message    the string to encrypt
     * @param publicKey  the public {@link Key} to be used for encryption
     * @return           the encrypted string
     * @throws Exception
     */
    public static String rsaEncrypt(String message, Key publicKey) throws Exception {
        Cipher c = Cipher.getInstance(ALGORITHM_NAME + "/" + MODE_OF_OPERATION + "/" + PADDING_SCHEME);
        c.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] cipherTextArray = c.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(cipherTextArray);
    }

    /**
     * Attempts to decrypt the {@code message} with an asymmetric encryption
     * @param encryptedMessage  the string to encrypt
     * @param privateKey        the public {@link Key} to be used for encryption
     * @return                  the decrypted string
     * @throws Exception
     */
    public static String rsaDecrypt(byte[] encryptedMessage, Key privateKey) throws Exception {
        Cipher c = Cipher.getInstance(ALGORITHM_NAME + "/" + MODE_OF_OPERATION + "/" + PADDING_SCHEME);
        c.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plainText = c.doFinal(encryptedMessage);
        return new String(plainText);
    }
}
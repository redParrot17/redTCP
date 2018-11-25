package cryptography;

import javax.crypto.Cipher;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

import static org.apache.commons.codec.CharEncoding.UTF_8;

public class SecuredRSAUsage {

    static int RSA_KEY_LENGTH = 4096;
    static String ALGORITHM_NAME = "RSA";
    private static String PADDING_SCHEME = "OAEPWITHSHA-512ANDMGF1PADDING";
    private static String MODE_OF_OPERATION = "ECB"; // This essentially means none behind the scene

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

    public static String sign(String plainText, PrivateKey privateKey) throws Exception {
        Signature privateSignature = Signature.getInstance("SHA256withRSA");
        privateSignature.initSign(privateKey);
        privateSignature.update(plainText.getBytes(UTF_8));
        byte[] signature = privateSignature.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
        Signature publicSignature = Signature.getInstance("SHA256withRSA");
        publicSignature.initVerify(publicKey);
        publicSignature.update(plainText.getBytes(UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return publicSignature.verify(signatureBytes);
    }
}
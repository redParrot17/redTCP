package cryptography;

import packets.EncryptionPacket;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class HybridCryptography {

    /**
     * @return Asymmetric {@link KeyPair} used for RSA encryption/decryption
     * @throws NoSuchAlgorithmException if someone messed with the code and changed
     *                                  the algorithm to one that is invalid
     */
    public static KeyPair generateKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator rsaKeyGen = KeyPairGenerator.getInstance(SecuredRSAUsage.ALGORITHM_NAME);
        rsaKeyGen.initialize(SecuredRSAUsage.RSA_KEY_LENGTH);
        return rsaKeyGen.generateKeyPair();
    }

    /**
     * Attempts to encrypt the string using both symmetric and asymmetric techniques
     *
     * @param input         String input that is to be encrypted
     * @param key           {@link PublicKey} used for asymmetric encryption
     * @param gcmParamSpec  GCM Parameter Specifications used for the encryption
     * @param aadData       extra data tag to be included within the final encryption
     * @return              String array of [ asymmetrically encrypted symmetric encryption key , symmetrically encrypted data ]
     */
    public static String[] encrypt(String input, PublicKey key, GCMParameterSpec gcmParamSpec, byte[] aadData) {
        SecretKey aesKey = null;
        try {
            KeyGenerator keygen = KeyGenerator.getInstance("AES") ; // Specifying algorithm key will be used for
            keygen.init(SecuredGCMUsage.AES_KEY_SIZE); // Specifying Key size to be used, Note: This would need JCE Unlimited Strength to be installed explicitly
            aesKey = keygen.generateKey();
        } catch(NoSuchAlgorithmException noSuchAlgoExc) {
            System.out.println("Key being request is for AES algorithm, but this cryptographic algorithm is not available in the environment "  + noSuchAlgoExc);
            System.exit(1);
        }
        byte[] encryptedData = SecuredGCMUsage.aesEncrypt(input, aesKey,  gcmParamSpec, aadData);
        try {
            String encodedKey = Base64.getEncoder().encodeToString(aesKey.getEncoded());
            String encryptedKey = SecuredRSAUsage.rsaEncrypt(encodedKey, key);
            return new String[]{encryptedKey, Base64.getEncoder().encodeToString(encryptedData)};
        } catch(Exception e) {System.out.println("Exception while encryption/decryption"); e.printStackTrace(); }
        return null;
    }

    /**
     * Attempts to decrypt the string using both symmetric and asymmetric techniques
     *
     * @param packet     {@link EncryptionPacket} that contains the encrypted data
     * @param key        {@link PrivateKey} used for the asymmetric decryption
     * @param aadData    the extra data tag used during the encryption
     * @throws Exception
     * @return           the original decrypted String
     */
    public static String decrypt(EncryptionPacket packet, PrivateKey key, byte[] aadData) throws Exception {
        String encryptedKey = packet.getKey();
        String encryptedData = packet.getPayload();
        GCMParameterSpec gcmParamSpec = packet.getGcmParamSpec();
        String decryptedKey = SecuredRSAUsage.rsaDecrypt(Base64.getDecoder().decode(encryptedKey), key);
        byte[] decodedKey = Base64.getDecoder().decode(decryptedKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        byte[] decryptedText = SecuredGCMUsage.aesDecrypt(Base64.getDecoder().decode(encryptedData), originalKey, gcmParamSpec, aadData);
        return new String(decryptedText);
    }

}

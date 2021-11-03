package privacymanager.android.utils.security;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import javax.crypto.Cipher;

public class AsymmetricCryptography {
    private static final String RSA = "RSA";

    /**
     * Create a KeyPair instance for generating asymmetric crypto keys, public and private.
     *
     * @return KeyPair instance with
     * @throws Exception Can not create an instance of keyPairGenerator
     */
    public static KeyPair generateRSAKeyPair() throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(2048, secureRandom);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Encrypt text with provided public key.
     *
     * @param plainText Text to encrypt
     * @param publicKey Public key
     * @return Encrypted text - bytes
     * @throws Exception Can not get cipher instance
     */
    public static byte[] do_RSAEncryption(
        String plainText,
        PublicKey publicKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        return cipher.doFinal(plainText.getBytes());
    }

    /**
     * Decrypt text with provided private key.
     *
     * @param cipherText Encrypted text
     * @param privateKey Private key
     * @return Decrypted text - String
     * @throws Exception Can not get Cipher instance
     */
    public static String do_RSADecryption(
        byte[] cipherText,
        PrivateKey privateKey) throws Exception
    {
        Cipher cipher = Cipher.getInstance(RSA);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] result = cipher.doFinal(cipherText);

        return new String(result);
    }
}
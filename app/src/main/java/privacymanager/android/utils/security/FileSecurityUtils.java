package privacymanager.android.utils.security;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;


/**
 * Class made for working with encryption/decryption of user files.
 */
public class FileSecurityUtils {
    private SecretKey secretKey;
    private Cipher cipher;


    public FileSecurityUtils() throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.secretKey = KeyGenerator.getInstance("AES").generateKey();
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /**
     * @param secretKey KeyGenerator key instance, by default is set to AES
     * @param cipher    cipher algorithm string, by default is set to AES/CBC/PKCS5Padding
     */
    public FileSecurityUtils(SecretKey secretKey, String cipher) throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.secretKey = secretKey;
        this.cipher = Cipher.getInstance(cipher);
    }

    /**
     * @param content  encrypted file content
     * @param fileName target file of encryption
     */
    public void encryptFile(String content, String fileName) throws InvalidKeyException, IOException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();

        try (
                FileOutputStream fileOut = new FileOutputStream(fileName);
                CipherOutputStream cipherOut = new CipherOutputStream(fileOut, cipher)
        ) {
            fileOut.write(iv);
            cipherOut.write(content.getBytes());
        }

    }

    /**
     * @param fileName target file of decryption
     * @return content of the decrypted file
     */
    public String decryptFile(String fileName) throws InvalidAlgorithmParameterException, InvalidKeyException, IOException {

        String content;

        try (FileInputStream fileIn = new FileInputStream(fileName)) {
            byte[] fileIv = new byte[16];
            fileIn.read(fileIv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));

            try (
                    CipherInputStream cipherIn = new CipherInputStream(fileIn, cipher);
                    InputStreamReader inputReader = new InputStreamReader(cipherIn);
                    BufferedReader reader = new BufferedReader(inputReader)
            ) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                content = sb.toString();
            }

        }
        return content;
    }

    public void setSecretKey(SecretKey secret)  {
        this.secretKey = secret;
    }

    public void setCipher(String cipherAlgorithm) throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.cipher = Cipher.getInstance(cipherAlgorithm);
    }

    public SecretKey getSecretKey()  {
        return this.secretKey;
    }

    public Cipher getCipher() {
        return this.cipher;
    }
}
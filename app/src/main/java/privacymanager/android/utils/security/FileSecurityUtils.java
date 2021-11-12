package privacymanager.android.utils.security;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;


/**
 * Class made for working with encryption/decryption of user files.
 */
public class FileSecurityUtils {
    //Arbitrarily selected 8-byte salt sequence:
    private static final byte[] salt = {
            (byte) 0x43, (byte) 0x76, (byte) 0x95, (byte) 0xc7,
            (byte) 0x5b, (byte) 0xd7, (byte) 0x45, (byte) 0x17
    };

    private static Cipher makeCipher(String pass, Boolean decryptMode) throws GeneralSecurityException {

        //Use a KeyFactory to derive the corresponding key from the passphrase:
        PBEKeySpec keySpec = new PBEKeySpec(pass.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(keySpec);

        //Create parameters from the salt and an arbitrary number of iterations:
        PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 42);

        //Set up the cipher:
        Cipher cipher = Cipher.getInstance("PBEWithMD5AndDES");

        //Set the cipher mode to decryption or encryption:
        if (decryptMode) {
            cipher.init(Cipher.ENCRYPT_MODE, key, pbeParamSpec);
        } else {
            cipher.init(Cipher.DECRYPT_MODE, key, pbeParamSpec);
        }

        return cipher;
    }


    /**
     * Encrypts one file to a second file using a key derived from a passphrase:
     **/
    public static void encryptFile(String sourcePath, String destinationPath, String pass)
            throws IOException, GeneralSecurityException {
        byte[] decData;
        byte[] encData;
        File inFile = new File(sourcePath);
        //Generate the cipher using pass:
        Cipher cipher = FileSecurityUtils.makeCipher(pass, true);

        FileInputStream inStream = new FileInputStream(inFile);

        int blockSize = 8;

        //Figure out how many bytes are padded
        int paddedCount = blockSize - ((int) inFile.length() % blockSize);

        //Figure out full size including padding
        int padded = (int) inFile.length() + paddedCount;

        decData = new byte[padded];


        inStream.read(decData);

        inStream.close();

        //Write out padding bytes as per PKCS5 algorithm
        for (int i = (int) inFile.length(); i < padded; ++i) {
            decData[i] = (byte) paddedCount;
        }

        //Encrypt the file data:
        encData = cipher.doFinal(decData);

        FileOutputStream outStream = new FileOutputStream(new File(destinationPath));
        outStream.write(encData);
        outStream.close();
    }


    /**
     * Decrypts one file to a second file using a key derived from a passphrase:
     **/
    public static void decryptFile(String sourcePath, String destinationPath, String pass)
            throws GeneralSecurityException, IOException, IllegalBlockSizeException, BadPaddingException {
        byte[] encData;
        byte[] decData;
        File inFile = new File(sourcePath);

        //Generate the cipher using pass:
        Cipher cipher = FileSecurityUtils.makeCipher(pass, false);

        //Read in the file:
        FileInputStream inStream = new FileInputStream(inFile);
        encData = new byte[(int) inFile.length()];
        inStream.read(encData);
        inStream.close();
        //Decrypt the file data:
        decData = cipher.doFinal(encData);

        //Figure out how much padding to remove
        int padCount = (int) decData[decData.length - 1];

        if (padCount >= 1 && padCount <= 8) {
            decData = Arrays.copyOfRange(decData, 0, decData.length - padCount);
        }

        FileOutputStream target = new FileOutputStream(new File(destinationPath + ".dec"));
        target.write(decData);
        target.close();
    }
}
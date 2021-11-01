package privacymanager.android.utils.security;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

public class FileSecurityUtils {
    private SecretKey secretKey;
    private String cipher;

    FileSecurityUtils(SecretKey secretKey, String transformation) {
        this.secretKey = secretKey;
        this.cipher = Cipher.getInstance(transformation);
    }


}

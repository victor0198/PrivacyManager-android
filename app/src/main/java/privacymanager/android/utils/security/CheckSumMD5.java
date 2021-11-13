package privacymanager.android.utils.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

public class CheckSumMD5 {

    public static String checksum(MessageDigest digest, File file) throws IOException {
        // Get file input stream for reading the file
        // content
        FileInputStream fis = new FileInputStream(file);

        // Create byte array to read data in chunks
        byte[] byteArray = new byte[1024];
        int bytesCount = 0;

        // read the data from file and update that data in
        // the message digest
        while ((bytesCount = fis.read(byteArray)) != -1) {
            digest.update(byteArray, 0, bytesCount);
        }

        fis.close();

        byte[] bytes = digest.digest();

        StringBuilder sb = new StringBuilder();

        for (byte aByte : bytes) {

            // the following line converts the decimal into
            // hexadecimal format and appends that to the
            // StringBuilder object
            sb.append(Integer
                    .toString((aByte & 0xff) + 0x100, 16)
                    .substring(1));
        }

        // return the complete hash
        return sb.toString();
    }
}

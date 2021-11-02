package privacymanager.android.utils.props;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Properties;

public class Props {
    /**
     * Encrypt a text with AES encoding
     *
     * @param ctx            current application context
     * @param propertyName   property name
     * @return property value
     */
    public static String getAppProperty(Context ctx, String propertyName) {
        String resultProperty = "";
        Properties properties = new Properties();
        try {
            properties.load(ctx.getAssets().open("app.properties"));
        }catch (IOException e){
            Log.d(Props.class.toString(), "Could not find properties file.");
        }

        resultProperty = properties.getProperty(propertyName);

        return resultProperty;
    }
}

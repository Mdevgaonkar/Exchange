package flo.org.campusmein.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Mayur on 02/12/16.
 */

public class credentials {

    public static final String CREDENTIALS_FILE = "credentials";
    public static final String CREDENTIALS = "keys";

    String credentials;

    private Context context;

    public credentials(Context context){
        this.context=context;
    }

    public String getCredentials() {
        return credentials = readPreferences(context,CREDENTIALS,"false");
    }

    public void setCredentials(String credentials) {
        this.credentials = credentials;
        setPreferences(context,CREDENTIALS,credentials);
    }

    private static void setPreferences(Context actContext, String preferenceName, String preferenceValue){

        SharedPreferences savedPreferences = actContext.getSharedPreferences(CREDENTIALS_FILE,actContext.MODE_PRIVATE);
        SharedPreferences.Editor editor = savedPreferences.edit();
        editor.putString(preferenceName,preferenceValue);
        editor.apply();

    }

    private static String readPreferences(Context actContext, String preferenceName, String preferenceDefaultValue){

        SharedPreferences savedPreferences = actContext.getSharedPreferences(CREDENTIALS_FILE,actContext.MODE_PRIVATE);
        return savedPreferences.getString(preferenceName,preferenceDefaultValue);

    }

}
